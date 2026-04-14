package com.csmp.supply.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csmp.common.core.exception.ServiceException;
import com.csmp.common.core.utils.StringUtils;
import com.csmp.common.json.utils.JsonUtils;
import com.csmp.common.mybatis.core.page.PageQuery;
import com.csmp.common.mybatis.core.page.TableDataInfo;
import com.csmp.supply.api.domain.bo.CollectExecuteBo;
import com.csmp.supply.api.domain.vo.CollectExecuteResultVo;
import com.csmp.supply.domain.SupplyCloudPlatform;
import com.csmp.supply.domain.SupplyCloudTenant;
import com.csmp.supply.domain.SupplyCollectConfig;
import com.csmp.supply.domain.SupplyCollectLog;
import com.csmp.supply.domain.bo.SupplyCollectConfigBo;
import com.csmp.supply.domain.bo.SupplyCollectLogBo;
import com.csmp.supply.domain.enums.CollectExecuteModeEnum;
import com.csmp.supply.domain.enums.CollectResultStatusEnum;
import com.csmp.supply.domain.enums.EnableStatusEnum;
import com.csmp.supply.domain.vo.SupplyCollectConfigVo;
import com.csmp.supply.domain.vo.SupplyCollectLogVo;
import com.csmp.supply.mapper.SupplyCloudPlatformMapper;
import com.csmp.supply.mapper.SupplyCloudTenantMapper;
import com.csmp.supply.mapper.SupplyCollectConfigMapper;
import com.csmp.supply.mapper.SupplyCollectLogMapper;
import com.csmp.supply.service.ISupplyCollectConfigService;
import com.csmp.supply.support.SupplyCollectExecutor;
import com.csmp.supply.support.SupplyCollectResult;
import com.csmp.supply.support.SupplyIdGenerator;
import com.csmp.supply.support.SupplyMaskHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 采集配置服务实现
 *
 * @author csmp
 */
@Service
@RequiredArgsConstructor
public class SupplyCollectConfigServiceImpl extends AbstractSupplyService implements ISupplyCollectConfigService {

    private final SupplyCollectConfigMapper collectConfigMapper;
    private final SupplyCollectLogMapper collectLogMapper;
    private final SupplyCloudTenantMapper cloudTenantMapper;
    private final SupplyCloudPlatformMapper cloudPlatformMapper;
    private final SupplyCollectExecutor collectExecutor;
    private final SupplyIdGenerator idGenerator;

