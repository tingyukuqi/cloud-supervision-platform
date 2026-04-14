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
import com.csmp.supply.domain.SupplyCollectConfig;
import com.csmp.supply.domain.SupplyEventSubscription;
import com.csmp.supply.domain.bo.SupplyCloudPlatformBo;
import com.csmp.supply.domain.enums.EnableStatusEnum;
import com.csmp.supply.domain.vo.SupplyCloudPlatformVo;
import com.csmp.supply.domain.vo.SupplyOptionVo;
import com.csmp.supply.mapper.SupplyCloudPlatformMapper;
import com.csmp.supply.mapper.SupplyCollectConfigMapper;
import com.csmp.supply.mapper.SupplyEventSubscriptionMapper;
import com.csmp.supply.service.ISupplyCloudPlatformService;
import com.csmp.supply.support.SupplyIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 云平台服务实现
 *
 * @author csmp
 */
@Service
@RequiredArgsConstructor
public class SupplyCloudPlatformServiceImpl extends AbstractSupplyService implements ISupplyCloudPlatformService {

    private final SupplyCloudPlatformMapper cloudPlatformMapper;
    private final SupplyCollectConfigMapper collectConfigMapper;
    private final SupplyEventSubscriptionMapper eventSubscriptionMapper;
    private final SupplyIdGenerator idGenerator;

