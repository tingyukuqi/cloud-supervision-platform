package com.csmp.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import com.csmp.common.core.constant.CacheNames;
import com.csmp.common.core.constant.SystemConstants;
import com.csmp.common.core.constant.TenantConstants;
import com.csmp.common.core.exception.ServiceException;
import com.csmp.common.core.utils.StringUtils;
import com.csmp.common.core.utils.TreeBuildUtils;
import com.csmp.common.satoken.utils.LoginHelper;
import com.csmp.common.tenant.helper.TenantHelper;
import com.csmp.system.api.RemoteTenantService;
import com.csmp.system.api.domain.vo.RemoteTenantVo;
import com.csmp.system.domain.SysDept;
import com.csmp.system.domain.SysPost;
import com.csmp.system.domain.SysUser;
import com.csmp.system.domain.bo.SysOrgBo;
import com.csmp.system.domain.vo.SysDeptVo;
import com.csmp.system.domain.vo.SysOrgVo;
import com.csmp.system.mapper.SysDeptMapper;
import com.csmp.system.mapper.SysPostMapper;
import com.csmp.system.mapper.SysUserMapper;
import com.csmp.system.service.ISysOrgService;
import com.csmp.system.service.ISysPostService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 组织机构管理 服务实现
 * <p>
 * 组织机构 = sys_dept 中 parent_id = 0 的根部门
 *
 * @author csmp
 */
@RequiredArgsConstructor
@Service
public class SysOrgServiceImpl implements ISysOrgService {

    private final SysDeptMapper deptMapper;
    private final SysUserMapper userMapper;
    private final SysPostMapper postMapper;
    private final ISysPostService postService;
    private final RemoteTenantService remoteTenantService;

    /**
     * 查询组织机构列表（parent_id = 0 的根部门）
     */
    @Override
    public List<SysOrgVo> selectOrgList(SysOrgBo bo) {
        LambdaQueryWrapper<SysDept> lqw = buildOrgQueryWrapper(bo);
        List<SysDeptVo> depts = deptMapper.selectDeptList(lqw);
        return fillTenantName(depts.stream().map(this::deptVoToOrgVo).toList());
    }

    /**
     * 查询组织机构树结构（下拉选择用）
     */
    @Override
    public List<Tree<Long>> selectOrgTreeSelect() {
        LambdaQueryWrapper<SysDept> lqw = Wrappers.lambdaQuery();
        lqw.eq(SysDept::getDelFlag, SystemConstants.NORMAL);
        lqw.eq(SysDept::getParentId, 0L);
        lqw.orderByAsc(SysDept::getOrderNum);
        List<SysDeptVo> orgs = deptMapper.selectDeptList(lqw);
        if (CollUtil.isEmpty(orgs)) {
            return CollUtil.newArrayList();
        }
        return TreeBuildUtils.buildMultiRoot(
            orgs,
            SysDeptVo::getDeptId,
            SysDeptVo::getParentId,
            (node, treeNode) -> treeNode
                .setId(node.getDeptId())
                .setParentId(node.getParentId())
                .setName(node.getDeptName())
                .setWeight(node.getOrderNum())
                .putExtra("disabled", SystemConstants.DISABLE.equals(node.getStatus()))
        );
    }

    /**
     * 根据组织ID查询信息
     */
    @Cacheable(cacheNames = CacheNames.SYS_ORG, key = "#orgId")
    @Override
    public SysOrgVo selectOrgById(Long orgId) {
        List<SysDeptVo> depts = deptMapper.selectDeptList(new LambdaQueryWrapper<SysDept>()
            .eq(SysDept::getDeptId, orgId)
            .eq(SysDept::getParentId, 0L)
            .last("limit 1"));
        if (CollUtil.isEmpty(depts)) {
            return null;
        }
        return fillTenantName(List.of(deptVoToOrgVo(depts.get(0)))).get(0);
    }

    /**
     * 通过组织ID串查询组织
     */
    @Override
    public List<SysOrgVo> selectOrgByIds(List<Long> orgIds) {
        if (CollUtil.isEmpty(orgIds)) {
            return List.of();
        }
        List<SysDeptVo> depts = deptMapper.selectDeptList(new LambdaQueryWrapper<SysDept>()
            .eq(SysDept::getStatus, SystemConstants.NORMAL)
            .eq(SysDept::getParentId, 0L)
            .in(SysDept::getDeptId, orgIds));
        return fillTenantName(depts.stream().map(this::deptVoToOrgVo).toList());
    }

