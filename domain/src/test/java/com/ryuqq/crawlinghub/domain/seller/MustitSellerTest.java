package com.ryuqq.crawlinghub.domain.seller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * MustitSeller Domain Aggregate 단위 테스트
 *
 * <p>테스트 범위:</p>
 * <ul>
 *   <li>Happy Path: 정상 생성 및 비즈니스 메서드</li>
 *   <li>Edge Cases: 경계값 테스트</li>
 *   <li>Exception Cases: 예외 상황 처리</li>
 *   <li>Invariant Validation: 불변식 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-01-30
 */
@DisplayName("MustitSeller Domain 단위 테스트")
class MustitSellerTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTests {

        @Test
        @DisplayName("유효한 입력으로 신규 MustitSeller 생성 성공")
        void shouldCreateNewSellerWithValidInputs() {
            // Given
            String sellerCode = "SEL001";
            String sellerName = "테스트셀러";

            // When
            MustitSeller seller = MustitSeller.forNew(sellerCode, sellerName);

            // Then
            assertThat(seller).isNotNull();
            assertThat(seller.getIdValue()).isNull();
            assertThat(seller.getSellerCode()).isEqualTo(sellerCode);
            assertThat(seller.getSellerName()).isEqualTo(sellerName);
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(seller.getTotalProductCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("ID와 함께 MustitSeller 생성 성공")
        void shouldCreateSellerWithId() {
            // Given
            MustitSellerId id = MustitSellerId.of(1L);
            String sellerCode = "SEL002";
            String sellerName = "기존셀러";
            SellerStatus status = SellerStatus.ACTIVE;

            // When
            MustitSeller seller = MustitSeller.of(id, sellerCode, sellerName, status);

            // Then
            assertThat(seller).isNotNull();
            assertThat(seller.getIdValue()).isEqualTo(1L);
            assertThat(seller.getSellerCode()).isEqualTo(sellerCode);
            assertThat(seller.getSellerName()).isEqualTo(sellerName);
            assertThat(seller.getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("DB reconstitute로 모든 필드 포함 MustitSeller 생성 성공")
        void shouldReconstituteSellerFromDatabase() {
            // Given
            MustitSeller seller = MustitSellerFixture.createActive();

            // Then
            assertThat(seller).isNotNull();
            assertThat(seller.getIdValue()).isEqualTo(1L);
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(seller.getTotalProductCount()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("상태 전이 테스트")
    class StatusTransitionTests {

        @Test
        @DisplayName("ACTIVE 상태의 셀러를 PAUSED로 변경 성공")
        void shouldPauseActiveSellerSuccessfully() {
            // Given
            MustitSeller seller = MustitSellerFixture.createActive();
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);

            // When
            seller.pause();

            // Then
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.PAUSED);
        }

        @Test
        @DisplayName("ACTIVE 상태의 셀러를 DISABLED로 변경 성공")
        void shouldDisableActiveSellerSuccessfully() {
            // Given
            MustitSeller seller = MustitSellerFixture.createActive();
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);

            // When
            seller.disable();

            // Then
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.DISABLED);
        }

        @Test
        @DisplayName("DISABLED 상태의 셀러를 ACTIVE로 변경 성공")
        void shouldActivateDisabledSellerSuccessfully() {
            // Given
            MustitSeller seller = MustitSellerFixture.createDisabled();
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.DISABLED);

            // When
            seller.activate();

            // Then
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
        }

        @Test
        @DisplayName("PAUSED 상태의 셀러를 ACTIVE로 변경 성공")
        void shouldActivatePausedSellerSuccessfully() {
            // Given
            MustitSeller seller = MustitSellerFixture.createPaused();
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.PAUSED);

            // When
            seller.activate();

            // Then
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("비즈니스 메서드 테스트")
    class BusinessMethodTests {

        @Test
        @DisplayName("상품 수 업데이트 성공")
        void shouldUpdateProductCountSuccessfully() {
            // Given
            MustitSeller seller = MustitSellerFixture.createActive();
            Integer newCount = 200;

            // When
            seller.updateProductCount(newCount);

            // Then
            assertThat(seller.getTotalProductCount()).isEqualTo(newCount);
        }

        @Test
        @DisplayName("상품 수를 0으로 업데이트 성공")
        void shouldUpdateProductCountToZeroSuccessfully() {
            // Given
            MustitSeller seller = MustitSellerFixture.createActive();

            // When
            seller.updateProductCount(0);

            // Then
            assertThat(seller.getTotalProductCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("크롤링 완료 기록 성공")
        void shouldRecordCrawlingCompleteSuccessfully() {
            // Given
            MustitSeller seller = MustitSellerFixture.createActive();
            assertThat(seller.getLastCrawledAt()).isNull();

            // When
            seller.recordCrawlingComplete();

            // Then
            assertThat(seller.getLastCrawledAt()).isNotNull();
        }

        @Test
        @DisplayName("상품 수를 여러 번 업데이트해도 마지막 값이 유지됨")
        void shouldKeepLastProductCount() {
            // Given
            MustitSeller seller = MustitSellerFixture.createActive();

            // When
            seller.updateProductCount(100);
            seller.updateProductCount(200);

            // Then
            assertThat(seller.getTotalProductCount()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("예외 케이스 테스트")
    class ExceptionTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("셀러 코드가 null 또는 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenSellerCodeIsNullOrBlank(String invalidCode) {
            assertThatThrownBy(() ->
                MustitSeller.forNew(invalidCode, "테스트셀러")
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 코드는 필수입니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("셀러 이름이 null 또는 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenSellerNameIsNullOrBlank(String invalidName) {
            assertThatThrownBy(() ->
                MustitSeller.forNew("SEL001", invalidName)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 이름은 필수입니다");
        }

        @Test
        @DisplayName("ID가 null이면 of() 메서드는 예외 발생")
        void shouldThrowExceptionWhenIdIsNullInOfMethod() {
            assertThatThrownBy(() ->
                MustitSeller.of(
                    null,
                    "SEL001",
                    "테스트셀러",
                    SellerStatus.ACTIVE
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MustitSeller ID는 필수입니다");
        }

        @Test
        @DisplayName("상품 수가 null이면 예외 발생")
        void shouldThrowExceptionWhenProductCountIsNull() {
            MustitSeller seller = MustitSellerFixture.createActive();

            assertThatThrownBy(() -> seller.updateProductCount(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품 수는 0 이상이어야 합니다");
        }

        @Test
        @DisplayName("상품 수가 음수이면 예외 발생")
        void shouldThrowExceptionWhenProductCountIsNegative() {
            MustitSeller seller = MustitSellerFixture.createActive();

            assertThatThrownBy(() -> seller.updateProductCount(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품 수는 0 이상이어야 합니다");
        }
    }

    @Nested
    @DisplayName("Edge Case 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("상품 수 0으로 시작하는 신규 셀러")
        void shouldHaveZeroProductCountWhenNew() {
            MustitSeller seller = MustitSellerFixture.create();

            assertThat(seller.getTotalProductCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("매우 큰 상품 수도 정상 업데이트")
        void shouldHandleVeryLargeProductCount() {
            MustitSeller seller = MustitSellerFixture.createActive();

            seller.updateProductCount(Integer.MAX_VALUE);

            assertThat(seller.getTotalProductCount()).isEqualTo(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("DISABLED 상태에서 크롤링 불가")
        void shouldNotCrawlWhenDisabled() {
            MustitSeller seller = MustitSellerFixture.createDisabled();

            assertThat(seller.canCrawl()).isFalse();
        }

        @Test
        @DisplayName("ACTIVE 상태에서 크롤링 가능")
        void shouldCrawlWhenActive() {
            MustitSeller seller = MustitSellerFixture.createActive();

            assertThat(seller.canCrawl()).isTrue();
        }
    }

    @Nested
    @DisplayName("불변식 검증 테스트")
    class InvariantTests {

        @Test
        @DisplayName("셀러 코드는 변경 불가능")
        void shouldNotChangeSellerCodeAfterCreation() {
            MustitSeller seller = MustitSellerFixture.createWithCode("ORIGINAL_CODE");
            String originalCode = seller.getSellerCode();

            seller.activate();
            seller.pause();

            assertThat(seller.getSellerCode()).isEqualTo(originalCode);
        }

        @Test
        @DisplayName("생성 시간은 변경되지 않음")
        void shouldNotChangeCreatedAtAfterCreation() {
            MustitSeller seller = MustitSellerFixture.createActive();
            java.time.LocalDateTime originalCreatedAt = seller.getCreatedAt();

            seller.updateProductCount(100);
            seller.activate();

            assertThat(seller.getCreatedAt()).isEqualTo(originalCreatedAt);
        }

        @Test
        @DisplayName("상태 변경 시 updatedAt이 갱신됨")
        void shouldUpdateUpdatedAtWhenStatusChanges() {
            MustitSeller seller = MustitSellerFixture.createActive();
            java.time.LocalDateTime originalUpdatedAt = seller.getUpdatedAt();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            seller.pause();

            assertThat(seller.getUpdatedAt()).isAfter(originalUpdatedAt);
        }
    }

    @Nested
    @DisplayName("Law of Demeter 준수 테스트")
    class LawOfDemeterTests {

        @Test
        @DisplayName("getIdValue() 메서드로 ID 값을 직접 반환")
        void shouldReturnIdValueDirectly() {
            MustitSeller seller = MustitSellerFixture.createActive();

            Long idValue = seller.getIdValue();

            assertThat(idValue).isNotNull();
            assertThat(idValue).isEqualTo(1L);
        }

        @Test
        @DisplayName("신규 생성 시 getIdValue()는 null 반환")
        void shouldReturnNullIdValueForNewSeller() {
            MustitSeller seller = MustitSellerFixture.create();

            Long idValue = seller.getIdValue();

            assertThat(idValue).isNull();
        }

        @Test
        @DisplayName("모든 필드는 getter 체이닝 없이 직접 접근 가능")
        void shouldProvideDirectAccessWithoutChaining() {
            MustitSeller seller = MustitSellerFixture.createActive();

            assertThat(seller.getIdValue()).isNotNull();
            assertThat(seller.getSellerCode()).isNotNull();
            assertThat(seller.getSellerName()).isNotNull();
            assertThat(seller.getStatus()).isNotNull();
            assertThat(seller.getTotalProductCount()).isNotNull();
            assertThat(seller.getCreatedAt()).isNotNull();
            assertThat(seller.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("equals 및 hashCode 테스트")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("같은 ID를 가진 두 MustitSeller는 equals()가 true 반환")
        void shouldReturnTrueForSameId() {
            MustitSeller seller1 = MustitSellerFixture.createWithId(1L);
            MustitSeller seller2 = MustitSellerFixture.createWithId(1L);

            assertThat(seller1).isEqualTo(seller2);
        }

        @Test
        @DisplayName("다른 ID를 가진 두 MustitSeller는 equals()가 false 반환")
        void shouldReturnFalseForDifferentId() {
            MustitSeller seller1 = MustitSellerFixture.createWithId(1L);
            MustitSeller seller2 = MustitSellerFixture.createWithId(2L);

            assertThat(seller1).isNotEqualTo(seller2);
        }

        @Test
        @DisplayName("같은 ID를 가진 두 MustitSeller는 같은 hashCode 반환")
        void shouldReturnSameHashCodeForSameId() {
            MustitSeller seller1 = MustitSellerFixture.createWithId(1L);
            MustitSeller seller2 = MustitSellerFixture.createWithId(1L);

            assertThat(seller1.hashCode()).isEqualTo(seller2.hashCode());
        }
    }
}