    @Override
    public TableDataInfo<SupplyCloudPlatformVo> queryPageList(SupplyCloudPlatformBo bo, PageQuery pageQuery) {
        String tenantScope = queryTenantScope();
        LambdaQueryWrapper<SupplyCloudPlatform> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(tenantScope), SupplyCloudPlatform::getTenantId, tenantScope);
        lqw.eq(StringUtils.isNotBlank(bo.getPlatformCode()), SupplyCloudPlatform::getPlatformCode, bo.getPlatformCode());
        lqw.like(StringUtils.isNotBlank(bo.getPlatformName()), SupplyCloudPlatform::getPlatformName, bo.getPlatformName());
        lqw.eq(StringUtils.isNotBlank(bo.getPlatformType()), SupplyCloudPlatform::getPlatformType, bo.getPlatformType());
        lqw.eq(StringUtils.isNotBlank(bo.getProviderCode()), SupplyCloudPlatform::getProviderCode, bo.getProviderCode());
        lqw.eq(StringUtils.isNotBlank(bo.getResourcePoolCode()), SupplyCloudPlatform::getResourcePoolCode, bo.getResourcePoolCode());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SupplyCloudPlatform::getStatus, bo.getStatus());
        lqw.orderByDesc(SupplyCloudPlatform::getCreateTime);
        Page<SupplyCloudPlatform> page = cloudPlatformMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page.convert(this::toVo));
    }

    @Override
    public List<SupplyCloudPlatformVo> queryList(SupplyCloudPlatformBo bo) {
        return queryPageList(bo, new PageQuery(Integer.MAX_VALUE, 1)).getRows();
    }

    @Override
    public SupplyCloudPlatformVo queryById(Long platformId) {
        return toVo(getPlatformOrThrow(platformId));
    }

    @Override
    public boolean insertByBo(SupplyCloudPlatformBo bo) {
        validateWriteRules(bo);
        validateUnique(bo, currentTenantId());
        SupplyCloudPlatform entity = new SupplyCloudPlatform();
        BeanUtil.copyProperties(bo, entity);
        entity.setId(idGenerator.nextId());
        entity.setTenantId(currentTenantId());
        entity.setStatus(StringUtils.defaultIfBlank(bo.getStatus(), EnableStatusEnum.ENABLE.getCode()));
        return cloudPlatformMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateByBo(SupplyCloudPlatformBo bo) {
        SupplyCloudPlatform entity = getPlatformOrThrow(bo.getPlatformId());
        validateWriteRules(bo);
        validateUnique(bo, resolveTargetTenantId(entity.getTenantId()));
        entity.setPlatformCode(bo.getPlatformCode());
        entity.setPlatformName(bo.getPlatformName());
        entity.setPlatformType(bo.getPlatformType());
        entity.setProviderCode(bo.getProviderCode());
        entity.setResourcePoolCode(bo.getResourcePoolCode());
        entity.setRegionCode(bo.getRegionCode());
        entity.setAccessUrl(bo.getAccessUrl());
        entity.setApiVersion(bo.getApiVersion());
        entity.setDescription(bo.getDescription());
        entity.setStatus(StringUtils.defaultIfBlank(bo.getStatus(), entity.getStatus()));
        entity.setTenantId(resolveTargetTenantId(entity.getTenantId()));
        return cloudPlatformMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        for (Long id : ids) {
            SupplyCloudPlatform platform = getPlatformOrThrow(id);
            String tenantScope = resolveTargetTenantId(platform.getTenantId());
            if (Boolean.TRUE.equals(isValid)) {
                boolean hasCollectConfig = collectConfigMapper.exists(Wrappers.<SupplyCollectConfig>lambdaQuery()
                    .eq(StringUtils.isNotBlank(tenantScope), SupplyCollectConfig::getTenantId, tenantScope)
                    .eq(SupplyCollectConfig::getCloudPlatformId, id));
                if (hasCollectConfig) {
                    throw new ServiceException("云平台已关联采集配置，不能删除");
                }
                boolean hasEventSubscription = eventSubscriptionMapper.exists(Wrappers.<SupplyEventSubscription>lambdaQuery()
                    .eq(StringUtils.isNotBlank(tenantScope), SupplyEventSubscription::getTenantId, tenantScope)
                    .eq(SupplyEventSubscription::getCloudPlatformId, id));
                if (hasEventSubscription) {
                    throw new ServiceException("云平台已关联事件订阅，不能删除");
                }
            }
        }
        return cloudPlatformMapper.deleteByIds(ids) > 0;
    }

    @Override
    public List<SupplyOptionVo> queryOptions(String providerCode, String status) {
        String tenantScope = queryTenantScope();
        List<SupplyCloudPlatform> list = cloudPlatformMapper.selectList(Wrappers.<SupplyCloudPlatform>lambdaQuery()
            .eq(StringUtils.isNotBlank(tenantScope), SupplyCloudPlatform::getTenantId, tenantScope)
            .eq(StringUtils.isNotBlank(providerCode), SupplyCloudPlatform::getProviderCode, providerCode)
            .eq(StringUtils.isNotBlank(status), SupplyCloudPlatform::getStatus, status)
            .orderByAsc(SupplyCloudPlatform::getPlatformName));
        return list.stream().map(item -> {
            SupplyOptionVo vo = new SupplyOptionVo();
            vo.setLabel(item.getPlatformName());
            vo.setValue(item.getId());
            vo.setExtra(Map.of("platformCode", item.getPlatformCode(), "status", item.getStatus()));
            return vo;
        }).toList();
    }

    private void validateWriteRules(SupplyCloudPlatformBo bo) {
        validateHttpUrl(bo.getAccessUrl(), "访问地址");
    }

    private void validateUnique(SupplyCloudPlatformBo bo, String tenantScope) {
        boolean duplicatedCode = cloudPlatformMapper.exists(Wrappers.<SupplyCloudPlatform>lambdaQuery()
            .eq(StringUtils.isNotBlank(tenantScope), SupplyCloudPlatform::getTenantId, tenantScope)
            .eq(SupplyCloudPlatform::getPlatformCode, bo.getPlatformCode())
            .ne(Objects.nonNull(bo.getPlatformId()), SupplyCloudPlatform::getId, bo.getPlatformId()));
        if (duplicatedCode) {
            throw new ServiceException("云平台编码已存在");
        }
        boolean duplicatedName = cloudPlatformMapper.exists(Wrappers.<SupplyCloudPlatform>lambdaQuery()
            .eq(StringUtils.isNotBlank(tenantScope), SupplyCloudPlatform::getTenantId, tenantScope)
            .eq(SupplyCloudPlatform::getPlatformName, bo.getPlatformName())
            .ne(Objects.nonNull(bo.getPlatformId()), SupplyCloudPlatform::getId, bo.getPlatformId()));
        if (duplicatedName) {
            throw new ServiceException("云平台名称已存在");
        }
    }

    private SupplyCloudPlatform getPlatformOrThrow(Long platformId) {
        String tenantScope = queryTenantScope();
        SupplyCloudPlatform entity = cloudPlatformMapper.selectOne(Wrappers.<SupplyCloudPlatform>lambdaQuery()
            .eq(StringUtils.isNotBlank(tenantScope), SupplyCloudPlatform::getTenantId, tenantScope)
            .eq(SupplyCloudPlatform::getId, platformId));
        if (entity == null) {
            throw new ServiceException("云平台不存在");
        }
        return entity;
    }

    private SupplyCloudPlatformVo toVo(SupplyCloudPlatform entity) {
        SupplyCloudPlatformVo vo = new SupplyCloudPlatformVo();
        BeanUtil.copyProperties(entity, vo);
        vo.setPlatformId(entity.getId());
        return vo;
    }
}