    @Override
    public TableDataInfo<SupplyCollectConfigVo> queryPageList(SupplyCollectConfigBo bo, PageQuery pageQuery) {
        String tenantScope = queryTenantScope();
        LambdaQueryWrapper<SupplyCollectConfig> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(tenantScope), SupplyCollectConfig::getTenantId, tenantScope);
        lqw.eq(Objects.nonNull(bo.getCloudPlatformId()), SupplyCollectConfig::getCloudPlatformId, bo.getCloudPlatformId());
        lqw.eq(StringUtils.isNotBlank(bo.getProviderCode()), SupplyCollectConfig::getProviderCode, bo.getProviderCode());
        lqw.eq(StringUtils.isNotBlank(bo.getCollectScope()), SupplyCollectConfig::getCollectScope, bo.getCollectScope());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SupplyCollectConfig::getStatus, bo.getStatus());
        lqw.orderByDesc(SupplyCollectConfig::getCreateTime);
        Page<SupplyCollectConfig> page = collectConfigMapper.selectPage(pageQuery.build(), lqw);
        Map<Long, String> platformNames = platformNameMap(page.getRecords().stream().map(SupplyCollectConfig::getCloudPlatformId).toList());
        List<SupplyCollectConfigVo> list = page.getRecords().stream().map(item -> toVo(item, platformNames)).toList();
        return new TableDataInfo<>(list, page.getTotal());
    }

    @Override
    public SupplyCollectConfigVo queryById(Long collectConfigId) {
        SupplyCollectConfig config = getConfigOrThrow(collectConfigId);
        return toVo(config, platformNameMap(List.of(config.getCloudPlatformId())));
    }

    @Override
    public boolean insertByBo(SupplyCollectConfigBo bo) {
        validateWriteRules(bo, null);
        String tenantScope = resolvePlatformTenantId(bo.getCloudPlatformId());
        validateConfigUnique(bo, tenantScope);
        SupplyCollectConfig entity = new SupplyCollectConfig();
        BeanUtil.copyProperties(bo, entity);
        entity.setId(idGenerator.nextId());
        entity.setTenantId(tenantScope);
        entity.setStatus(StringUtils.defaultIfBlank(bo.getStatus(), EnableStatusEnum.ENABLE.getCode()));
        if (entity.getVerifySsl() == null) {
            entity.setVerifySsl(Boolean.TRUE);
        }
        if (entity.getTimeoutSeconds() == null) {
            entity.setTimeoutSeconds(30);
        }
        if (entity.getRetryTimes() == null) {
            entity.setRetryTimes(0);
        }
        return collectConfigMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateByBo(SupplyCollectConfigBo bo) {
        SupplyCollectConfig entity = getConfigOrThrow(bo.getCollectConfigId());
        validateWriteRules(bo, entity);
        String tenantScope = resolvePlatformTenantId(bo.getCloudPlatformId());
        validateConfigUnique(bo, tenantScope);
        String existingStatus = entity.getStatus();
        Integer existingTimeoutSeconds = entity.getTimeoutSeconds();
        Integer existingRetryTimes = entity.getRetryTimes();
        Boolean existingVerifySsl = entity.getVerifySsl();
        String effectiveAuthPayload = resolveAuthPayload(bo, entity);
        BeanUtil.copyProperties(bo, entity);
        entity.setId(bo.getCollectConfigId());
        entity.setTenantId(tenantScope);
        entity.setAuthPayload(effectiveAuthPayload);
        entity.setTimeoutSeconds(bo.getTimeoutSeconds() != null ? bo.getTimeoutSeconds() : Objects.requireNonNullElse(existingTimeoutSeconds, 30));
        entity.setRetryTimes(bo.getRetryTimes() != null ? bo.getRetryTimes() : Objects.requireNonNullElse(existingRetryTimes, 0));
        entity.setVerifySsl(bo.getVerifySsl() != null ? bo.getVerifySsl() : Objects.requireNonNullElse(existingVerifySsl, Boolean.TRUE));
        entity.setStatus(StringUtils.defaultIfBlank(bo.getStatus(), existingStatus));
        return collectConfigMapper.updateById(entity) > 0;
    }

    @Override
    public boolean changeStatus(Long collectConfigId, String status) {
        SupplyCollectConfig entity = getConfigOrThrow(collectConfigId);
        entity.setStatus(status);
        return collectConfigMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (Boolean.TRUE.equals(isValid)) {
            for (Long id : ids) {
                SupplyCollectConfig config = getConfigOrThrow(id);
                if (EnableStatusEnum.isEnabled(config.getStatus())) {
                    throw new ServiceException("启用状态的采集配置不能删除");
                }
            }
        }
        return collectConfigMapper.deleteByIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CollectExecuteResultVo executeCollect(CollectExecuteBo bo) {
        SupplyCollectConfig config = getConfigOrThrow(bo.getCollectConfigId());
        String tenantScope = resolveTargetTenantId(config.getTenantId());
        if (!EnableStatusEnum.isEnabled(config.getStatus())) {
            throw new ServiceException("采集配置已停用，禁止执行");
        }
        Date now = new Date();
        SupplyCollectLog log = new SupplyCollectLog();
        log.setId(idGenerator.nextId());
        log.setTenantId(tenantScope);
        log.setCollectConfigId(config.getId());
        log.setCloudPlatformId(config.getCloudPlatformId());
        log.setCollectScope(config.getCollectScope());
        log.setExecuteMode(StringUtils.defaultIfBlank(bo.getExecuteMode(), CollectExecuteModeEnum.MANUAL.getCode()));
        log.setJobInstanceId(String.valueOf(log.getId()));
        log.setTraceId(idGenerator.nextTraceId());
        log.setTriggerUserId(bo.getTriggerUserId());
        log.setStartTime(now);
        log.setResultStatus(CollectResultStatusEnum.RUNNING.getCode());
        log.setConfigSnapshot(JsonUtils.toJsonString(config));
        collectLogMapper.insert(log);

        SupplyCollectResult result;
        try {
            result = collectExecutor.execute(config);
            if (result.isSuccess() && "tenant".equals(config.getCollectScope())) {
                ensureTenantSnapshot(config, now);
            }
            log.setResultStatus(result.isSuccess() ? CollectResultStatusEnum.SUCCESS.getCode() : CollectResultStatusEnum.FAILED.getCode());
            log.setSyncStatus(result.isSuccess() ? CollectResultStatusEnum.SUCCESS.getCode() : CollectResultStatusEnum.FAILED.getCode());
            log.setResourceCount(result.getResourceCount());
            log.setCloudTenantCount(result.getCloudTenantCount());
            log.setSyncRecordCount(result.getSyncRecordCount());
            log.setErrorMessage(result.isSuccess() ? null : result.getMessage());
            config.setLastCollectStatus(log.getResultStatus());
            config.setLastErrorMessage(log.getErrorMessage());
            config.setLastSuccessTime(result.isSuccess() ? now : config.getLastSuccessTime());
        } catch (Exception e) {
            log.setResultStatus(CollectResultStatusEnum.FAILED.getCode());
            log.setSyncStatus(CollectResultStatusEnum.FAILED.getCode());
            log.setErrorMessage(StringUtils.substring(e.getMessage(), 0, 500));
            config.setLastCollectStatus(log.getResultStatus());
            config.setLastErrorMessage(log.getErrorMessage());
        }
        log.setEndTime(new Date());
        log.setDurationMs(log.getEndTime().getTime() - now.getTime());
        collectLogMapper.updateById(log);

        config.setLastCollectTime(now);
        config.setNextCollectTime(null);
        collectConfigMapper.updateById(config);

        CollectExecuteResultVo vo = new CollectExecuteResultVo();
        vo.setAccepted(Boolean.TRUE);
        vo.setTraceId(log.getTraceId());
        vo.setJobInstanceId(log.getJobInstanceId());
        vo.setMessage(CollectResultStatusEnum.SUCCESS.getCode().equals(log.getResultStatus()) ? "采集执行完成" : "采集执行失败");
        return vo;
    }

    @Override
    public TableDataInfo<SupplyCollectLogVo> queryLogPage(Long collectConfigId, SupplyCollectLogBo bo, PageQuery pageQuery) {
        SupplyCollectConfig config = getConfigOrThrow(collectConfigId);
        String tenantScope = resolveTargetTenantId(config.getTenantId());
        LambdaQueryWrapper<SupplyCollectLog> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(tenantScope), SupplyCollectLog::getTenantId, tenantScope);
        lqw.eq(SupplyCollectLog::getCollectConfigId, collectConfigId);
        lqw.eq(StringUtils.isNotBlank(bo.getResultStatus()), SupplyCollectLog::getResultStatus, bo.getResultStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getExecuteMode()), SupplyCollectLog::getExecuteMode, bo.getExecuteMode());
        lqw.orderByDesc(SupplyCollectLog::getStartTime);
        Page<SupplyCollectLog> page = collectLogMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page.convert(this::toLogVo));
    }

    private void validateWriteRules(SupplyCollectConfigBo bo, SupplyCollectConfig current) {
        validateHttpUrl(bo.getCollectUrl(), "采集地址");
        validateOptionalHttpUrl(bo.getSyncEndpoint(), "同步回调地址");
        if (requiresAuthPayload(bo.getAuthType()) && StringUtils.isBlank(resolveAuthPayload(bo, current))) {
            throw new ServiceException("鉴权配置不能为空");
        }
    }

    private String resolveAuthPayload(SupplyCollectConfigBo bo, SupplyCollectConfig current) {
        if (current != null
            && StringUtils.isBlank(bo.getAuthPayload())
            && StringUtils.equalsIgnoreCase(bo.getAuthType(), current.getAuthType())) {
            return current.getAuthPayload();
        }
        return bo.getAuthPayload();
    }

    private void validateConfigUnique(SupplyCollectConfigBo bo, String tenantScope) {
        boolean duplicated = collectConfigMapper.exists(Wrappers.<SupplyCollectConfig>lambdaQuery()
            .eq(StringUtils.isNotBlank(tenantScope), SupplyCollectConfig::getTenantId, tenantScope)
            .eq(SupplyCollectConfig::getCloudPlatformId, bo.getCloudPlatformId())
            .eq(SupplyCollectConfig::getCollectScope, bo.getCollectScope())
            .ne(Objects.nonNull(bo.getCollectConfigId()), SupplyCollectConfig::getId, bo.getCollectConfigId()));
        if (duplicated) {
            throw new ServiceException("同一云平台和采集范围只允许一个采集配置");
        }
    }

    private SupplyCollectConfig getConfigOrThrow(Long collectConfigId) {
        String tenantScope = queryTenantScope();
        SupplyCollectConfig config = collectConfigMapper.selectOne(Wrappers.<SupplyCollectConfig>lambdaQuery()
            .eq(StringUtils.isNotBlank(tenantScope), SupplyCollectConfig::getTenantId, tenantScope)
            .eq(SupplyCollectConfig::getId, collectConfigId));
        if (config == null) {
            throw new ServiceException("采集配置不存在");
        }
        return config;
    }

    private String resolvePlatformTenantId(Long cloudPlatformId) {
        SupplyCloudPlatform platform = cloudPlatformMapper.selectById(cloudPlatformId);
        if (platform == null) {
            throw new ServiceException("云平台不存在");
        }
        return resolveTargetTenantId(platform.getTenantId());
    }

    private Map<Long, String> platformNameMap(List<Long> platformIds) {
        if (platformIds.isEmpty()) {
            return Map.of();
        }
        return cloudPlatformMapper.selectBatchIds(platformIds).stream()
            .collect(Collectors.toMap(SupplyCloudPlatform::getId, SupplyCloudPlatform::getPlatformName, (a, b) -> a));
    }

    private SupplyCollectConfigVo toVo(SupplyCollectConfig entity, Map<Long, String> platformNames) {
        SupplyCollectConfigVo vo = new SupplyCollectConfigVo();
        BeanUtil.copyProperties(entity, vo);
        vo.setCollectConfigId(entity.getId());
        vo.setCloudPlatformName(platformNames.get(entity.getCloudPlatformId()));
        vo.setAuthPayloadMasked(SupplyMaskHelper.maskJsonPayload(entity.getAuthPayload()));
        vo.setScopeFilter(parseJsonObject(entity.getScopeFilter()));
        vo.setCollectOptions(parseJsonObject(entity.getCollectOptions()));
        return vo;
    }

    private SupplyCollectLogVo toLogVo(SupplyCollectLog entity) {
        SupplyCollectLogVo vo = new SupplyCollectLogVo();
        BeanUtil.copyProperties(entity, vo);
        vo.setCollectLogId(entity.getId());
        vo.setConfigSnapshot(parseJsonObject(entity.getConfigSnapshot()));
        return vo;
    }

    private void ensureTenantSnapshot(SupplyCollectConfig config, Date now) {
        String tenantScope = resolveTargetTenantId(config.getTenantId());
        SupplyCloudTenant tenant = cloudTenantMapper.selectOne(Wrappers.<SupplyCloudTenant>lambdaQuery()
            .eq(StringUtils.isNotBlank(tenantScope), SupplyCloudTenant::getTenantId, tenantScope)
            .eq(SupplyCloudTenant::getCloudPlatformId, config.getCloudPlatformId())
            .eq(SupplyCloudTenant::getExternalTenantId, "default"));
        if (tenant == null) {
            tenant = new SupplyCloudTenant();
            tenant.setId(idGenerator.nextId());
            tenant.setTenantId(tenantScope);
            tenant.setCloudPlatformId(config.getCloudPlatformId());
            tenant.setExternalTenantId("default");
            tenant.setCloudTenantName("默认云租户");
            tenant.setCloudTenantCode("DEFAULT");
            tenant.setTenantStatus("active");
            tenant.setRegionCode("default");
            tenant.setSourceAccountIdentifier(config.getConnectorCode());
            tenant.setSyncStatus(CollectResultStatusEnum.SUCCESS.getCode());
            tenant.setLastSyncTime(now);
            tenant.setRawPayload(JsonUtils.toJsonString(Map.of("providerCode", config.getProviderCode(), "collectConfigId", config.getId())));
            cloudTenantMapper.insert(tenant);
        } else {
            tenant.setSyncStatus(CollectResultStatusEnum.SUCCESS.getCode());
            tenant.setLastSyncTime(now);
            cloudTenantMapper.updateById(tenant);
        }
    }
}
