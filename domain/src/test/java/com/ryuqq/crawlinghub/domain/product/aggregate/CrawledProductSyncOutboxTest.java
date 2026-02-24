package com.ryuqq.crawlinghub.domain.product.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductSyncOutboxFixture;
import com.ryuqq.crawlinghub.domain.product.event.ExternalSyncRequestedEvent;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductSyncOutboxId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@DisplayName("CrawledProductSyncOutbox Aggregate 단위 테스트")
class CrawledProductSyncOutboxTest {

    private static final Instant NOW = FixedClock.aDefaultClock().instant();

    @Nested
    @DisplayName("forNew() 팩토리 메서드 테스트")
    class ForNewTest {

        @Test
        @DisplayName("CREATE 타입으로 신규 Outbox를 생성한다")
        void createNewOutboxForCreate() {
            // when
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutbox.forNew(
                            CrawledProductId.of(1L),
                            SellerId.of(100L),
                            12345L,
                            CrawledProductSyncOutbox.SyncType.CREATE,
                            null,
                            NOW);

            // then
            assertThat(outbox.getCrawledProductId().value()).isEqualTo(1L);
            assertThat(outbox.getSellerIdValue()).isEqualTo(100L);
            assertThat(outbox.getItemNo()).isEqualTo(12345L);
            assertThat(outbox.getSyncType()).isEqualTo(CrawledProductSyncOutbox.SyncType.CREATE);
            assertThat(outbox.getStatus()).isEqualTo(ProductOutboxStatus.PENDING);
            assertThat(outbox.getRetryCount()).isEqualTo(0);
            assertThat(outbox.getIdempotencyKey()).isNotBlank();
            assertThat(outbox.getCreatedAt()).isEqualTo(NOW);
            assertThat(outbox.getProcessedAt()).isNull();
        }

        @Test
        @DisplayName("UPDATE_PRICE 타입으로 신규 Outbox를 생성한다")
        void createNewOutboxForUpdatePrice() {
            // when
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutbox.forNew(
                            CrawledProductId.of(1L),
                            SellerId.of(100L),
                            12345L,
                            CrawledProductSyncOutbox.SyncType.UPDATE_PRICE,
                            99999L,
                            NOW);

            // then
            assertThat(outbox.getSyncType())
                    .isEqualTo(CrawledProductSyncOutbox.SyncType.UPDATE_PRICE);
            assertThat(outbox.getExternalProductId()).isEqualTo(99999L);
            assertThat(outbox.isPending()).isTrue();
        }

