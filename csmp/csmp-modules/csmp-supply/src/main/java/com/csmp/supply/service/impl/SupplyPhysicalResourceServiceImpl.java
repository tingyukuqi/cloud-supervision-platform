package com.csmp.supply.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csmp.common.core.exception.ServiceException;
import com.csmp.common.core.utils.StringUtils;
import com.csmp.common.excel.core.ExcelResult;
import com.csmp.common.excel.utils.ExcelUtil;
import com.csmp.common.mybatis.core.page.PageQuery;
import com.csmp.common.mybatis.core.page.TableDataInfo;
import com.csmp.supply.domain.SupplyPhysicalResource;
import com.csmp.supply.domain.SupplySupplier;
import com.csmp.supply.domain.SupplySupplierUser;
import com.csmp.supply.domain.bo.SupplyPhysicalResourceBo;
import com.csmp.supply.domain.enums.EnableStatusEnum;
import com.csmp.supply.domain.vo.SupplyImportResultVo;
import com.csmp.supply.domain.vo.SupplyPhysicalResourceImportVo;
import com.csmp.supply.domain.vo.SupplyPhysicalResourceVo;
import com.csmp.supply.mapper.SupplyPhysicalResourceMapper;
import com.csmp.supply.mapper.SupplySupplierMapper;
import com.csmp.supply.mapper.SupplySupplierUserMapper;
import com.csmp.supply.service.ISupplyPhysicalResourceService;
import com.csmp.supply.support.SupplyIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 物理资源服务实现
 *
 * @author csmp
 */
@Service
@RequiredArgsConstructor
public class SupplyPhysicalResourceServiceImpl extends AbstractSupplyService implements ISupplyPhysicalResourceService {

    private final SupplyPhysicalResourceMapper physicalResourceMapper;
    private final SupplySupplierMapper supplierMapper;
    private final SupplySupplierUserMapper supplierUserMapper;
    private final SupplyIdGenerator idGenerator;

    @Override
    public TableDataInfo<SupplyPhysicalResourceVo> queryPageList(SupplyPhysicalResourceBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SupplyPhysicalResource> lqw = buildQueryWrapper(bo);
        Page<SupplyPhysicalResource> page = physicalResourceMapper.selectPage(pageQuery.build(), lqw);
        Map<Long, String> supplierNames = supplierNameMap(page.getRecords().stream().map(SupplyPhysicalResource::getSupplierId).filter(Objects::nonNull).toList());
        List<SupplyPhysicalResourceVo> list = page.getRecords().stream().map(item -> toVo(item, supplierNames)).toList();
        return new TableDataInfo<>(list, page.getTotal());
    }

    @Override
    public List<SupplyPhysicalResourceVo> queryList(SupplyPhysicalResourceBo bo) {
        List<SupplyPhysicalResource> list = physicalResourceMapper.selectList(buildQueryWrapper(bo));
        Map<Long, String> supplierNames = supplierNameMap(list.stream().map(SupplyPhysicalResource::getSupplierId).filter(Objects::nonNull).toList());
        return list.stream().map(item -> toVo(item, supplierNames)).toList();
    }

    @Override
    public SupplyPhysicalResourceVo queryById(Long resourceId) {
        SupplyPhysicalResource entity = getResourceOrThrow(resourceId);
        return toVo(entity, supplierNameMap(List.of(entity.getSupplierId())));
    }

