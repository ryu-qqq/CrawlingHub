package com.ryuqq.crawlinghub.adapter.event;

import com.ryuqq.crawlinghub.application.schedule.port.CrawlScheduleCommandPort;
import com.ryuqq.crawlinghub.application.schedule.port.EventBridgePort;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleDeletedEvent;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleDisabledEvent;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleEnabledEvent;
import com.ryuqq.crawlinghub.domain.schedule.event.ScheduleUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for ScheduleEventBridgeHandler.
 *
 * @author crawlinghub (noreply@crawlinghub.com)
 */
@ExtendWith(MockitoExtension.class)
class ScheduleEventBridgeHandlerTest {

    @Mock
    private EventBridgePort eventBridgePort;

    @Mock
    private CrawlScheduleCommandPort scheduleCommandPort;

    @Mock
    private CrawlSchedule schedule;

    @InjectMocks
    private ScheduleEventBridgeHandler handler;

    @BeforeEach
    void setUp() {
        // Mock setup if needed
    }

    @Test
    @DisplayName("handleScheduleEnabled 성공 - EventBridge 규칙 생성 및 활성화")
    void handleScheduleEnabledSuccess() {
        // given
        ScheduleEnabledEvent event = new ScheduleEnabledEvent(
                1L, "test-rule", "0 0 * * *", "Test Schedule", "{}"
        );

        // when
        handler.handleScheduleEnabled(event);

        // then
        verify(eventBridgePort).createRule("test-rule", "0 0 * * *", "Crawling schedule: Test Schedule");
        verify(eventBridgePort).addTarget("test-rule", "{}");
        verify(eventBridgePort).enableRule("test-rule");
        verify(scheduleCommandPort, never()).findById(any());
    }

    @Test
    @DisplayName("handleScheduleEnabled 실패 - Compensation Transaction 실행")
    void handleScheduleEnabledFailureWithCompensation() {
        // given
        ScheduleEnabledEvent event = new ScheduleEnabledEvent(
                1L, "test-rule", "0 0 * * *", "Test Schedule", "{}"
        );

        doThrow(new RuntimeException("EventBridge error"))
                .when(eventBridgePort).createRule(any(), any(), any());

        when(scheduleCommandPort.findById(ScheduleId.of(1L)))
                .thenReturn(Optional.of(schedule));

        // when
        handler.handleScheduleEnabled(event);

        // then
        verify(scheduleCommandPort).findById(ScheduleId.of(1L));
        verify(schedule).disable();
        verify(scheduleCommandPort).save(schedule);
    }

    @Test
    @DisplayName("handleScheduleDisabled 성공 - EventBridge 규칙 비활성화")
    void handleScheduleDisabledSuccess() {
        // given
        ScheduleDisabledEvent event = new ScheduleDisabledEvent(1L, "test-rule");

        when(eventBridgePort.ruleExists("test-rule")).thenReturn(true);

        // when
        handler.handleScheduleDisabled(event);

        // then
        verify(eventBridgePort).ruleExists("test-rule");
        verify(eventBridgePort).disableRule("test-rule");
        verify(scheduleCommandPort, never()).findById(any());
    }

    @Test
    @DisplayName("handleScheduleDisabled 스킵 - 규칙이 존재하지 않음")
    void handleScheduleDisabledSkipWhenRuleNotExists() {
        // given
        ScheduleDisabledEvent event = new ScheduleDisabledEvent(1L, "test-rule");

        when(eventBridgePort.ruleExists("test-rule")).thenReturn(false);

        // when
        handler.handleScheduleDisabled(event);

        // then
        verify(eventBridgePort).ruleExists("test-rule");
        verify(eventBridgePort, never()).disableRule(any());
    }

    @Test
    @DisplayName("handleScheduleDisabled 실패 - Compensation Transaction 실행")
    void handleScheduleDisabledFailureWithCompensation() {
        // given
        ScheduleDisabledEvent event = new ScheduleDisabledEvent(1L, "test-rule");

        when(eventBridgePort.ruleExists("test-rule")).thenReturn(true);
        doThrow(new RuntimeException("EventBridge error"))
                .when(eventBridgePort).disableRule(any());

        when(scheduleCommandPort.findById(ScheduleId.of(1L)))
                .thenReturn(Optional.of(schedule));

        // when
        handler.handleScheduleDisabled(event);

        // then
        verify(scheduleCommandPort).findById(ScheduleId.of(1L));
        verify(schedule).enable();
        verify(scheduleCommandPort).save(schedule);
    }