        @Test
        @DisplayName("UPDATE 타입에 externalProductId가 없으면 예외가 발생한다")
        void throwWhenUpdateWithoutExternalId() {
            assertThatThrownBy(
                            () ->
                                    CrawledProductSyncOutbox.forNew(
                                            CrawledProductId.of(1L),
                                            SellerId.of(100L),
                                            12345L,
                                            CrawledProductSyncOutbox.SyncType.UPDATE_PRICE,
                                            null,
                                            NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("externalProductId");
        }

        @Test
        @DisplayName("신규 생성 시 ID는 null이다 (Auto Increment)")
        void idIsNullForNew() {
            // when
            CrawledProductSyncOutbox outbox = CrawledProductSyncOutboxFixture.aPendingForCreate();

            // then
            assertThat(outbox.getId().isNew()).isTrue();
        }
    }

    @Nested
    @DisplayName("reconstitute() 팩토리 메서드 테스트")
    class ReconstituteTest {

        @Test
        @DisplayName("기존 데이터로 Outbox를 복원한다")
        void reconstituteOutbox() {
            // when
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();

            // then
            assertThat(outbox.getId()).isEqualTo(CrawledProductSyncOutboxId.of(1L));
            assertThat(outbox.getStatus()).isEqualTo(ProductOutboxStatus.PENDING);
            assertThat(outbox.getIdempotencyKey()).isEqualTo("sync-key-123");
        }
    }

    @Nested
    @DisplayName("상태 전환 메서드 테스트")
    class StateTransitionTest {

        @Test
        @DisplayName("markAsProcessing - PROCESSING 상태로 전환한다")
        void markAsProcessing() {
            // given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();

            // when
            outbox.markAsProcessing(NOW);

            // then
            assertThat(outbox.getStatus()).isEqualTo(ProductOutboxStatus.PROCESSING);
            assertThat(outbox.getProcessedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("markAsSent - SENT 상태로 전환한다")
        void markAsSent() {
            // given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();

            // when
            outbox.markAsSent(NOW);

            // then
            assertThat(outbox.isSent()).isTrue();
            assertThat(outbox.getErrorMessage()).isNull();
        }

        @Test
        @DisplayName("markAsCompleted - CREATE 타입은 externalProductId를 저장한다")
        void markAsCompletedForCreate() {
            // given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();

            // when
            outbox.markAsCompleted(77777L, NOW);

            // then
            assertThat(outbox.isCompleted()).isTrue();
            assertThat(outbox.getExternalProductId()).isEqualTo(77777L);
        }

        @Test
        @DisplayName("markAsCompleted - CREATE 타입에 externalProductId가 없으면 예외가 발생한다")
        void throwWhenCreateCompletedWithoutExternalId() {
            // given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();

            assertThatThrownBy(() -> outbox.markAsCompleted(null, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("externalProductId");
        }

        @Test
        @DisplayName("markAsFailed - FAILED 상태로 전환하고 retryCount를 증가시킨다")
        void markAsFailed() {
            // given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            int initialRetryCount = outbox.getRetryCount();

            // when
            outbox.markAsFailed("Connection error", NOW);

            // then
            assertThat(outbox.getStatus()).isEqualTo(ProductOutboxStatus.FAILED);
            assertThat(outbox.getRetryCount()).isEqualTo(initialRetryCount + 1);
            assertThat(outbox.getErrorMessage()).isEqualTo("Connection error");
        }

        @Test
        @DisplayName("resetToPending - FAILED 상태에서 재시도 가능하면 PENDING으로 복귀한다")
        void resetToPending() {
            // given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedFailed();
            assertThat(outbox.canRetry()).isTrue();

            // when
            outbox.resetToPending();

            // then
            assertThat(outbox.isPending()).isTrue();
            assertThat(outbox.getErrorMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("비즈니스 규칙 테스트")
    class BusinessRuleTest {

        @Test
        @DisplayName("canRetry - retryCount < 3이면 true")
        void canRetryWhenBelowLimit() {
            // given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedFailed();

            // then
            assertThat(outbox.canRetry()).isTrue();
        }

        @Test
        @DisplayName("isCreateRequest - CREATE 타입이면 true")
        void isCreateRequest() {
            // given
            CrawledProductSyncOutbox outbox = CrawledProductSyncOutboxFixture.aPendingForCreate();

            // then
            assertThat(outbox.isCreateRequest()).isTrue();
            assertThat(outbox.isUpdateRequest()).isFalse();
        }

        @Test
        @DisplayName("isUpdateRequest - UPDATE 타입이면 true")
        void isUpdateRequest() {
            // given
            CrawledProductSyncOutbox outbox = CrawledProductSyncOutboxFixture.aPendingForUpdate();

            // then
            assertThat(outbox.isUpdateRequest()).isTrue();
            assertThat(outbox.isCreateRequest()).isFalse();
        }
    }

    @Nested
    @DisplayName("createSyncRequestedEvent() 이벤트 생성 테스트")
    class CreateSyncEventTest {

        @Test
        @DisplayName("CREATE 타입에서 ExternalSyncRequestedEvent를 생성한다")
        void createSyncEventForCreate() {
            // given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();

            // when
            ExternalSyncRequestedEvent event = outbox.createSyncRequestedEvent(NOW);

            // then
            assertThat(event).isNotNull();
            assertThat(event.syncType()).isEqualTo(ExternalSyncRequestedEvent.SyncType.CREATE);
        }

        @Test
        @DisplayName("UPDATE_PRICE 타입에서 UPDATE 이벤트를 생성한다")
        void createSyncEventForUpdate() {
            // given
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutbox.reconstitute(
                            CrawledProductSyncOutboxId.of(1L),
                            CrawledProductId.of(1L),
                            SellerId.of(100L),
                            12345L,
                            CrawledProductSyncOutbox.SyncType.UPDATE_PRICE,
                            "key-abc",
                            99999L,
                            ProductOutboxStatus.PENDING,
                            0,
                            null,
                            NOW,
                            null);

            // when
            ExternalSyncRequestedEvent event = outbox.createSyncRequestedEvent(NOW);

            // then
            assertThat(event.syncType()).isEqualTo(ExternalSyncRequestedEvent.SyncType.UPDATE);
        }
    }

    @Nested
    @DisplayName("SyncType 내부 enum 테스트")
    class SyncTypeTest {

        @Test
        @DisplayName("CREATE는 isCreate()가 true이다")
        void createIsCreate() {
            assertThat(CrawledProductSyncOutbox.SyncType.CREATE.isCreate()).isTrue();
            assertThat(CrawledProductSyncOutbox.SyncType.CREATE.isUpdate()).isFalse();
        }

        @Test
        @DisplayName("UPDATE_PRICE는 isUpdate()가 true이다")
        void updateIsUpdate() {
            assertThat(CrawledProductSyncOutbox.SyncType.UPDATE_PRICE.isUpdate()).isTrue();
            assertThat(CrawledProductSyncOutbox.SyncType.UPDATE_PRICE.isCreate()).isFalse();
        }

        @Test
        @DisplayName("CREATE에서 toChangeType()은 예외가 발생한다")
        void createToChangeTypeThrows() {
            assertThatThrownBy(() -> CrawledProductSyncOutbox.SyncType.CREATE.toChangeType())
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("모든 UPDATE 타입은 toChangeType()으로 변환된다")
        void updateTypesConvertToChangeType() {
            assertThat(CrawledProductSyncOutbox.SyncType.UPDATE_PRICE.toChangeType()).isNotNull();
            assertThat(CrawledProductSyncOutbox.SyncType.UPDATE_IMAGE.toChangeType()).isNotNull();
            assertThat(CrawledProductSyncOutbox.SyncType.UPDATE_DESCRIPTION.toChangeType())
                    .isNotNull();
            assertThat(CrawledProductSyncOutbox.SyncType.UPDATE_OPTION_STOCK.toChangeType())
                    .isNotNull();
            assertThat(CrawledProductSyncOutbox.SyncType.UPDATE_PRODUCT_INFO.toChangeType())
                    .isNotNull();
        }
    }

    @Nested
    @DisplayName("generateIdempotencyKey() 테스트")
    class GenerateIdempotencyKeyTest {

        @Test
        @DisplayName("UUID 형태의 멱등성 키를 생성한다")
        void generateUuidKey() {
            // when
            String key =
                    CrawledProductSyncOutbox.generateIdempotencyKey(
                            CrawledProductId.of(1L), CrawledProductSyncOutbox.SyncType.CREATE);

            // then
            assertThat(key).isNotBlank();
            assertThat(key).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }
    }
}
