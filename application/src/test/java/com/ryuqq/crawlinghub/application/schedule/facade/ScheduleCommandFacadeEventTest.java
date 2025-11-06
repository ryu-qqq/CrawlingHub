package com.ryuqq.crawlinghub.application.schedule.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.crawl.schedule.dto.command.CreateScheduleCommandFixture;
import com.ryuqq.crawlinghub.application.crawl.schedule.dto.command.UpdateScheduleCommandFixture;
import com.ryuqq.crawlinghub.application.schedule.dto.command.CreateScheduleCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.command.UpdateScheduleCommand;
import com.ryuqq.crawlinghub.application.schedule.port.out.LoadSchedulePort;
import com.ryuqq.crawlinghub.application.schedule.port.out.SaveSchedulePort;
import com.ryuqq.crawlinghub.application.schedule.port.out.ScheduleOutboxPort;
import com.ryuqq.crawlinghub.application.schedule.validator.CronExpressionValidator;
import com.ryuqq.crawlinghub.domain.crawl.schedule.CrawlScheduleFixture;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.CrawlScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * ScheduleCommandFacade 이벤트 발행 테스트
 *
 * <p>Domain Event 등록 및 이벤트 발행 로직을 검증합니다.</p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleCommandFacade 이벤트 발행 테스트")
class ScheduleCommandFacadeEventTest {

    @Mock
    private CronExpressionValidator cronValidator;

    @Mock
    private SaveSchedulePort saveSchedulePort;

    @Mock
    private LoadSchedulePort loadSchedulePort;

    @Mock
    private ScheduleOutboxPort outboxPort;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ScheduleCommandFacade sut;

    @BeforeEach
    void setUp() throws Exception {
        // 공통 설정: Cron 검증 성공
        given(cronValidator.isValid(anyString())).willReturn(true);
        given(cronValidator.calculateNextExecution(anyString(), any(LocalDateTime.class)))
            .willReturn(LocalDateTime.now().plusHours(1));

        // ObjectMapper 설정
        given(objectMapper.writeValueAsString(any())).willReturn("{\"test\":\"payload\"}");
    }

    @Nested
    @DisplayName("createSchedule 메서드는")
    class Describe_create_schedule {

        private CreateScheduleCommand command;
        private CrawlSchedule savedSchedule;

