package com.ryuqq.crawlinghub.adapter.in.sqs.task;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.messaging.CrawlTaskPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawlTaskListenerMapper 단위 테스트
 *
 * <p>CrawlTaskPayload → ExecuteCrawlTaskCommand 변환을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@DisplayName("CrawlTaskListenerMapper 단위 테스트")
class CrawlTaskListenerMapperTest {

    private CrawlTaskListenerMapper sut;

    @BeforeEach
    void setUp() {
        sut = new CrawlTaskListenerMapper();
    }

    @Nested
    @DisplayName("toCommand 메서드 테스트")
    class ToCommandTest {

        @Test
        @DisplayName("[성공] CrawlTaskPayload의 모든 필드가 ExecuteCrawlTaskCommand에 올바르게 매핑된다")
        void shouldMapAllFieldsCorrectly() {
            // Given
            CrawlTaskPayload payload =
                    new CrawlTaskPayload(
                            1L, // taskId
                            10L, // schedulerId
                            100L, // sellerId
                            "PRODUCT", // taskType
                            "https://example.com/products" // endpoint
                            );

            // When
            ExecuteCrawlTaskCommand command = sut.toCommand(payload);

            // Then
            assertThat(command.taskId()).isEqualTo(1L);
            assertThat(command.schedulerId()).isEqualTo(10L);
            assertThat(command.sellerId()).isEqualTo(100L);
            assertThat(command.taskType()).isEqualTo("PRODUCT");
            assertThat(command.endpoint()).isEqualTo("https://example.com/products");
        }

        @Test
        @DisplayName("[성공] CATEGORY 타입 페이로드도 올바르게 매핑된다")
        void shouldMapCategoryTypePayload() {
            // Given
            CrawlTaskPayload payload =
                    new CrawlTaskPayload(
                            2L, 20L, 200L, "CATEGORY", "https://example.com/categories");

            // When
            ExecuteCrawlTaskCommand command = sut.toCommand(payload);

            // Then
            assertThat(command.taskId()).isEqualTo(2L);
            assertThat(command.taskType()).isEqualTo("CATEGORY");
            assertThat(command.endpoint()).isEqualTo("https://example.com/categories");
        }

        @Test
        @DisplayName("[성공] 동일 페이로드로 여러 번 변환해도 일관된 결과를 반환한다")
        void shouldReturnConsistentResultsForSamePayload() {
            // Given
            CrawlTaskPayload payload =
                    new CrawlTaskPayload(3L, 30L, 300L, "PRODUCT", "https://shop.com/items");

            // When
            ExecuteCrawlTaskCommand command1 = sut.toCommand(payload);
            ExecuteCrawlTaskCommand command2 = sut.toCommand(payload);

            // Then
            assertThat(command1.taskId()).isEqualTo(command2.taskId());
            assertThat(command1.schedulerId()).isEqualTo(command2.schedulerId());
            assertThat(command1.sellerId()).isEqualTo(command2.sellerId());
            assertThat(command1.taskType()).isEqualTo(command2.taskType());
            assertThat(command1.endpoint()).isEqualTo(command2.endpoint());
        }
    }
}
