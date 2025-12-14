package com.ryuqq.crawlinghub.domain.seller.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.seller.MustItSellerNameFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerNameFixture;
import com.ryuqq.crawlinghub.domain.seller.event.SellerDeActiveEvent;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.Instant;
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

    @Nested
    @DisplayName("forNew() - 신규 생성")
    class ForNew {

        @Test
        @DisplayName("[성공] 신규 Seller 생성 시 ACTIVE 상태로 생성됨")
        void shouldCreateWithActiveStatus() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();

            // when
            Seller seller =
                    Seller.forNew(
                            MustItSellerNameFixture.aDefaultName(),
                            SellerNameFixture.aDefaultName(),
                            clock);

            // then
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(seller.isActive()).isTrue();
        }

        @Test
        @DisplayName("[성공] 신규 Seller 생성 시 productCount는 0")
        void shouldCreateWithZeroProductCount() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();

            // when
            Seller seller =
                    Seller.forNew(
                            MustItSellerNameFixture.aDefaultName(),
                            SellerNameFixture.aDefaultName(),
                            clock);

            // then
            assertThat(seller.getProductCount()).isZero();
        }

        @Test
        @DisplayName("[성공] 신규 Seller 생성 시 ID는 null (Auto Increment)")
        void shouldCreateWithNullId() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();

            // when
            Seller seller =
                    Seller.forNew(
                            MustItSellerNameFixture.aDefaultName(),
                            SellerNameFixture.aDefaultName(),
                            clock);

            // then
            assertThat(seller.getSellerId()).isNull();
            assertThat(seller.getSellerIdValue()).isNull();
        }

        @Test
        @DisplayName("[성공] 신규 Seller 생성 시 createdAt/updatedAt 설정됨")
        void shouldSetTimestamps() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Instant expectedTime = clock.instant();

            // when
            Seller seller =
                    Seller.forNew(
                            MustItSellerNameFixture.aDefaultName(),
                            SellerNameFixture.aDefaultName(),
                            clock);

            // then
            assertThat(seller.getCreatedAt()).isEqualTo(expectedTime);
            assertThat(seller.getUpdatedAt()).isEqualTo(expectedTime);
        }
    }

    @Nested
    @DisplayName("of() - ID 기반 생성")
    class Of {

        @Test
        @DisplayName("[실패] sellerId가 null이면 예외 발생")
        void shouldThrowWhenSellerIdIsNull() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Instant now = clock.instant();

            // when & then
            assertThatThrownBy(
                            () ->
                                    Seller.of(
                                            null,
                                            MustItSellerNameFixture.aDefaultName(),
                                            SellerNameFixture.aDefaultName(),
                                            SellerStatus.ACTIVE,
                                            0,
                                            now,
                                            now))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sellerId는 null일 수 없습니다");
        }

        @Test
        @DisplayName("[성공] 유효한 파라미터로 Seller 생성")
        void shouldCreateWithValidParameters() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            SellerId sellerId = SellerId.of(1L);
            Instant now = clock.instant();

            // when
            Seller seller =
                    Seller.of(
                            sellerId,
                            MustItSellerNameFixture.aDefaultName(),
                            SellerNameFixture.aDefaultName(),
                            SellerStatus.ACTIVE,
                            10,
                            now,
                            now);

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
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anInactiveSeller(clock);
            assertThat(seller.isInactive()).isTrue();

            // when
            seller.activate(clock);

            // then
            assertThat(seller.isActive()).isTrue();
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
        }

        @Test
        @DisplayName("[성공] 이미 ACTIVE면 무시됨")
        void shouldIgnoreWhenAlreadyActive() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSeller(clock);
            Instant originalUpdatedAt = seller.getUpdatedAt();

            // when
            seller.activate(clock);

            // then
            assertThat(seller.isActive()).isTrue();
            assertThat(seller.getUpdatedAt()).isEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("[성공] 활성화 시 updatedAt 갱신됨")
        void shouldUpdateTimestampOnActivation() {
            // given
            FixedClock initialClock = FixedClock.at("2025-01-01T00:00:00Z");
            Instant initialTime = initialClock.instant();
            FixedClock activationClock = FixedClock.at("2025-11-27T12:00:00Z");

            Seller seller =
                    Seller.of(
                            SellerId.of(1L),
                            MustItSellerNameFixture.aDefaultName(),
                            SellerNameFixture.aDefaultName(),
                            SellerStatus.INACTIVE,
                            0,
                            initialTime,
                            initialTime);

            // when
            seller.activate(activationClock);

            // then
            Instant expectedUpdatedAt = activationClock.instant();
            assertThat(seller.getUpdatedAt()).isEqualTo(expectedUpdatedAt);
        }
    }

    @Nested
    @DisplayName("deactivate() - 비활성화")
    class Deactivate {

        @Test
        @DisplayName("[성공] ACTIVE → INACTIVE 전환")
        void shouldDeactivateFromActive() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSeller(clock);
            assertThat(seller.isActive()).isTrue();

            // when
            seller.deactivate(clock);

            // then
            assertThat(seller.isInactive()).isTrue();
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.INACTIVE);
        }

        @Test
        @DisplayName("[성공] 비활성화 시 SellerDeActiveEvent 발행")
        void shouldPublishDeActiveEvent() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSeller(clock);

            // when
            seller.deactivate(clock);

            // then
            assertThat(seller.getDomainEvents()).hasSize(1);
            assertThat(seller.getDomainEvents().get(0)).isInstanceOf(SellerDeActiveEvent.class);

            SellerDeActiveEvent event = (SellerDeActiveEvent) seller.getDomainEvents().get(0);
            assertThat(event.sellerId()).isEqualTo(seller.getSellerId());
        }

        @Test
        @DisplayName("[성공] 이미 INACTIVE면 무시됨 (이벤트 발행 안 함)")
        void shouldIgnoreWhenAlreadyInactive() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anInactiveSeller(clock);

            // when
            seller.deactivate(clock);

            // then
            assertThat(seller.isInactive()).isTrue();
            assertThat(seller.getDomainEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("update() - 통합 수정")
    class Update {

        @Test
        @DisplayName("[성공] MustItSellerName 변경")
        void shouldUpdateMustItSellerName() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSeller(clock);
            MustItSellerName newName = MustItSellerName.of("new-mustit-name");

            // when
            seller.update(newName, null, null, clock);

            // then
            assertThat(seller.getMustItSellerName()).isEqualTo(newName);
        }

        @Test
        @DisplayName("[성공] SellerName 변경")
        void shouldUpdateSellerName() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSeller(clock);
            SellerName newName = SellerName.of("new-seller-name");

            // when
            seller.update(null, newName, null, clock);

            // then
            assertThat(seller.getSellerName()).isEqualTo(newName);
        }

        @Test
        @DisplayName("[성공] 상태 ACTIVE → INACTIVE 변경 시 이벤트 발행")
        void shouldPublishEventWhenDeactivating() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSeller(clock);

            // when
            seller.update(null, null, SellerStatus.INACTIVE, clock);

            // then
            assertThat(seller.isInactive()).isTrue();
            assertThat(seller.getDomainEvents()).hasSize(1);
            assertThat(seller.getDomainEvents().get(0)).isInstanceOf(SellerDeActiveEvent.class);
        }

        @Test
        @DisplayName("[성공] 상태 INACTIVE → ACTIVE 변경")
        void shouldActivateWhenStatusChangedToActive() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anInactiveSeller(clock);

            // when
            seller.update(null, null, SellerStatus.ACTIVE, clock);

            // then
            assertThat(seller.isActive()).isTrue();
        }

        @Test
        @DisplayName("[성공] 동일한 값으로 update 시 변경 없음")
        void shouldNotUpdateWhenSameValue() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSeller(clock);
            MustItSellerName sameMustItName = seller.getMustItSellerName();
            SellerName sameName = seller.getSellerName();
            Instant originalUpdatedAt = seller.getUpdatedAt();

            // when
            seller.update(sameMustItName, sameName, SellerStatus.ACTIVE, clock);

            // then
            assertThat(seller.getUpdatedAt()).isEqualTo(originalUpdatedAt);
            assertThat(seller.getDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("[성공] null 파라미터는 해당 필드를 변경하지 않음")
        void shouldNotChangeWhenParameterIsNull() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSeller(clock);
            MustItSellerName originalMustItName = seller.getMustItSellerName();
            SellerName originalName = seller.getSellerName();
            SellerStatus originalStatus = seller.getStatus();

            // when
            seller.update(null, null, null, clock);

            // then
            assertThat(seller.getMustItSellerName()).isEqualTo(originalMustItName);
            assertThat(seller.getSellerName()).isEqualTo(originalName);
            assertThat(seller.getStatus()).isEqualTo(originalStatus);
        }
    }

    @Nested
    @DisplayName("updateProductCount() - 상품 수 업데이트")
    class UpdateProductCount {

        @Test
        @DisplayName("[성공] 상품 수 업데이트")
        void shouldUpdateProductCount() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSeller(clock);

            // when
            seller.updateProductCount(100, clock);

            // then
            assertThat(seller.getProductCount()).isEqualTo(100);
        }

        @Test
        @DisplayName("[성공] 상품 수 변경 시 updatedAt 갱신")
        void shouldUpdateTimestamp() {
            // given
            FixedClock initialClock = FixedClock.at("2025-01-01T00:00:00Z");
            Instant initialTime = initialClock.instant();
            FixedClock updateClock = FixedClock.at("2025-11-27T12:00:00Z");

            Seller seller =
                    Seller.of(
                            SellerId.of(1L),
                            MustItSellerNameFixture.aDefaultName(),
                            SellerNameFixture.aDefaultName(),
                            SellerStatus.ACTIVE,
                            0,
                            initialTime,
                            initialTime);

            // when
            seller.updateProductCount(50, updateClock);

            // then
            Instant expectedUpdatedAt = updateClock.instant();
            assertThat(seller.getUpdatedAt()).isEqualTo(expectedUpdatedAt);
        }

        @Test
        @DisplayName("[성공] 동일한 상품 수로 업데이트 시 updatedAt 변경 없음")
        void shouldNotUpdateTimestampWhenSameCount() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSellerWithProducts(100, clock);
            Instant originalUpdatedAt = seller.getUpdatedAt();

            // when
            seller.updateProductCount(100, clock);

            // then
            assertThat(seller.getUpdatedAt()).isEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("[실패] 음수 상품 수 업데이트 시 예외 발생")
        void shouldThrowWhenNegativeCount() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSeller(clock);

            // when & then
            assertThatThrownBy(() -> seller.updateProductCount(-1, clock))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("상품 수는 0 이상");
        }

        @Test
        @DisplayName("[성공] 0으로 업데이트 가능")
        void shouldAllowZeroCount() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSellerWithProducts(100, clock);

            // when
            seller.updateProductCount(0, clock);

            // then
            assertThat(seller.getProductCount()).isZero();
        }
    }

    @Nested
    @DisplayName("clearDomainEvents() - 이벤트 초기화")
    class ClearDomainEvents {

        @Test
        @DisplayName("[성공] 이벤트 목록이 비워짐")
        void shouldClearEvents() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSeller(clock);
            seller.deactivate(clock); // 이벤트 발행
            assertThat(seller.getDomainEvents()).hasSize(1);

            // when
            seller.clearDomainEvents();

            // then
            assertThat(seller.getDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("[성공] 이벤트가 없어도 예외 없이 동작")
        void shouldNotThrowWhenNoEvents() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSeller(clock);
            assertThat(seller.getDomainEvents()).isEmpty();

            // when & then (예외 없이 동작)
            seller.clearDomainEvents();
            assertThat(seller.getDomainEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("needsUpdate*() - 변경 필요 여부")
    class NeedsUpdate {

        @Test
        @DisplayName("[성공] 다른 MustItSellerName이면 true 반환")
        void shouldReturnTrueWhenDifferentMustItSellerName() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSeller(clock);
            MustItSellerName newName = MustItSellerName.of("different-name");

            // when & then
            assertThat(seller.needsUpdateMustItSellerName(newName)).isTrue();
        }

        @Test
        @DisplayName("[성공] 동일한 MustItSellerName이면 false 반환")
        void shouldReturnFalseWhenSameMustItSellerName() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSeller(clock);
            MustItSellerName sameName = seller.getMustItSellerName();

            // when & then
            assertThat(seller.needsUpdateMustItSellerName(sameName)).isFalse();
        }

        @Test
        @DisplayName("[성공] null MustItSellerName이면 false 반환")
        void shouldReturnFalseWhenNullMustItSellerName() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSeller(clock);

            // when & then
            assertThat(seller.needsUpdateMustItSellerName(null)).isFalse();
        }

        @Test
        @DisplayName("[성공] 다른 SellerName이면 true 반환")
        void shouldReturnTrueWhenDifferentSellerName() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSeller(clock);
            SellerName newName = SellerName.of("different-name");

            // when & then
            assertThat(seller.needsUpdateSellerName(newName)).isTrue();
        }

        @Test
        @DisplayName("[성공] 동일한 SellerName이면 false 반환")
        void shouldReturnFalseWhenSameSellerName() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSeller(clock);
            SellerName sameName = seller.getSellerName();

            // when & then
            assertThat(seller.needsUpdateSellerName(sameName)).isFalse();
        }

        @Test
        @DisplayName("[성공] null SellerName이면 false 반환")
        void shouldReturnFalseWhenNullSellerName() {
            // given
            FixedClock clock = FixedClock.aDefaultClock();
            Seller seller = SellerFixture.anActiveSeller(clock);

            // when & then
            assertThat(seller.needsUpdateSellerName(null)).isFalse();
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
