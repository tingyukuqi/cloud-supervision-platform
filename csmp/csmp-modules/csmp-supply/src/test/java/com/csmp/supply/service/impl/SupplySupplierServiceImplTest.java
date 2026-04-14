package com.csmp.supply.service.impl;

import com.csmp.common.core.exception.ServiceException;
import com.csmp.common.core.constant.TenantConstants;
import com.csmp.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csmp.supply.domain.SupplySupplier;
import com.csmp.supply.domain.SupplySupplierUser;
import com.csmp.supply.domain.bo.SupplySupplierBo;
import com.csmp.supply.domain.bo.SupplySupplierUserBindBo;
import com.csmp.supply.domain.vo.SupplySupplierUserVo;
import com.csmp.system.api.RemoteUserService;
import com.csmp.system.api.domain.bo.RemoteUserOptionQueryBo;
import com.csmp.system.api.domain.vo.RemoteUserVo;
import org.junit.jupiter.api.Assertions;
import com.csmp.supply.mapper.SupplyCloudPlatformMapper;
import com.csmp.supply.mapper.SupplyPhysicalResourceMapper;
import com.csmp.supply.mapper.SupplySupplierMapper;
import com.csmp.supply.mapper.SupplySupplierPlatformAccountMapper;
import com.csmp.supply.mapper.SupplySupplierUserMapper;
import com.csmp.supply.support.SupplyIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
class SupplySupplierServiceImplTest {

    private static final String TENANT_ID = "00000000000000000001";

    @Mock
    private SupplySupplierMapper supplierMapper;
    @Mock
    private SupplySupplierPlatformAccountMapper supplierPlatformAccountMapper;
    @Mock
    private SupplySupplierUserMapper supplierUserMapper;
    @Mock
    private SupplyPhysicalResourceMapper physicalResourceMapper;
    @Mock
    private SupplyCloudPlatformMapper cloudPlatformMapper;
    @Mock
    private SupplyIdGenerator idGenerator;
    @Mock
    private RemoteUserService remoteUserService;

    private SupplySupplierServiceImpl supplierService;

    @BeforeEach
    void setUp() {
        supplierService = spy(new SupplySupplierServiceImpl(
            supplierMapper,
            supplierPlatformAccountMapper,
            supplierUserMapper,
            physicalResourceMapper,
            cloudPlatformMapper,
            idGenerator
        ));
        lenient().doReturn(TENANT_ID).when(supplierService).currentTenantId();
        ReflectionTestUtils.setField(supplierService, "remoteUserService", remoteUserService);
    }

    @Test
    void insertByBoShouldRejectDuplicateSupplierCode() {
        SupplySupplierBo bo = new SupplySupplierBo();
        bo.setSupplierCode("SUP-001");
        bo.setSupplierName("联通云");

        when(supplierMapper.selectOne(any())).thenReturn(new SupplySupplier());

        assertThrows(ServiceException.class, () -> supplierService.insertByBo(bo));
    }

    @Test
    void deleteWithValidByIdsShouldRejectReferencedSupplier() {
        SupplySupplier supplier = new SupplySupplier();
        supplier.setId(1L);
        supplier.setSupplierName("联通云");

        when(supplierMapper.selectOne(any())).thenReturn(supplier);
        when(supplierPlatformAccountMapper.exists(any())).thenReturn(true);

        assertThrows(ServiceException.class, () -> supplierService.deleteWithValidByIds(java.util.List.of(1L), true));
    }

    @Test
    void insertByBoShouldRejectInvalidCreditCode() {
        SupplySupplierBo bo = new SupplySupplierBo();
        bo.setSupplierCode("SUP-002");
        bo.setSupplierName("联通云");
        bo.setSupplierType("cloud_provider");
        bo.setCreditCode("invalid-credit-code");

        assertThrows(ServiceException.class, () -> supplierService.insertByBo(bo));
    }

    @Test
    void insertByBoShouldPersistNullWhenCreditCodeIsBlank() {
        SupplySupplierBo bo = new SupplySupplierBo();
        bo.setSupplierCode("SUP-003");
        bo.setSupplierName("联通云");
        bo.setSupplierType("cloud_provider");
        bo.setCreditCode("  ");

        when(idGenerator.nextId()).thenReturn(100L);
        when(supplierMapper.insert(any(SupplySupplier.class))).thenReturn(1);

        supplierService.insertByBo(bo);

        ArgumentCaptor<SupplySupplier> captor = ArgumentCaptor.forClass(SupplySupplier.class);
        verify(supplierMapper).insert(captor.capture());
        Assertions.assertNull(captor.getValue().getCreditCode());
    }

