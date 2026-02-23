package com.ryuqq.crawlinghub.application.schedule.factory.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.application.common.dto.command.UpdateContext;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.schedule.dto.bundle.CrawlSchedulerBundle;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RegisterCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.command.UpdateCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerUpdateData;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@Tag("application")
@Tag("factory")
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlSchedulerCommandFactory 단위 테스트")
class CrawlSchedulerCommandFactoryTest {

    @Mock private TimeProvider timeProvider;

    private CrawlSchedulerCommandFactory factory;
    private Instant fixedInstant;

    @BeforeEach
    void setUp() {
        factory = new CrawlSchedulerCommandFactory(timeProvider);
        fixedInstant = Instant.parse("2024-01-15T10:00:00Z");
    }

    @Nested
    @DisplayName("createBundle() 메서드는")
    class CreateBundleMethod {

        @Test
        @DisplayName("RegisterCrawlSchedulerCommand로 CrawlSchedulerBundle을 생성한다")
        void shouldCreateBundleFromCommand() {
            // Given
            given(timeProvider.now()).willReturn(fixedInstant);
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
        @DisplayName("생성된 Bundle의 registeredAt이 설정된다")
        void shouldCreateBundleWithRegisteredAt() {
            // Given
            given(timeProvider.now()).willReturn(fixedInstant);
            RegisterCrawlSchedulerCommand command =
                    new RegisterCrawlSchedulerCommand(100L, "scheduler-name", "cron(0 12 * * ? *)");

            // When
            CrawlSchedulerBundle bundle = factory.createBundle(command);

            // Then
            assertThat(bundle.registeredAt()).isEqualTo(fixedInstant);
        }
    }

    @Nested
    @DisplayName("createScheduler() 메서드는")
    class CreateSchedulerMethod {

        @Test
        @DisplayName("RegisterCrawlSchedulerCommand로 CrawlScheduler를 생성한다")
        void shouldCreateSchedulerFromCommand() {
            // Given
            given(timeProvider.now()).willReturn(fixedInstant);
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
            given(timeProvider.now()).willReturn(fixedInstant);
            RegisterCrawlSchedulerCommand command =
                    new RegisterCrawlSchedulerCommand(100L, "new-scheduler", "cron(0 0 * * ? *)");

            // When
            CrawlScheduler scheduler = factory.createScheduler(command);

            // Then
            assertThat(scheduler.getCrawlSchedulerId()).isNull();
        }
    }

    @Nested
    @DisplayName("createUpdateContext() 메서드는")
    class CreateUpdateContextMethod {

        @Test
        @DisplayName("UpdateCrawlSchedulerCommand로 UpdateContext를 생성한다")
        void shouldCreateUpdateContextFromCommand() {
            // Given
            given(timeProvider.now()).willReturn(fixedInstant);
            UpdateCrawlSchedulerCommand command =
                    new UpdateCrawlSchedulerCommand(
                            10L, "updated-scheduler", "cron(0 12 * * ? *)", true);

            // When
            UpdateContext<CrawlSchedulerId, CrawlSchedulerUpdateData> context =
                    factory.createUpdateContext(command);

            // Then
            assertThat(context).isNotNull();
            assertThat(context.id()).isEqualTo(CrawlSchedulerId.of(10L));
            assertThat(context.updateData().schedulerName().value()).isEqualTo("updated-scheduler");
            assertThat(context.updateData().cronExpression().value())
                    .isEqualTo("cron(0 12 * * ? *)");
            assertThat(context.updateData().status()).isEqualTo(SchedulerStatus.ACTIVE);
            assertThat(context.changedAt()).isEqualTo(fixedInstant);
        }

        @Test
        @DisplayName("active=false일 때 INACTIVE 상태로 생성한다")
        void shouldCreateInactiveStatusWhenActiveFalse() {
            // Given
            given(timeProvider.now()).willReturn(fixedInstant);
            UpdateCrawlSchedulerCommand command =
                    new UpdateCrawlSchedulerCommand(10L, "scheduler", "cron(0 0 * * ? *)", false);

            // When
            UpdateContext<CrawlSchedulerId, CrawlSchedulerUpdateData> context =
                    factory.createUpdateContext(command);

            // Then
            assertThat(context.updateData().status()).isEqualTo(SchedulerStatus.INACTIVE);
        }
    }
}
