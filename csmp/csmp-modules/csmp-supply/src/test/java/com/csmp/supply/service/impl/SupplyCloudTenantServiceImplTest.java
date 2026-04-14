package com.csmp.supply.service.impl;
import com.csmp.supply.domain.SupplyOrgCloudTenantBind;
import com.csmp.supply.domain.vo.SupplyOptionVo;
import com.csmp.supply.mapper.SupplyCloudPlatformMapper;
import com.csmp.supply.mapper.SupplyCloudTenantMapper;
import com.csmp.supply.mapper.SupplyCollectConfigMapper;
import com.csmp.supply.mapper.SupplyOrgCloudTenantBindMapper;
import com.csmp.supply.service.ISupplyCollectConfigService;
import com.csmp.system.api.RemoteDeptService;
import com.csmp.system.api.RemoteTenantService;
import com.csmp.system.api.domain.vo.RemoteTenantVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
class SupplyCloudTenantServiceImplTest {

    private static final String TENANT_ID = "000000";

    @Mock
    private SupplyCloudTenantMapper cloudTenantMapper;
    @Mock
    private SupplyCloudPlatformMapper cloudPlatformMapper;
    @Mock
    private SupplyOrgCloudTenantBindMapper bindMapper;
    @Mock
    private SupplyCollectConfigMapper collectConfigMapper;
    @Mock
    private ISupplyCollectConfigService collectConfigService;
    @Mock
    private RemoteDeptService remoteDeptService;
    @Mock
    private RemoteTenantService remoteTenantService;

    private SupplyCloudTenantServiceImpl cloudTenantService;

    @BeforeEach
    void setUp() {
        cloudTenantService = spy(new SupplyCloudTenantServiceImpl(
            cloudTenantMapper,
            cloudPlatformMapper,
            bindMapper,
            collectConfigMapper,
            collectConfigService,
            remoteDeptService
        ));
        lenient().doReturn(TENANT_ID).when(cloudTenantService).currentTenantId();
        ReflectionTestUtils.setField(cloudTenantService, "remoteTenantService", remoteTenantService);
    }

    @Test
    void queryOptionsShouldUseRemoteCloudTenants() {
        RemoteTenantVoExt cloudTenant = new RemoteTenantVoExt();
        cloudTenant.setId(2L);
        cloudTenant.setTenantId("000002");
        cloudTenant.setCompanyName("云租户A");
        cloudTenant.setTenantType("cloud_tenant");

        RemoteTenantVoExt platformTenant = new RemoteTenantVoExt();
        platformTenant.setId(3L);
        platformTenant.setTenantId("000003");
        platformTenant.setCompanyName("平台运营");
        platformTenant.setTenantType("platform_operation");

        SupplyOrgCloudTenantBind bind = new SupplyOrgCloudTenantBind();
        bind.setCloudTenantSnapshotId(2L);
        bind.setBindStatus("bound");

        when(remoteTenantService.queryList()).thenReturn(List.of(cloudTenant, platformTenant));
        when(bindMapper.selectList(any())).thenReturn(List.of(bind));

        List<SupplyOptionVo> options = cloudTenantService.queryOptions(100L, "云租户", "bound");

        assertEquals(1, options.size());
        assertEquals("云租户A", options.get(0).getLabel());
        assertEquals(2L, options.get(0).getValue());
        @SuppressWarnings("unchecked")
        Map<String, Object> extra = (Map<String, Object>) options.get(0).getExtra();
        assertEquals("bound", extra.get("bindStatus"));
        assertEquals("000002", extra.get("tenantId"));
    }

    private static class RemoteTenantVoExt extends RemoteTenantVo {

        private String tenantType;

        public String getTenantType() {
            return tenantType;
        }

        public void setTenantType(String tenantType) {
            this.tenantType = tenantType;
        }
    }
}
