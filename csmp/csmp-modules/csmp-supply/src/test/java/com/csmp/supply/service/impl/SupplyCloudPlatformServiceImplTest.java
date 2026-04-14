package com.csmp.supply.service.impl;

import com.csmp.common.core.exception.ServiceException;
import com.csmp.common.core.constant.TenantConstants;
import com.csmp.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csmp.supply.domain.bo.SupplyCloudPlatformBo;
import com.csmp.supply.domain.SupplyCloudPlatform;
import com.csmp.supply.mapper.SupplyCloudPlatformMapper;
import com.csmp.supply.mapper.SupplyCollectConfigMapper;
import com.csmp.supply.mapper.SupplyEventSubscriptionMapper;
import com.csmp.supply.support.SupplyIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
class SupplyCloudPlatformServiceImplTest {

    private static final String TENANT_ID = "00000000000000000001";

    @Mock
    private SupplyCloudPlatformMapper cloudPlatformMapper;
    @Mock
    private SupplyCollectConfigMapper collectConfigMapper;
    @Mock
    private SupplyEventSubscriptionMapper eventSubscriptionMapper;
    @Mock
    private SupplyIdGenerator idGenerator;

    private SupplyCloudPlatformServiceImpl cloudPlatformService;

    @BeforeEach
    void setUp() {
        cloudPlatformService = spy(new SupplyCloudPlatformServiceImpl(
            cloudPlatformMapper,
            collectConfigMapper,
            eventSubscriptionMapper,
            idGenerator
        ));
        lenient().doReturn(TENANT_ID).when(cloudPlatformService).currentTenantId();
    }

    @Test
    void insertByBoShouldRejectInvalidAccessUrl() {
        SupplyCloudPlatformBo bo = new SupplyCloudPlatformBo();
        bo.setPlatformCode("PLAT-001");
        bo.setPlatformName("上海联通云");
        bo.setPlatformType("private_openstack");
        bo.setProviderCode("unicom_cloud");
        bo.setAccessUrl("collector.internal/platform");

        assertThrows(ServiceException.class, () -> cloudPlatformService.insertByBo(bo));
    }

    @Test
    void queryPageListShouldNotFilterTenantWhenPlatformOperationTenant() {
        doReturn(TenantConstants.DEFAULT_TENANT_ID).when(cloudPlatformService).currentTenantId();
        when(cloudPlatformMapper.selectPage(any(), any())).thenReturn(new Page<>());

        cloudPlatformService.queryPageList(new SupplyCloudPlatformBo(), new PageQuery());

        ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SupplyCloudPlatform>> wrapperCaptor =
            ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class);
        verify(cloudPlatformMapper).selectPage(any(), wrapperCaptor.capture());
        assertEquals(0, wrapperCaptor.getValue().getParamNameValuePairs().size());
    }

    @Test
    void updateByBoShouldKeepOriginalTenantIdWhenPlatformOperationTenant() {
        doReturn(TenantConstants.DEFAULT_TENANT_ID).when(cloudPlatformService).currentTenantId();
        SupplyCloudPlatform existing = new SupplyCloudPlatform();
        existing.setId(1L);
        existing.setTenantId("000123");
        existing.setPlatformCode("PLAT-001");
        existing.setPlatformName("上海联通云");
        existing.setPlatformType("private_openstack");
        existing.setProviderCode("unicom_cloud");
        existing.setResourcePoolCode("pool-a");
        existing.setAccessUrl("https://example.com");

        SupplyCloudPlatformBo bo = new SupplyCloudPlatformBo();
        bo.setPlatformId(1L);
        bo.setPlatformCode("PLAT-001");
        bo.setPlatformName("上海联通云");
        bo.setPlatformType("private_openstack");
        bo.setProviderCode("unicom_cloud");
        bo.setResourcePoolCode("pool-a");
        bo.setAccessUrl("https://example.com");

        when(cloudPlatformMapper.selectOne(any())).thenReturn(existing);
        when(cloudPlatformMapper.exists(any())).thenReturn(false, false);
        when(cloudPlatformMapper.updateById(any(SupplyCloudPlatform.class))).thenReturn(1);

        cloudPlatformService.updateByBo(bo);

        ArgumentCaptor<SupplyCloudPlatform> captor = ArgumentCaptor.forClass(SupplyCloudPlatform.class);
        verify(cloudPlatformMapper).updateById(captor.capture());
        assertEquals("000123", captor.getValue().getTenantId());
    }
}
