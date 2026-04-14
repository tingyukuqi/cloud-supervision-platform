package com.csmp.supply.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csmp.common.core.exception.ServiceException;
import com.csmp.common.core.utils.StringUtils;
import com.csmp.common.mybatis.core.page.PageQuery;
import com.csmp.common.mybatis.core.page.TableDataInfo;
import com.csmp.supply.domain.SupplyCloudPlatform;
import com.csmp.supply.domain.SupplyCloudTenant;
import com.csmp.supply.domain.SupplyOrgCloudTenantBind;
import com.csmp.supply.domain.bo.SupplyOrgCloudTenantBindBo;
import com.csmp.supply.domain.vo.SupplyOptionVo;
import com.csmp.supply.domain.vo.SupplyOrgCloudTenantBindVo;
import com.csmp.supply.mapper.SupplyCloudPlatformMapper;
import com.csmp.supply.mapper.SupplyCloudTenantMapper;
import com.csmp.supply.mapper.SupplyOrgCloudTenantBindMapper;
import com.csmp.supply.service.ISupplyOrgCloudTenantBindService;
import com.csmp.supply.support.SupplyIdGenerator;
import com.csmp.system.api.RemoteDeptService;
import com.csmp.system.api.RemoteTenantService;
import com.csmp.system.api.domain.vo.RemoteDeptVo;
import com.csmp.system.api.domain.vo.RemoteTenantVo;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 组织绑定服务实现
 *
 * @author csmp
 */
@Service
@RequiredArgsConstructor
public class SupplyOrgCloudTenantBindServiceImpl extends AbstractSupplyService implements ISupplyOrgCloudTenantBindService {

    private static final String CLOUD_TENANT_TYPE = "cloud_tenant";

    private final SupplyOrgCloudTenantBindMapper bindMapper;
    private final SupplyCloudPlatformMapper cloudPlatformMapper;
    private final SupplyCloudTenantMapper cloudTenantMapper;
    private final RemoteDeptService remoteDeptService;
    private final SupplyIdGenerator idGenerator;

    @DubboReference(mock = "true")
    private RemoteTenantService remoteTenantService;

