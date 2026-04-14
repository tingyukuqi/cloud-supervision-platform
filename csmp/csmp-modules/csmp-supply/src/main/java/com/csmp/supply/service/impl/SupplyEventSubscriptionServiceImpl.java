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
import com.csmp.supply.domain.SupplyCloudPlatform;
import com.csmp.supply.domain.SupplyEventLog;
import com.csmp.supply.domain.SupplyEventSubscription;
import com.csmp.supply.domain.bo.SupplyEventIngestBo;
import com.csmp.supply.domain.bo.SupplyEventLogBo;
import com.csmp.supply.domain.bo.SupplyEventSubscriptionBo;
import com.csmp.supply.domain.enums.CollectResultStatusEnum;
import com.csmp.supply.domain.enums.EnableStatusEnum;
import com.csmp.supply.domain.vo.SupplyEventLogVo;
import com.csmp.supply.domain.vo.SupplyEventSubscriptionVo;
import com.csmp.supply.mapper.SupplyCloudPlatformMapper;
import com.csmp.supply.mapper.SupplyEventLogMapper;
import com.csmp.supply.mapper.SupplyEventSubscriptionMapper;
import com.csmp.supply.service.ISupplyEventSubscriptionService;
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
 * 事件订阅服务实现
 *
 * @author csmp
 */
@Service
@RequiredArgsConstructor
public class SupplyEventSubscriptionServiceImpl extends AbstractSupplyService implements ISupplyEventSubscriptionService {

    private final SupplyEventSubscriptionMapper eventSubscriptionMapper;
    private final SupplyEventLogMapper eventLogMapper;
    private final SupplyCloudPlatformMapper cloudPlatformMapper;
    private final SupplyIdGenerator idGenerator;