    @Override
    public boolean insertByBo(SupplyPhysicalResourceBo bo) {
        Long supplierId = resolveSupplierId(bo.getSupplierId());
        SupplySupplier supplier = ensureSupplierEnabled(supplierId);
        validateResourceUnique(bo, resolveTargetTenantId(supplier.getTenantId()));
        SupplyPhysicalResource entity = new SupplyPhysicalResource();
        BeanUtil.copyProperties(bo, entity);
        entity.setId(idGenerator.nextId());
        entity.setTenantId(resolveTargetTenantId(supplier.getTenantId()));
        entity.setSupplierId(supplierId);
        return physicalResourceMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateByBo(SupplyPhysicalResourceBo bo) {
        SupplyPhysicalResource entity = getResourceOrThrow(bo.getResourceId());
        Long supplierId = resolveSupplierId(Objects.requireNonNullElse(bo.getSupplierId(), entity.getSupplierId()));
        SupplySupplier supplier = ensureSupplierEnabled(supplierId);
        validateResourceUnique(bo, resolveTargetTenantId(supplier.getTenantId()));
        BeanUtil.copyProperties(bo, entity);
        entity.setId(bo.getResourceId());
        entity.setTenantId(resolveTargetTenantId(supplier.getTenantId()));
        entity.setSupplierId(supplierId);
        return physicalResourceMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        return physicalResourceMapper.deleteByIds(ids) > 0;
    }

    @Override
    public SupplyImportResultVo importData(InputStream inputStream, boolean updateSupport) throws Exception {
        ExcelResult<SupplyPhysicalResourceImportVo> excelResult = ExcelUtil.importExcel(inputStream, SupplyPhysicalResourceImportVo.class, true);
        List<String> failures = new ArrayList<>();
        int success = 0;
        int update = 0;
        int row = 1;
        for (SupplyPhysicalResourceImportVo item : excelResult.getList()) {
            row++;
            try {
                SupplyPhysicalResourceBo bo = BeanUtil.toBean(item, SupplyPhysicalResourceBo.class);
                bo.setPurchaseDate(parseDate(item.getPurchaseDate()));
                bo.setExpireDate(parseDate(item.getExpireDate()));
                SupplyPhysicalResource existing = physicalResourceMapper.selectOne(Wrappers.<SupplyPhysicalResource>lambdaQuery()
                    .eq(StringUtils.isNotBlank(queryTenantScope()), SupplyPhysicalResource::getTenantId, queryTenantScope())
                    .eq(SupplyPhysicalResource::getResourceCode, item.getResourceCode()));
                if (existing == null) {
                    insertByBo(bo);
                    success++;
                } else if (updateSupport) {
                    bo.setResourceId(existing.getId());
                    updateByBo(bo);
                    success++;
                    update++;
                } else {
                    failures.add("第" + row + "行：资源编号已存在");
                }
            } catch (Exception e) {
                failures.add("第" + row + "行：" + e.getMessage());
            }
        }
        SupplyImportResultVo resultVo = new SupplyImportResultVo();
        resultVo.setTotalCount(excelResult.getList().size());
        resultVo.setSuccessCount(success);
        resultVo.setFailureCount(failures.size());
        resultVo.setUpdateCount(update);
        resultVo.setFailureMessages(failures);
        return resultVo;
    }

    private LambdaQueryWrapper<SupplyPhysicalResource> buildQueryWrapper(SupplyPhysicalResourceBo bo) {
        String tenantScope = queryTenantScope();
        LambdaQueryWrapper<SupplyPhysicalResource> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(tenantScope), SupplyPhysicalResource::getTenantId, tenantScope);
        lqw.eq(Objects.nonNull(bo.getSupplierId()), SupplyPhysicalResource::getSupplierId, bo.getSupplierId());
        lqw.eq(StringUtils.isNotBlank(bo.getDeviceType()), SupplyPhysicalResource::getDeviceType, bo.getDeviceType());
        lqw.eq(StringUtils.isNotBlank(bo.getResourceStatus()), SupplyPhysicalResource::getResourceStatus, bo.getResourceStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getIdcLocation()), SupplyPhysicalResource::getIdcLocation, bo.getIdcLocation());
        lqw.like(StringUtils.isNotBlank(bo.getKeyword()), SupplyPhysicalResource::getDeviceName, bo.getKeyword())
            .or(StringUtils.isNotBlank(bo.getKeyword()))
            .like(StringUtils.isNotBlank(bo.getKeyword()), SupplyPhysicalResource::getSerialNumber, bo.getKeyword());
        lqw.ge(StringUtils.isNotBlank(bo.getBeginCreateTime()), SupplyPhysicalResource::getCreateTime, bo.getBeginCreateTime());
        lqw.le(StringUtils.isNotBlank(bo.getEndCreateTime()), SupplyPhysicalResource::getCreateTime, bo.getEndCreateTime());
        lqw.orderByDesc(SupplyPhysicalResource::getCreateTime);
        return lqw;
    }

    private Long resolveSupplierId(Long supplierId) {
        if (supplierId != null) {
            return supplierId;
        }
        String tenantScope = queryTenantScope();
        SupplySupplierUser bind = supplierUserMapper.selectOne(Wrappers.<SupplySupplierUser>lambdaQuery()
            .eq(StringUtils.isNotBlank(tenantScope), SupplySupplierUser::getTenantId, tenantScope)
            .eq(SupplySupplierUser::getUserId, currentUserId()));
        if (bind == null) {
            throw new ServiceException("未找到当前用户的供应商绑定关系，请先选择供应商");
        }
        return bind.getSupplierId();
    }

    private SupplySupplier ensureSupplierEnabled(Long supplierId) {
        SupplySupplier supplier = supplierMapper.selectById(supplierId);
        if (supplier == null) {
            throw new ServiceException("供应商不存在");
        }
        if (!EnableStatusEnum.isEnabled(supplier.getStatus())) {
            throw new ServiceException("供应商已停用，禁止新增或修改物理资源");
        }
        return supplier;
    }

    private void validateResourceUnique(SupplyPhysicalResourceBo bo, String tenantScope) {
        boolean duplicatedCode = physicalResourceMapper.exists(Wrappers.<SupplyPhysicalResource>lambdaQuery()
            .eq(StringUtils.isNotBlank(tenantScope), SupplyPhysicalResource::getTenantId, tenantScope)
            .eq(SupplyPhysicalResource::getResourceCode, bo.getResourceCode())
            .ne(Objects.nonNull(bo.getResourceId()), SupplyPhysicalResource::getId, bo.getResourceId()));
        if (duplicatedCode) {
            throw new ServiceException("资源编号已存在");
        }
        boolean duplicatedSn = physicalResourceMapper.exists(Wrappers.<SupplyPhysicalResource>lambdaQuery()
            .eq(StringUtils.isNotBlank(tenantScope), SupplyPhysicalResource::getTenantId, tenantScope)
            .eq(SupplyPhysicalResource::getSerialNumber, bo.getSerialNumber())
            .ne(Objects.nonNull(bo.getResourceId()), SupplyPhysicalResource::getId, bo.getResourceId()));
        if (duplicatedSn) {
            throw new ServiceException("序列号已存在");
        }
    }

    private SupplyPhysicalResource getResourceOrThrow(Long resourceId) {
        String tenantScope = queryTenantScope();
        SupplyPhysicalResource entity = physicalResourceMapper.selectOne(Wrappers.<SupplyPhysicalResource>lambdaQuery()
            .eq(StringUtils.isNotBlank(tenantScope), SupplyPhysicalResource::getTenantId, tenantScope)
            .eq(SupplyPhysicalResource::getId, resourceId));
        if (entity == null) {
            throw new ServiceException("物理资源不存在");
        }
        return entity;
    }

    private Map<Long, String> supplierNameMap(List<Long> supplierIds) {
        if (supplierIds.isEmpty()) {
            return Map.of();
        }
        return supplierMapper.selectBatchIds(supplierIds).stream()
            .collect(Collectors.toMap(SupplySupplier::getId, SupplySupplier::getSupplierName, (a, b) -> a));
    }

    private SupplyPhysicalResourceVo toVo(SupplyPhysicalResource entity, Map<Long, String> supplierNames) {
        SupplyPhysicalResourceVo vo = new SupplyPhysicalResourceVo();
        BeanUtil.copyProperties(entity, vo);
        vo.setResourceId(entity.getId());
        vo.setSupplierName(supplierNames.get(entity.getSupplierId()));
        vo.setSpecPayload(parseJsonObject(entity.getSpecPayload()));
        return vo;
    }

    private Date parseDate(String text) throws Exception {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        return new SimpleDateFormat("yyyy-MM-dd").parse(text);
    }
}