    @Test
    void insertByBoShouldAllowDuplicateCreditCode() {
        SupplySupplierBo bo = new SupplySupplierBo();
        bo.setSupplierCode("SUP-004");
        bo.setSupplierName("联通云二号");
        bo.setSupplierType("cloud_provider");
        bo.setCreditCode("91310000MA1ABCDE1X");

        when(supplierMapper.selectOne(any())).thenReturn(null, null);
        when(idGenerator.nextId()).thenReturn(101L);
        when(supplierMapper.insert(any(SupplySupplier.class))).thenReturn(1);

        supplierService.insertByBo(bo);

        verify(supplierMapper).insert(any(SupplySupplier.class));
    }

    @Test
    void updateByBoShouldPersistNullWhenCreditCodeIsBlank() {
        SupplySupplier existing = new SupplySupplier();
        existing.setId(1L);
        existing.setSupplierCode("SUP-001");
        existing.setSupplierName("联通云");
        existing.setSupplierType("cloud_provider");
        existing.setCreditCode("91310000MA1ABCDE1X");

        SupplySupplierBo bo = new SupplySupplierBo();
        bo.setSupplierId(1L);
        bo.setSupplierCode("SUP-001");
        bo.setSupplierName("联通云");
        bo.setSupplierType("cloud_provider");
        bo.setCreditCode(" ");

        when(supplierMapper.selectOne(any())).thenReturn(existing, null, null);
        when(supplierMapper.updateById(any(SupplySupplier.class))).thenReturn(1);

        supplierService.updateByBo(bo);

        ArgumentCaptor<SupplySupplier> captor = ArgumentCaptor.forClass(SupplySupplier.class);
        verify(supplierMapper).updateById(captor.capture());
        Assertions.assertNull(captor.getValue().getCreditCode());
    }

    @Test
    void bindUsersShouldUpdateExistingBindingWhenUserAlreadyBound() {
        SupplySupplier supplier = new SupplySupplier();
        supplier.setId(1L);
        supplier.setSupplierName("联通云");

        SupplySupplierUser currentBind = new SupplySupplierUser();
        currentBind.setId(201L);
        currentBind.setTenantId(TENANT_ID);
        currentBind.setSupplierId(99L);
        currentBind.setUserId(3001L);

        SupplySupplierUserBindBo bo = new SupplySupplierUserBindBo();
        bo.setUserIds(List.of(3001L));

        when(supplierMapper.selectOne(any())).thenReturn(supplier);
        when(supplierUserMapper.selectList(any())).thenReturn(List.of(), List.of(currentBind));

        supplierService.bindUsers(1L, bo);

        ArgumentCaptor<SupplySupplierUser> captor = ArgumentCaptor.forClass(SupplySupplierUser.class);
        verify(supplierUserMapper).updateById(captor.capture());
        Assertions.assertEquals(1L, captor.getValue().getSupplierId());
        Assertions.assertEquals(3001L, captor.getValue().getUserId());
    }

    @Test
    void bindUsersShouldPhysicallyDeleteRemovedBindings() {
        SupplySupplier supplier = new SupplySupplier();
        supplier.setId(1L);
        supplier.setSupplierName("联通云");

        SupplySupplierUser removedBind = new SupplySupplierUser();
        removedBind.setId(701L);
        removedBind.setTenantId(TENANT_ID);
        removedBind.setSupplierId(1L);
        removedBind.setUserId(3001L);

        SupplySupplierUserBindBo bo = new SupplySupplierUserBindBo();
        bo.setUserIds(List.of());

        when(supplierMapper.selectOne(any())).thenReturn(supplier);
        when(supplierUserMapper.selectList(any())).thenReturn(List.of(removedBind));

        supplierService.bindUsers(1L, bo);

        verify(supplierUserMapper).deletePhysicalByIds(List.of(701L), TENANT_ID);
    }

