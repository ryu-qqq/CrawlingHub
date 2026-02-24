package com.ryuqq.crawlinghub.application.task.dto.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CrawlTaskPayload 단위 테스트
 *
 * <p>SQS 메시지 페이로드 생성 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlTaskPayload 테스트")
class CrawlTaskPayloadTest {

    @Nested
    @DisplayName("from() 팩토리 메서드 테스트")
    class From {

        @Test
        @DisplayName("[성공] CrawlTask로부터 페이로드 생성")
        void shouldCreatePayloadFromCrawlTask() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();

            // When
            CrawlTaskPayload payload = CrawlTaskPayload.from(task);

            // Then
            assertThat(payload).isNotNull();
            assertThat(payload.schedulerId()).isEqualTo(task.getCrawlSchedulerIdValue());
            assertThat(payload.sellerId()).isEqualTo(task.getSellerIdValue());
            assertThat(payload.taskType()).isEqualTo(task.getTaskType().name());
        }

        @Test
        @DisplayName("[성공] 생성된 페이로드의 endpoint는 null이 아님")
        void shouldHaveNonNullEndpoint() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();

            // When
            CrawlTaskPayload payload = CrawlTaskPayload.from(task);

            // Then
            assertThat(payload.endpoint()).isNotNull();
        }
    }

    @Nested
    @DisplayName("레코드 필드 테스트")
    class RecordFields {

        @Test
        @DisplayName("[성공] 직접 생성한 페이로드의 필드 확인")
        void shouldAccessAllFields() {
            // Given
            CrawlTaskPayload payload =
                    new CrawlTaskPayload(1L, 2L, 3L, "MINI_SHOP", "https://example.com");

            // Then
            assertThat(payload.taskId()).isEqualTo(1L);
            assertThat(payload.schedulerId()).isEqualTo(2L);
            assertThat(payload.sellerId()).isEqualTo(3L);
            assertThat(payload.taskType()).isEqualTo("MINI_SHOP");
            assertThat(payload.endpoint()).isEqualTo("https://example.com");
        }
    }
}