    @Test
    @DisplayName("handleScheduleDeleted 성공 - EventBridge 규칙 및 타겟 삭제")
    void handleScheduleDeletedSuccess() {
        // given
        ScheduleDeletedEvent event = new ScheduleDeletedEvent(1L, "test-rule", true);

        when(eventBridgePort.ruleExists("test-rule")).thenReturn(true);

        // when
        handler.handleScheduleDeleted(event);

        // then
        verify(eventBridgePort).ruleExists("test-rule");
        verify(eventBridgePort).removeTargets("test-rule");
        verify(eventBridgePort).deleteRule("test-rule");
    }

    @Test
    @DisplayName("handleScheduleDeleted 스킵 - 스케줄이 비활성화 상태였음")
    void handleScheduleDeletedSkipWhenWasNotEnabled() {
        // given
        ScheduleDeletedEvent event = new ScheduleDeletedEvent(1L, "test-rule", false);

        // when
        handler.handleScheduleDeleted(event);

        // then
        verify(eventBridgePort, never()).ruleExists(any());
        verify(eventBridgePort, never()).removeTargets(any());
        verify(eventBridgePort, never()).deleteRule(any());
    }

    @Test
    @DisplayName("handleScheduleDeleted 실패 - Compensation 불가능 (이미 DB 삭제됨)")
    void handleScheduleDeletedFailureNoCompensation() {
        // given
        ScheduleDeletedEvent event = new ScheduleDeletedEvent(1L, "test-rule", true);

        when(eventBridgePort.ruleExists("test-rule")).thenReturn(true);
        doThrow(new RuntimeException("EventBridge error"))
                .when(eventBridgePort).removeTargets(any());

        // when
        handler.handleScheduleDeleted(event);

        // then
        verify(eventBridgePort).ruleExists("test-rule");
        verify(eventBridgePort).removeTargets("test-rule");
        verify(scheduleCommandPort, never()).findById(any());
    }

    @Test
    @DisplayName("handleScheduleUpdated 성공 - EventBridge 규칙 업데이트")
    void handleScheduleUpdatedSuccess() {
        // given
        ScheduleUpdatedEvent event = new ScheduleUpdatedEvent(
                1L, "test-rule", "0 0 * * *", "Updated description"
        );

        when(eventBridgePort.ruleExists("test-rule")).thenReturn(true);

        // when
        handler.handleScheduleUpdated(event);

        // then
        verify(eventBridgePort).ruleExists("test-rule");
        verify(eventBridgePort).updateRule("test-rule", "0 0 * * *", "Updated description");
    }

    @Test
    @DisplayName("handleScheduleUpdated 스킵 - 규칙이 존재하지 않음")
    void handleScheduleUpdatedSkipWhenRuleNotExists() {
        // given
        ScheduleUpdatedEvent event = new ScheduleUpdatedEvent(
                1L, "test-rule", "0 0 * * *", "Updated description"
        );

        when(eventBridgePort.ruleExists("test-rule")).thenReturn(false);

        // when
        handler.handleScheduleUpdated(event);

        // then
        verify(eventBridgePort).ruleExists("test-rule");
        verify(eventBridgePort, never()).updateRule(any(), any(), any());
    }

    @Test
    @DisplayName("handleScheduleUpdated 실패 - Compensation 없음 (DB는 이미 정상)")
    void handleScheduleUpdatedFailureNoCompensation() {
        // given
        ScheduleUpdatedEvent event = new ScheduleUpdatedEvent(
                1L, "test-rule", "0 0 * * *", "Updated description"
        );

        when(eventBridgePort.ruleExists("test-rule")).thenReturn(true);
        doThrow(new RuntimeException("EventBridge error"))
                .when(eventBridgePort).updateRule(any(), any(), any());

        // when
        handler.handleScheduleUpdated(event);

        // then
        verify(eventBridgePort).updateRule("test-rule", "0 0 * * *", "Updated description");
        verify(scheduleCommandPort, never()).findById(any());
    }
}