    @Test
    void queryUserListShouldReturnBoundUsers() {
        SupplySupplier supplier = new SupplySupplier();
        supplier.setId(1L);
        supplier.setSupplierName("联通云");

        SupplySupplierUser bind = new SupplySupplierUser();
        bind.setId(501L);
        bind.setSupplierId(1L);
        bind.setUserId(3001L);

        RemoteUserVo remoteUser = new RemoteUserVo();
        remoteUser.setUserId(3001L);
        remoteUser.setUserName("zhangsan");
        remoteUser.setNickName("张三");
        remoteUser.setPhonenumber("13800000000");
        remoteUser.setEmail("zhangsan@test.com");
        remoteUser.setStatus("0");

        when(supplierMapper.selectOne(any())).thenReturn(supplier);
        when(supplierUserMapper.selectList(any())).thenReturn(List.of(bind));
        when(remoteUserService.selectListByIds(List.of(3001L))).thenReturn(List.of(remoteUser));

        List<SupplySupplierUserVo> list = supplierService.queryUserList(1L);

        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals(501L, list.get(0).getBindingId());
        Assertions.assertEquals("zhangsan", list.get(0).getUserName());
        Assertions.assertEquals("张三", list.get(0).getNickName());
    }

    @Test
    void queryBindableUsersShouldExcludeUsersBoundByOtherSuppliers() {
        SupplySupplier supplier = new SupplySupplier();
        supplier.setId(1L);
        supplier.setSupplierName("联通云");

        SupplySupplierUser currentSupplierBind = new SupplySupplierUser();
        currentSupplierBind.setSupplierId(1L);
        currentSupplierBind.setUserId(3001L);

        SupplySupplierUser otherSupplierBind = new SupplySupplierUser();
        otherSupplierBind.setSupplierId(99L);
        otherSupplierBind.setUserId(3002L);

        RemoteUserVo remoteUser = new RemoteUserVo();
        remoteUser.setUserId(3001L);
        remoteUser.setUserName("zhangsan");

        when(supplierMapper.selectOne(any())).thenReturn(supplier);
        when(supplierUserMapper.selectList(any())).thenReturn(List.of(currentSupplierBind, otherSupplierBind));
        when(remoteUserService.selectOptionList(any())).thenReturn(List.of(remoteUser));

        List<RemoteUserVo> list = supplierService.queryBindableUsers(1L, "zhang", 101L);

        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals(3001L, list.get(0).getUserId());

        ArgumentCaptor<RemoteUserOptionQueryBo> captor = ArgumentCaptor.forClass(RemoteUserOptionQueryBo.class);
        verify(remoteUserService).selectOptionList(captor.capture());
        Assertions.assertEquals("zhang", captor.getValue().getKeyword());
        Assertions.assertEquals(101L, captor.getValue().getDeptId());
        Assertions.assertEquals(List.of(3002L), captor.getValue().getExcludeUserIds());
    }

    @Test
    void queryPageListShouldNotFilterTenantWhenPlatformOperationTenant() {
        doReturn(TenantConstants.DEFAULT_TENANT_ID).when(supplierService).currentTenantId();
        when(supplierMapper.selectPage(any(), any())).thenReturn(new Page<>());

        supplierService.queryPageList(new SupplySupplierBo(), new PageQuery());

        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SupplySupplier>> wrapperCaptor =
            ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class);
        verify(supplierMapper).selectPage(any(), wrapperCaptor.capture());
        Assertions.assertEquals(0, wrapperCaptor.getValue().getParamNameValuePairs().size());
    }

    @Test
    void updateByBoShouldKeepOriginalTenantIdWhenPlatformOperationTenant() {
        doReturn(TenantConstants.DEFAULT_TENANT_ID).when(supplierService).currentTenantId();
        SupplySupplier existing = new SupplySupplier();
        existing.setId(1L);
        existing.setTenantId("000123");
        existing.setSupplierCode("SUP-001");
        existing.setSupplierName("联通云");
        existing.setSupplierType("cloud_provider");

        SupplySupplierBo bo = new SupplySupplierBo();
        bo.setSupplierId(1L);
        bo.setSupplierCode("SUP-001");
        bo.setSupplierName("联通云");
        bo.setSupplierType("cloud_provider");

        when(supplierMapper.selectOne(any())).thenReturn(existing, null, null);
        when(supplierMapper.updateById(any(SupplySupplier.class))).thenReturn(1);

        supplierService.updateByBo(bo);

        ArgumentCaptor<SupplySupplier> captor = ArgumentCaptor.forClass(SupplySupplier.class);
        verify(supplierMapper).updateById(captor.capture());
        Assertions.assertEquals("000123", captor.getValue().getTenantId());
    }
}
