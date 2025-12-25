package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CrawlCompletionStatus VO 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlCompletionStatus 테스트")
class CrawlCompletionStatusTest {

    private static final Instant TIME_1 = Instant.parse("2025-01-01T10:00:00Z");
    private static final Instant TIME_2 = Instant.parse("2025-01-01T11:00:00Z");
    private static final Instant TIME_3 = Instant.parse("2025-01-01T12:00:00Z");

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("초기 상태는 모두 미완료")
        void shouldCreateInitialWithAllNull() {
            // given & when
            CrawlCompletionStatus status = CrawlCompletionStatus.initial();

            // then
            assertThat(status.miniShopCrawledAt()).isNull();
            assertThat(status.detailCrawledAt()).isNull();
            assertThat(status.optionCrawledAt()).isNull();
        }
    }

    @Nested
    @DisplayName("상태 변경 메서드 테스트")
    class StateTransitionTest {

        @Test
        @DisplayName("MINI_SHOP 크롤링 완료 표시")
        void shouldMarkMiniShopCrawled() {
            // given
            CrawlCompletionStatus initial = CrawlCompletionStatus.initial();

            // when
            CrawlCompletionStatus updated = initial.withMiniShopCrawled(TIME_1);

            // then
            assertThat(updated.miniShopCrawledAt()).isEqualTo(TIME_1);
            assertThat(updated.detailCrawledAt()).isNull();
            assertThat(updated.optionCrawledAt()).isNull();
        }

        @Test
        @DisplayName("DETAIL 크롤링 완료 표시")
        void shouldMarkDetailCrawled() {
            // given
            CrawlCompletionStatus initial = CrawlCompletionStatus.initial();

            // when
            CrawlCompletionStatus updated = initial.withDetailCrawled(TIME_2);

            // then
            assertThat(updated.miniShopCrawledAt()).isNull();
            assertThat(updated.detailCrawledAt()).isEqualTo(TIME_2);
            assertThat(updated.optionCrawledAt()).isNull();
        }

        @Test
        @DisplayName("OPTION 크롤링 완료 표시")
        void shouldMarkOptionCrawled() {
            // given
            CrawlCompletionStatus initial = CrawlCompletionStatus.initial();

            // when
            CrawlCompletionStatus updated = initial.withOptionCrawled(TIME_3);

            // then
            assertThat(updated.miniShopCrawledAt()).isNull();
            assertThat(updated.detailCrawledAt()).isNull();
            assertThat(updated.optionCrawledAt()).isEqualTo(TIME_3);
        }
    }

    @Nested
    @DisplayName("완료 여부 확인 테스트")
    class CompletionCheckTest {

        @Test
        @DisplayName("개별 크롤링 완료 여부 확인")
        void shouldCheckIndividualCompletion() {
            // given
            CrawlCompletionStatus status =
                    CrawlCompletionStatus.initial()
                            .withMiniShopCrawled(TIME_1)
                            .withDetailCrawled(TIME_2);

            // when & then
            assertThat(status.isMiniShopCrawled()).isTrue();
            assertThat(status.isDetailCrawled()).isTrue();
            assertThat(status.isOptionCrawled()).isFalse();
        }

        @Test
        @DisplayName("모든 크롤링 완료 여부 확인")
        void shouldCheckAllCrawled() {
            // given
            CrawlCompletionStatus partial =
                    CrawlCompletionStatus.initial()
                            .withMiniShopCrawled(TIME_1)
                            .withDetailCrawled(TIME_2);
            CrawlCompletionStatus complete = partial.withOptionCrawled(TIME_3);

            // when & then
            assertThat(partial.isAllCrawled()).isFalse();
            assertThat(complete.isAllCrawled()).isTrue();
        }

        @Test
        @DisplayName("외부 서버 동기화 가능 여부 확인")
        void shouldCheckCanSyncToExternalServer() {
            // given
            CrawlCompletionStatus partial =
                    CrawlCompletionStatus.initial().withMiniShopCrawled(TIME_1);
            CrawlCompletionStatus complete =
                    CrawlCompletionStatus.initial()
                            .withMiniShopCrawled(TIME_1)
                            .withDetailCrawled(TIME_2)
                            .withOptionCrawled(TIME_3);

            // when & then
            assertThat(partial.canSyncToExternalServer()).isFalse();
            assertThat(complete.canSyncToExternalServer()).isTrue();
        }
    }

    @Nested
    @DisplayName("통계 메서드 테스트")
    class StatisticsTest {

        @Test
        @DisplayName("완료된 크롤링 타입 개수")
        void shouldCountCompletedCrawls() {
            // given
            CrawlCompletionStatus none = CrawlCompletionStatus.initial();
            CrawlCompletionStatus one = none.withMiniShopCrawled(TIME_1);
            CrawlCompletionStatus two = one.withDetailCrawled(TIME_2);
            CrawlCompletionStatus three = two.withOptionCrawled(TIME_3);

            // when & then
            assertThat(none.getCompletedCount()).isZero();
            assertThat(one.getCompletedCount()).isEqualTo(1);
            assertThat(two.getCompletedCount()).isEqualTo(2);
            assertThat(three.getCompletedCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("미완료 크롤링 타입 설명")
        void shouldReturnPendingCrawlTypes() {
            // given
            CrawlCompletionStatus all =
                    CrawlCompletionStatus.initial()
                            .withMiniShopCrawled(TIME_1)
                            .withDetailCrawled(TIME_2)
                            .withOptionCrawled(TIME_3);
            CrawlCompletionStatus partial =
                    CrawlCompletionStatus.initial().withMiniShopCrawled(TIME_1);
            CrawlCompletionStatus none = CrawlCompletionStatus.initial();

            // when & then
            assertThat(all.getPendingCrawlTypes()).isEmpty();
            assertThat(partial.getPendingCrawlTypes()).isEqualTo("DETAIL, OPTION");
            assertThat(none.getPendingCrawlTypes()).isEqualTo("MINI_SHOP, DETAIL, OPTION");
        }
    }

    @Nested
    @DisplayName("시간 관련 메서드 테스트")
    class TimeMethodTest {

        @Test
        @DisplayName("가장 최근 크롤링 시각")
        void shouldReturnLastCrawledAt() {
            // given
            CrawlCompletionStatus status =
                    CrawlCompletionStatus.initial()
                            .withMiniShopCrawled(TIME_1)
                            .withDetailCrawled(TIME_3)
                            .withOptionCrawled(TIME_2);

            // when & then
            assertThat(status.getLastCrawledAt()).isEqualTo(TIME_3);
        }

        @Test
        @DisplayName("모두 null이면 최근 시각도 null")
        void shouldReturnNullWhenAllNull() {
            // given
            CrawlCompletionStatus status = CrawlCompletionStatus.initial();

            // when & then
            assertThat(status.getLastCrawledAt()).isNull();
        }

        @Test
        @DisplayName("가장 오래된 크롤링 시각")
        void shouldReturnOldestCrawledAt() {
            // given
            CrawlCompletionStatus status =
                    CrawlCompletionStatus.initial()
                            .withMiniShopCrawled(TIME_2)
                            .withDetailCrawled(TIME_1)
                            .withOptionCrawled(TIME_3);

            // when & then
            assertThat(status.getOldestCrawledAt()).isEqualTo(TIME_1);
        }

        @Test
        @DisplayName("미완료가 있으면 가장 오래된 시각은 null")
        void shouldReturnNullOldestWhenIncomplete() {
            // given
            CrawlCompletionStatus status =
                    CrawlCompletionStatus.initial()
                            .withMiniShopCrawled(TIME_1)
                            .withDetailCrawled(TIME_2);

            // when & then
            assertThat(status.getOldestCrawledAt()).isNull();
        }
    }
}
