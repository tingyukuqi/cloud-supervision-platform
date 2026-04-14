package com.csmp.supply.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csmp.common.core.utils.StringUtils;
import com.csmp.common.mybatis.core.page.PageQuery;
import com.csmp.common.mybatis.core.page.TableDataInfo;
import com.csmp.supply.api.domain.bo.CollectExecuteBo;
import com.csmp.supply.api.domain.vo.CollectExecuteResultVo;
import com.csmp.supply.domain.SupplyCloudPlatform;
import com.csmp.supply.domain.SupplyCloudTenant;
import com.csmp.supply.domain.SupplyCollectConfig;
import com.csmp.supply.domain.SupplyOrgCloudTenantBind;
import com.csmp.supply.domain.bo.SupplyCloudTenantBo;
import com.csmp.supply.domain.vo.SupplyCloudTenantVo;
import com.csmp.supply.domain.vo.SupplyOptionVo;
import com.csmp.supply.mapper.SupplyCloudPlatformMapper;
import com.csmp.supply.mapper.SupplyCloudTenantMapper;
import com.csmp.supply.mapper.SupplyCollectConfigMapper;
import com.csmp.supply.mapper.SupplyOrgCloudTenantBindMapper;
import com.csmp.supply.service.ISupplyCloudTenantService;
import com.csmp.supply.service.ISupplyCollectConfigService;
import com.csmp.system.api.RemoteDeptService;
import com.csmp.system.api.RemoteTenantService;
import com.csmp.system.api.domain.vo.RemoteTenantVo;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 云租户服务实现
 *
 * @author csmp
 */
@Service
@RequiredArgsConstructor
public class SupplyCloudTenantServiceImpl extends AbstractSupplyService implements ISupplyCloudTenantService {

    private static final String CLOUD_TENANT_TYPE = "cloud_tenant";
    private static final String UNBOUND_STATUS = "unbound";

    private final SupplyCloudTenantMapper cloudTenantMapper;
    private final SupplyCloudPlatformMapper cloudPlatformMapper;
    private final SupplyOrgCloudTenantBindMapper bindMapper;
    private final SupplyCollectConfigMapper collectConfigMapper;
    private final ISupplyCollectConfigService collectConfigService;
    private final RemoteDeptService remoteDeptService;

    @DubboReference(mock = "true")
    private RemoteTenantService remoteTenantService;