    @Override
    public TableDataInfo<SupplyOrgCloudTenantBindVo> queryPageList(SupplyOrgCloudTenantBindBo bo, PageQuery pageQuery) {
        String tenantScope = queryTenantScope();
        LambdaQueryWrapper<SupplyOrgCloudTenantBind> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(tenantScope), SupplyOrgCloudTenantBind::getTenantId, tenantScope);
        lqw.eq(Objects.nonNull(bo.getOrgId()), SupplyOrgCloudTenantBind::getOrgId, bo.getOrgId());
        lqw.eq(Objects.nonNull(bo.getCloudPlatformId()), SupplyOrgCloudTenantBind::getCloudPlatformId, bo.getCloudPlatformId());
        lqw.eq(Objects.nonNull(bo.getCloudTenantSnapshotId()), SupplyOrgCloudTenantBind::getCloudTenantSnapshotId, bo.getCloudTenantSnapshotId());
        lqw.eq(StringUtils.isNotBlank(bo.getBindStatus()), SupplyOrgCloudTenantBind::getBindStatus, bo.getBindStatus());
        lqw.orderByDesc(SupplyOrgCloudTenantBind::getCreateTime);
        Page<SupplyOrgCloudTenantBind> page = bindMapper.selectPage(pageQuery.build(), lqw);
        List<Long> deptIds = page.getRecords().stream().map(SupplyOrgCloudTenantBind::getOrgId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> deptNameMap = deptIds.isEmpty() ? Collections.emptyMap() : remoteDeptService.selectDeptNamesByIds(deptIds);
        List<Long> platformIds = page.getRecords().stream().map(SupplyOrgCloudTenantBind::getCloudPlatformId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> platformNameMap = platformIds.isEmpty() ? Collections.emptyMap() :
            cloudPlatformMapper.selectBatchIds(platformIds).stream()
                .collect(Collectors.toMap(SupplyCloudPlatform::getId, SupplyCloudPlatform::getPlatformName, (a, b) -> a));
        Map<Long, String> cloudTenantNameMap = cloudTenantNameMap();
        List<SupplyOrgCloudTenantBindVo> list = page.getRecords().stream()
            .map(item -> toVo(item, deptNameMap, platformNameMap, cloudTenantNameMap))
            .toList();
        return new TableDataInfo<>(list, page.getTotal());
    }

    @Override
    public SupplyOrgCloudTenantBindVo queryById(Long bindingId) {
        SupplyOrgCloudTenantBind entity = getBindOrThrow(bindingId);
        Map<Long, String> deptNameMap = remoteDeptService.selectDeptNamesByIds(List.of(entity.getOrgId()));
        Map<Long, String> platformNameMap = cloudPlatformMapper.selectBatchIds(List.of(entity.getCloudPlatformId())).stream()
            .collect(Collectors.toMap(SupplyCloudPlatform::getId, SupplyCloudPlatform::getPlatformName));
        Map<Long, String> cloudTenantNameMap = cloudTenantNameMap();
        return toVo(entity, deptNameMap, platformNameMap, cloudTenantNameMap);
    }

    @Override
    public boolean insertByBo(SupplyOrgCloudTenantBindBo bo) {
        Date effectiveTime = bo.getEffectiveTime() == null ? new Date() : bo.getEffectiveTime();
        String bindStatus = StringUtils.defaultIfBlank(bo.getBindStatus(), "bound");
        validateWriteRules(bindStatus, effectiveTime, bo.getInvalidTime());
        String tenantScope = validateBind(bo);
        SupplyOrgCloudTenantBind entity = new SupplyOrgCloudTenantBind();
        BeanUtil.copyProperties(bo, entity);
        entity.setId(idGenerator.nextId());
        entity.setTenantId(tenantScope);
        entity.setBindStatus(bindStatus);
        entity.setEffectiveTime(effectiveTime);
        return bindMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateByBo(SupplyOrgCloudTenantBindBo bo) {
        SupplyOrgCloudTenantBind entity = getBindOrThrow(bo.getBindingId());
        String bindStatus = StringUtils.defaultIfBlank(bo.getBindStatus(), entity.getBindStatus());
        Date effectiveTime = bo.getEffectiveTime() == null ? entity.getEffectiveTime() : bo.getEffectiveTime();
        Date invalidTime = bo.getInvalidTime() == null ? entity.getInvalidTime() : bo.getInvalidTime();
        validateWriteRules(bindStatus, effectiveTime, invalidTime);
        String tenantScope = validateBind(bo);
        BeanUtil.copyProperties(bo, entity);
        entity.setId(bo.getBindingId());
        entity.setTenantId(tenantScope);
        entity.setBindStatus(bindStatus);
        entity.setEffectiveTime(effectiveTime);
        entity.setInvalidTime(invalidTime);
        return bindMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return bindMapper.deleteByIds(ids) > 0;
    }

    @Override
    public List<SupplyOptionVo> queryOrgOptions() {
        return remoteDeptService.selectDeptsByList().stream().map(item -> {
            SupplyOptionVo vo = new SupplyOptionVo();
            vo.setLabel(item.getDeptName());
            vo.setValue(item.getDeptId());
            return vo;
        }).toList();
    }

    private void validateWriteRules(String bindStatus, Date effectiveTime, Date invalidTime) {
        if (StringUtils.equalsIgnoreCase(bindStatus, "unbound") && invalidTime == null) {
            throw new ServiceException("解绑时间不能为空");
        }
        if (effectiveTime != null && invalidTime != null && invalidTime.before(effectiveTime)) {
            throw new ServiceException("失效时间不能早于生效时间");
        }
    }

    private String validateBind(SupplyOrgCloudTenantBindBo bo) {
        SupplyCloudPlatform cloudPlatform = cloudPlatformMapper.selectById(bo.getCloudPlatformId());
        if (cloudPlatform == null) {
            throw new ServiceException("云平台不存在");
        }
        if (!cloudTenantMap().containsKey(bo.getCloudTenantSnapshotId())) {
            throw new ServiceException("云租户不存在");
        }
        boolean orgExists = remoteDeptService.selectDeptsByList().stream().map(RemoteDeptVo::getDeptId).anyMatch(id -> Objects.equals(id, bo.getOrgId()));
        if (!orgExists) {
            throw new ServiceException("组织不存在");
        }
        String tenantScope = resolveTargetTenantId(cloudPlatform.getTenantId());
        boolean duplicated = bindMapper.exists(Wrappers.<SupplyOrgCloudTenantBind>lambdaQuery()
            .eq(StringUtils.isNotBlank(tenantScope), SupplyOrgCloudTenantBind::getTenantId, tenantScope)
            .eq(SupplyOrgCloudTenantBind::getCloudTenantSnapshotId, bo.getCloudTenantSnapshotId())
            .ne(Objects.nonNull(bo.getBindingId()), SupplyOrgCloudTenantBind::getId, bo.getBindingId()));
        if (duplicated) {
            throw new ServiceException("该云租户已存在绑定关系");
        }
        return tenantScope;
    }

    private SupplyOrgCloudTenantBind getBindOrThrow(Long bindingId) {
        String tenantScope = queryTenantScope();
        SupplyOrgCloudTenantBind entity = bindMapper.selectOne(Wrappers.<SupplyOrgCloudTenantBind>lambdaQuery()
            .eq(StringUtils.isNotBlank(tenantScope), SupplyOrgCloudTenantBind::getTenantId, tenantScope)
            .eq(SupplyOrgCloudTenantBind::getId, bindingId));
        if (entity == null) {
            throw new ServiceException("绑定关系不存在");
        }
        return entity;
    }

    private Map<Long, String> cloudTenantNameMap() {
        return cloudTenantMap().values().stream()
            .collect(Collectors.toMap(RemoteTenantVo::getId, RemoteTenantVo::getCompanyName, (a, b) -> a));
    }

    private Map<Long, RemoteTenantVo> cloudTenantMap() {
        List<RemoteTenantVo> tenants = remoteTenantService.queryList();
        if (tenants == null) {
            return Collections.emptyMap();
        }
        return tenants.stream()
            .filter(item -> StringUtils.equalsIgnoreCase(CLOUD_TENANT_TYPE, getTenantType(item)))
            .collect(Collectors.toMap(RemoteTenantVo::getId, item -> item, (a, b) -> a));
    }

    private String getTenantType(RemoteTenantVo item) {
        try {
            return BeanUtil.getProperty(item, "tenantType");
        } catch (Exception ignored) {
            return null;
        }
    }

    private SupplyOrgCloudTenantBindVo toVo(SupplyOrgCloudTenantBind entity, Map<Long, String> deptNameMap,
                                            Map<Long, String> platformNameMap, Map<Long, String> cloudTenantNameMap) {
        SupplyOrgCloudTenantBindVo vo = new SupplyOrgCloudTenantBindVo();
        BeanUtil.copyProperties(entity, vo);
        vo.setBindingId(entity.getId());
        vo.setOrgName(deptNameMap.get(entity.getOrgId()));
        vo.setCloudPlatformName(platformNameMap.get(entity.getCloudPlatformId()));
        vo.setCloudTenantName(cloudTenantNameMap.get(entity.getCloudTenantSnapshotId()));
        return vo;
    }
}
