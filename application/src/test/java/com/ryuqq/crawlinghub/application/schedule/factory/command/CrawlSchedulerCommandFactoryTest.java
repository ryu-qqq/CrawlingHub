package com.ryuqq.crawlinghub.application.schedule.factory.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.crawlinghub.application.schedule.dto.CrawlSchedulerBundle;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RegisterCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@Tag("application")
@Tag("factory")
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlSchedulerCommandFactory 단위 테스트")
class CrawlSchedulerCommandFactoryTest {

    @Mock private ClockHolder clockHolder;

    private ObjectMapper objectMapper;
    private CrawlSchedulerCommandFactory factory;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        factory = new CrawlSchedulerCommandFactory(clockHolder, objectMapper);
        fixedClock = Clock.fixed(Instant.parse("2024-01-15T10:00:00Z"), ZoneId.of("UTC"));
    }

    @Nested
    @DisplayName("createBundle() 메서드는")
    class CreateBundleMethod {

        @Test
        @DisplayName("RegisterCrawlSchedulerCommand로 CrawlSchedulerBundle을 생성한다")
        void shouldCreateBundleFromCommand() {
            // Given
            given(clockHolder.getClock()).willReturn(fixedClock);
            RegisterCrawlSchedulerCommand command =
                    new RegisterCrawlSchedulerCommand(100L, "test-scheduler", "cron(0 0 * * ? *)");

            // When
            CrawlSchedulerBundle bundle = factory.createBundle(command);

            // Then
            assertThat(bundle).isNotNull();
            assertThat(bundle.scheduler()).isNotNull();
            assertThat(bundle.scheduler().getSellerIdValue()).isEqualTo(100L);
            assertThat(bundle.scheduler().getSchedulerNameValue()).isEqualTo("test-scheduler");
            assertThat(bundle.scheduler().getCronExpressionValue()).isEqualTo("cron(0 0 * * ? *)");
            assertThat(bundle.scheduler().getStatus()).isEqualTo(SchedulerStatus.ACTIVE);
        }

        @Test
        @DisplayName("생성된 Bundle의 eventPayload는 JSON 형식이다")
        void shouldCreateBundleWithJsonEventPayload() throws JsonProcessingException {
            // Given
            given(clockHolder.getClock()).willReturn(fixedClock);
            RegisterCrawlSchedulerCommand command =
                    new RegisterCrawlSchedulerCommand(100L, "scheduler-name", "cron(0 12 * * ? *)");

            // When
            CrawlSchedulerBundle bundle = factory.createBundle(command);

            // Then
            assertThat(bundle.eventPayload()).isNotBlank();
            // JSON 유효성 검증
            Object parsed = objectMapper.readValue(bundle.eventPayload(), Object.class);
            assertThat(parsed).isNotNull();
        }

        @Test
        @DisplayName("이벤트 페이로드에 필수 필드가 포함된다")
        void shouldIncludeRequiredFieldsInEventPayload() {
            // Given
            given(clockHolder.getClock()).willReturn(fixedClock);
            RegisterCrawlSchedulerCommand command =
                    new RegisterCrawlSchedulerCommand(100L, "scheduler-name", "cron(0 12 * * ? *)");

            // When
            CrawlSchedulerBundle bundle = factory.createBundle(command);

            // Then
            String payload = bundle.eventPayload();
            assertThat(payload).contains("sellerId");
            assertThat(payload).contains("schedulerName");
            assertThat(payload).contains("cronExpression");
            assertThat(payload).contains("status");
        }
    }

    @Nested
    @DisplayName("createScheduler() 메서드는")
    class CreateSchedulerMethod {

        @Test
        @DisplayName("RegisterCrawlSchedulerCommand로 CrawlScheduler를 생성한다")
        void shouldCreateSchedulerFromCommand() {
            // Given
            given(clockHolder.getClock()).willReturn(fixedClock);
            RegisterCrawlSchedulerCommand command =
                    new RegisterCrawlSchedulerCommand(200L, "my-scheduler", "cron(30 6 * * ? *)");

            // When
            CrawlScheduler scheduler = factory.createScheduler(command);

            // Then
            assertThat(scheduler.getSellerIdValue()).isEqualTo(200L);
            assertThat(scheduler.getSchedulerNameValue()).isEqualTo("my-scheduler");
            assertThat(scheduler.getCronExpressionValue()).isEqualTo("cron(30 6 * * ? *)");
            assertThat(scheduler.getStatus()).isEqualTo(SchedulerStatus.ACTIVE);
        }

        @Test
        @DisplayName("생성된 스케줄러는 ID가 null이다 (영속화 전)")
        void shouldCreateSchedulerWithNullId() {
            // Given
            given(clockHolder.getClock()).willReturn(fixedClock);
            RegisterCrawlSchedulerCommand command =
                    new RegisterCrawlSchedulerCommand(100L, "new-scheduler", "cron(0 0 * * ? *)");

            // When
            CrawlScheduler scheduler = factory.createScheduler(command);

            // Then
            assertThat(scheduler.getCrawlSchedulerId()).isNull();
        }
    }

    @Nested
    @DisplayName("toEventPayload() 메서드는")
    class ToEventPayloadMethod {

        @Test
        @DisplayName("CrawlScheduler를 JSON 페이로드로 변환한다")
        void shouldConvertSchedulerToJsonPayload() throws JsonProcessingException {
            // Given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler(100L);

            // When
            String payload = factory.toEventPayload(scheduler);

            // Then
            assertThat(payload).isNotBlank();
            Object parsed = objectMapper.readValue(payload, Object.class);
            assertThat(parsed).isNotNull();
        }

        @Test
        @DisplayName("페이로드에 schedulerId, sellerId, schedulerName, cronExpression, status가 포함된다")
        void shouldIncludeRequiredFieldsInPayload() {
            // Given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler(999L);

            // When
            String payload = factory.toEventPayload(scheduler);

            // Then
            assertThat(payload).contains("\"schedulerId\"");
            assertThat(payload).contains("\"sellerId\"");
            assertThat(payload).contains("\"schedulerName\"");
            assertThat(payload).contains("\"cronExpression\"");
            assertThat(payload).contains("\"status\"");
        }

        @Test
        @DisplayName("INACTIVE 상태의 스케줄러도 변환한다")
        void shouldConvertInactiveScheduler() {
            // Given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anInactiveScheduler();

            // When
            String payload = factory.toEventPayload(scheduler);

            // Then
            assertThat(payload).contains("\"status\":\"INACTIVE\"");
        }
    }

    @Nested
    @DisplayName("JSON 직렬화 실패 시")
    class JsonSerializationFailure {

        @Test
        @DisplayName("toEventPayload 실패 시 IllegalStateException을 던진다")
        void shouldThrowIllegalStateExceptionWhenSerializationFails()
                throws JsonProcessingException {
            // Given
            ObjectMapper failingMapper = Mockito.mock(ObjectMapper.class);
            CrawlSchedulerCommandFactory failingFactory =
                    new CrawlSchedulerCommandFactory(clockHolder, failingMapper);
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();

            given(failingMapper.writeValueAsString(Mockito.any()))
                    .willThrow(new JsonProcessingException("Serialization failed") {});

            // When & Then
            assertThatThrownBy(() -> failingFactory.toEventPayload(scheduler))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이벤트 페이로드 생성 실패");
        }
    }
}
