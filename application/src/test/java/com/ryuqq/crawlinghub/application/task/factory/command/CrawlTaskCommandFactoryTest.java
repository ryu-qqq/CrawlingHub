package com.ryuqq.crawlinghub.application.task.factory.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.task.dto.bundle.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.command.TriggerCrawlTaskCommand;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Instant;
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
@DisplayName("CrawlTaskCommandFactory 단위 테스트")
class CrawlTaskCommandFactoryTest {

    @Mock private TimeProvider timeProvider;

    private ObjectMapper objectMapper;
    private CrawlTaskCommandFactory factory;
    private Instant fixedInstant;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        factory = new CrawlTaskCommandFactory(objectMapper, timeProvider);
        fixedInstant = Instant.parse("2024-01-15T10:00:00Z");
    }

    @Nested
    @DisplayName("createBundle(TriggerCrawlTaskCommand, CrawlScheduler, Seller) 메서드는")
    class CreateBundleFromTriggerCommand {

        @Test
        @DisplayName("TriggerCrawlTaskCommand와 CrawlScheduler, Seller로 CrawlTaskBundle을 생성한다")
        void shouldCreateBundleFromTriggerCommandAndScheduler() {
            // Given
            given(timeProvider.now()).willReturn(fixedInstant);
            TriggerCrawlTaskCommand command = new TriggerCrawlTaskCommand(100L);
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler(100L);
            Seller seller = SellerFixture.anActiveSeller();

            // When
            CrawlTaskBundle bundle = factory.createBundle(command, scheduler, seller);

            // Then
            assertThat(bundle).isNotNull();
            assertThat(bundle.crawlTask()).isNotNull();
            assertThat(bundle.crawlTask().getCrawlSchedulerId().value())
                    .isEqualTo(scheduler.getCrawlSchedulerId().value());
            assertThat(bundle.crawlTask().getSellerId().value())
                    .isEqualTo(scheduler.getSellerIdValue());
            assertThat(bundle.crawlTask().getTaskType()).isEqualTo(CrawlTaskType.SEARCH);
        }

        @Test
        @DisplayName("생성된 Bundle의 outboxPayload는 JSON 형식이다")
        void shouldCreateBundleWithJsonOutboxPayload() throws JsonProcessingException {
            // Given
            given(timeProvider.now()).willReturn(fixedInstant);
            TriggerCrawlTaskCommand command = new TriggerCrawlTaskCommand(100L);
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler(100L);
            Seller seller = SellerFixture.anActiveSeller();

            // When
            CrawlTaskBundle bundle = factory.createBundle(command, scheduler, seller);

            // Then
            assertThat(bundle.outboxPayload()).isNotBlank();
            // JSON 유효성 검증
            Object parsed = objectMapper.readValue(bundle.outboxPayload(), Object.class);
            assertThat(parsed).isNotNull();
        }
    }

    @Nested
    @DisplayName("createBundle(CreateCrawlTaskCommand) 메서드는")
    class CreateBundleFromCreateCommand {

        @Test
        @DisplayName("META 타입 CreateCrawlTaskCommand로 CrawlTaskBundle을 생성한다")
        void shouldCreateBundleForMetaTask() {
            // Given
            given(timeProvider.now()).willReturn(fixedInstant);
            CreateCrawlTaskCommand command =
                    CreateCrawlTaskCommand.forMeta(100L, 200L, "test-seller");

            // When
            CrawlTaskBundle bundle = factory.createBundle(command);

            // Then
            assertThat(bundle.crawlTask().getTaskType()).isEqualTo(CrawlTaskType.META);
            assertThat(bundle.crawlTask().getCrawlSchedulerId().value()).isEqualTo(100L);
            assertThat(bundle.crawlTask().getSellerId().value()).isEqualTo(200L);
        }

        @Test
        @DisplayName("MINI_SHOP 타입 CreateCrawlTaskCommand로 CrawlTaskBundle을 생성한다")
        void shouldCreateBundleForMiniShopTask() {
            // Given
            given(timeProvider.now()).willReturn(fixedInstant);
            CreateCrawlTaskCommand command =
                    CreateCrawlTaskCommand.forMiniShop(100L, 200L, "test-seller", 1L);

            // When
            CrawlTaskBundle bundle = factory.createBundle(command);

            // Then
            assertThat(bundle.crawlTask().getTaskType()).isEqualTo(CrawlTaskType.MINI_SHOP);
        }

        @Test
        @DisplayName("DETAIL 타입 CreateCrawlTaskCommand로 CrawlTaskBundle을 생성한다")
        void shouldCreateBundleForDetailTask() {
            // Given
            given(timeProvider.now()).willReturn(fixedInstant);
            CreateCrawlTaskCommand command =
                    CreateCrawlTaskCommand.forDetail(100L, 200L, "test-seller", 12345L);

            // When
            CrawlTaskBundle bundle = factory.createBundle(command);

            // Then
            assertThat(bundle.crawlTask().getTaskType()).isEqualTo(CrawlTaskType.DETAIL);
        }

        @Test
        @DisplayName("OPTION 타입 CreateCrawlTaskCommand로 CrawlTaskBundle을 생성한다")
        void shouldCreateBundleForOptionTask() {
            // Given
            given(timeProvider.now()).willReturn(fixedInstant);
            CreateCrawlTaskCommand command =
                    CreateCrawlTaskCommand.forOption(100L, 200L, "test-seller", 12345L);

            // When
            CrawlTaskBundle bundle = factory.createBundle(command);

            // Then
            assertThat(bundle.crawlTask().getTaskType()).isEqualTo(CrawlTaskType.OPTION);
        }

        @Test
        @DisplayName("생성된 Bundle의 outboxPayload는 필수 필드를 포함한다")
        void shouldCreateBundleWithRequiredFieldsInPayload() throws JsonProcessingException {
            // Given
            given(timeProvider.now()).willReturn(fixedInstant);
            CreateCrawlTaskCommand command =
                    CreateCrawlTaskCommand.forMeta(100L, 200L, "test-seller");

            // When
            CrawlTaskBundle bundle = factory.createBundle(command);

            // Then
            String payload = bundle.outboxPayload();
            assertThat(payload).contains("schedulerId");
            assertThat(payload).contains("sellerId");
            assertThat(payload).contains("taskType");
            assertThat(payload).contains("endpoint");
        }
    }

    @Nested
    @DisplayName("toOutboxPayload(CrawlTask) 메서드는")
    class ToOutboxPayloadFromTask {

        @Test
        @DisplayName("CrawlTask를 JSON 페이로드로 변환한다")
        void shouldConvertCrawlTaskToJsonPayload() throws JsonProcessingException {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();

            // When
            String payload = factory.toOutboxPayload(task);

            // Then
            assertThat(payload).isNotBlank();
            Object parsed = objectMapper.readValue(payload, Object.class);
            assertThat(parsed).isNotNull();
        }

        @Test
        @DisplayName("페이로드에 schedulerId, sellerId, taskType, endpoint가 포함된다")
        void shouldIncludeRequiredFieldsInPayload() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();

            // When
            String payload = factory.toOutboxPayload(task);

            // Then
            assertThat(payload).contains("\"schedulerId\"");
            assertThat(payload).contains("\"sellerId\"");
            assertThat(payload).contains("\"taskType\"");
            assertThat(payload).contains("\"endpoint\"");
        }
    }

    @Nested
    @DisplayName("toOutboxPayload(CrawlTask, CrawlScheduler) 메서드는")
    class ToOutboxPayloadFromTaskAndScheduler {

        @Test
        @DisplayName("CrawlTask와 CrawlScheduler를 JSON 페이로드로 변환한다")
        void shouldConvertTaskAndSchedulerToJsonPayload() throws JsonProcessingException {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();

            // When
            String payload = factory.toOutboxPayload(task, scheduler);

            // Then
            assertThat(payload).isNotBlank();
            Object parsed = objectMapper.readValue(payload, Object.class);
            assertThat(parsed).isNotNull();
        }

        @Test
        @DisplayName("스케줄러 정보가 페이로드에 포함된다")
        void shouldIncludeSchedulerInfoInPayload() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler(999L);

            // When
            String payload = factory.toOutboxPayload(task, scheduler);

            // Then
            assertThat(payload).contains("\"schedulerId\":999");
        }
    }

    @Nested
    @DisplayName("JSON 직렬화 실패 시")
    class JsonSerializationFailure {

        @Test
        @DisplayName("ObjectMapper 실패 시 IllegalStateException을 던진다")
        void shouldThrowIllegalStateExceptionWhenSerializationFails()
                throws JsonProcessingException {
            // Given
            ObjectMapper failingMapper = Mockito.mock(ObjectMapper.class);
            CrawlTaskCommandFactory failingFactory =
                    new CrawlTaskCommandFactory(failingMapper, timeProvider);
            CrawlTask task = CrawlTaskFixture.aWaitingTask();

            given(failingMapper.writeValueAsString(Mockito.any()))
                    .willThrow(new JsonProcessingException("Serialization failed") {});

            // When & Then
            assertThatThrownBy(() -> failingFactory.toOutboxPayload(task))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Outbox 페이로드 생성 실패");
        }
    }
}
