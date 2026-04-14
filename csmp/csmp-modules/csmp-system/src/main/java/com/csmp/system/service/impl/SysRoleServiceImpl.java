package com.csmp.system.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csmp.common.core.constant.CacheNames;
import com.csmp.common.core.constant.SystemConstants;
import com.csmp.common.core.constant.TenantConstants;
import com.csmp.common.core.exception.ServiceException;
import com.csmp.common.core.utils.MapstructUtils;
import com.csmp.common.core.utils.StreamUtils;
import com.csmp.common.core.utils.StringUtils;
import com.csmp.common.mybatis.core.page.PageQuery;
import com.csmp.common.mybatis.core.page.TableDataInfo;
import com.csmp.common.satoken.utils.LoginHelper;
import com.csmp.system.api.model.LoginUser;
import com.csmp.system.domain.SysRole;
import com.csmp.system.domain.SysRoleDept;
import com.csmp.system.domain.SysRoleEffectiveMenu;
import com.csmp.system.domain.SysRoleHiddenMenu;
import com.csmp.system.domain.SysRoleMenu;
import com.csmp.system.domain.SysUserRole;
import com.csmp.system.domain.bo.SysRoleBo;
import com.csmp.system.domain.vo.SysRoleVo;
import com.csmp.system.mapper.SysRoleDeptMapper;
import com.csmp.system.mapper.SysRoleEffectiveMenuMapper;
import com.csmp.system.mapper.SysRoleHiddenMenuMapper;
import com.csmp.system.mapper.SysRoleMapper;
import com.csmp.system.mapper.SysRoleMenuMapper;
import com.csmp.system.mapper.SysUserRoleMapper;
import com.csmp.system.event.RolePermissionChangedEvent;
import com.csmp.system.service.IRoleEffectiveMenuService;
import com.csmp.system.service.IRoleHierarchyService;
import com.csmp.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 角色 业务层处理
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysRoleServiceImpl implements ISysRoleService {

    private final SysRoleMapper baseMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysRoleHiddenMenuMapper hiddenMenuMapper;
    private final SysRoleEffectiveMenuMapper effectiveMenuMapper;
    private final IRoleHierarchyService hierarchyService;
    private final IRoleEffectiveMenuService effectiveMenuService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 分页查询角色列表
     *
     * @param role      查询条件
     * @param pageQuery 分页参数
     * @return 角色分页列表
     */
    @Override
    public TableDataInfo<SysRoleVo> selectPageRoleList(SysRoleBo role, PageQuery pageQuery) {
        Page<SysRoleVo> page = baseMapper.selectPageRoleList(pageQuery.build(), this.buildQueryWrapper(role));
        enrichParentRoleNames(page.getRecords());
        return TableDataInfo.build(page);
    }

    /**
     * 根据条件查询角色数据
     *
     * @param role 角色信息
     * @return 角色数据集合信息
     */
    @Override
    public List<SysRoleVo> selectRoleList(SysRoleBo role) {
        List<SysRoleVo> list = baseMapper.selectRoleList(this.buildQueryWrapper(role));
        enrichParentRoleNames(list);
        return list;
    }

    private Wrapper<SysRole> buildQueryWrapper(SysRoleBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SysRole> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ObjectUtil.isNotNull(bo.getRoleId()), SysRole::getRoleId, bo.getRoleId())
            .like(StringUtils.isNotBlank(bo.getRoleName()), SysRole::getRoleName, bo.getRoleName())
            .eq(StringUtils.isNotBlank(bo.getStatus()), SysRole::getStatus, bo.getStatus())
            .like(StringUtils.isNotBlank(bo.getRoleKey()), SysRole::getRoleKey, bo.getRoleKey())
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                SysRole::getCreateTime, params.get("beginTime"), params.get("endTime"))
            .orderByAsc(SysRole::getRoleSort).orderByAsc(SysRole::getCreateTime);
        return wrapper;
    }

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @Override
    public List<SysRoleVo> selectRolesByUserId(Long userId) {
        return baseMapper.selectRolesByUserId(userId);
    }

    /**
     * 根据用户ID查询角色列表(包含被授权状态)
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @Override
    public List<SysRoleVo> selectRolesAuthByUserId(Long userId) {
        List<SysRoleVo> userRoles = baseMapper.selectRolesByUserId(userId);
        List<SysRoleVo> roles = selectRoleAll();
        // 使用HashSet提高查找效率
        Set<Long> userRoleIds = StreamUtils.toSet(userRoles, SysRoleVo::getRoleId);
        for (SysRoleVo role : roles) {
            if (userRoleIds.contains(role.getRoleId())) {
                role.setFlag(true);
            }
        }
        return roles;
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectRolePermissionByUserId(Long userId) {
        List<SysRoleVo> perms = baseMapper.selectRolesByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (SysRoleVo perm : perms) {
            if (ObjectUtil.isNotNull(perm)) {
                permsSet.addAll(StringUtils.splitList(perm.getRoleKey().trim()));
            }
        }
        return permsSet;
    }

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    @Override
    public List<SysRoleVo> selectRoleAll() {
        return this.selectRoleList(new SysRoleBo());
    }

    /**
     * 根据用户ID获取角色选择框列表
     *
     * @param userId 用户ID
     * @return 选中角色ID列表
     */
    @Override
    public List<Long> selectRoleListByUserId(Long userId) {
        List<SysRoleVo> list = baseMapper.selectRolesByUserId(userId);
        return StreamUtils.toList(list, SysRoleVo::getRoleId);
    }

    /**
     * 通过角色ID查询角色
     *
     * @param roleId 角色ID
     * @return 角色对象信息
     */
    @Override
    public SysRoleVo selectRoleById(Long roleId) {
        SysRoleVo roleVo = baseMapper.selectRoleById(roleId);
        if (roleVo == null) {
            return null;
        }
        if (roleVo.getParentId() != null) {
            SysRole parentRole = baseMapper.selectById(roleVo.getParentId());
            if (parentRole != null) {
                roleVo.setParentRoleName(parentRole.getRoleName());
            }
        }
        List<Long> hiddenMenuIds = hiddenMenuMapper.selectHiddenMenuIdsByRoleId(roleId);
        roleVo.setHiddenMenuIds(hiddenMenuIds.toArray(Long[]::new));
        List<Long> inheritedMenuIds = effectiveMenuMapper.selectEffectiveMenuDetailByRoleId(roleId).stream()
            .filter(menu -> "INHERITED".equals(menu.getSource()))
            .map(SysRoleEffectiveMenu::getMenuId)
            .toList();
        roleVo.setInheritedMenuIds(inheritedMenuIds.toArray(Long[]::new));
        return roleVo;
    }

    private void enrichParentRoleNames(List<SysRoleVo> roles) {
        if (CollUtil.isEmpty(roles)) {
            return;
        }
        List<Long> parentIds = roles.stream()
            .map(SysRoleVo::getParentId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (CollUtil.isEmpty(parentIds)) {
            return;
        }
        Map<Long, String> parentNameMap = StreamUtils.toMap(
            baseMapper.selectByIds(parentIds),
            SysRole::getRoleId,
            SysRole::getRoleName
        );
        roles.forEach(role -> role.setParentRoleName(parentNameMap.get(role.getParentId())));
    }

    /**
     * 通过角色ID串查询角色
     *
     * @param roleIds 角色ID串
     * @return 角色列表信息
     */
    @Override
    public List<SysRoleVo> selectRoleByIds(List<Long> roleIds) {
        return baseMapper.selectRoleList(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getStatus, SystemConstants.NORMAL)
            .in(CollUtil.isNotEmpty(roleIds), SysRole::getRoleId, roleIds));
    }

    /**
     * 校验角色名称是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public boolean checkRoleNameUnique(SysRoleBo role) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getRoleName, role.getRoleName())
            .ne(ObjectUtil.isNotNull(role.getRoleId()), SysRole::getRoleId, role.getRoleId()));
        return !exist;
    }

    /**
     * 校验角色权限是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public boolean checkRoleKeyUnique(SysRoleBo role) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getRoleKey, role.getRoleKey())
            .ne(ObjectUtil.isNotNull(role.getRoleId()), SysRole::getRoleId, role.getRoleId()));
        return !exist;
    }

    /**
     * 校验角色是否允许操作
     *
     * @param role 角色信息
     */
    @Override
    public void checkRoleAllowed(SysRoleBo role) {
        if (ObjectUtil.isNotNull(role.getRoleId()) && LoginHelper.isSuperAdmin(role.getRoleId())) {
            throw new ServiceException("不允许操作超级管理员角色");
        }
        String[] keys = new String[]{TenantConstants.SUPER_ADMIN_ROLE_KEY, TenantConstants.TENANT_ADMIN_ROLE_KEY};
        // 新增不允许使用 管理员标识符
        if (ObjectUtil.isNull(role.getRoleId())
            && StringUtils.equalsAny(role.getRoleKey(), keys)) {
            throw new ServiceException("不允许使用系统内置管理员角色标识符!");
        }
        // 修改不允许修改 管理员标识符
        if (ObjectUtil.isNotNull(role.getRoleId())) {
            SysRole sysRole = baseMapper.selectById(role.getRoleId());
            // 如果标识符不相等 判断为修改了管理员标识符
            if (!StringUtils.equals(sysRole.getRoleKey(), role.getRoleKey())) {
                if (StringUtils.equalsAny(sysRole.getRoleKey(), keys)) {
                    throw new ServiceException("不允许修改系统内置管理员角色标识符!");
                } else if (StringUtils.equalsAny(role.getRoleKey(), keys)) {
                    throw new ServiceException("不允许使用系统内置管理员角色标识符!");
                }
            }
        }
    }

    /**
     * 校验角色是否有数据权限
     *
     * @param roleId 角色id
     */
    @Override
    public void checkRoleDataScope(Long roleId) {
        if (ObjectUtil.isNull(roleId)) {
            return;
        }
        this.checkRoleDataScope(Collections.singletonList(roleId));
    }

    /**
     * 校验角色是否有数据权限
     *
     * @param roleIds 角色ID列表（支持传单个ID）
     */
    @Override
    public void checkRoleDataScope(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds) || LoginHelper.isSuperAdmin()) {
            return;
        }
        long count = baseMapper.selectRoleCount(roleIds);
        if (count != roleIds.size()) {
            throw new ServiceException("没有权限访问部分角色数据！");
        }
    }

    /**
     * 通过角色ID查询角色使用数量
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    public long countUserRoleByRoleId(Long roleId) {
        return userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
    }

    /**
     * 新增保存角色信息
     *
     * @param bo 角色信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRole(SysRoleBo bo) {
        // 层级校验
        hierarchyService.validateParent(null, bo.getParentId());
        hierarchyService.validateDataScopeConstraint(null, bo.getParentId(), bo.getDataScope(), bo.getDeptIds());

        SysRole role = MapstructUtils.convert(bo, SysRole.class);
        role.setRoleLevel(hierarchyService.calculateRoleLevel(bo.getParentId()));
        baseMapper.insert(role);
        bo.setRoleId(role.getRoleId());

        int rows = insertRoleMenu(bo);

        // 插入隐藏菜单
        insertHiddenMenus(bo);

        // 发布权限变更事件
        eventPublisher.publishEvent(new RolePermissionChangedEvent(this, role.getRoleId()));

        return rows;
    }

    /**
     * 修改保存角色信息
     *
     * @param bo 角色信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRole(SysRoleBo bo) {
        SysRole role = MapstructUtils.convert(bo, SysRole.class);

        if (SystemConstants.DISABLE.equals(role.getStatus()) && this.countUserRoleByRoleId(role.getRoleId()) > 0) {
            throw new ServiceException("角色已分配，不能禁用!");
        }

        // 层级校验
        hierarchyService.validateParent(bo.getRoleId(), bo.getParentId());
        hierarchyService.validateDataScopeConstraint(bo.getRoleId(), bo.getParentId(), bo.getDataScope(), bo.getDeptIds());

        SysRole oldRole = baseMapper.selectById(role.getRoleId());
        role.setRoleLevel(hierarchyService.calculateRoleLevel(bo.getParentId()));

        baseMapper.updateById(role);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, role.getRoleId()));
        int rows = insertRoleMenu(bo);

        // 更新隐藏菜单
        hiddenMenuMapper.delete(new LambdaQueryWrapper<SysRoleHiddenMenu>().eq(SysRoleHiddenMenu::getRoleId, role.getRoleId()));
        insertHiddenMenus(bo);

        // 发布权限变更事件
        eventPublisher.publishEvent(new RolePermissionChangedEvent(this, List.of(role.getRoleId()), true));

        return rows;
    }

    /**
     * 修改角色状态
     *
     * @param roleId 角色ID
     * @param status 角色状态
     * @return 结果
     */
    @Override
    public int updateRoleStatus(Long roleId, String status) {
        if (SystemConstants.DISABLE.equals(status) && this.countUserRoleByRoleId(roleId) > 0) {
            throw new ServiceException("角色已分配，不能禁用!");
        }
        int rows = baseMapper.update(null,
            new UpdateWrapper<SysRole>()
                .set("status", status)
                .eq("role_id", roleId));
        if (rows > 0) {
            eventPublisher.publishEvent(new RolePermissionChangedEvent(this, List.of(roleId), true));
        }
        return rows;
    }

    /**
     * 修改数据权限信息
     *
     * @param bo 角色信息
     * @return 结果
     */
    @CacheEvict(cacheNames = CacheNames.SYS_ROLE_CUSTOM, key = "#bo.roleId")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int authDataScope(SysRoleBo bo) {
        SysRole role = MapstructUtils.convert(bo, SysRole.class);
        // 修改角色信息
        baseMapper.updateById(role);
        // 删除角色与部门关联
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, role.getRoleId()));
        // 新增角色和部门信息（数据权限）
        return insertRoleDept(bo);
    }

    /**
     * 新增角色菜单信息
     *
     * @param role 角色对象
     */
    private int insertRoleMenu(SysRoleBo role) {
        int rows = 1;
        // 新增用户与角色管理
        List<SysRoleMenu> list = new ArrayList<>();
        for (Long menuId : role.getMenuIds()) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(role.getRoleId());
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if (CollUtil.isNotEmpty(list)) {
            rows = roleMenuMapper.insertBatch(list) ? list.size() : 0;
        }
        return rows;
    }

    /**
     * 新增角色部门信息(数据权限)
     *
     * @param role 角色对象
     */
    private int insertRoleDept(SysRoleBo role) {
        int rows = 1;
        // 新增角色与部门（数据权限）管理
        List<SysRoleDept> list = new ArrayList<>();
        for (Long deptId : role.getDeptIds()) {
            SysRoleDept rd = new SysRoleDept();
            rd.setRoleId(role.getRoleId());
            rd.setDeptId(deptId);
            list.add(rd);
        }
        if (CollUtil.isNotEmpty(list)) {
            rows = roleDeptMapper.insertBatch(list) ? list.size() : 0;
        }
        return rows;
    }

    /**
     * 通过角色ID删除角色
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @CacheEvict(cacheNames = CacheNames.SYS_ROLE_CUSTOM, key = "#roleId")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRoleById(Long roleId) {
        // 检查是否有子角色
        List<Long> childIds = baseMapper.selectChildRoleIds(roleId);
        if (cn.hutool.core.collection.CollUtil.isNotEmpty(childIds)) {
            throw new ServiceException("该角色下存在子角色，不能删除!");
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, roleId));
        hiddenMenuMapper.delete(new LambdaQueryWrapper<SysRoleHiddenMenu>().eq(SysRoleHiddenMenu::getRoleId, roleId));
        effectiveMenuMapper.delete(new LambdaQueryWrapper<SysRoleEffectiveMenu>().eq(SysRoleEffectiveMenu::getRoleId, roleId));
        return baseMapper.deleteById(roleId);
    }

    /**
     * 批量删除角色信息
     *
     * @param roleIds 需要删除的角色ID
     * @return 结果
     */
    @CacheEvict(cacheNames = CacheNames.SYS_ROLE_CUSTOM, allEntries = true)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRoleByIds(List<Long> roleIds) {
        checkRoleDataScope(roleIds);
        List<SysRole> roles = baseMapper.selectByIds(roleIds);
        for (SysRole role : roles) {
            checkRoleAllowed(BeanUtil.toBean(role, SysRoleBo.class));
            if (countUserRoleByRoleId(role.getRoleId()) > 0) {
                throw new ServiceException(String.format("%1$s已分配，不能删除!", role.getRoleName()));
            }
            if (cn.hutool.core.collection.CollUtil.isNotEmpty(baseMapper.selectChildRoleIds(role.getRoleId()))) {
                throw new ServiceException(String.format("%1$s下存在子角色，不能删除!", role.getRoleName()));
            }
        }
        // 删除角色与菜单关联
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds));
        // 删除角色与部门关联
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().in(SysRoleDept::getRoleId, roleIds));
        hiddenMenuMapper.delete(new LambdaQueryWrapper<SysRoleHiddenMenu>().in(SysRoleHiddenMenu::getRoleId, roleIds));
        effectiveMenuMapper.delete(new LambdaQueryWrapper<SysRoleEffectiveMenu>().in(SysRoleEffectiveMenu::getRoleId, roleIds));
        return baseMapper.deleteByIds(roleIds);
    }

    /**
     * 取消授权用户角色
     *
     * @param userRole 用户和角色关联信息
     * @return 结果
     */
    @Override
    public int deleteAuthUser(SysUserRole userRole) {
        if (LoginHelper.getUserId().equals(userRole.getUserId())) {
            throw new ServiceException("不允许修改当前用户角色!");
        }
        int rows = userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
            .eq(SysUserRole::getRoleId, userRole.getRoleId())
            .eq(SysUserRole::getUserId, userRole.getUserId()));
        if (rows > 0) {
            cleanOnlineUser(List.of(userRole.getUserId()));
        }
        return rows;
    }

    /**
     * 批量取消授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要取消授权的用户数据ID
     * @return 结果
     */
    @Override
    public int deleteAuthUsers(Long roleId, Long[] userIds) {
        List<Long> ids = List.of(userIds);
        if (ids.contains(LoginHelper.getUserId())) {
            throw new ServiceException("不允许修改当前用户角色!");
        }
        int rows = userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
            .eq(SysUserRole::getRoleId, roleId)
            .in(SysUserRole::getUserId, ids));
        if (rows > 0) {
            cleanOnlineUser(ids);
        }
        return rows;
    }

    /**
     * 批量选择授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要授权的用户数据ID
     * @return 结果
     */
    @Override
    public int insertAuthUsers(Long roleId, Long[] userIds) {
        // 新增用户与角色管理
        int rows = 1;
        List<Long> ids = List.of(userIds);
        if (ids.contains(LoginHelper.getUserId())) {
            throw new ServiceException("不允许修改当前用户角色!");
        }
        List<SysUserRole> list = StreamUtils.toList(ids, userId -> {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            return ur;
        });
        if (CollUtil.isNotEmpty(list)) {
            rows = userRoleMapper.insertBatch(list) ? list.size() : 0;
        }
        if (rows > 0) {
            cleanOnlineUser(ids);
        }
        return rows;
    }

    /**
     * 根据角色ID清除该角色关联的所有在线用户的登录状态（踢出在线用户）
     *
     * <p>
     * 先判断角色是否绑定用户，若无绑定则直接返回
     * 然后遍历当前所有在线Token，查找拥有该角色的用户并强制登出
     * 注意：在线用户量过大时，操作可能导致 Redis 阻塞，需谨慎调用
     * </p>
     *
     * @param roleId 角色ID
     */
    @Override
    public void cleanOnlineUserByRole(Long roleId) {
        // 如果角色未绑定用户 直接返回
        Long num = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
        if (num == 0) {
            return;
        }
        List<String> keys = StpUtil.searchTokenValue("", 0, -1, false);
        if (CollUtil.isEmpty(keys)) {
            return;
        }
        // 角色关联的在线用户量过大会导致redis阻塞卡顿 谨慎操作
        keys.parallelStream().forEach(key -> {
            String token = StringUtils.substringAfterLast(key, ":");
            // 如果已经过期则跳过
            if (StpUtil.stpLogic.getTokenActiveTimeoutByToken(token) < -1) {
                return;
            }
            LoginUser loginUser = LoginHelper.getLoginUser(token);
            if (ObjectUtil.isNull(loginUser) || CollUtil.isEmpty(loginUser.getRoles())) {
                return;
            }
            if (loginUser.getRoles().stream().anyMatch(r -> r.getRoleId().equals(roleId))) {
                try {
                    StpUtil.logoutByTokenValue(token);
                } catch (NotLoginException ignored) {
                }
            }
        });
    }

    /**
     * 根据用户ID列表清除对应在线用户的登录状态（踢出指定用户）
     *
     * <p>
     * 遍历当前所有在线Token，匹配用户ID列表中的用户，强制登出
     * 注意：在线用户量过大时，操作可能导致 Redis 阻塞，需谨慎调用
     * </p>
     *
     * @param userIds 需要清除的用户ID列表
     */
    @Override
    public void cleanOnlineUser(List<Long> userIds) {
        List<String> keys = StpUtil.searchTokenValue("", 0, -1, false);
        if (CollUtil.isEmpty(keys)) {
            return;
        }
        // 角色关联的在线用户量过大会导致redis阻塞卡顿 谨慎操作
        keys.parallelStream().forEach(key -> {
            String token = StringUtils.substringAfterLast(key, ":");
            // 如果已经过期则跳过
            if (StpUtil.stpLogic.getTokenActiveTimeoutByToken(token) < -1) {
                return;
            }
            LoginUser loginUser = LoginHelper.getLoginUser(token);
            if (ObjectUtil.isNull(loginUser)) {
                return;
            }
            if (userIds.contains(loginUser.getUserId())) {
                try {
                    StpUtil.logoutByTokenValue(token);
                } catch (NotLoginException ignored) {
                }
            }
        });
    }

    /**
     * 新增角色隐藏菜单
     */
    private void insertHiddenMenus(SysRoleBo bo) {
        if (bo.getHiddenMenuIds() == null || bo.getHiddenMenuIds().length == 0) {
            return;
        }
        List<SysRoleHiddenMenu> list = new ArrayList<>();
        for (Long menuId : bo.getHiddenMenuIds()) {
            SysRoleHiddenMenu hm = new SysRoleHiddenMenu();
            hm.setRoleId(bo.getRoleId());
            hm.setMenuId(menuId);
            list.add(hm);
        }
        hiddenMenuMapper.insertBatch(list);
    }

    @Override
    public List<SysRoleVo> selectRoleTree() {
        return hierarchyService.selectRoleTree();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hideInheritedMenus(Long roleId, Long[] menuIds) {
        checkRoleDataScope(roleId);
        if (menuIds == null || menuIds.length == 0) {
            return;
        }
        Map<Long, SysRoleEffectiveMenu> effectiveMenuMap = new HashMap<>();
        for (SysRoleEffectiveMenu menu : effectiveMenuMapper.selectEffectiveMenuDetailByRoleId(roleId)) {
            effectiveMenuMap.put(menu.getMenuId(), menu);
        }
        Set<Long> existingHiddenIds = new HashSet<>(hiddenMenuMapper.selectHiddenMenuIdsByRoleId(roleId));
        List<SysRoleHiddenMenu> list = new ArrayList<>();
        for (Long menuId : new LinkedHashSet<>(Arrays.asList(menuIds))) {
            if (existingHiddenIds.contains(menuId)) {
                continue;
            }
            SysRoleEffectiveMenu effectiveMenu = effectiveMenuMap.get(menuId);
            if (effectiveMenu == null || !"INHERITED".equals(effectiveMenu.getSource())) {
                throw new ServiceException("只能隐藏继承菜单!");
            }
            SysRoleHiddenMenu hm = new SysRoleHiddenMenu();
            hm.setRoleId(roleId);
            hm.setMenuId(menuId);
            list.add(hm);
        }
        if (CollUtil.isNotEmpty(list)) {
            hiddenMenuMapper.insertBatch(list);
        }
        eventPublisher.publishEvent(new RolePermissionChangedEvent(this, List.of(roleId), true));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreInheritedMenus(Long roleId, Long[] menuIds) {
        checkRoleDataScope(roleId);
        if (menuIds == null || menuIds.length == 0) {
            return;
        }
        hiddenMenuMapper.delete(new LambdaQueryWrapper<SysRoleHiddenMenu>()
            .eq(SysRoleHiddenMenu::getRoleId, roleId)
            .in(SysRoleHiddenMenu::getMenuId, Arrays.asList(menuIds)));
        eventPublisher.publishEvent(new RolePermissionChangedEvent(this, List.of(roleId), true));
    }

    @Override
    public List<SysRoleEffectiveMenu> selectEffectiveMenus(Long roleId) {
        checkRoleDataScope(roleId);
        return effectiveMenuMapper.selectEffectiveMenuDetailByRoleId(roleId);
    }

}
