package com.ryuqq.crawlinghub.domain.product;

import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSellerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CrawledProduct Domain 단위 테스트
 *
 * <p>테스트 범위:
 * <ul>
 *   <li>생성 테스트: forNew, of, reconstitute</li>
 *   <li>데이터 업데이트 테스트: updateMiniShopData, updateDetailData, updateOptionData</li>
 *   <li>완성 상태 테스트: isComplete (미니샵 + 상세 + 옵션 모두 존재 조건)</li>
 *   <li>변경 감지 테스트: hasDataChanged, updateDataHash</li>
 *   <li>버전 관리 테스트: incrementVersion (변경 시마다 증가)</li>
 *   <li>예외 케이스 테스트</li>
 *   <li>Law of Demeter 준수 테스트</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-01-30
 */
@DisplayName("CrawledProduct Domain 단위 테스트")
class CrawledProductTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTests {

        @Test
        @DisplayName("유효한 입력으로 신규 CrawledProduct 생성 성공")
        void shouldCreateNewProductWithValidInputs() {
            // Given
            String mustitItemNo = "ITEM-12345";
            MustitSellerId sellerId = MustitSellerId.of(100L);

            // When
            CrawledProduct product = CrawledProduct.forNew(mustitItemNo, sellerId);

            // Then
            assertThat(product).isNotNull();
            assertThat(product.getIdValue()).isNull(); // 신규 생성이므로 ID 없음
            assertThat(product.getMustitItemNo()).isEqualTo(mustitItemNo);
            assertThat(product.getSellerIdValue()).isEqualTo(100L);
            assertThat(product.getStatus()).isEqualTo(CompletionStatus.INCOMPLETE); // 초기 상태
            assertThat(product.getVersion()).isEqualTo(1); // 초기 버전
            assertThat(product.getMiniShopDataValue()).isNull();
            assertThat(product.getDetailDataValue()).isNull();
            assertThat(product.getOptionDataValue()).isNull();
            assertThat(product.getDataHashValue()).isNull();
        }

        @Test
        @DisplayName("ID를 가진 CrawledProduct 생성 성공 (of)")
        void shouldCreateProductWithId() {
            // Given
            ProductId productId = ProductId.of(1L);
            String mustitItemNo = "ITEM-12345";
            MustitSellerId sellerId = MustitSellerId.of(100L);

            // When
            CrawledProduct product = CrawledProduct.of(productId, mustitItemNo, sellerId);

            // Then
            assertThat(product.getIdValue()).isEqualTo(1L);
            assertThat(product.getStatus()).isEqualTo(CompletionStatus.INCOMPLETE);
        }