    @Override
    public TableDataInfo<SupplyEventSubscriptionVo> queryPageList(SupplyEventSubscriptionBo bo, PageQuery pageQuery) {
        String tenantScope = queryTenantScope();
        LambdaQueryWrapper<SupplyEventSubscription> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(tenantScope), SupplyEventSubscription::getTenantId, tenantScope);
        lqw.eq(Objects.nonNull(bo.getCloudPlatformId()), SupplyEventSubscription::getCloudPlatformId, bo.getCloudPlatformId());
        lqw.eq(StringUtils.isNotBlank(bo.getProviderCode()), SupplyEventSubscription::getProviderCode, bo.getProviderCode());
        lqw.eq(StringUtils.isNotBlank(bo.getEventScope()), SupplyEventSubscription::getEventScope, bo.getEventScope());
        lqw.eq(StringUtils.isNotBlank(bo.getIngestMode()), SupplyEventSubscription::getIngestMode, bo.getIngestMode());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SupplyEventSubscription::getStatus, bo.getStatus());
        lqw.orderByDesc(SupplyEventSubscription::getCreateTime);
        Page<SupplyEventSubscription> page = eventSubscriptionMapper.selectPage(pageQuery.build(), lqw);
        Map<Long, String> platformNames = platformNameMap(page.getRecords().stream().map(SupplyEventSubscription::getCloudPlatformId).toList());
        List<SupplyEventSubscriptionVo> list = page.getRecords().stream().map(item -> toVo(item, platformNames)).toList();
        return new TableDataInfo<>(list, page.getTotal());
    }

    @Override
    public SupplyEventSubscriptionVo queryById(Long subscriptionId) {
        SupplyEventSubscription entity = getSubscriptionOrThrow(subscriptionId);
        return toVo(entity, platformNameMap(List.of(entity.getCloudPlatformId())));
    }

    @Override
    public boolean insertByBo(SupplyEventSubscriptionBo bo) {
        EventWriteFields fields = resolveWriteFields(bo, null);
        validateWriteRules(fields);
        String tenantScope = resolvePlatformTenantId(bo.getCloudPlatformId());
        validateUnique(bo, tenantScope);
        SupplyEventSubscription entity = new SupplyEventSubscription();
        BeanUtil.copyProperties(bo, entity);
        entity.setId(idGenerator.nextId());
        entity.setTenantId(tenantScope);
        entity.setAuthType(fields.authType());
        entity.setAuthPayload(fields.authPayload());
        entity.setStatus(StringUtils.defaultIfBlank(bo.getStatus(), EnableStatusEnum.ENABLE.getCode()));
        return eventSubscriptionMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateByBo(SupplyEventSubscriptionBo bo) {
        SupplyEventSubscription entity = getSubscriptionOrThrow(bo.getSubscriptionId());
        EventWriteFields fields = resolveWriteFields(bo, entity);
        validateWriteRules(fields);
        String tenantScope = resolvePlatformTenantId(bo.getCloudPlatformId());
        validateUnique(bo, tenantScope);
        String existingStatus = entity.getStatus();
        BeanUtil.copyProperties(bo, entity);
        entity.setId(bo.getSubscriptionId());
        entity.setTenantId(tenantScope);
        entity.setIngestMode(fields.ingestMode());
        entity.setTopicName(fields.topicName());
        entity.setConsumerGroup(fields.consumerGroup());
        entity.setEndpointPath(fields.endpointPath());
        entity.setAuthType(fields.authType());
        entity.setAuthPayload(fields.authPayload());
        entity.setStatus(StringUtils.defaultIfBlank(bo.getStatus(), existingStatus));
        return eventSubscriptionMapper.updateById(entity) > 0;
    }

    @Override
    public boolean changeStatus(Long subscriptionId, String status) {
        SupplyEventSubscription entity = getSubscriptionOrThrow(subscriptionId);
        entity.setStatus(status);
        return eventSubscriptionMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return eventSubscriptionMapper.deleteByIds(ids) > 0;
    }

    @Override
    public TableDataInfo<SupplyEventLogVo> queryEventPage(Long subscriptionId, SupplyEventLogBo bo, PageQuery pageQuery) {
        SupplyEventSubscription subscription = getSubscriptionOrThrow(subscriptionId);
        String tenantScope = resolveTargetTenantId(subscription.getTenantId());
        LambdaQueryWrapper<SupplyEventLog> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(tenantScope), SupplyEventLog::getTenantId, tenantScope);
        lqw.eq(SupplyEventLog::getSubscriptionId, subscriptionId);
        lqw.eq(StringUtils.isNotBlank(bo.getProcessStatus()), SupplyEventLog::getProcessStatus, bo.getProcessStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getEventScope()), SupplyEventLog::getEventScope, bo.getEventScope());
        lqw.orderByDesc(SupplyEventLog::getIngestTime);
        Page<SupplyEventLog> page = eventLogMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page.convert(this::toLogVo));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean ingestEvent(Long subscriptionId, SupplyEventIngestBo bo) {
        SupplyEventSubscription subscription = getSubscriptionOrThrow(subscriptionId);
        String tenantScope = resolveTargetTenantId(subscription.getTenantId());
        if (!EnableStatusEnum.isEnabled(subscription.getStatus())) {
            throw new ServiceException("事件订阅已停用");
        }
        boolean duplicated = eventLogMapper.exists(Wrappers.<SupplyEventLog>lambdaQuery()
            .eq(StringUtils.isNotBlank(tenantScope), SupplyEventLog::getTenantId, tenantScope)
            .eq(SupplyEventLog::getSubscriptionId, subscriptionId)
            .eq(SupplyEventLog::getSourceEventId, bo.getSourceEventId()));
        if (duplicated) {
            throw new ServiceException("事件已接收，禁止重复入库");
        }
        Date now = new Date();
        SupplyEventLog entity = new SupplyEventLog();
        entity.setId(idGenerator.nextId());
        entity.setTenantId(tenantScope);
        entity.setSubscriptionId(subscriptionId);
        entity.setCloudPlatformId(subscription.getCloudPlatformId());
        entity.setEventScope(StringUtils.defaultIfBlank(bo.getEventScope(), subscription.getEventScope()));
        entity.setEventKey(bo.getEventKey());
        entity.setSourceEventId(bo.getSourceEventId());
        entity.setEventTime(parseDateTime(bo.getEventTime()));
        entity.setIngestTime(now);
        entity.setProcessStatus(CollectResultStatusEnum.RECEIVED.getCode());
        entity.setTraceId(idGenerator.nextTraceId());
        entity.setRawPayload(JsonUtils.toJsonString(bo.getPayload()));
        entity.setNormalizedPayload(JsonUtils.toJsonString(Map.of(
            "providerCode", subscription.getProviderCode(),
            "eventScope", entity.getEventScope(),
            "eventKey", bo.getEventKey(),
            "payload", bo.getPayload()
        )));
        eventLogMapper.insert(entity);
        subscription.setLastEventTime(now);
        subscription.setLastErrorMessage(null);
        eventSubscriptionMapper.updateById(subscription);
        return true;
    }

    private void validateWriteRules(EventWriteFields fields) {
        if (StringUtils.inStringIgnoreCase(fields.ingestMode(), "rocketmq", "kafka_adapter")) {
            if (StringUtils.isBlank(fields.topicName())) {
                throw new ServiceException("事件主题不能为空");
            }
            if (StringUtils.isBlank(fields.consumerGroup())) {
                throw new ServiceException("消费组不能为空");
            }
        }
        if (StringUtils.equalsIgnoreCase(fields.ingestMode(), "webhook")) {
            validateEndpointPath(fields.endpointPath(), "回调路径");
        } else {
            validateOptionalEndpointPath(fields.endpointPath(), "回调路径");
        }
        if (StringUtils.isNotBlank(fields.authPayload()) && StringUtils.isBlank(fields.authType())) {
            throw new ServiceException("鉴权类型不能为空");
        }
        if (requiresAuthPayload(fields.authType()) && StringUtils.isBlank(fields.authPayload())) {
            throw new ServiceException("鉴权配置不能为空");
        }
    }

    private EventWriteFields resolveWriteFields(SupplyEventSubscriptionBo bo, SupplyEventSubscription current) {
        String effectiveIngestMode = bo.getIngestMode();
        boolean sameMode = current != null && StringUtils.equalsIgnoreCase(effectiveIngestMode, current.getIngestMode());
        String effectiveTopicName = preserveWhenBlank(bo.getTopicName(), current == null ? null : current.getTopicName(), sameMode);
        String effectiveConsumerGroup = preserveWhenBlank(bo.getConsumerGroup(), current == null ? null : current.getConsumerGroup(), sameMode);
        String effectiveEndpointPath = preserveWhenBlank(bo.getEndpointPath(), current == null ? null : current.getEndpointPath(), sameMode);
        String effectiveAuthType = current == null ? bo.getAuthType() : StringUtils.defaultIfBlank(bo.getAuthType(), current.getAuthType());
        boolean sameAuthType = current != null && StringUtils.equalsIgnoreCase(effectiveAuthType, current.getAuthType());
        String effectiveAuthPayload = preserveWhenBlank(bo.getAuthPayload(), current == null ? null : current.getAuthPayload(), sameAuthType);
        return new EventWriteFields(effectiveIngestMode, effectiveTopicName, effectiveConsumerGroup, effectiveEndpointPath, effectiveAuthType, effectiveAuthPayload);
    }

    private String preserveWhenBlank(String candidate, String existing, boolean keepExisting) {
        if (keepExisting && StringUtils.isBlank(candidate)) {
            return existing;
        }
        return candidate;
    }

    private void validateUnique(SupplyEventSubscriptionBo bo, String tenantScope) {
        boolean duplicated = eventSubscriptionMapper.exists(Wrappers.<SupplyEventSubscription>lambdaQuery()
            .eq(StringUtils.isNotBlank(tenantScope), SupplyEventSubscription::getTenantId, tenantScope)
            .eq(SupplyEventSubscription::getCloudPlatformId, bo.getCloudPlatformId())
            .eq(SupplyEventSubscription::getEventScope, bo.getEventScope())
            .ne(Objects.nonNull(bo.getSubscriptionId()), SupplyEventSubscription::getId, bo.getSubscriptionId()));
        if (duplicated) {
            throw new ServiceException("同一云平台和事件范围只允许一个订阅");
        }
    }

    private SupplyEventSubscription getSubscriptionOrThrow(Long subscriptionId) {
        String tenantScope = queryTenantScope();
        SupplyEventSubscription entity = eventSubscriptionMapper.selectOne(Wrappers.<SupplyEventSubscription>lambdaQuery()
            .eq(StringUtils.isNotBlank(tenantScope), SupplyEventSubscription::getTenantId, tenantScope)
            .eq(SupplyEventSubscription::getId, subscriptionId));
        if (entity == null) {
            throw new ServiceException("事件订阅不存在");
        }
        return entity;
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

    private SupplyEventSubscriptionVo toVo(SupplyEventSubscription entity, Map<Long, String> platformNames) {
        SupplyEventSubscriptionVo vo = new SupplyEventSubscriptionVo();
        BeanUtil.copyProperties(entity, vo);
        vo.setSubscriptionId(entity.getId());
        vo.setCloudPlatformName(platformNames.get(entity.getCloudPlatformId()));
        vo.setAuthPayloadMasked(SupplyMaskHelper.maskJsonPayload(entity.getAuthPayload()));
        return vo;
    }

    private SupplyEventLogVo toLogVo(SupplyEventLog entity) {
        SupplyEventLogVo vo = new SupplyEventLogVo();
        BeanUtil.copyProperties(entity, vo);
        vo.setEventLogId(entity.getId());
        vo.setRawPayload(parseJsonObject(entity.getRawPayload()));
        vo.setNormalizedPayload(parseJsonObject(entity.getNormalizedPayload()));
        return vo;
    }

    private record EventWriteFields(String ingestMode, String topicName, String consumerGroup,
                                    String endpointPath, String authType, String authPayload) {
    }
}
