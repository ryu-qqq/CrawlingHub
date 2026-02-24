package com.ryuqq.crawlinghub.application.task.port.out.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CrawlTaskQueryPort.TaskTypeCount 레코드 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlTaskQueryPort.TaskTypeCount 테스트")
class TaskTypeCountTest {

    @Nested
    @DisplayName("생성 및 조회 테스트")
    class Creation {

        @Test
        @DisplayName("[성공] 전체/성공/실패 카운트 정상 생성")
        void shouldCreateWithCorrectValues() {
            // Given & When
            CrawlTaskQueryPort.TaskTypeCount count =
                    new CrawlTaskQueryPort.TaskTypeCount(100L, 80L, 20L);

            // Then
            assertThat(count.total()).isEqualTo(100L);
            assertThat(count.success()).isEqualTo(80L);
            assertThat(count.failed()).isEqualTo(20L);
        }

        @Test
        @DisplayName("[성공] 모두 0인 경우")
        void shouldCreateWithZeroValues() {
            // Given & When
            CrawlTaskQueryPort.TaskTypeCount count =
                    new CrawlTaskQueryPort.TaskTypeCount(0L, 0L, 0L);

            // Then
            assertThat(count.total()).isZero();
            assertThat(count.success()).isZero();
            assertThat(count.failed()).isZero();
        }

        @Test
        @DisplayName("[성공] 동일한 값이면 equals() true")
        void shouldBeEqualWhenSameValues() {
            // Given
            CrawlTaskQueryPort.TaskTypeCount count1 =
                    new CrawlTaskQueryPort.TaskTypeCount(50L, 40L, 10L);
            CrawlTaskQueryPort.TaskTypeCount count2 =
                    new CrawlTaskQueryPort.TaskTypeCount(50L, 40L, 10L);

            // Then
            assertThat(count1).isEqualTo(count2);
            assertThat(count1.hashCode()).isEqualTo(count2.hashCode());
        }

        @Test
        @DisplayName("[성공] 다른 값이면 equals() false")
        void shouldNotBeEqualWhenDifferentValues() {
            // Given
            CrawlTaskQueryPort.TaskTypeCount count1 =
                    new CrawlTaskQueryPort.TaskTypeCount(100L, 90L, 10L);
            CrawlTaskQueryPort.TaskTypeCount count2 =
                    new CrawlTaskQueryPort.TaskTypeCount(100L, 80L, 20L);

            // Then
            assertThat(count1).isNotEqualTo(count2);
        }

        @Test
        @DisplayName("[성공] toString() 정상 출력")
        void shouldHaveToString() {
            // Given
            CrawlTaskQueryPort.TaskTypeCount count =
                    new CrawlTaskQueryPort.TaskTypeCount(200L, 150L, 50L);

            // When
            String result = count.toString();

            // Then
            assertThat(result).contains("200");
            assertThat(result).contains("150");
            assertThat(result).contains("50");
        }
    }
}
