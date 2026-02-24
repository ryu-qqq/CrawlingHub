package com.ryuqq.crawlinghub.application.schedule.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerHistoryIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.crawlinghub.application.schedule.dto.bundle.CrawlSchedulerBundle;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerHistory;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CrawlSchedulerBundle 단위 테스트
 *
 * <p>불변 DTO 번들의 with* 메서드 및 create* 메서드 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlSchedulerBundle 테스트")
class CrawlSchedulerBundleTest {

    private static final Instant FIXED_NOW = Instant.parse("2025-01-01T00:00:00Z");

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class Of {

        @Test
        @DisplayName("[성공] 스케줄러와 등록 시각으로 번들 생성")
        void shouldCreateBundleWithSchedulerAndTime() {
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlSchedulerBundle bundle = CrawlSchedulerBundle.of(scheduler, FIXED_NOW);

            assertThat(bundle.scheduler()).isEqualTo(scheduler);
            assertThat(bundle.registeredAt()).isEqualTo(FIXED_NOW);
            assertThat(bundle.savedSchedulerId()).isNull();
            assertThat(bundle.savedHistoryId()).isNull();
        }
    }

    @Nested
    @DisplayName("withSchedulerId() 테스트")
    class WithSchedulerId {

        @Test
        @DisplayName("[성공] 스케줄러 ID 설정 후 새 번들 반환")
        void shouldReturnNewBundleWithSchedulerId() {
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlSchedulerBundle bundle = CrawlSchedulerBundle.of(scheduler, FIXED_NOW);
            CrawlSchedulerId schedulerId = CrawlSchedulerIdFixture.anAssignedId();

            CrawlSchedulerBundle updated = bundle.withSchedulerId(schedulerId);

            assertThat(updated.savedSchedulerId()).isEqualTo(schedulerId);
            assertThat(bundle.savedSchedulerId()).isNull(); // 원본 불변
        }
    }

    @Nested
    @DisplayName("withHistoryId() 테스트")
    class WithHistoryId {

        @Test
        @DisplayName("[성공] 히스토리 ID 설정 후 새 번들 반환")
        void shouldReturnNewBundleWithHistoryId() {
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlSchedulerBundle bundle = CrawlSchedulerBundle.of(scheduler, FIXED_NOW);
            CrawlSchedulerHistoryId historyId = CrawlSchedulerHistoryIdFixture.anAssignedId();

            CrawlSchedulerBundle updated = bundle.withHistoryId(historyId);

            assertThat(updated.savedHistoryId()).isEqualTo(historyId);
            assertThat(bundle.savedHistoryId()).isNull(); // 원본 불변
        }
    }

    @Nested
    @DisplayName("getSavedSchedulerId() 테스트")
    class GetSavedSchedulerId {

        @Test
        @DisplayName("[성공] 저장된 스케줄러 ID 반환")
        void shouldReturnSavedSchedulerId() {
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlSchedulerId schedulerId = CrawlSchedulerIdFixture.anAssignedId();
            CrawlSchedulerBundle bundle =
                    CrawlSchedulerBundle.of(scheduler, FIXED_NOW).withSchedulerId(schedulerId);

            assertThat(bundle.getSavedSchedulerId()).isEqualTo(schedulerId);
        }

        @Test
        @DisplayName("[실패] 스케줄러 ID가 없으면 IllegalStateException 발생")
        void shouldThrowWhenSchedulerIdNotAssigned() {
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlSchedulerBundle bundle = CrawlSchedulerBundle.of(scheduler, FIXED_NOW);

            assertThatThrownBy(bundle::getSavedSchedulerId)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("스케줄러 ID");
        }
    }

    @Nested
    @DisplayName("createHistory() 테스트")
    class CreateHistory {

        @Test
        @DisplayName("[성공] 스케줄러 ID 할당 후 히스토리 생성")
        void shouldCreateHistoryWhenSchedulerIdAssigned() {
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlSchedulerId schedulerId = CrawlSchedulerIdFixture.anAssignedId();
            CrawlSchedulerBundle bundle =
                    CrawlSchedulerBundle.of(scheduler, FIXED_NOW).withSchedulerId(schedulerId);

            CrawlSchedulerHistory history = bundle.createHistory();

            assertThat(history).isNotNull();
        }

        @Test
        @DisplayName("[실패] 스케줄러 ID 없으면 IllegalStateException 발생")
        void shouldThrowWhenSchedulerIdNotAssigned() {
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlSchedulerBundle bundle = CrawlSchedulerBundle.of(scheduler, FIXED_NOW);

            assertThatThrownBy(bundle::createHistory)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("스케줄러 ID");
        }
    }

    @Nested
    @DisplayName("createOutBox() 테스트")
    class CreateOutBox {

        @Test
        @DisplayName("[성공] 스케줄러 ID + 히스토리 ID 할당 후 아웃박스 생성")
        void shouldCreateOutBoxWhenAllIdsAssigned() {
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlSchedulerId schedulerId = CrawlSchedulerIdFixture.anAssignedId();
            CrawlSchedulerHistoryId historyId = CrawlSchedulerHistoryIdFixture.anAssignedId();
            CrawlSchedulerBundle bundle =
                    CrawlSchedulerBundle.of(scheduler, FIXED_NOW)
                            .withSchedulerId(schedulerId)
                            .withHistoryId(historyId);

            CrawlSchedulerOutBox outBox = bundle.createOutBox();

            assertThat(outBox).isNotNull();
        }

        @Test
        @DisplayName("[실패] 히스토리 ID 없으면 IllegalStateException 발생")
        void shouldThrowWhenHistoryIdNotAssigned() {
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlSchedulerId schedulerId = CrawlSchedulerIdFixture.anAssignedId();
            CrawlSchedulerBundle bundle =
                    CrawlSchedulerBundle.of(scheduler, FIXED_NOW).withSchedulerId(schedulerId);

            assertThatThrownBy(bundle::createOutBox)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("히스토리 ID");
        }

        @Test
        @DisplayName("[실패] 스케줄러 ID 없으면 IllegalStateException 발생")
        void shouldThrowWhenSchedulerIdNotAssigned() {
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlSchedulerHistoryId historyId = CrawlSchedulerHistoryIdFixture.anAssignedId();
            CrawlSchedulerBundle bundle =
                    CrawlSchedulerBundle.of(scheduler, FIXED_NOW).withHistoryId(historyId);

            assertThatThrownBy(bundle::createOutBox)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("스케줄러 ID");
        }
    }

    @Nested
    @DisplayName("getScheduler() 테스트")
    class GetScheduler {

        @Test
        @DisplayName("[성공] 스케줄러 반환")
        void shouldReturnScheduler() {
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlSchedulerBundle bundle = CrawlSchedulerBundle.of(scheduler, FIXED_NOW);

            assertThat(bundle.getScheduler()).isEqualTo(scheduler);
        }
    }
}
