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
class MustItSellerTest {

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
            MustItSeller seller = MustItSeller.forNew(sellerCode, sellerName);

            // Then
            assertThat(seller).isNotNull();
            assertThat(seller.getIdValue()).isNull();
            assertThat(seller.getSellerCode()).isEqualTo(sellerCode);
            assertThat(seller.getSellerNameValue()).isEqualTo(sellerName);
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(seller.getTotalProductCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("ID와 함께 MustitSeller 생성 성공")
        void shouldCreateSellerWithId() {
            // Given
            MustItSellerId id = MustItSellerId.of(1L);
            String sellerCode = "SEL002";
            String sellerName = "기존셀러";
            SellerStatus status = SellerStatus.ACTIVE;

            // When
            MustItSeller seller = MustItSeller.of(id, sellerCode, sellerName, status);

            // Then
            assertThat(seller).isNotNull();
            assertThat(seller.getIdValue()).isEqualTo(1L);
            assertThat(seller.getSellerCode()).isEqualTo(sellerCode);
            assertThat(seller.getSellerNameValue()).isEqualTo(sellerName);
            assertThat(seller.getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("DB reconstitute로 모든 필드 포함 MustitSeller 생성 성공")
        void shouldReconstituteSellerFromDatabase() {
            // Given
            MustItSeller seller = MustitSellerFixture.createActive();

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
            MustItSeller seller = MustitSellerFixture.createActive();
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
            MustItSeller seller = MustitSellerFixture.createActive();
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
            MustItSeller seller = MustitSellerFixture.createDisabled();
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
            MustItSeller seller = MustitSellerFixture.createPaused();
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
            MustItSeller seller = MustitSellerFixture.createActive();
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
            MustItSeller seller = MustitSellerFixture.createActive();

            // When
            seller.updateProductCount(0);

            // Then
            assertThat(seller.getTotalProductCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("크롤링 완료 기록 성공")
        void shouldRecordCrawlingCompleteSuccessfully() {
            // Given
            MustItSeller seller = MustitSellerFixture.createActive();
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
            MustItSeller seller = MustitSellerFixture.createActive();

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
                MustItSeller.forNew(invalidCode, "테스트셀러")
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
                MustItSeller.forNew("SEL001", invalidName)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 이름은 필수입니다");
        }

        @Test
        @DisplayName("ID가 null이면 of() 메서드는 예외 발생")
        void shouldThrowExceptionWhenIdIsNullInOfMethod() {
            assertThatThrownBy(() ->
                MustItSeller.of(
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
            MustItSeller seller = MustitSellerFixture.createActive();

            assertThatThrownBy(() -> seller.updateProductCount(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품 수는 0 이상이어야 합니다");
        }

        @Test
        @DisplayName("상품 수가 음수이면 예외 발생")
        void shouldThrowExceptionWhenProductCountIsNegative() {
            MustItSeller seller = MustitSellerFixture.createActive();

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
            MustItSeller seller = MustitSellerFixture.create();

            assertThat(seller.getTotalProductCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("매우 큰 상품 수도 정상 업데이트")
        void shouldHandleVeryLargeProductCount() {
            MustItSeller seller = MustitSellerFixture.createActive();

            seller.updateProductCount(Integer.MAX_VALUE);

            assertThat(seller.getTotalProductCount()).isEqualTo(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("DISABLED 상태에서 크롤링 불가")
        void shouldNotCrawlWhenDisabled() {
            MustItSeller seller = MustitSellerFixture.createDisabled();

            assertThat(seller.canCrawl()).isFalse();
        }

        @Test
        @DisplayName("ACTIVE 상태에서 크롤링 가능")
        void shouldCrawlWhenActive() {
            MustItSeller seller = MustitSellerFixture.createActive();

            assertThat(seller.canCrawl()).isTrue();
        }
    }

    @Nested
    @DisplayName("불변식 검증 테스트")
    class InvariantTests {

        @Test
        @DisplayName("셀러 코드는 변경 불가능")
        void shouldNotChangeSellerCodeAfterCreation() {
            MustItSeller seller = MustitSellerFixture.createWithCode("ORIGINAL_CODE");
            String originalCode = seller.getSellerCode();

            seller.activate();
            seller.pause();

            assertThat(seller.getSellerCode()).isEqualTo(originalCode);
        }

        @Test
        @DisplayName("생성 시간은 변경되지 않음")
        void shouldNotChangeCreatedAtAfterCreation() {
            MustItSeller seller = MustitSellerFixture.createActive();
            java.time.LocalDateTime originalCreatedAt = seller.getCreatedAt();

            seller.updateProductCount(100);
            seller.activate();

            assertThat(seller.getCreatedAt()).isEqualTo(originalCreatedAt);
        }

        @Test
        @DisplayName("상태 변경 시 updatedAt이 갱신됨")
        void shouldUpdateUpdatedAtWhenStatusChanges() {
            MustItSeller seller = MustitSellerFixture.createActive();
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
            MustItSeller seller = MustitSellerFixture.createActive();

            Long idValue = seller.getIdValue();

            assertThat(idValue).isNotNull();
            assertThat(idValue).isEqualTo(1L);
        }

        @Test
        @DisplayName("신규 생성 시 getIdValue()는 null 반환")
        void shouldReturnNullIdValueForNewSeller() {
            MustItSeller seller = MustitSellerFixture.create();

            Long idValue = seller.getIdValue();

            assertThat(idValue).isNull();
        }

        @Test
        @DisplayName("모든 필드는 getter 체이닝 없이 직접 접근 가능")
        void shouldProvideDirectAccessWithoutChaining() {
            MustItSeller seller = MustitSellerFixture.createActive();

            assertThat(seller.getIdValue()).isNotNull();
            assertThat(seller.getSellerCode()).isNotNull();
            assertThat(seller.getSellerNameValue()).isNotNull();
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
            MustItSeller seller1 = MustitSellerFixture.createWithId(1L);
            MustItSeller seller2 = MustitSellerFixture.createWithId(1L);

            assertThat(seller1).isEqualTo(seller2);
        }

        @Test
        @DisplayName("다른 ID를 가진 두 MustitSeller는 equals()가 false 반환")
        void shouldReturnFalseForDifferentId() {
            MustItSeller seller1 = MustitSellerFixture.createWithId(1L);
            MustItSeller seller2 = MustitSellerFixture.createWithId(2L);

            assertThat(seller1).isNotEqualTo(seller2);
        }

        @Test
        @DisplayName("같은 ID를 가진 두 MustitSeller는 같은 hashCode 반환")
        void shouldReturnSameHashCodeForSameId() {
            MustItSeller seller1 = MustitSellerFixture.createWithId(1L);
            MustItSeller seller2 = MustitSellerFixture.createWithId(1L);

            assertThat(seller1.hashCode()).isEqualTo(seller2.hashCode());
        }

        @Test
        @DisplayName("ID가 null인 경우 sellerName 기반 equals")
        void shouldUseSellerNameForEqualsWhenIdIsNull() {
            // Given
            MustItSeller seller1 = MustItSeller.forNew("SEL001", "같은이름");
            MustItSeller seller2 = MustItSeller.forNew("SEL002", "같은이름");

            // Then
            assertThat(seller1).isEqualTo(seller2);
        }

        @Test
        @DisplayName("ID가 null인 경우 sellerName 기반 hashCode")
        void shouldUseSellerNameForHashCodeWhenIdIsNull() {
            // Given
            MustItSeller seller1 = MustItSeller.forNew("SEL001", "같은이름");
            MustItSeller seller2 = MustItSeller.forNew("SEL002", "같은이름");

            // Then
            assertThat(seller1.hashCode()).isEqualTo(seller2.hashCode());
        }

        @Test
        @DisplayName("자기 자신과는 equals")
        void shouldBeEqualToItself() {
            // Given
            MustItSeller seller = MustitSellerFixture.createActive();

            // Then
            assertThat(seller).isEqualTo(seller);
        }

        @Test
        @DisplayName("null과는 not equals")
        void shouldNotBeEqualToNull() {
            // Given
            MustItSeller seller = MustitSellerFixture.createActive();

            // Then
            assertThat(seller).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입 객체와는 not equals")
        void shouldNotBeEqualToDifferentType() {
            // Given
            MustItSeller seller = MustitSellerFixture.createActive();
            Object other = "Not a seller";

            // Then
            assertThat(seller).isNotEqualTo(other);
        }

        @Test
        @DisplayName("ID가 null인 seller와 ID가 있는 seller는 not equals")
        void shouldNotBeEqualWhenOneHasNullId() {
            // Given
            MustItSeller sellerWithoutId = MustItSeller.forNew("SEL001", "셀러A");
            MustItSeller sellerWithId = MustitSellerFixture.createWithId(1L);

            // Then
            assertThat(sellerWithoutId).isNotEqualTo(sellerWithId);
        }
    }

    @Nested
    @DisplayName("추가 Factory Method 테스트")
    class AdditionalFactoryMethodTests {

        @Test
        @DisplayName("reconstitute() ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenReconstituteWithNullId() {
            assertThatThrownBy(() ->
                MustItSeller.reconstitute(
                    null,
                    "SEL001",
                    "테스트셀러",
                    SellerStatus.ACTIVE,
                    100,
                    java.time.LocalDateTime.now(),
                    java.time.LocalDateTime.now(),
                    java.time.LocalDateTime.now()
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DB reconstitute는 ID가 필수입니다");
        }

        @Test
        @DisplayName("reconstitute() 모든 필드가 정확히 설정됨")
        void shouldSetAllFieldsCorrectlyWhenReconstitute() {
            // Given
            MustItSellerId id = MustItSellerId.of(100L);
            Integer productCount = 500;
            java.time.LocalDateTime lastCrawled = java.time.LocalDateTime.now().minusDays(1);
            java.time.LocalDateTime created = java.time.LocalDateTime.now().minusDays(10);
            java.time.LocalDateTime updated = java.time.LocalDateTime.now().minusDays(1);

            // When
            MustItSeller seller = MustItSeller.reconstitute(
                id,
                "SEL999",
                "재구성셀러",
                SellerStatus.PAUSED,
                productCount,
                lastCrawled,
                created,
                updated
            );

            // Then
            assertThat(seller.getIdValue()).isEqualTo(100L);
            assertThat(seller.getSellerCode()).isEqualTo("SEL999");
            assertThat(seller.getSellerNameValue()).isEqualTo("재구성셀러");
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.PAUSED);
            assertThat(seller.getTotalProductCount()).isEqualTo(productCount);
            assertThat(seller.getLastCrawledAt()).isEqualTo(lastCrawled);
            assertThat(seller.getCreatedAt()).isEqualTo(created);
            assertThat(seller.getUpdatedAt()).isEqualTo(updated);
        }

        @Test
        @DisplayName("of() 메서드의 sellerCode가 null이면 예외 발생")
        void shouldThrowExceptionWhenOfWithNullSellerCode() {
            assertThatThrownBy(() ->
                MustItSeller.of(
                    MustItSellerId.of(1L),
                    null,
                    "테스트셀러",
                    SellerStatus.ACTIVE
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 코드는 필수입니다");
        }

        @Test
        @DisplayName("of() 메서드의 sellerName이 null이면 예외 발생")
        void shouldThrowExceptionWhenOfWithNullSellerName() {
            assertThatThrownBy(() ->
                MustItSeller.of(
                    MustItSellerId.of(1L),
                    "SEL001",
                    null,
                    SellerStatus.ACTIVE
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 이름은 필수입니다");
        }

        @Test
        @DisplayName("of() 메서드의 status가 null이면 예외 발생")
        void shouldThrowExceptionWhenOfWithNullStatus() {
            assertThatThrownBy(() ->
                MustItSeller.of(
                    MustItSellerId.of(1L),
                    "SEL001",
                    "테스트셀러",
                    null
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 상태는 필수입니다");
        }
    }

    @Nested
    @DisplayName("추가 비즈니스 메서드 테스트")
    class AdditionalBusinessMethodTests {

        @Test
        @DisplayName("DISABLED 상태에서 validateCanCrawl() 호출 시 예외 발생")
        void shouldThrowExceptionWhenValidateCrawlForDisabledSeller() {
            // Given
            MustItSeller seller = MustitSellerFixture.createDisabled();

            // When & Then
            assertThatThrownBy(seller::validateCanCrawl)
                .isInstanceOf(com.ryuqq.crawlinghub.domain.seller.exception.InactiveSellerException.class);
        }

        @Test
        @DisplayName("ACTIVE 상태에서 validateCanCrawl() 호출 시 예외 없음")
        void shouldNotThrowExceptionWhenValidateCrawlForActiveSeller() {
            // Given
            MustItSeller seller = MustitSellerFixture.createActive();

            // When & Then
            assertThatCode(seller::validateCanCrawl)
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("PAUSED 상태에서 validateCanCrawl() 호출 시 예외 없음 (크롤링 가능)")
        void shouldNotThrowExceptionWhenValidateCrawlForPausedSeller() {
            // Given
            MustItSeller seller = MustitSellerFixture.createPaused();

            // When & Then
            assertThatCode(seller::validateCanCrawl)
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("PAUSED 상태에서 canCrawl()은 true 반환")
        void shouldReturnTrueForCanCrawlWhenPaused() {
            // Given
            MustItSeller seller = MustitSellerFixture.createPaused();

            // Then
            assertThat(seller.canCrawl()).isTrue();
        }

        @Test
        @DisplayName("isActive()는 ACTIVE 상태에서만 true 반환")
        void shouldReturnTrueForIsActiveOnlyWhenActive() {
            // Given
            MustItSeller activeSeller = MustitSellerFixture.createActive();
            MustItSeller pausedSeller = MustitSellerFixture.createPaused();
            MustItSeller disabledSeller = MustitSellerFixture.createDisabled();

            // Then
            assertThat(activeSeller.isActive()).isTrue();
            assertThat(pausedSeller.isActive()).isFalse();
            assertThat(disabledSeller.isActive()).isFalse();
        }

        @Test
        @DisplayName("hasStatus()는 해당 상태일 때만 true 반환")
        void shouldReturnTrueForHasStatusOnlyWhenMatches() {
            // Given
            MustItSeller seller = MustitSellerFixture.createActive();

            // Then
            assertThat(seller.hasStatus(SellerStatus.ACTIVE)).isTrue();
            assertThat(seller.hasStatus(SellerStatus.PAUSED)).isFalse();
            assertThat(seller.hasStatus(SellerStatus.DISABLED)).isFalse();
        }

        @Test
        @DisplayName("getSellerName()은 SellerName Value Object 반환")
        void shouldReturnSellerNameValueObject() {
            // Given
            MustItSeller seller = MustItSeller.forNew("SEL001", "테스트셀러");

            // When
            SellerName sellerName = seller.getSellerName();

            // Then
            assertThat(sellerName).isNotNull();
            assertThat(sellerName.getValue()).isEqualTo("테스트셀러");
        }

        @Test
        @DisplayName("신규 생성 셀러의 lastCrawledAt은 null")
        void shouldHaveNullLastCrawledAtForNewSeller() {
            // Given
            MustItSeller seller = MustItSeller.forNew("SEL001", "신규셀러");

            // Then
            assertThat(seller.getLastCrawledAt()).isNull();
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 주요 필드 정보를 포함")
        void shouldIncludeKeyFieldsInToString() {
            // Given
            MustItSeller seller = MustitSellerFixture.createActive();

            // When
            String result = seller.toString();

            // Then
            assertThat(result).contains("MustitSeller");
            assertThat(result).contains("id=");
            assertThat(result).contains("sellerCode=");
            assertThat(result).contains("sellerName=");
            assertThat(result).contains("status=");
            assertThat(result).contains("totalProductCount=");
        }

        @Test
        @DisplayName("ID가 null인 신규 셀러의 toString()")
        void shouldHandleNullIdInToString() {
            // Given
            MustItSeller seller = MustItSeller.forNew("SEL001", "신규셀러");

            // When
            String result = seller.toString();

            // Then
            assertThat(result).contains("id=null");
        }
    }

    @Nested
    @DisplayName("시나리오 테스트")
    class ScenarioTests {

        @Test
        @DisplayName("상태 전이 시나리오: ACTIVE → PAUSED → DISABLED → ACTIVE")
        void shouldTransitionThroughMultipleStates() {
            // Given
            MustItSeller seller = MustitSellerFixture.createActive();

            // When & Then
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);

            seller.pause();
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.PAUSED);
            assertThat(seller.canCrawl()).isTrue();

            seller.disable();
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.DISABLED);
            assertThat(seller.canCrawl()).isFalse();

            seller.activate();
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(seller.canCrawl()).isTrue();
        }

        @Test
        @DisplayName("크롤링 시나리오: 크롤링 전후 상태 변화")
        void shouldHandleCrawlingScenario() {
            // Given
            MustItSeller seller = MustitSellerFixture.createActive();
            assertThat(seller.getLastCrawledAt()).isNull();
            assertThat(seller.getTotalProductCount()).isEqualTo(100);

            // When
            seller.validateCanCrawl();  // 크롤링 가능 검증
            seller.updateProductCount(150);  // 크롤링으로 상품 수 업데이트
            seller.recordCrawlingComplete();  // 크롤링 완료 기록

            // Then
            assertThat(seller.getLastCrawledAt()).isNotNull();
            assertThat(seller.getTotalProductCount()).isEqualTo(150);
        }

        @Test
        @DisplayName("비활성화된 셀러는 크롤링 불가 시나리오")
        void shouldPreventCrawlingForDisabledSeller() {
            // Given
            MustItSeller seller = MustitSellerFixture.createActive();
            seller.disable();

            // When & Then
            assertThat(seller.canCrawl()).isFalse();
            assertThatThrownBy(seller::validateCanCrawl)
                .isInstanceOf(com.ryuqq.crawlinghub.domain.seller.exception.InactiveSellerException.class);
        }

        @Test
        @DisplayName("전체 라이프사이클 시나리오")
        void shouldHandleFullLifecycle() {
            // 1. 신규 셀러 생성
            MustItSeller seller = MustItSeller.forNew("SEL001", "신규셀러");
            assertThat(seller.getIdValue()).isNull();
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(seller.getTotalProductCount()).isEqualTo(0);

            // 2. 초기 크롤링
            seller.updateProductCount(50);
            seller.recordCrawlingComplete();

            // 3. 일시정지
            seller.pause();
            assertThat(seller.canCrawl()).isTrue();  // PAUSED도 크롤링 가능

            // 4. 재활성화
            seller.activate();
            assertThat(seller.isActive()).isTrue();

            // 5. 추가 크롤링
            seller.updateProductCount(100);
            seller.recordCrawlingComplete();

            // 6. 최종 비활성화
            seller.disable();
            assertThat(seller.canCrawl()).isFalse();

            // Then: 상태는 변경되었지만 기본 속성은 유지
            assertThat(seller.getSellerCode()).isEqualTo("SEL001");
            assertThat(seller.getSellerNameValue()).isEqualTo("신규셀러");
            assertThat(seller.getTotalProductCount()).isEqualTo(100);
            assertThat(seller.getLastCrawledAt()).isNotNull();
        }
    }
}
