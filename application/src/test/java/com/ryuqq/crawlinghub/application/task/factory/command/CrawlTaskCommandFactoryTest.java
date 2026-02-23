package com.ryuqq.crawlinghub.application.task.factory.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.task.dto.bundle.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
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
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@Tag("application")
@Tag("factory")
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskCommandFactory 단위 테스트")
class CrawlTaskCommandFactoryTest {

    @Mock private TimeProvider timeProvider;

    private CrawlTaskCommandFactory factory;
    private Instant fixedInstant;

    @BeforeEach
    void setUp() {
        factory = new CrawlTaskCommandFactory(timeProvider);
        fixedInstant = Instant.parse("2024-01-15T10:00:00Z");
    }

    @Nested
    @DisplayName("createBundle(CrawlScheduler, Seller) 메서드는")
    class CreateBundleFromSchedulerAndSeller {

        @Test
        @DisplayName("CrawlScheduler와 Seller로 CrawlTaskBundle을 생성한다")
        void shouldCreateBundleFromSchedulerAndSeller() {
            // Given
            given(timeProvider.now()).willReturn(fixedInstant);
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler(100L);
            Seller seller = SellerFixture.anActiveSeller();

            // When
            CrawlTaskBundle bundle = factory.createBundle(scheduler, seller);

            // Then
            assertThat(bundle).isNotNull();
            assertThat(bundle.crawlTask()).isNotNull();
            assertThat(bundle.crawlTask().getCrawlSchedulerIdValue())
                    .isEqualTo(scheduler.getCrawlSchedulerIdValue());
            assertThat(bundle.crawlTask().getSellerIdValue())
                    .isEqualTo(scheduler.getSellerIdValue());
            assertThat(bundle.crawlTask().getTaskType()).isEqualTo(CrawlTaskType.SEARCH);
        }
    }

    @Nested
    @DisplayName("createBundle(CreateCrawlTaskCommand) 메서드는")
    class CreateBundleFromCreateCommand {

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
    }

    @Nested
    @DisplayName("createRetryBundle(CrawlTask) 메서드는")
    class CreateRetryBundle {

        @Test
        @DisplayName("CrawlTask로 재시도용 Bundle을 생성한다")
        void shouldCreateRetryBundle() {
            // Given
            given(timeProvider.now()).willReturn(fixedInstant);
            CrawlTask task = CrawlTaskFixture.aFailedTask();

            // When
            CrawlTaskBundle bundle = factory.createRetryBundle(task);

            // Then
            assertThat(bundle).isNotNull();
            assertThat(bundle.crawlTask()).isEqualTo(task);
            assertThat(bundle.createdAt()).isEqualTo(fixedInstant);
        }
    }
}