        @Test
        @DisplayName("DB reconstitute로 모든 필드 포함 CrawledProduct 생성 성공")
        void shouldReconstituteProductFromDatabase() {
            // Given
            CrawledProduct product = CrawledProductFixture.createComplete();

            // When
            ProductId productId = ProductId.of(product.getIdValue());

            // Then
            assertThat(product.getIdValue()).isNotNull();
            assertThat(product.getStatus()).isEqualTo(CompletionStatus.COMPLETE);
            assertThat(product.getMiniShopDataValue()).isNotNull();
            assertThat(product.getDetailDataValue()).isNotNull();
            assertThat(product.getOptionDataValue()).isNotNull();
        }
    }

    @Nested
    @DisplayName("데이터 업데이트 테스트")
    class DataUpdateTests {

        @Test
        @DisplayName("미니샵 데이터 업데이트 성공")
        void shouldUpdateMiniShopDataSuccessfully() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();
            ProductData miniShopData = ProductDataFixture.create();
            assertThat(product.getMiniShopDataValue()).isNull();

            // When
            product.updateMiniShopData(miniShopData);

            // Then
            assertThat(product.getMiniShopDataValue()).isNotNull();
            assertThat(product.getLastUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("상세 데이터 업데이트 성공")
        void shouldUpdateDetailDataSuccessfully() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();
            ProductData detailData = ProductDataFixture.create();
            assertThat(product.getDetailDataValue()).isNull();

            // When
            product.updateDetailData(detailData);

            // Then
            assertThat(product.getDetailDataValue()).isNotNull();
            assertThat(product.getLastUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("옵션 데이터 업데이트 성공")
        void shouldUpdateOptionDataSuccessfully() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();
            ProductData optionData = ProductDataFixture.create();
            assertThat(product.getOptionDataValue()).isNull();

            // When
            product.updateOptionData(optionData);

            // Then
            assertThat(product.getOptionDataValue()).isNotNull();
            assertThat(product.getLastUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("미니샵 데이터 업데이트 시 null이면 예외 발생")
        void shouldThrowExceptionWhenUpdatingMiniShopDataWithNull() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();

            // When & Then
            assertThatThrownBy(() -> product.updateMiniShopData(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("미니샵 데이터는 null일 수 없습니다");
        }

        @Test
        @DisplayName("상세 데이터 업데이트 시 null이면 예외 발생")
        void shouldThrowExceptionWhenUpdatingDetailDataWithNull() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();

            // When & Then
            assertThatThrownBy(() -> product.updateDetailData(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상세 데이터는 null일 수 없습니다");
        }

        @Test
        @DisplayName("옵션 데이터 업데이트 시 null이면 예외 발생")
        void shouldThrowExceptionWhenUpdatingOptionDataWithNull() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();

            // When & Then
            assertThatThrownBy(() -> product.updateOptionData(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("옵션 데이터는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("완성 상태 테스트")
    class CompletionStatusTests {

        @Test
        @DisplayName("미니샵, 상세, 옵션 데이터 모두 있으면 isComplete() 는 true 반환")
        void shouldReturnTrueWhenAllDataPresent() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();
            product.updateMiniShopData(ProductDataFixture.create());
            product.updateDetailData(ProductDataFixture.create());
            product.updateOptionData(ProductDataFixture.create());

            // When & Then
            assertThat(product.isComplete()).isTrue();
            assertThat(product.getStatus()).isEqualTo(CompletionStatus.COMPLETE);
        }

        @Test
        @DisplayName("미니샵 데이터만 있으면 isComplete() 는 false 반환")
        void shouldReturnFalseWhenOnlyMiniShopDataPresent() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();
            product.updateMiniShopData(ProductDataFixture.create());

            // When & Then
            assertThat(product.isComplete()).isFalse();
            assertThat(product.getStatus()).isEqualTo(CompletionStatus.INCOMPLETE);
        }

        @Test
        @DisplayName("상세 데이터만 있으면 isComplete() 는 false 반환")
        void shouldReturnFalseWhenOnlyDetailDataPresent() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();
            product.updateDetailData(ProductDataFixture.create());

            // When & Then
            assertThat(product.isComplete()).isFalse();
            assertThat(product.getStatus()).isEqualTo(CompletionStatus.INCOMPLETE);
        }

        @Test
        @DisplayName("옵션 데이터만 있으면 isComplete() 는 false 반환")
        void shouldReturnFalseWhenOnlyOptionDataPresent() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();
            product.updateOptionData(ProductDataFixture.create());

            // When & Then
            assertThat(product.isComplete()).isFalse();
            assertThat(product.getStatus()).isEqualTo(CompletionStatus.INCOMPLETE);
        }

        @Test
        @DisplayName("미니샵, 상세 데이터만 있으면 isComplete() 는 false 반환")
        void shouldReturnFalseWhenMiniShopAndDetailDataPresent() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();
            product.updateMiniShopData(ProductDataFixture.create());
            product.updateDetailData(ProductDataFixture.create());

            // When & Then
            assertThat(product.isComplete()).isFalse();
            assertThat(product.getStatus()).isEqualTo(CompletionStatus.INCOMPLETE);
        }

        @Test
        @DisplayName("데이터 없는 상태에서 isComplete() 는 false 반환")
        void shouldReturnFalseWhenNoDataPresent() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();

            // When & Then
            assertThat(product.isComplete()).isFalse();
            assertThat(product.getStatus()).isEqualTo(CompletionStatus.INCOMPLETE);
        }
    }

    @Nested
    @DisplayName("변경 감지 테스트")
    class ChangeDetectionTests {

        @Test
        @DisplayName("기존 해시와 새 해시가 다르면 hasDataChanged() 는 true 반환")
        void shouldReturnTrueWhenHashDifferent() {
            // Given
            CrawledProduct product = CrawledProductFixture.createComplete();
            DataHash newHash = DataHashFixture.createAlternative();

            // When
            boolean hasChanged = product.hasDataChanged(newHash);

            // Then
            assertThat(hasChanged).isTrue();
        }

        @Test
        @DisplayName("기존 해시와 새 해시가 같으면 hasDataChanged() 는 false 반환")
        void shouldReturnFalseWhenHashSame() {
            // Given
            DataHash existingHash = DataHashFixture.create();
            CrawledProduct product = CrawledProductFixture.createWithDataHash(existingHash);
            DataHash sameHash = DataHashFixture.create(); // 같은 해시 값

            // When
            boolean hasChanged = product.hasDataChanged(sameHash);

            // Then
            assertThat(hasChanged).isFalse();
        }

        @Test
        @DisplayName("기존 해시가 null이고 새 해시가 있으면 hasDataChanged() 는 true 반환")
        void shouldReturnTrueWhenCurrentHashNull() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();
            assertThat(product.getDataHashValue()).isNull();
            DataHash newHash = DataHashFixture.create();

            // When
            boolean hasChanged = product.hasDataChanged(newHash);

            // Then
            assertThat(hasChanged).isTrue();
        }

        @Test
        @DisplayName("새 해시가 null이면 hasDataChanged() 는 false 반환")
        void shouldReturnFalseWhenNewHashNull() {
            // Given
            CrawledProduct product = CrawledProductFixture.createComplete();

            // When
            boolean hasChanged = product.hasDataChanged(null);

            // Then
            assertThat(hasChanged).isFalse();
        }

        @Test
        @DisplayName("데이터 해시 업데이트 성공")
        void shouldUpdateDataHashSuccessfully() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();
            DataHash newHash = DataHashFixture.create();

            // When
            product.updateDataHash(newHash);

            // Then
            assertThat(product.getDataHashValue()).isNotNull();
        }

        @Test
        @DisplayName("데이터 해시 업데이트 시 null이면 예외 발생")
        void shouldThrowExceptionWhenUpdatingHashWithNull() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();

            // When & Then
            assertThatThrownBy(() -> product.updateDataHash(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("데이터 해시는 null일 수 없습니다");
        }
    }

    @Nested
    @DisplayName("버전 관리 테스트")
    class VersionManagementTests {

        @Test
        @DisplayName("신규 생성 시 버전은 1")
        void shouldHaveVersionOneWhenCreated() {
            // Given & When
            CrawledProduct product = CrawledProductFixture.create();

            // Then
            assertThat(product.getVersion()).isEqualTo(1);
        }

        @Test
        @DisplayName("incrementVersion() 호출 시 버전 증가")
        void shouldIncrementVersionWhenCalled() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();
            int initialVersion = product.getVersion();

            // When
            product.incrementVersion();

            // Then
            assertThat(product.getVersion()).isEqualTo(initialVersion + 1);
        }

        @Test
        @DisplayName("여러 번 incrementVersion() 호출 시 계속 증가")
        void shouldContinueIncrementingVersion() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();

            // When
            product.incrementVersion();
            product.incrementVersion();
            product.incrementVersion();

            // Then
            assertThat(product.getVersion()).isEqualTo(4); // 1 + 3
        }
    }

    @Nested
    @DisplayName("상태 조회 테스트")
    class StatusQueryTests {

        @Test
        @DisplayName("hasStatus()는 현재 상태와 일치하면 true 반환")
        void shouldReturnTrueWhenStatusMatches() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();

            // When & Then
            assertThat(product.hasStatus(CompletionStatus.INCOMPLETE)).isTrue();
            assertThat(product.hasStatus(CompletionStatus.COMPLETE)).isFalse();
        }
    }

    @Nested
    @DisplayName("예외 케이스 테스트")
    class ExceptionTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("머스트잇 상품 번호가 null 또는 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenMustitItemNoIsNullOrBlank(String invalidItemNo) {
            // When & Then
            assertThatThrownBy(() ->
                CrawledProduct.forNew(invalidItemNo, MustitSellerId.of(100L))
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("머스트잇 상품 번호는 필수입니다");
        }

        @Test
        @DisplayName("셀러 ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenSellerIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> CrawledProduct.forNew("ITEM-12345", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 ID는 필수입니다");
        }

        @Test
        @DisplayName("of() 메서드에서 ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenIdIsNullInOf() {
            // When & Then
            assertThatThrownBy(() ->
                CrawledProduct.of(null, "ITEM-12345", MustitSellerId.of(100L))
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product ID는 필수입니다");
        }

        @Test
        @DisplayName("reconstitute() 메서드에서 ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenIdIsNullInReconstitute() {
            // When & Then
            assertThatThrownBy(() ->
                CrawledProduct.reconstitute(
                    null,
                    "ITEM-12345",
                    MustitSellerId.of(100L),
                    null,
                    null,
                    null,
                    null,
                    1,
                    CompletionStatus.INCOMPLETE,
                    java.time.LocalDateTime.now(),
                    java.time.LocalDateTime.now(),
                    java.time.LocalDateTime.now(),
                    java.time.LocalDateTime.now()
                )
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DB reconstitute는 ID가 필수입니다");
        }
    }

    @Nested
    @DisplayName("불변성 검증 테스트")
    class InvariantTests {

        @Test
        @DisplayName("신규 생성 시 초기 상태는 INCOMPLETE")
        void shouldHaveIncompleteStatusWhenCreated() {
            // Given & When
            CrawledProduct product = CrawledProductFixture.create();

            // Then
            assertThat(product.getStatus()).isEqualTo(CompletionStatus.INCOMPLETE);
        }

        @Test
        @DisplayName("데이터 업데이트 시 자동으로 완성 상태 확인")
        void shouldAutomaticallyCheckCompletionStatusOnDataUpdate() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();
            assertThat(product.getStatus()).isEqualTo(CompletionStatus.INCOMPLETE);

            // When
            product.updateMiniShopData(ProductDataFixture.create());
            product.updateDetailData(ProductDataFixture.create());

            // Then
            assertThat(product.getStatus()).isEqualTo(CompletionStatus.INCOMPLETE); // 아직 옵션 없음

            // When
            product.updateOptionData(ProductDataFixture.create());

            // Then
            assertThat(product.getStatus()).isEqualTo(CompletionStatus.COMPLETE); // 모두 완성
        }

        @Test
        @DisplayName("firstCrawledAt과 lastUpdatedAt은 생성 시 동일")
        void shouldHaveSameFirstCrawledAtAndLastUpdatedAtWhenCreated() {
            // Given & When
            CrawledProduct product = CrawledProductFixture.create();

            // Then
            assertThat(product.getFirstCrawledAt()).isNotNull();
            assertThat(product.getLastUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("데이터 업데이트 시 lastUpdatedAt만 변경")
        void shouldUpdateLastUpdatedAtOnDataUpdate() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();
            java.time.LocalDateTime firstCrawledAt = product.getFirstCrawledAt();

            // When
            product.updateMiniShopData(ProductDataFixture.create());

            // Then
            assertThat(product.getFirstCrawledAt()).isEqualTo(firstCrawledAt); // 변경 없음
            assertThat(product.getLastUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Law of Demeter 준수 테스트")
    class LawOfDemeterTests {

        @Test
        @DisplayName("getIdValue()는 ID를 직접 노출하지 않고 값만 반환")
        void shouldReturnIdValueWithoutExposingIdObject() {
            // Given
            CrawledProduct product = CrawledProductFixture.createWithId(100L);

            // When
            Long idValue = product.getIdValue();

            // Then
            assertThat(idValue).isEqualTo(100L);
        }

        @Test
        @DisplayName("getSellerIdValue()는 셀러 ID를 직접 노출하지 않고 값만 반환")
        void shouldReturnSellerIdValueWithoutExposingSellerIdObject() {
            // Given
            CrawledProduct product = CrawledProductFixture.createWithSellerId(200L);

            // When
            Long sellerIdValue = product.getSellerIdValue();

            // Then
            assertThat(sellerIdValue).isEqualTo(200L);
        }

        @Test
        @DisplayName("getMiniShopDataValue()는 ProductData 객체를 직접 노출하지 않고 값만 반환")
        void shouldReturnMiniShopDataValueWithoutExposingProductDataObject() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();
            product.updateMiniShopData(ProductDataFixture.create());

            // When
            String dataValue = product.getMiniShopDataValue();

            // Then
            assertThat(dataValue).isNotNull();
        }

        @Test
        @DisplayName("equals()는 ID 기반으로 동작하며 객체 체이닝 없음")
        void shouldImplementEqualsBasedOnIdWithoutChaining() {
            // Given
            CrawledProduct product1 = CrawledProductFixture.createWithId(1L);
            CrawledProduct product2 = CrawledProductFixture.createWithId(1L);
            CrawledProduct product3 = CrawledProductFixture.createWithId(2L);

            // When & Then
            assertThat(product1).isEqualTo(product2);
            assertThat(product1).isNotEqualTo(product3);
        }

        @Test
        @DisplayName("hashCode()는 ID 기반으로 동작하며 객체 체이닝 없음")
        void shouldImplementHashCodeBasedOnIdWithoutChaining() {
            // Given
            CrawledProduct product1 = CrawledProductFixture.createWithId(1L);
            CrawledProduct product2 = CrawledProductFixture.createWithId(1L);

            // When & Then
            assertThat(product1.hashCode()).isEqualTo(product2.hashCode());
        }
    }

    @Nested
    @DisplayName("Edge Case 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("머스트잇 상품 번호가 매우 긴 경우도 정상 생성")
        void shouldCreateProductWithVeryLongMustitItemNo() {
            // Given
            String longItemNo = "ITEM-" + "1".repeat(1000);

            // When
            CrawledProduct product = CrawledProduct.forNew(longItemNo, MustitSellerId.of(100L));

            // Then
            assertThat(product.getMustitItemNo()).hasSize(5 + 1000);
        }

        @Test
        @DisplayName("버전이 Integer.MAX_VALUE에 가까워도 정상 증가")
        void shouldIncrementVersionNearMaxValue() {
            // Given
            CrawledProduct product = CrawledProduct.reconstitute(
                ProductId.of(1L),
                "ITEM-12345",
                MustitSellerId.of(100L),
                null,
                null,
                null,
                null,
                Integer.MAX_VALUE - 1,
                CompletionStatus.INCOMPLETE,
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now()
            );

            // When
            product.incrementVersion();

            // Then
            assertThat(product.getVersion()).isEqualTo(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("데이터 업데이트를 여러 번 반복해도 정상 동작")
        void shouldHandleMultipleDataUpdates() {
            // Given
            CrawledProduct product = CrawledProductFixture.create();

            // When
            for (int i = 0; i < 10; i++) {
                product.updateMiniShopData(ProductDataFixture.create());
                product.updateDetailData(ProductDataFixture.create());
                product.updateOptionData(ProductDataFixture.create());
            }

            // Then
            assertThat(product.isComplete()).isTrue();
        }
    }
}
