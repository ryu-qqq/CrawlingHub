package com.ryuqq.crawlinghub.domain.schedule.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("CrawlSchedulerOubBoxStatus 단위 테스트")
class CrawlSchedulerOubBoxStatusTest {

    @Nested
    @DisplayName("enum 값 존재 테스트")
    class EnumValueTest {

        @Test
        @DisplayName("PENDING 값이 존재한다")
        void pendingExists() {
            assertThat(CrawlSchedulerOubBoxStatus.PENDING).isNotNull();
        }

        @Test
        @DisplayName("PROCESSING 값이 존재한다")
        void processingExists() {
            assertThat(CrawlSchedulerOubBoxStatus.PROCESSING).isNotNull();
        }

        @Test
        @DisplayName("COMPLETED 값이 존재한다")
        void completedExists() {
            assertThat(CrawlSchedulerOubBoxStatus.COMPLETED).isNotNull();
        }

        @Test
        @DisplayName("FAILED 값이 존재한다")
        void failedExists() {
            assertThat(CrawlSchedulerOubBoxStatus.FAILED).isNotNull();
        }

        @Test
        @DisplayName("총 4개의 상태가 존재한다")
        void hasFourValues() {
            assertThat(CrawlSchedulerOubBoxStatus.values()).hasSize(4);
        }
    }

    @Nested
    @DisplayName("displayName() 메서드 테스트")
    class DisplayNameTest {

        @Test
        @DisplayName("PENDING의 displayName은 '대기'이다")
        void pendingDisplayName() {
            assertThat(CrawlSchedulerOubBoxStatus.PENDING.displayName()).isEqualTo("대기");
        }

        @Test
        @DisplayName("PROCESSING의 displayName은 '처리중'이다")
        void processingDisplayName() {
            assertThat(CrawlSchedulerOubBoxStatus.PROCESSING.displayName()).isEqualTo("처리중");
        }

        @Test
        @DisplayName("COMPLETED의 displayName은 '완료'이다")
        void completedDisplayName() {
            assertThat(CrawlSchedulerOubBoxStatus.COMPLETED.displayName()).isEqualTo("완료");
        }

        @Test
        @DisplayName("FAILED의 displayName은 '실패'이다")
        void failedDisplayName() {
            assertThat(CrawlSchedulerOubBoxStatus.FAILED.displayName()).isEqualTo("실패");
        }

        @ParameterizedTest
        @EnumSource(CrawlSchedulerOubBoxStatus.class)
        @DisplayName("모든 상태의 displayName은 null이 아니다")
        void allDisplayNamesAreNotNull(CrawlSchedulerOubBoxStatus status) {
            assertThat(status.displayName()).isNotNull().isNotBlank();
        }
    }

    @Nested
    @DisplayName("valueOf() 테스트")
    class ValueOfTest {

        @Test
        @DisplayName("문자열로 PENDING을 찾는다")
        void valueOfPending() {
            assertThat(CrawlSchedulerOubBoxStatus.valueOf("PENDING"))
                    .isEqualTo(CrawlSchedulerOubBoxStatus.PENDING);
        }

        @Test
        @DisplayName("문자열로 PROCESSING을 찾는다")
        void valueOfProcessing() {
            assertThat(CrawlSchedulerOubBoxStatus.valueOf("PROCESSING"))
                    .isEqualTo(CrawlSchedulerOubBoxStatus.PROCESSING);
        }

        @Test
        @DisplayName("문자열로 COMPLETED를 찾는다")
        void valueOfCompleted() {
            assertThat(CrawlSchedulerOubBoxStatus.valueOf("COMPLETED"))
                    .isEqualTo(CrawlSchedulerOubBoxStatus.COMPLETED);
        }

        @Test
        @DisplayName("문자열로 FAILED를 찾는다")
        void valueOfFailed() {
            assertThat(CrawlSchedulerOubBoxStatus.valueOf("FAILED"))
                    .isEqualTo(CrawlSchedulerOubBoxStatus.FAILED);
        }
    }
}
