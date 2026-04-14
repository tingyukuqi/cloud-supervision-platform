package com.csmp.supply.domain.bo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("dev")
class SupplyJsonObjectStringBoDeserializeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void collectConfigBoShouldAcceptObjectJsonFields() throws Exception {
        SupplyCollectConfigBo bo = objectMapper.readValue("""
            {
              "cloudPlatformId": 1,
              "providerCode": "openstack",
              "collectUrl": "https://collector.example.com",
              "collectScope": "tenant",
              "collectMode": "scheduled_pull",
              "syncStrategy": "incremental",
              "connectorCode": "openstack_v3",
              "authType": "ak_sk",
              "authPayload": {
                "accessKey": "ak",
                "secretKey": "sk"
              },
              "scopeFilter": {
                "region": "cn-north-1"
              },
              "collectOptions": {
                "pageSize": 200
              },
              "executeCycle": "6h"
            }
            """, SupplyCollectConfigBo.class);

        assertEquals(objectMapper.readTree("""
            {"accessKey":"ak","secretKey":"sk"}
            """), toJsonNode(bo.getAuthPayload()));
        assertEquals(objectMapper.readTree("""
            {"region":"cn-north-1"}
            """), toJsonNode(bo.getScopeFilter()));
        assertEquals(objectMapper.readTree("""
            {"pageSize":200}
            """), toJsonNode(bo.getCollectOptions()));
    }

    @Test
    void eventSubscriptionBoShouldAcceptObjectAuthPayload() throws Exception {
        SupplyEventSubscriptionBo bo = objectMapper.readValue("""
            {
              "cloudPlatformId": 1,
              "providerCode": "openstack",
              "eventScope": "resource",
              "ingestMode": "webhook",
              "authType": "signature",
              "authPayload": {
                "secret": "token"
              },
              "dataFormat": "json",
              "schemaVersion": "v1"
            }
            """, SupplyEventSubscriptionBo.class);

        assertEquals(objectMapper.readTree("""
            {"secret":"token"}
            """), toJsonNode(bo.getAuthPayload()));
    }

    @Test
    void jsonObjectStringFieldsShouldConvertBlankStringToNull() throws Exception {
        SupplyCollectConfigBo collectConfigBo = objectMapper.readValue("""
            {
              "cloudPlatformId": 1,
              "providerCode": "openstack",
              "collectUrl": "https://collector.example.com",
              "collectScope": "tenant",
              "collectMode": "scheduled_pull",
              "syncStrategy": "incremental",
              "connectorCode": "openstack_v3",
              "authType": "ak_sk",
              "authPayload": "",
              "scopeFilter": "   ",
              "collectOptions": "",
              "executeCycle": "6h"
            }
            """, SupplyCollectConfigBo.class);
        SupplyPhysicalResourceBo physicalResourceBo = objectMapper.readValue("""
            {
              "resourceCode": "PHY-003",
              "deviceName": "测试设备",
              "deviceType": "server",
              "serialNumber": "SN-003",
              "resourceStatus": "running",
              "specPayload": ""
            }
            """, SupplyPhysicalResourceBo.class);

        assertNull(collectConfigBo.getAuthPayload());
        assertNull(collectConfigBo.getScopeFilter());
        assertNull(collectConfigBo.getCollectOptions());
        assertNull(physicalResourceBo.getSpecPayload());
    }

    private JsonNode toJsonNode(String value) throws Exception {
        return objectMapper.readTree(value);
    }
}
