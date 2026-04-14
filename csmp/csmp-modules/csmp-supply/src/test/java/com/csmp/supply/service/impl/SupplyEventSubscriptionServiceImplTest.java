package com.csmp.supply.service.impl;

import com.csmp.common.core.exception.ServiceException;
import com.csmp.common.core.utils.SpringUtils;
import com.csmp.supply.domain.SupplyCloudPlatform;
import com.csmp.supply.domain.SupplyEventLog;
import com.csmp.supply.domain.SupplyEventSubscription;
import com.csmp.supply.domain.bo.SupplyEventIngestBo;
import com.csmp.supply.domain.bo.SupplyEventSubscriptionBo;
import com.csmp.supply.mapper.SupplyCloudPlatformMapper;
import com.csmp.supply.mapper.SupplyEventLogMapper;
import com.csmp.supply.mapper.SupplyEventSubscriptionMapper;
import com.csmp.supply.support.SupplyIdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
class SupplyEventSubscriptionServiceImplTest {

    private static final String TENANT_ID = "00000000000000000001";
    private static AnnotationConfigApplicationContext applicationContext;

    @Mock
    private SupplyEventSubscriptionMapper eventSubscriptionMapper;
    @Mock
    private SupplyEventLogMapper eventLogMapper;
    @Mock
    private SupplyCloudPlatformMapper cloudPlatformMapper;
    @Mock
    private SupplyIdGenerator idGenerator;

    private SupplyEventSubscriptionServiceImpl eventSubscriptionService;

    @BeforeAll
    static void initSpringContext() {
        applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.registerBean(SpringUtils.class);
        applicationContext.registerBean(ObjectMapper.class, () -> new ObjectMapper());
        applicationContext.refresh();
    }

    @AfterAll
    static void closeSpringContext() {
        if (applicationContext != null) {
            applicationContext.close();
        }
    }

    @BeforeEach
    void setUp() {
        eventSubscriptionService = spy(new SupplyEventSubscriptionServiceImpl(
            eventSubscriptionMapper,
            eventLogMapper,
            cloudPlatformMapper,
            idGenerator
        ));
        lenient().doReturn(TENANT_ID).when(eventSubscriptionService).currentTenantId();
    }

    @Test
    void ingestEventShouldRejectDuplicateSourceEventId() {
        SupplyEventSubscription subscription = new SupplyEventSubscription();
        subscription.setId(1L);
        subscription.setStatus("0");
        subscription.setEventScope("alarm");

        when(eventSubscriptionMapper.selectOne(any())).thenReturn(subscription);
        when(eventLogMapper.exists(any())).thenReturn(true);

        SupplyEventIngestBo bo = new SupplyEventIngestBo();
        bo.setSourceEventId("evt-001");

        assertThrows(ServiceException.class, () -> eventSubscriptionService.ingestEvent(1L, bo));
    }

    @Test
    void ingestEventShouldPersistNormalizedEvent() {
        SupplyEventSubscription subscription = new SupplyEventSubscription();
        subscription.setId(1L);
        subscription.setCloudPlatformId(2L);
        subscription.setProviderCode("unicom_cloud");
        subscription.setStatus("0");
        subscription.setEventScope("alarm");

        when(eventSubscriptionMapper.selectOne(any())).thenReturn(subscription);
        when(eventLogMapper.exists(any())).thenReturn(false);
        when(idGenerator.nextId()).thenReturn(200L);
        when(idGenerator.nextTraceId()).thenReturn("trace-event-1");

        SupplyEventIngestBo bo = new SupplyEventIngestBo();
        bo.setSourceEventId("evt-001");
        bo.setEventTime("2026-04-12 10:00:00");
        bo.setEventKey("alarm.trigger");
        bo.setPayload(Map.of("level", "critical"));

        eventSubscriptionService.ingestEvent(1L, bo);

        ArgumentCaptor<SupplyEventLog> logCaptor = ArgumentCaptor.forClass(SupplyEventLog.class);
        verify(eventLogMapper).insert(logCaptor.capture());
        assertEquals("received", logCaptor.getValue().getProcessStatus());
        assertEquals("alarm.trigger", logCaptor.getValue().getEventKey());
    }

    @Test
    void insertByBoShouldRejectRocketMqWithoutTopicAndConsumerGroup() {
        SupplyEventSubscriptionBo bo = new SupplyEventSubscriptionBo();
        bo.setCloudPlatformId(2L);
        bo.setProviderCode("unicom_cloud");
        bo.setEventScope("alarm");
        bo.setIngestMode("rocketmq");
        bo.setDataFormat("json");
        bo.setSchemaVersion("1.0");

        assertThrows(ServiceException.class, () -> eventSubscriptionService.insertByBo(bo));
    }

    @Test
    void insertByBoShouldRejectWebhookWithoutEndpointPath() {
        SupplyEventSubscriptionBo bo = new SupplyEventSubscriptionBo();
        bo.setCloudPlatformId(2L);
        bo.setProviderCode("unicom_cloud");
        bo.setEventScope("alarm");
        bo.setIngestMode("webhook");
        bo.setDataFormat("json");
        bo.setSchemaVersion("1.0");

        assertThrows(ServiceException.class, () -> eventSubscriptionService.insertByBo(bo));
    }
}