        @BeforeEach
        void setUp() {
            command = CreateScheduleCommandFixture.create();

            // ID가 없는 신규 스케줄
            CrawlSchedule newSchedule = CrawlScheduleFixture.create();
            // ID가 있는 저장된 스케줄 (첫 번째 save 호출 결과)
            savedSchedule = CrawlScheduleFixture.createWithId(1L);

            given(outboxPort.existsByIdemKey(anyString())).willReturn(false);
            given(saveSchedulePort.save(any(CrawlSchedule.class)))
                .willReturn(savedSchedule); // 첫 번째 저장 후 ID 생성
            given(outboxPort.save(any(SellerCrawlScheduleOutbox.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        }

        @Test
        @DisplayName("이벤트 등록 후 Schedule을 두 번 저장한다")
        void it_saves_schedule_twice_for_event_registration() {
            // When
            sut.createSchedule(command);

            // Then: 첫 번째 저장 (ID 생성) + 두 번째 저장 (이벤트 발행)
            then(saveSchedulePort).should(times(2)).save(any(CrawlSchedule.class));
        }

        @Test
        @DisplayName("이벤트가 등록된 Schedule을 저장한다")
        void it_saves_schedule_with_registered_event() {
            // When
            sut.createSchedule(command);

            // Then: 두 번째 save 호출 시 이벤트가 등록되어 있어야 함
            ArgumentCaptor<CrawlSchedule> scheduleCaptor = ArgumentCaptor.forClass(CrawlSchedule.class);
            then(saveSchedulePort).should(times(2)).save(scheduleCaptor.capture());

            // 두 번째 저장된 Schedule 확인 (이벤트가 등록된 상태)
            CrawlSchedule scheduleWithEvent = scheduleCaptor.getAllValues().get(1);
            assertThat(scheduleWithEvent).isNotNull();
            assertThat(scheduleWithEvent.getIdValue()).isEqualTo(savedSchedule.getIdValue());
        }

        @Test
        @DisplayName("정상적으로 응답을 반환한다")
        void it_returns_response() {
            // When
            var response = sut.createSchedule(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.scheduleId()).isEqualTo(savedSchedule.getIdValue());
            assertThat(response.sellerId()).isEqualTo(savedSchedule.getSellerIdValue());
            assertThat(response.cronExpression()).isEqualTo(savedSchedule.getCronExpressionValue());
        }

        @Test
        @DisplayName("Outbox를 저장한다")
        void it_saves_outbox() {
            // When
            sut.createSchedule(command);

            // Then
            then(outboxPort).should().save(any(SellerCrawlScheduleOutbox.class));
        }
    }

    @Nested
    @DisplayName("updateSchedule 메서드는")
    class Describe_update_schedule {

        private UpdateScheduleCommand command;
        private CrawlSchedule existingSchedule;
        private CrawlSchedule updatedSchedule;

        @BeforeEach
        void setUp() {
            command = UpdateScheduleCommandFixture.create();

            // 기존 스케줄
            existingSchedule = CrawlScheduleFixture.createWithId(command.scheduleId());
            // 업데이트된 스케줄
            updatedSchedule = CrawlScheduleFixture.createWithId(command.scheduleId());
            updatedSchedule.updateSchedule(CronExpression.of(command.cronExpression()));

            given(loadSchedulePort.findById(CrawlScheduleId.of(command.scheduleId())))
                .willReturn(Optional.of(existingSchedule));
            given(outboxPort.existsByIdemKey(anyString())).willReturn(false);
            given(saveSchedulePort.save(any(CrawlSchedule.class)))
                .willReturn(updatedSchedule); // 첫 번째 저장 결과
            given(outboxPort.save(any(SellerCrawlScheduleOutbox.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        }

        @Test
        @DisplayName("이벤트 등록 후 Schedule을 한 번 저장한다")
        void it_saves_schedule_once_with_event_registration() {
            // When
            sut.updateSchedule(command);

            // Then: 이벤트를 첫 번째 save 전에 등록하므로 한 번의 저장으로 처리
            then(saveSchedulePort).should(times(1)).save(any(CrawlSchedule.class));
        }

        @Test
        @DisplayName("이벤트가 등록된 Schedule을 저장한다")
        void it_saves_schedule_with_registered_event() {
            // When
            sut.updateSchedule(command);

            // Then: save 호출 시 이벤트가 등록되어 있어야 함
            ArgumentCaptor<CrawlSchedule> scheduleCaptor = ArgumentCaptor.forClass(CrawlSchedule.class);
            then(saveSchedulePort).should(times(1)).save(scheduleCaptor.capture());

            // 저장된 Schedule 확인 (이벤트가 등록된 상태)
            CrawlSchedule scheduleWithEvent = scheduleCaptor.getValue();
            assertThat(scheduleWithEvent).isNotNull();
            assertThat(scheduleWithEvent.getIdValue()).isEqualTo(updatedSchedule.getIdValue());
        }

        @Test
        @DisplayName("정상적으로 응답을 반환한다")
        void it_returns_response() {
            // When
            var response = sut.updateSchedule(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.scheduleId()).isEqualTo(updatedSchedule.getIdValue());
            assertThat(response.sellerId()).isEqualTo(updatedSchedule.getSellerIdValue());
            assertThat(response.cronExpression()).isEqualTo(updatedSchedule.getCronExpressionValue());
        }

        @Test
        @DisplayName("Outbox를 저장한다")
        void it_saves_outbox() {
            // When
            sut.updateSchedule(command);

            // Then
            then(outboxPort).should().save(any(SellerCrawlScheduleOutbox.class));
        }
    }
}