    @Override
    public TableDataInfo<SupplyCloudTenantVo> queryPageList(SupplyCloudTenantBo bo, PageQuery pageQuery) {
        String tenantScope = queryTenantScope();
        LambdaQueryWrapper<SupplyCloudTenant> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(tenantScope), SupplyCloudTenant::getTenantId, tenantScope);
        lqw.eq(Objects.nonNull(bo.getCloudPlatformId()), SupplyCloudTenant::getCloudPlatformId, bo.getCloudPlatformId());
        lqw.eq(StringUtils.isNotBlank(bo.getTenantStatus()), SupplyCloudTenant::getTenantStatus, bo.getTenantStatus());
        lqw.and(StringUtils.isNotBlank(bo.getKeyword()), w -> w.like(SupplyCloudTenant::getCloudTenantName, bo.getKeyword())
            .or().like(SupplyCloudTenant::getCloudTenantCode, bo.getKeyword()));
        lqw.orderByDesc(SupplyCloudTenant::getLastSyncTime);
        Page<SupplyCloudTenant> page = cloudTenantMapper.selectPage(pageQuery.build(), lqw);
        List<Long> platformIds = page.getRecords().stream()
            .map(SupplyCloudTenant::getCloudPlatformId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, String> platformNameMap = platformIds.isEmpty()
            ? Collections.emptyMap()
            : cloudPlatformMapper.selectBatchIds(platformIds).stream()
                .collect(Collectors.toMap(SupplyCloudPlatform::getId, SupplyCloudPlatform::getPlatformName, (a, b) -> a));
        List<Long> snapshotIds = page.getRecords().stream().map(SupplyCloudTenant::getId).toList();
        List<SupplyOrgCloudTenantBind> binds = snapshotIds.isEmpty()
            ? List.of()
            : bindMapper.selectList(Wrappers.<SupplyOrgCloudTenantBind>lambdaQuery()
                .eq(StringUtils.isNotBlank(tenantScope), SupplyOrgCloudTenantBind::getTenantId, tenantScope)
                .in(SupplyOrgCloudTenantBind::getCloudTenantSnapshotId, snapshotIds));
        Map<Long, SupplyOrgCloudTenantBind> bindMap = binds.stream().collect(Collectors.toMap(SupplyOrgCloudTenantBind::getCloudTenantSnapshotId, item -> item, (a, b) -> a));
        List<Long> deptIds = binds.stream().map(SupplyOrgCloudTenantBind::getOrgId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> deptNameMap = deptIds.isEmpty() ? Collections.emptyMap() : remoteDeptService.selectDeptNamesByIds(deptIds);
        List<SupplyCloudTenantVo> list = page.getRecords().stream().map(item -> toVo(item, platformNameMap, bindMap, deptNameMap)).toList();
        if (StringUtils.isNotBlank(bo.getBindStatus())) {
            list = list.stream().filter(item -> bo.getBindStatus().equals(item.getBindStatus())).toList();
        }
        return new TableDataInfo<>(list, page.getTotal());
    }

    @Override
    public CollectExecuteResultVo refreshByCloudPlatformId(Long cloudPlatformId, Long triggerUserId) {
        String tenantScope = queryTenantScope();
        SupplyCollectConfig config = collectConfigMapper.selectOne(Wrappers.<SupplyCollectConfig>lambdaQuery()
            .eq(StringUtils.isNotBlank(tenantScope), SupplyCollectConfig::getTenantId, tenantScope)
            .eq(SupplyCollectConfig::getCloudPlatformId, cloudPlatformId)
            .eq(SupplyCollectConfig::getCollectScope, "tenant")
            .last("limit 1"));
        CollectExecuteBo bo = new CollectExecuteBo();
        bo.setTriggerUserId(triggerUserId);
        bo.setExecuteMode("refresh");
        if (config != null) {
            bo.setCollectConfigId(config.getId());
            return collectConfigService.executeCollect(bo);
        }
        CollectExecuteResultVo vo = new CollectExecuteResultVo();
        vo.setAccepted(Boolean.FALSE);
        vo.setMessage("当前云平台未配置云租户采集配置");
        return vo;
    }

    @Override
    public List<SupplyOptionVo> queryOptions(Long cloudPlatformId, String keyword, String bindStatus) {
        String tenantScope = queryTenantScope();
        Map<Long, String> bindStatusMap = bindMapper.selectList(Wrappers.<SupplyOrgCloudTenantBind>lambdaQuery()
                .eq(StringUtils.isNotBlank(tenantScope), SupplyOrgCloudTenantBind::getTenantId, tenantScope))
            .stream()
            .collect(Collectors.toMap(SupplyOrgCloudTenantBind::getCloudTenantSnapshotId, item -> item.getBindStatus(), (a, b) -> a));
        return listCloudTenants(keyword).stream()
            .map(item -> new CloudTenantOptionSource(item, bindStatusMap.getOrDefault(item.getId(), UNBOUND_STATUS)))
            .filter(item -> StringUtils.isBlank(bindStatus)
                || StringUtils.equalsIgnoreCase(bindStatus, item.bindStatus()))
            .map(item -> buildOption(item.tenant(), item.bindStatus(), cloudPlatformId))
            .toList();
    }

    private List<RemoteTenantVo> listCloudTenants(String keyword) {
        List<RemoteTenantVo> tenants = remoteTenantService.queryList();
        if (tenants == null) {
            return List.of();
        }
        return tenants.stream()
            .filter(item -> StringUtils.equalsIgnoreCase(CLOUD_TENANT_TYPE, getTenantType(item)))
            .filter(item -> StringUtils.isBlank(keyword)
                || StringUtils.containsAnyIgnoreCase(item.getCompanyName(), keyword)
                || StringUtils.containsAnyIgnoreCase(item.getTenantId(), keyword))
            .toList();
    }

    private SupplyOptionVo buildOption(RemoteTenantVo item, String bindStatus, Long cloudPlatformId) {
        SupplyOptionVo vo = new SupplyOptionVo();
        vo.setLabel(item.getCompanyName());
        vo.setValue(item.getId());
        vo.setExtra(Map.of(
            "tenantId", item.getTenantId(),
            "tenantType", getTenantType(item),
            "bindStatus", StringUtils.defaultIfBlank(bindStatus, UNBOUND_STATUS),
            "cloudPlatformId", cloudPlatformId
        ));
        return vo;
    }

    private String getTenantType(RemoteTenantVo item) {
        try {
            return BeanUtil.getProperty(item, "tenantType");
        } catch (Exception ignored) {
            return null;
        }
    }

    private record CloudTenantOptionSource(RemoteTenantVo tenant, String bindStatus) {
    }

    private SupplyCloudTenantVo toVo(SupplyCloudTenant entity, Map<Long, String> platformNameMap,
                                     Map<Long, SupplyOrgCloudTenantBind> bindMap, Map<Long, String> deptNameMap) {
        SupplyCloudTenantVo vo = new SupplyCloudTenantVo();
        BeanUtil.copyProperties(entity, vo);
        vo.setCloudTenantSnapshotId(entity.getId());
        vo.setCloudPlatformName(platformNameMap.get(entity.getCloudPlatformId()));
        vo.setRawPayload(parseJsonObject(entity.getRawPayload()));
        SupplyOrgCloudTenantBind bind = bindMap.get(entity.getId());
        vo.setBindStatus(bind == null ? "unbound" : bind.getBindStatus());
        if (bind != null) {
            vo.setBoundOrgId(bind.getOrgId());
            vo.setBoundOrgName(deptNameMap.get(bind.getOrgId()));
        }
        return vo;
    }
}
