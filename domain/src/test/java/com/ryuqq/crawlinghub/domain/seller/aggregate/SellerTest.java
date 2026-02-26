package com.ryuqq.crawlinghub.domain.seller.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.seller.MustItSellerNameFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerNameFixture;
import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import com.ryuqq.crawlinghub.domain.seller.event.SellerDeActiveEvent;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerUpdateData;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Seller Aggregate Root 단위 테스트
 *
 * <p>Kent Beck TDD 스타일 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("Seller Aggregate 테스트")
class SellerTest {

    private static final Instant FIXED_INSTANT = FixedClock.aDefaultClock().instant();

    @Nested
    @DisplayName("forNew() - 신규 생성")
    class ForNew {

        @Test
        @DisplayName("[성공] 신규 Seller 생성 시 ACTIVE 상태로 생성됨")
        void shouldCreateWithActiveStatus() {
            // when
            Seller seller =
                    Seller.forNew(
                            MustItSellerNameFixture.aDefaultName(),
                            SellerNameFixture.aDefaultName(),
                            FIXED_INSTANT);

            // then
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(seller.isActive()).isTrue();
        }

        @Test
        @DisplayName("[성공] 신규 Seller 생성 시 productCount는 0")
        void shouldCreateWithZeroProductCount() {
            // when
            Seller seller =
                    Seller.forNew(
                            MustItSellerNameFixture.aDefaultName(),
                            SellerNameFixture.aDefaultName(),
                            FIXED_INSTANT);

            // then
            assertThat(seller.getProductCount()).isZero();
        }

        @Test
        @DisplayName("[성공] 신규 Seller 생성 시 ID는 null (Auto Increment)")
        void shouldCreateWithNullId() {
            // when
            Seller seller =
                    Seller.forNew(
                            MustItSellerNameFixture.aDefaultName(),
                            SellerNameFixture.aDefaultName(),
                            FIXED_INSTANT);

            // then
            assertThat(seller.getSellerId()).isNull();
            assertThat(seller.getSellerIdValue()).isNull();
        }

        @Test
        @DisplayName("[성공] 신규 Seller 생성 시 createdAt/updatedAt 설정됨")
        void shouldSetTimestamps() {
            // when
            Seller seller =
                    Seller.forNew(
                            MustItSellerNameFixture.aDefaultName(),
                            SellerNameFixture.aDefaultName(),
                            FIXED_INSTANT);

            // then
            assertThat(seller.getCreatedAt()).isEqualTo(FIXED_INSTANT);
            assertThat(seller.getUpdatedAt()).isEqualTo(FIXED_INSTANT);
        }
    }

    @Nested
    @DisplayName("of() - ID 기반 생성")
    class Of {