    /**
     * 校验组织名称是否唯一（同租户下组织名称不重复）
     */
    @Override
    public boolean checkOrgNameUnique(SysOrgBo bo) {
        String targetTenantId = resolveTargetTenantId(bo.getTenantId(), bo.getOrgId());
        boolean exist = runWithTenant(targetTenantId, () -> deptMapper.exists(new LambdaQueryWrapper<SysDept>()
            .eq(SysDept::getDeptName, bo.getOrgName())
            .eq(SysDept::getParentId, 0L)
            .ne(ObjectUtil.isNotNull(bo.getOrgId()), SysDept::getDeptId, bo.getOrgId())));
        return !exist;
    }

    /**
     * 新增组织机构
     * <p>
     * 创建一个 parent_id = 0, ancestors = "0" 的根部门
     */
    @CacheEvict(cacheNames = CacheNames.SYS_DEPT_AND_CHILD, allEntries = true)
    @Override
    public int insertOrg(SysOrgBo bo) {
        String targetTenantId = resolveTargetTenantId(bo.getTenantId(), null);
        SysDept dept = new SysDept();
        dept.setTenantId(targetTenantId);
        dept.setDeptName(bo.getOrgName());
        dept.setParentId(0L);
        dept.setAncestors(SystemConstants.ROOT_DEPT_ANCESTORS);
        dept.setDeptCategory(bo.getDeptCategory());
        dept.setOrderNum(bo.getOrderNum());
        dept.setLeader(bo.getLeader());
        dept.setPhone(bo.getPhone());
        dept.setEmail(bo.getEmail());
        dept.setStatus(bo.getStatus());
        return runWithTenant(targetTenantId, () -> deptMapper.insert(dept));
    }

