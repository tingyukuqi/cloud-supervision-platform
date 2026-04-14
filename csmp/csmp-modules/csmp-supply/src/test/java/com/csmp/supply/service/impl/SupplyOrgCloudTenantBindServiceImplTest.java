package com.csmp.supply.service.impl;
import com.csmp.common.core.exception.ServiceException;
import com.csmp.supply.domain.SupplyCloudPlatform;
import com.csmp.supply.domain.bo.SupplyOrgCloudTenantBindBo;
import com.csmp.supply.mapper.SupplyCloudPlatformMapper;
import com.csmp.supply.mapper.SupplyCloudTenantMapper;
import com.csmp.supply.mapper.SupplyOrgCloudTenantBindMapper;
import com.csmp.supply.support.SupplyIdGenerator;
import com.csmp.system.api.RemoteDeptService;
import com.csmp.system.api.RemoteTenantService;
import com.csmp.system.api.domain.vo.RemoteDeptVo;
import com.csmp.system.api.domain.vo.RemoteTenantVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
class SupplyOrgCloudTenantBindServiceImplTest {

    private static final String TENANT_ID = "00000000000000000001";

    @Mock
    private SupplyOrgCloudTenantBindMapper bindMapper;
    @Mock
    private SupplyCloudPlatformMapper cloudPlatformMapper;
    @Mock
    private SupplyCloudTenantMapper cloudTenantMapper;
    @Mock
    private RemoteDeptService remoteDeptService;
    @Mock
    private RemoteTenantService remoteTenantService;
    @Mock
    private SupplyIdGenerator idGenerator;

    private SupplyOrgCloudTenantBindServiceImpl bindService;

    @BeforeEach
    void setUp() {
        bindService = spy(new SupplyOrgCloudTenantBindServiceImpl(
            bindMapper,
            cloudPlatformMapper,
            cloudTenantMapper,
            remoteDeptService,
            idGenerator
        ));
        lenient().doReturn(TENANT_ID).when(bindService).currentTenantId();
        ReflectionTestUtils.setField(bindService, "remoteTenantService", remoteTenantService);
    }

    @Test
    void insertByBoShouldRejectDuplicateActiveBinding() {
        SupplyCloudPlatform platform = new SupplyCloudPlatform();
        platform.setId(1L);
        RemoteTenantVoExt cloudTenant = new RemoteTenantVoExt();
        cloudTenant.setId(2L);
        cloudTenant.setTenantType("cloud_tenant");
        RemoteDeptVo deptVo = new RemoteDeptVo();
        deptVo.setDeptId(3L);
        deptVo.setDeptName("上海事业部");

        when(cloudPlatformMapper.selectById(1L)).thenReturn(platform);
        when(remoteTenantService.queryList()).thenReturn(List.of(cloudTenant));
        when(remoteDeptService.selectDeptsByList()).thenReturn(List.of(deptVo));
        when(bindMapper.exists(any())).thenReturn(true);

        SupplyOrgCloudTenantBindBo bo = new SupplyOrgCloudTenantBindBo();
        bo.setCloudPlatformId(1L);
        bo.setCloudTenantSnapshotId(2L);
        bo.setOrgId(3L);

        assertThrows(ServiceException.class, () -> bindService.insertByBo(bo));
    }

    @Test
    void insertByBoShouldRejectUnboundStatusWithoutInvalidTime() {
        SupplyOrgCloudTenantBindBo bo = new SupplyOrgCloudTenantBindBo();
        bo.setCloudPlatformId(1L);
        bo.setCloudTenantSnapshotId(2L);
        bo.setOrgId(3L);
        bo.setBindStatus("unbound");

        assertThrows(ServiceException.class, () -> bindService.insertByBo(bo));
    }

    @Test
    void insertByBoShouldRejectTenantThatIsNotCloudTenant() {
        SupplyCloudPlatform platform = new SupplyCloudPlatform();
        platform.setId(1L);
        RemoteTenantVoExt platformTenant = new RemoteTenantVoExt();
        platformTenant.setId(2L);
        platformTenant.setTenantType("platform_operation");
        RemoteDeptVo deptVo = new RemoteDeptVo();
        deptVo.setDeptId(3L);
        deptVo.setDeptName("上海事业部");

        when(cloudPlatformMapper.selectById(1L)).thenReturn(platform);
        when(remoteTenantService.queryList()).thenReturn(List.of(platformTenant));

        SupplyOrgCloudTenantBindBo bo = new SupplyOrgCloudTenantBindBo();
        bo.setCloudPlatformId(1L);
        bo.setCloudTenantSnapshotId(2L);
        bo.setOrgId(3L);

        assertThrows(ServiceException.class, () -> bindService.insertByBo(bo));
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