        @Test
        @DisplayName("[실패] sellerId가 null이면 예외 발생")
        void shouldThrowWhenSellerIdIsNull() {
            // when & then
            assertThatThrownBy(
                            () ->
                                    Seller.of(
                                            null,
                                            MustItSellerNameFixture.aDefaultName(),
                                            SellerNameFixture.aDefaultName(),
                                            null,
                                            SellerStatus.ACTIVE,
                                            0,
                                            FIXED_INSTANT,
                                            FIXED_INSTANT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sellerId는 null일 수 없습니다");
        }

        @Test
        @DisplayName("[성공] 유효한 파라미터로 Seller 생성")
        void shouldCreateWithValidParameters() {
            // given
            SellerId sellerId = SellerId.of(1L);

            // when
            Seller seller =
                    Seller.of(
                            sellerId,
                            MustItSellerNameFixture.aDefaultName(),
                            SellerNameFixture.aDefaultName(),
                            null,
                            SellerStatus.ACTIVE,
                            10,
                            FIXED_INSTANT,
                            FIXED_INSTANT);

            // then
            assertThat(seller.getSellerId()).isEqualTo(sellerId);
            assertThat(seller.getSellerIdValue()).isEqualTo(1L);
            assertThat(seller.getProductCount()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("reconstitute() - 영속성 복원")
    class Reconstitute {

        @Test
        @DisplayName("[성공] 모든 필드가 정상 복원됨")
        void shouldRestoreAllFields() {
            // given
            SellerId sellerId = SellerId.of(99L);
            MustItSellerName mustItSellerName = MustItSellerName.of("mustit-seller");
            SellerName sellerName = SellerName.of("commerce-seller");
            Instant createdAt = Instant.parse("2025-01-01T00:00:00Z");
            Instant updatedAt = Instant.parse("2024-01-01T12:00:00Z");

            // when
            Seller seller =
                    Seller.reconstitute(
                            sellerId,
                            mustItSellerName,
                            sellerName,
                            null,
                            SellerStatus.INACTIVE,
                            50,
                            createdAt,
                            updatedAt);

            // then
            assertThat(seller.getSellerId()).isEqualTo(sellerId);
            assertThat(seller.getMustItSellerName()).isEqualTo(mustItSellerName);
            assertThat(seller.getSellerName()).isEqualTo(sellerName);
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.INACTIVE);
            assertThat(seller.getProductCount()).isEqualTo(50);
            assertThat(seller.getCreatedAt()).isEqualTo(createdAt);
            assertThat(seller.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    @DisplayName("activate() - 활성화")
    class Activate {

        @Test
        @DisplayName("[성공] INACTIVE → ACTIVE 전환")
        void shouldActivateFromInactive() {
            // given
            Seller seller = SellerFixture.anInactiveSeller(FIXED_INSTANT);
            assertThat(seller.isInactive()).isTrue();

            // when
            seller.activate(FIXED_INSTANT);

            // then
            assertThat(seller.isActive()).isTrue();
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
        }

        @Test
        @DisplayName("[성공] 이미 ACTIVE면 무시됨")
        void shouldIgnoreWhenAlreadyActive() {
            // given
            Seller seller = SellerFixture.anActiveSeller(FIXED_INSTANT);
            Instant originalUpdatedAt = seller.getUpdatedAt();

            // when
            seller.activate(FIXED_INSTANT);

            // then
            assertThat(seller.isActive()).isTrue();
            assertThat(seller.getUpdatedAt()).isEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("[성공] 활성화 시 updatedAt 갱신됨")
        void shouldUpdateTimestampOnActivation() {
            // given
            Instant initialTime = Instant.parse("2025-01-01T00:00:00Z");
            Instant activationTime = Instant.parse("2025-11-27T12:00:00Z");

            Seller seller =
                    Seller.of(
                            SellerId.of(1L),
                            MustItSellerNameFixture.aDefaultName(),
                            SellerNameFixture.aDefaultName(),
                            null,
                            SellerStatus.INACTIVE,
                            0,
                            initialTime,
                            initialTime);

            // when
            seller.activate(activationTime);

            // then
            assertThat(seller.getUpdatedAt()).isEqualTo(activationTime);
        }
    }

    @Nested
    @DisplayName("deactivate() - 비활성화")
    class Deactivate {

        @Test
        @DisplayName("[성공] ACTIVE → INACTIVE 전환")
        void shouldDeactivateFromActive() {
            // given
            Seller seller = SellerFixture.anActiveSeller(FIXED_INSTANT);
            assertThat(seller.isActive()).isTrue();

            // when
            seller.deactivate(FIXED_INSTANT);

            // then
            assertThat(seller.isInactive()).isTrue();
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.INACTIVE);
        }

        @Test
        @DisplayName("[성공] 비활성화 시 SellerDeActiveEvent 발행")
        void shouldPublishDeActiveEvent() {
            // given
            Seller seller = SellerFixture.anActiveSeller(FIXED_INSTANT);

            // when
            seller.deactivate(FIXED_INSTANT);

            // then
            List<DomainEvent> events = seller.pollEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(SellerDeActiveEvent.class);

            SellerDeActiveEvent event = (SellerDeActiveEvent) events.get(0);
            assertThat(event.sellerId()).isEqualTo(seller.getSellerId());
        }

        @Test
        @DisplayName("[성공] 이미 INACTIVE면 무시됨 (이벤트 발행 안 함)")
        void shouldIgnoreWhenAlreadyInactive() {
            // given
            Seller seller = SellerFixture.anInactiveSeller(FIXED_INSTANT);

            // when
            seller.deactivate(FIXED_INSTANT);

            // then
            assertThat(seller.isInactive()).isTrue();
            assertThat(seller.pollEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("update() - SellerUpdateData 기반 수정")
    class Update {

        @Test
        @DisplayName("[성공] 모든 필드 변경")
        void shouldUpdateAllFields() {
            // given
            Seller seller = SellerFixture.anActiveSeller(FIXED_INSTANT);
            MustItSellerName newMustItName = MustItSellerName.of("new-mustit-name");
            SellerName newSellerName = SellerName.of("new-seller-name");
            SellerUpdateData updateData =
                    SellerUpdateData.of(newMustItName, newSellerName, SellerStatus.ACTIVE);

            // when
            seller.update(updateData, FIXED_INSTANT);

            // then
            assertThat(seller.getMustItSellerName()).isEqualTo(newMustItName);
            assertThat(seller.getSellerName()).isEqualTo(newSellerName);
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
        }

        @Test
        @DisplayName("[성공] 상태 ACTIVE → INACTIVE 변경 시 이벤트 발행")
        void shouldPublishEventWhenDeactivating() {
            // given
            Seller seller = SellerFixture.anActiveSeller(FIXED_INSTANT);
            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            seller.getMustItSellerName(),
                            seller.getSellerName(),
                            SellerStatus.INACTIVE);

            // when
            seller.update(updateData, FIXED_INSTANT);

            // then
            assertThat(seller.isInactive()).isTrue();
            List<DomainEvent> events = seller.pollEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(SellerDeActiveEvent.class);
        }

        @Test
        @DisplayName("[성공] 상태 INACTIVE → ACTIVE 변경 시 이벤트 발행 안 함")
        void shouldNotPublishEventWhenActivating() {
            // given
            Seller seller = SellerFixture.anInactiveSeller(FIXED_INSTANT);
            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            seller.getMustItSellerName(),
                            seller.getSellerName(),
                            SellerStatus.ACTIVE);

            // when
            seller.update(updateData, FIXED_INSTANT);

            // then
            assertThat(seller.isActive()).isTrue();
            assertThat(seller.pollEvents()).isEmpty();
        }

        @Test
        @DisplayName("[성공] 동일한 상태 유지 시 이벤트 발행 안 함")
        void shouldNotPublishEventWhenSameStatus() {
            // given
            Seller seller = SellerFixture.anActiveSeller(FIXED_INSTANT);
            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            seller.getMustItSellerName(),
                            seller.getSellerName(),
                            SellerStatus.ACTIVE);

            // when
            seller.update(updateData, FIXED_INSTANT);

            // then
            assertThat(seller.pollEvents()).isEmpty();
        }

        @Test
        @DisplayName("[성공] update 시 updatedAt 갱신됨")
        void shouldUpdateTimestamp() {
            // given
            Instant initialTime = Instant.parse("2025-01-01T00:00:00Z");
            Instant updateTime = Instant.parse("2025-11-27T12:00:00Z");

            Seller seller =
                    Seller.of(
                            SellerId.of(1L),
                            MustItSellerNameFixture.aDefaultName(),
                            SellerNameFixture.aDefaultName(),
                            null,
                            SellerStatus.ACTIVE,
                            0,
                            initialTime,
                            initialTime);

            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            MustItSellerName.of("updated-mustit"),
                            SellerName.of("updated-seller"),
                            SellerStatus.ACTIVE);

            // when
            seller.update(updateData, updateTime);

            // then
            assertThat(seller.getUpdatedAt()).isEqualTo(updateTime);
        }
    }

    @Nested
    @DisplayName("updateProductCount() - 상품 수 업데이트")
    class UpdateProductCount {

        @Test
        @DisplayName("[성공] 상품 수 업데이트")
        void shouldUpdateProductCount() {
            // given
            Seller seller = SellerFixture.anActiveSeller(FIXED_INSTANT);

            // when
            seller.updateProductCount(100, FIXED_INSTANT);

            // then
            assertThat(seller.getProductCount()).isEqualTo(100);
        }

        @Test
        @DisplayName("[성공] 상품 수 변경 시 updatedAt 갱신")
        void shouldUpdateTimestamp() {
            // given
            Instant initialTime = Instant.parse("2025-01-01T00:00:00Z");
            Instant updateTime = Instant.parse("2025-11-27T12:00:00Z");

            Seller seller =
                    Seller.of(
                            SellerId.of(1L),
                            MustItSellerNameFixture.aDefaultName(),
                            SellerNameFixture.aDefaultName(),
                            null,
                            SellerStatus.ACTIVE,
                            0,
                            initialTime,
                            initialTime);

            // when
            seller.updateProductCount(50, updateTime);

            // then
            assertThat(seller.getUpdatedAt()).isEqualTo(updateTime);
        }

        @Test
        @DisplayName("[성공] 동일한 상품 수로 업데이트 시 updatedAt 변경 없음")
        void shouldNotUpdateTimestampWhenSameCount() {
            // given
            Seller seller = SellerFixture.anActiveSellerWithProducts(100, FIXED_INSTANT);
            Instant originalUpdatedAt = seller.getUpdatedAt();

            // when
            seller.updateProductCount(100, FIXED_INSTANT);

            // then
            assertThat(seller.getUpdatedAt()).isEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("[실패] 음수 상품 수 업데이트 시 예외 발생")
        void shouldThrowWhenNegativeCount() {
            // given
            Seller seller = SellerFixture.anActiveSeller(FIXED_INSTANT);

            // when & then
            assertThatThrownBy(() -> seller.updateProductCount(-1, FIXED_INSTANT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("상품 수는 0 이상");
        }

        @Test
        @DisplayName("[성공] 0으로 업데이트 가능")
        void shouldAllowZeroCount() {
            // given
            Seller seller = SellerFixture.anActiveSellerWithProducts(100, FIXED_INSTANT);

            // when
            seller.updateProductCount(0, FIXED_INSTANT);

            // then
            assertThat(seller.getProductCount()).isZero();
        }
    }

    @Nested
    @DisplayName("pollEvents() - 이벤트 폴링")
    class PollEvents {

        @Test
        @DisplayName("[성공] 이벤트 폴링 시 이벤트 목록 반환 후 내부 목록 비워짐")
        void shouldReturnEventsAndClearInternalList() {
            // given
            Seller seller = SellerFixture.anActiveSeller(FIXED_INSTANT);
            seller.deactivate(FIXED_INSTANT); // 이벤트 발행

            // when
            List<DomainEvent> events = seller.pollEvents();

            // then
            assertThat(events).hasSize(1);
            assertThat(seller.pollEvents()).isEmpty(); // 두 번째 호출은 비어있음
        }

        @Test
        @DisplayName("[성공] 이벤트가 없어도 예외 없이 빈 목록 반환")
        void shouldReturnEmptyListWhenNoEvents() {
            // given
            Seller seller = SellerFixture.anActiveSeller(FIXED_INSTANT);

            // when
            List<DomainEvent> events = seller.pollEvents();

            // then
            assertThat(events).isEmpty();
        }
    }

    @Nested
    @DisplayName("isBeingDeactivatedBy() - 비활성화 전환 판단")
    class IsBeingDeactivatedBy {

        @Test
        @DisplayName("[성공] ACTIVE → INACTIVE 전환이면 true")
        void shouldReturnTrueWhenActiveToInactive() {
            // given
            Seller seller = SellerFixture.anActiveSeller(FIXED_INSTANT);
            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            seller.getMustItSellerName(),
                            seller.getSellerName(),
                            SellerStatus.INACTIVE);

            // when & then
            assertThat(seller.isBeingDeactivatedBy(updateData)).isTrue();
        }

        @Test
        @DisplayName("[성공] ACTIVE → ACTIVE 유지면 false")
        void shouldReturnFalseWhenActiveToActive() {
            // given
            Seller seller = SellerFixture.anActiveSeller(FIXED_INSTANT);
            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            seller.getMustItSellerName(),
                            seller.getSellerName(),
                            SellerStatus.ACTIVE);

            // when & then
            assertThat(seller.isBeingDeactivatedBy(updateData)).isFalse();
        }

        @Test
        @DisplayName("[성공] INACTIVE → INACTIVE 유지면 false")
        void shouldReturnFalseWhenInactiveToInactive() {
            // given
            Seller seller = SellerFixture.anInactiveSeller();
            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            seller.getMustItSellerName(),
                            seller.getSellerName(),
                            SellerStatus.INACTIVE);

            // when & then
            assertThat(seller.isBeingDeactivatedBy(updateData)).isFalse();
        }

        @Test
        @DisplayName("[성공] INACTIVE → ACTIVE 전환이면 false")
        void shouldReturnFalseWhenInactiveToActive() {
            // given
            Seller seller = SellerFixture.anInactiveSeller();
            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            seller.getMustItSellerName(),
                            seller.getSellerName(),
                            SellerStatus.ACTIVE);

            // when & then
            assertThat(seller.isBeingDeactivatedBy(updateData)).isFalse();
        }
    }

    @Nested
    @DisplayName("Getter - Law of Demeter 지원 메서드")
    class GetterMethods {

        @Test
        @DisplayName("[성공] getMustItSellerNameValue() 원시값 반환")
        void shouldReturnMustItSellerNameValue() {
            // given
            Seller seller = SellerFixture.aNewActiveSeller("mustit-test", "seller-test");

            // when & then
            assertThat(seller.getMustItSellerNameValue()).isEqualTo("mustit-test");
        }

        @Test
        @DisplayName("[성공] getSellerNameValue() 원시값 반환")
        void shouldReturnSellerNameValue() {
            // given
            Seller seller = SellerFixture.aNewActiveSeller("mustit-test", "seller-test");

            // when & then
            assertThat(seller.getSellerNameValue()).isEqualTo("seller-test");
        }

        @Test
        @DisplayName("[성공] getSellerIdValue() - ID가 있으면 원시값 반환")
        void shouldReturnSellerIdValue() {
            // given
            Seller seller = SellerFixture.anActiveSeller(99L);

            // when & then
            assertThat(seller.getSellerIdValue()).isEqualTo(99L);
        }

        @Test
        @DisplayName("[성공] getSellerIdValue() - ID가 없으면 null 반환")
        void shouldReturnNullWhenNoSellerId() {
            // given
            Seller seller = SellerFixture.aNewActiveSeller();

            // when & then
            assertThat(seller.getSellerIdValue()).isNull();
        }
    }
}