    /**
     * 修改组织机构
     */
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.SYS_ORG, key = "#bo.orgId"),
        @CacheEvict(cacheNames = CacheNames.SYS_DEPT_AND_CHILD, allEntries = true)
    })
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOrg(SysOrgBo bo) {
        SysDept currentOrg = deptMapper.selectById(bo.getOrgId());
        if (currentOrg == null || !Long.valueOf(0L).equals(currentOrg.getParentId())) {
            throw new ServiceException("组织不存在");
        }
        String sourceTenantId = currentOrg.getTenantId();
        String targetTenantId = resolveTargetTenantId(bo.getTenantId(), bo.getOrgId());
        SysDept dept = new SysDept();
        dept.setDeptId(bo.getOrgId());
        dept.setTenantId(targetTenantId);
        dept.setDeptName(bo.getOrgName());
        dept.setParentId(0L);
        dept.setAncestors(SystemConstants.ROOT_DEPT_ANCESTORS);
        dept.setDeptCategory(bo.getDeptCategory());
        dept.setOrderNum(bo.getOrderNum());
        dept.setLeader(bo.getLeader());
        dept.setPhone(bo.getPhone());
        dept.setEmail(bo.getEmail());
        dept.setStatus(bo.getStatus());
        int rows = deptMapper.updateById(dept);
        if (StringUtils.equals(sourceTenantId, targetTenantId)) {
            return rows;
        }
        migrateOrgTenant(bo.getOrgId(), sourceTenantId, targetTenantId);
        return rows;
    }

    /**
     * 删除组织机构
     * <p>
     * 前置检查：无子部门、无关联用户、无关联岗位
     */
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.SYS_ORG, key = "#orgId"),
        @CacheEvict(cacheNames = CacheNames.SYS_DEPT_AND_CHILD, key = "#orgId")
    })
    @Override
    public int deleteOrgById(Long orgId) {
        // 检查是否有子部门
        boolean hasChild = deptMapper.exists(new LambdaQueryWrapper<SysDept>()
            .eq(SysDept::getParentId, orgId));
        if (hasChild) {
            throw new ServiceException("组织下存在子部门，不允许删除");
        }
        // 检查是否有关联用户
        boolean hasUser = userMapper.exists(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getDeptId, orgId));
        if (hasUser) {
            throw new ServiceException("组织下存在用户，不允许删除");
        }
        // 检查子部门下是否有关联用户（通过 ancestors 查找）
        List<Long> childDeptIds = deptMapper.selectDeptAndChildById(orgId);
        if (CollUtil.isNotEmpty(childDeptIds)) {
            boolean hasChildUser = userMapper.exists(new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getDeptId, childDeptIds));
            if (hasChildUser) {
                throw new ServiceException("组织下的部门存在用户，不允许删除");
            }
        }
        // 检查是否有关联岗位
        for (Long deptId : new java.util.LinkedHashSet<>(childDeptIds)) {
            if (postService.countPostByDeptId(deptId) > 0) {
                throw new ServiceException("组织下存在岗位，不允许删除");
            }
        }
        return deptMapper.deleteById(orgId);
    }

    // ========== 私有方法 ==========

    private LambdaQueryWrapper<SysDept> buildOrgQueryWrapper(SysOrgBo bo) {
        LambdaQueryWrapper<SysDept> lqw = Wrappers.lambdaQuery();
        lqw.eq(SysDept::getDelFlag, SystemConstants.NORMAL);
        lqw.eq(SysDept::getParentId, 0L);
        lqw.like(StringUtils.isNotBlank(bo.getOrgName()), SysDept::getDeptName, bo.getOrgName());
        lqw.like(StringUtils.isNotBlank(bo.getDeptCategory()), SysDept::getDeptCategory, bo.getDeptCategory());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SysDept::getStatus, bo.getStatus());
        lqw.orderByAsc(SysDept::getOrderNum);
        lqw.orderByAsc(SysDept::getDeptId);
        return lqw;
    }

    /**
     * 将 SysDeptVo 转换为 SysOrgVo
     */
    private SysOrgVo deptVoToOrgVo(SysDeptVo vo) {
        SysOrgVo orgVo = new SysOrgVo();
        orgVo.setOrgId(vo.getDeptId());
        orgVo.setTenantId(vo.getTenantId());
        orgVo.setOrgName(vo.getDeptName());
        orgVo.setDeptCategory(vo.getDeptCategory());
        orgVo.setOrderNum(vo.getOrderNum());
        orgVo.setLeader(vo.getLeader());
        orgVo.setLeaderName(vo.getLeaderName());
        orgVo.setPhone(vo.getPhone());
        orgVo.setEmail(vo.getEmail());
        orgVo.setStatus(vo.getStatus());
        orgVo.setCreateTime(vo.getCreateTime());
        return orgVo;
    }

    private List<SysOrgVo> fillTenantName(List<SysOrgVo> orgs) {
        if (CollUtil.isEmpty(orgs)) {
            return orgs;
        }
        List<RemoteTenantVo> tenants = remoteTenantService.queryList();
        if (CollUtil.isEmpty(tenants)) {
            return orgs;
        }
        Map<String, String> tenantNameMap = tenants.stream()
            .filter(tenant -> StringUtils.isNotBlank(tenant.getTenantId()))
            .collect(Collectors.toMap(RemoteTenantVo::getTenantId, RemoteTenantVo::getCompanyName, (left, right) -> left));
        orgs.forEach(org -> org.setTenantName(tenantNameMap.get(org.getTenantId())));
        return orgs;
    }

    String loginTenantId() {
        return LoginHelper.getTenantId();
    }

    String dataTenantId() {
        return TenantHelper.getTenantId();
    }

    String resolveTargetTenantId(String tenantId, Long orgId) {
        String targetTenantId = tenantId;
        if (StringUtils.isBlank(targetTenantId) && orgId != null) {
            SysDept currentOrg = deptMapper.selectById(orgId);
            if (currentOrg != null) {
                targetTenantId = currentOrg.getTenantId();
            }
        }
        String loginTenantId = loginTenantId();
        targetTenantId = StringUtils.isBlank(targetTenantId) ? loginTenantId : targetTenantId;
        if (StringUtils.isBlank(targetTenantId)) {
            throw new ServiceException("租户ID不能为空");
        }
        if (isPlatformTenant(loginTenantId)) {
            return targetTenantId;
        }
        if (!StringUtils.equals(loginTenantId, targetTenantId)) {
            throw new ServiceException("只能操作当前租户的数据");
        }
        return loginTenantId;
    }

    boolean isPlatformTenant(String tenantId) {
        return StringUtils.equals(TenantConstants.DEFAULT_TENANT_ID, tenantId);
    }

    private <T> T runWithTenant(String tenantId, Supplier<T> supplier) {
        if (StringUtils.equals(dataTenantId(), tenantId)) {
            return supplier.get();
        }
        return TenantHelper.dynamic(tenantId, supplier);
    }

    private void migrateOrgTenant(Long orgId, String sourceTenantId, String targetTenantId) {
        List<Long> deptIds = deptMapper.selectDeptAndChildById(orgId);
        if (CollUtil.isEmpty(deptIds)) {
            deptIds = List.of(orgId);
        }
        boolean hasUser = userMapper.exists(new LambdaQueryWrapper<SysUser>().in(SysUser::getDeptId, deptIds));
        if (hasUser) {
            throw new ServiceException("组织下存在用户，不允许直接修改租户");
        }
        deptMapper.update(null, new UpdateWrapper<SysDept>()
            .in("dept_id", deptIds)
            .eq("tenant_id", sourceTenantId)
            .set("tenant_id", targetTenantId));
        postMapper.update(null, new UpdateWrapper<SysPost>()
            .in("dept_id", deptIds)
            .eq("tenant_id", sourceTenantId)
            .set("tenant_id", targetTenantId));
    }

}
