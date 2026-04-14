package com.csmp.supply.service.impl;

import com.csmp.supply.domain.SupplyPhysicalResource;
import com.csmp.supply.domain.SupplySupplier;
import com.csmp.supply.domain.bo.SupplyPhysicalResourceBo;
import com.csmp.supply.mapper.SupplyPhysicalResourceMapper;
import com.csmp.supply.mapper.SupplySupplierMapper;
import com.csmp.supply.mapper.SupplySupplierUserMapper;
import com.csmp.supply.support.SupplyIdGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
class SupplyPhysicalResourceServiceImplTest {

    private static final String TENANT_ID = "00000000000000000001";

    @Mock
    private SupplyPhysicalResourceMapper physicalResourceMapper;
    @Mock
    private SupplySupplierMapper supplierMapper;
    @Mock
    private SupplySupplierUserMapper supplierUserMapper;
    @Mock
    private SupplyIdGenerator idGenerator;

    private SupplyPhysicalResourceServiceImpl physicalResourceService;

    @BeforeEach
    void setUp() {
        physicalResourceService = spy(new SupplyPhysicalResourceServiceImpl(
            physicalResourceMapper,
            supplierMapper,
            supplierUserMapper,
            idGenerator
        ));
        doReturn(TENANT_ID).when(physicalResourceService).currentTenantId();
    }

    @Test
    void insertByBoShouldAcceptObjectSpecPayloadAndPersistJsonString() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        SupplyPhysicalResourceBo bo = objectMapper.readValue("""
            {
              "resourceCode": "PHY-001",
              "supplierId": 10001,
              "deviceName": "核心防火墙",
              "deviceType": "firewall",
              "serialNumber": "SN-001",
              "resourceStatus": "running",
              "specPayload": {
                "cpu": 16,
                "memory": "64GB"
              }
            }
            """, SupplyPhysicalResourceBo.class);

        SupplySupplier supplier = new SupplySupplier();
        supplier.setId(10001L);
        supplier.setStatus("0");

        when(physicalResourceMapper.exists(any())).thenReturn(false);
        when(supplierMapper.selectById(anyLong())).thenReturn(supplier);
        when(idGenerator.nextId()).thenReturn(1L);
        when(physicalResourceMapper.insert(any(SupplyPhysicalResource.class))).thenReturn(1);

        physicalResourceService.insertByBo(bo);

        ArgumentCaptor<SupplyPhysicalResource> captor = ArgumentCaptor.forClass(SupplyPhysicalResource.class);
        verify(physicalResourceMapper).insert(captor.capture());
        JsonNode specPayload = objectMapper.readTree(captor.getValue().getSpecPayload());
        assertEquals(objectMapper.readTree("""
            {
              "cpu": 16,
              "memory": "64GB"
            }
            """), specPayload);
    }

    @Test
    void insertByBoShouldKeepJsonStringSpecPayloadCompatible() throws Exception {
        SupplyPhysicalResourceBo bo = new SupplyPhysicalResourceBo();
        bo.setResourceCode("PHY-002");
        bo.setSupplierId(10001L);
        bo.setDeviceName("核心交换机");
        bo.setDeviceType("switch");
        bo.setSerialNumber("SN-002");
        bo.setResourceStatus("running");
        bo.setSpecPayload("{\"cpu\":8,\"memory\":\"32GB\"}");

        SupplySupplier supplier = new SupplySupplier();
        supplier.setId(10001L);
        supplier.setStatus("0");

        when(physicalResourceMapper.exists(any())).thenReturn(false);
        when(supplierMapper.selectById(anyLong())).thenReturn(supplier);
        when(idGenerator.nextId()).thenReturn(2L);
        when(physicalResourceMapper.insert(any(SupplyPhysicalResource.class))).thenReturn(1);

        physicalResourceService.insertByBo(bo);

        ArgumentCaptor<SupplyPhysicalResource> captor = ArgumentCaptor.forClass(SupplyPhysicalResource.class);
        verify(physicalResourceMapper).insert(captor.capture());
        assertEquals("{\"cpu\":8,\"memory\":\"32GB\"}", captor.getValue().getSpecPayload());
    }
}
