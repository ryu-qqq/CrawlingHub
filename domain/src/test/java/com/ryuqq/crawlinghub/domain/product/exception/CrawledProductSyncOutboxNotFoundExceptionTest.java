package com.ryuqq.crawlinghub.domain.product.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("exception")
@DisplayName("CrawledProductSyncOutboxNotFoundException 단위 테스트")
class CrawledProductSyncOutboxNotFoundExceptionTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("outboxId로 예외를 생성한다")
        void createWithOutboxId() {
            // given
            long outboxId = 42L;

            // when
            CrawledProductSyncOutboxNotFoundException exception =
                    new CrawledProductSyncOutboxNotFoundException(outboxId);

            // then
            assertThat(exception).isNotNull();
            assertThat(exception).isInstanceOf(CrawledProductException.class);
        }

        @Test
        @DisplayName("메시지에 outboxId가 포함된다")
        void messageContainsOutboxId() {
            // when
            CrawledProductSyncOutboxNotFoundException exception =
                    new CrawledProductSyncOutboxNotFoundException(99L);

            // then
            assertThat(exception.getMessage()).contains("99");
        }
    }

    @Nested
    @DisplayName("ErrorCode 테스트")
    class ErrorCodeTest {

        @Test
        @DisplayName("에러 코드는 SYNC_OUTBOX_NOT_FOUND이다")
        void errorCodeIsSyncOutboxNotFound() {
            // when
            CrawledProductSyncOutboxNotFoundException exception =
                    new CrawledProductSyncOutboxNotFoundException(1L);

            // then
            assertThat(exception.getErrorCode())
                    .isEqualTo(CrawledProductErrorCode.SYNC_OUTBOX_NOT_FOUND);
        }

        @Test
        @DisplayName("에러 코드의 HTTP 상태는 404이다")
        void errorCodeHttpStatusIs404() {
            // when
            CrawledProductSyncOutboxNotFoundException exception =
                    new CrawledProductSyncOutboxNotFoundException(1L);

            // then
            assertThat(exception.httpStatus()).isEqualTo(404);
        }

        @Test
        @DisplayName("에러 코드는 OUTBOX-001이다")
        void errorCodeIsOutbox001() {
            // when
            CrawledProductSyncOutboxNotFoundException exception =
                    new CrawledProductSyncOutboxNotFoundException(1L);

            // then
            assertThat(exception.code()).isEqualTo("OUTBOX-001");
        }
    }
}
