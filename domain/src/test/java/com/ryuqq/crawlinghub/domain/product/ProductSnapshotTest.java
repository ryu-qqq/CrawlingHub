package com.ryuqq.crawlinghub.domain.product;

import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ProductSnapshot Aggregate Root 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@DisplayName("ProductSnapshot Aggregate Root 단위 테스트")
class ProductSnapshotTest {

    private static final Long MUST_IT_ITEM_NO = 12345L;
    private static final MustitSellerId SELLER_ID = new MustitSellerId(67890L);
    private static final ProductSnapshotId ID = new ProductSnapshotId(1L);
    private static final Clock FIXED_CLOCK = Clock.fixed(
        Instant.parse("2025-11-07T10:00:00Z"),
        ZoneId.systemDefault()
    );

    @Nested
    @DisplayName("Static Factory 메서드 테스트")
    class StaticFactoryTests {

        @Test
        @DisplayName("forNew()로 신규 Snapshot 생성 성공 (ID 없음)")
        void shouldCreateNewSnapshotWithoutId() {
            // When
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);

            // Then
            assertThat(snapshot).isNotNull();
            assertThat(snapshot.getIdValue()).isNull();
            assertThat(snapshot.getMustItItemNo()).isEqualTo(MUST_IT_ITEM_NO);
            assertThat(snapshot.getSellerIdValue()).isEqualTo(SELLER_ID.value());
            assertThat(snapshot.getVersion()).isEqualTo(1);  // 초기 버전은 1
            assertThat(snapshot.isComplete()).isFalse();
        }

        @Test
        @DisplayName("of()로 기존 Snapshot 생성 성공 (ID 있음)")
        void shouldCreateExistingSnapshotWithId() {
            // When
            ProductSnapshot snapshot = ProductSnapshot.of(ID, MUST_IT_ITEM_NO, SELLER_ID);

            // Then
            assertThat(snapshot).isNotNull();
            assertThat(snapshot.getIdValue()).isEqualTo(ID.value());
            assertThat(snapshot.getMustItItemNo()).isEqualTo(MUST_IT_ITEM_NO);
            assertThat(snapshot.getSellerIdValue()).isEqualTo(SELLER_ID.value());
        }

        @Test
        @DisplayName("of()에 null ID 전달 시 예외 발생")
        void shouldThrowExceptionWhenIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> ProductSnapshot.of(null, MUST_IT_ITEM_NO, SELLER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ProductSnapshot ID는 필수입니다");
        }

        @Test
        @DisplayName("reconstitute()로 완전한 Snapshot 재구성 성공")
        void shouldReconstructCompleteSnapshot() {
            // Given
            String productName = "테스트 상품";
            Long price = 50000L;
            String mainImageUrl = "https://example.com/image.jpg";
            List<ProductOption> options = List.of(
                new ProductOption("색상", "빨강", 10, 1000L),
                new ProductOption("사이즈", "L", 5, 0L)
            );
            Integer totalStock = 100;
            ProductInfoModule productInfo = new ProductInfoModule("테스트 설명", "테스트 제조사", "한국");
            ShippingModule shipping = new ShippingModule(3000, "일반배송", 3);
            ProductDetailInfoModule detailInfo = new ProductDetailInfoModule("https://example.com/detail.jpg", "<p>상세설명</p>");
            LocalDateTime lastSyncedAt = LocalDateTime.of(2025, 11, 7, 9, 0);
            Integer version = 5;
            LocalDateTime createdAt = LocalDateTime.of(2025, 11, 1, 10, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2025, 11, 7, 10, 0);

            // When
            ProductSnapshot snapshot = ProductSnapshot.reconstitute(
                ID, MUST_IT_ITEM_NO, SELLER_ID,
                productName, price, mainImageUrl,
                options, totalStock,
                productInfo, shipping, detailInfo,
                lastSyncedAt, version,
                createdAt, updatedAt
            );

            // Then
            assertThat(snapshot.getIdValue()).isEqualTo(ID.value());
            assertThat(snapshot.getProductName()).isEqualTo(productName);
            assertThat(snapshot.getPrice()).isEqualTo(price);
            assertThat(snapshot.getMainImageUrl()).isEqualTo(mainImageUrl);
            assertThat(snapshot.getOptions()).hasSize(2);
            assertThat(snapshot.getTotalStock()).isEqualTo(totalStock);
            assertThat(snapshot.getLastSyncedAt()).isEqualTo(lastSyncedAt);
            assertThat(snapshot.getVersion()).isEqualTo(version);
            assertThat(snapshot.getCreatedAt()).isEqualTo(createdAt);
            assertThat(snapshot.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(snapshot.isComplete()).isTrue();
        }

        @Test
        @DisplayName("reconstitute()에 null ID 전달 시 예외 발생")
        void shouldThrowExceptionWhenReconstructWithNullId() {
            // When & Then
            assertThatThrownBy(() -> ProductSnapshot.reconstitute(
                null, MUST_IT_ITEM_NO, SELLER_ID,
                "상품", 10000L, "url",
                List.of(), 0,
                null, null, null,
                null, 0,
                LocalDateTime.now(), LocalDateTime.now()
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DB reconstitute는 ID가 필수입니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("필수 필드 mustItItemNo가 null이면 예외 발생")
        void shouldThrowExceptionWhenMustItItemNoIsNull(Long nullMustItItemNo) {
            // When & Then
            assertThatThrownBy(() -> ProductSnapshot.forNew(nullMustItItemNo, SELLER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("머스트잇 상품 번호는 필수입니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("필수 필드 sellerId가 null이면 예외 발생")
        void shouldThrowExceptionWhenSellerIdIsNull(MustitSellerId nullSellerId) {
            // When & Then
            assertThatThrownBy(() -> ProductSnapshot.forNew(MUST_IT_ITEM_NO, nullSellerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("셀러 ID는 필수입니다");
        }
    }

    @Nested
    @DisplayName("상품명 업데이트 테스트")
    class UpdateProductNameTests {

        @Test
        @DisplayName("유효한 상품명으로 업데이트 성공")
        void shouldUpdateProductNameSuccessfully() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            String productName = "새로운 상품명";

            // When
            snapshot.updateProductName(productName);

            // Then
            assertThat(snapshot.getProductName()).isEqualTo(productName);
            assertThat(snapshot.getVersion()).isEqualTo(2);  // 초기 1 + 업데이트 1
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null 상품명으로 업데이트 시 예외 발생")
        void shouldThrowExceptionWhenProductNameIsNull(String nullName) {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);

            // When & Then
            assertThatThrownBy(() -> snapshot.updateProductName(nullName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품명은 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "  ", "\t", "\n"})
        @DisplayName("빈 문자열이나 공백 상품명으로 업데이트 시 예외 발생")
        void shouldThrowExceptionWhenProductNameIsBlank(String blankName) {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);

            // When & Then
            assertThatThrownBy(() -> snapshot.updateProductName(blankName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품명은 필수입니다");
        }
    }

    @Nested
    @DisplayName("가격 업데이트 테스트")
    class UpdatePriceTests {

        @Test
        @DisplayName("유효한 가격으로 업데이트 성공")
        void shouldUpdatePriceSuccessfully() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            Long price = 50000L;

            // When
            snapshot.updatePrice(price);

            // Then
            assertThat(snapshot.getPrice()).isEqualTo(price);
            assertThat(snapshot.getVersion()).isEqualTo(2);  // 초기 1 + 업데이트 1
        }

        @Test
        @DisplayName("가격 0으로 업데이트 성공 (경계값)")
        void shouldUpdatePriceToZero() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            Long price = 0L;

            // When
            snapshot.updatePrice(price);

            // Then
            assertThat(snapshot.getPrice()).isEqualTo(price);
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null 가격으로 업데이트 시 예외 발생")
        void shouldThrowExceptionWhenPriceIsNull(Long nullPrice) {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);

            // When & Then
            assertThatThrownBy(() -> snapshot.updatePrice(nullPrice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("가격은 0 이상이어야 합니다");
        }

        @Test
        @DisplayName("음수 가격으로 업데이트 시 예외 발생")
        void shouldThrowExceptionWhenPriceIsNegative() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            Long negativePrice = -1000L;

            // When & Then
            assertThatThrownBy(() -> snapshot.updatePrice(negativePrice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("가격은 0 이상이어야 합니다");
        }
    }

    @Nested
    @DisplayName("메인 이미지 업데이트 테스트")
    class UpdateMainImageTests {

        @Test
        @DisplayName("유효한 이미지 URL로 업데이트 성공")
        void shouldUpdateMainImageSuccessfully() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            String imageUrl = "https://example.com/image.jpg";

            // When
            snapshot.updateMainImage(imageUrl);

            // Then
            assertThat(snapshot.getMainImageUrl()).isEqualTo(imageUrl);
            assertThat(snapshot.getVersion()).isEqualTo(2);  // 초기 1 + 업데이트 1
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null 이미지 URL로 업데이트 시 예외 발생")
        void shouldThrowExceptionWhenImageUrlIsNull(String nullUrl) {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);

            // When & Then
            assertThatThrownBy(() -> snapshot.updateMainImage(nullUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("메인 이미지 URL은 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "  "})
        @DisplayName("빈 문자열 이미지 URL로 업데이트 시 예외 발생")
        void shouldThrowExceptionWhenImageUrlIsBlank(String blankUrl) {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);

            // When & Then
            assertThatThrownBy(() -> snapshot.updateMainImage(blankUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("메인 이미지 URL은 필수입니다");
        }
    }

    @Nested
    @DisplayName("옵션 업데이트 테스트")
    class UpdateOptionsTests {

        @Test
        @DisplayName("유효한 옵션 리스트로 업데이트 성공")
        void shouldUpdateOptionsSuccessfully() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            List<ProductOption> options = List.of(
                new ProductOption("색상", "빨강", 10, 1000L),
                new ProductOption("사이즈", "L", 5, 0L)
            );

            // When
            snapshot.updateOptions(options);

            // Then
            assertThat(snapshot.getOptions()).hasSize(2);
            assertThat(snapshot.getVersion()).isEqualTo(2);  // 초기 1 + 업데이트 1
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null 옵션 리스트로 업데이트 시 예외 발생")
        void shouldThrowExceptionWhenOptionsIsNull(List<ProductOption> nullOptions) {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);

            // When & Then
            assertThatThrownBy(() -> snapshot.updateOptions(nullOptions))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("옵션 리스트는 null일 수 없습니다");
        }

        @Test
        @DisplayName("빈 옵션 리스트로 업데이트 성공 (빈 리스트 허용)")
        void shouldUpdateWithEmptyOptions() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            List<ProductOption> emptyOptions = List.of();

            // When
            snapshot.updateOptions(emptyOptions);

            // Then
            assertThat(snapshot.getOptions()).isEmpty();
            assertThat(snapshot.getVersion()).isEqualTo(2);  // 초기 1 + 업데이트 1
        }

        @Test
        @DisplayName("getOptions()는 방어적 복사본을 반환한다")
        void shouldReturnDefensiveCopyOfOptions() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            List<ProductOption> options = new ArrayList<>(List.of(
                new ProductOption("색상", "빨강", 10, 1000L)
            ));
            snapshot.updateOptions(options);

            // When
            List<ProductOption> returnedOptions = snapshot.getOptions();
            returnedOptions.add(new ProductOption("사이즈", "L", 5, 0L));

            // Then
            assertThat(snapshot.getOptions()).hasSize(1); // 원본은 변경되지 않음
        }
    }

    @Nested
    @DisplayName("총 재고 업데이트 테스트")
    class UpdateTotalStockTests {

        @Test
        @DisplayName("유효한 총 재고로 업데이트 성공")
        void shouldUpdateTotalStockSuccessfully() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            Integer totalStock = 100;

            // When
            snapshot.updateTotalStock(totalStock);

            // Then
            assertThat(snapshot.getTotalStock()).isEqualTo(totalStock);
            assertThat(snapshot.getVersion()).isEqualTo(2);  // 초기 1 + 업데이트 1
        }

        @Test
        @DisplayName("총 재고 0으로 업데이트 성공 (경계값)")
        void shouldUpdateTotalStockToZero() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            Integer totalStock = 0;

            // When
            snapshot.updateTotalStock(totalStock);

            // Then
            assertThat(snapshot.getTotalStock()).isEqualTo(totalStock);
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null 총 재고로 업데이트 시 예외 발생")
        void shouldThrowExceptionWhenTotalStockIsNull(Integer nullStock) {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);

            // When & Then
            assertThatThrownBy(() -> snapshot.updateTotalStock(nullStock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("총 재고는 0 이상이어야 합니다");
        }

        @Test
        @DisplayName("음수 총 재고로 업데이트 시 예외 발생")
        void shouldThrowExceptionWhenTotalStockIsNegative() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            Integer negativeStock = -10;

            // When & Then
            assertThatThrownBy(() -> snapshot.updateTotalStock(negativeStock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("총 재고는 0 이상이어야 합니다");
        }
    }

    @Nested
    @DisplayName("상품 정보 모듈 업데이트 테스트")
    class UpdateProductInfoTests {

        @Test
        @DisplayName("유효한 상품 정보 모듈로 업데이트 성공")
        void shouldUpdateProductInfoSuccessfully() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            ProductInfoModule productInfo = new ProductInfoModule("상품 설명", "제조사명", "한국");

            // When
            snapshot.updateProductInfo(productInfo);

            // Then
            assertThat(snapshot.getProductInfo()).isEqualTo(productInfo);
            assertThat(snapshot.getVersion()).isEqualTo(2);  // 초기 1 + 업데이트 1
        }

        @Test
        @DisplayName("null 상품 정보 모듈로 업데이트 시 예외 발생")
        void shouldThrowExceptionWhenProductInfoIsNull() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);

            // When & Then
            assertThatThrownBy(() -> snapshot.updateProductInfo(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상품 정보 모듈은 필수입니다");
        }
    }

    @Nested
    @DisplayName("배송 모듈 업데이트 테스트")
    class UpdateShippingTests {

        @Test
        @DisplayName("유효한 배송 모듈로 업데이트 성공")
        void shouldUpdateShippingSuccessfully() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            ShippingModule shipping = new ShippingModule(3000, "일반배송", 3);

            // When
            snapshot.updateShipping(shipping);

            // Then
            assertThat(snapshot.getShipping()).isEqualTo(shipping);
            assertThat(snapshot.getVersion()).isEqualTo(2);  // 초기 1 + 업데이트 1
        }

        @Test
        @DisplayName("null 배송 모듈로 업데이트 시 예외 발생")
        void shouldThrowExceptionWhenShippingIsNull() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);

            // When & Then
            assertThatThrownBy(() -> snapshot.updateShipping(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("배송 모듈은 필수입니다");
        }
    }

    @Nested
    @DisplayName("상세 정보 모듈 업데이트 테스트")
    class UpdateDetailInfoTests {

        @Test
        @DisplayName("유효한 상세 정보 모듈로 업데이트 성공")
        void shouldUpdateDetailInfoSuccessfully() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            ProductDetailInfoModule detailInfo = new ProductDetailInfoModule("https://example.com/detail.jpg", "<p>상세 설명</p>");

            // When
            snapshot.updateDetailInfo(detailInfo);

            // Then
            assertThat(snapshot.getDetailInfo()).isEqualTo(detailInfo);
            assertThat(snapshot.getVersion()).isEqualTo(2);  // 초기 1 + 업데이트 1
        }

        @Test
        @DisplayName("null 상세 정보 모듈로 업데이트 시 예외 발생")
        void shouldThrowExceptionWhenDetailInfoIsNull() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);

            // When & Then
            assertThatThrownBy(() -> snapshot.updateDetailInfo(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("상세 정보 모듈은 필수입니다");
        }
    }

    @Nested
    @DisplayName("버전 관리 테스트")
    class VersionManagementTests {

        @Test
        @DisplayName("업데이트마다 버전이 1씩 증가한다")
        void shouldIncrementVersionOnEachUpdate() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            assertThat(snapshot.getVersion()).isEqualTo(1);  // 초기 버전은 1

            // When
            snapshot.updateProductName("상품1");
            assertThat(snapshot.getVersion()).isEqualTo(2);  // 초기 1 + 업데이트 1

            snapshot.updatePrice(10000L);
            assertThat(snapshot.getVersion()).isEqualTo(3);  // 초기 1 + 업데이트 2

            snapshot.updateMainImage("url");
            assertThat(snapshot.getVersion()).isEqualTo(4);  // 초기 1 + 업데이트 3

            // Then
            assertThat(snapshot.getVersion()).isEqualTo(4);
        }

        @Test
        @DisplayName("여러 필드 업데이트 시 버전이 누적된다")
        void shouldAccumulateVersionOnMultipleUpdates() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);

            // When
            snapshot.updateProductName("상품");
            snapshot.updatePrice(10000L);
            snapshot.updateMainImage("url");
            snapshot.updateOptions(List.of(new ProductOption("색상", "빨강", 10, 0L)));
            snapshot.updateTotalStock(100);

            // Then
            assertThat(snapshot.getVersion()).isEqualTo(6);  // 초기 1 + 업데이트 5
        }
    }

    @Nested
    @DisplayName("완전성 검사 테스트")
    class CompletenessTests {

        @Test
        @DisplayName("필수 필드 없으면 isComplete()는 false 반환")
        void shouldReturnFalseWhenIncomplete() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);

            // When & Then
            assertThat(snapshot.isComplete()).isFalse();
        }

        @Test
        @DisplayName("상품명만 있으면 isComplete()는 false 반환")
        void shouldReturnFalseWhenOnlyProductName() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            snapshot.updateProductName("상품");

            // When & Then
            assertThat(snapshot.isComplete()).isFalse();
        }

        @Test
        @DisplayName("상품명 + 가격만 있으면 isComplete()는 false 반환")
        void shouldReturnFalseWhenOnlyProductNameAndPrice() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            snapshot.updateProductName("상품");
            snapshot.updatePrice(10000L);

            // When & Then
            assertThat(snapshot.isComplete()).isFalse();
        }

        @Test
        @DisplayName("모든 필수 필드 있으면 isComplete()는 true 반환")
        void shouldReturnTrueWhenComplete() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            snapshot.updateProductName("상품");
            snapshot.updatePrice(10000L);
            snapshot.updateOptions(List.of(new ProductOption("색상", "빨강", 10, 0L)));

            // When & Then
            assertThat(snapshot.isComplete()).isTrue();
        }

        @Test
        @DisplayName("isReadyForSync()는 isComplete()와 동일하다")
        void shouldMirrorIsComplete() {
            // Given
            ProductSnapshot incomplete = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            ProductSnapshot complete = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            complete.updateProductName("상품");
            complete.updatePrice(10000L);
            complete.updateOptions(List.of(new ProductOption("색상", "빨강", 10, 0L)));

            // When & Then
            assertThat(incomplete.isReadyForSync()).isEqualTo(incomplete.isComplete());
            assertThat(complete.isReadyForSync()).isEqualTo(complete.isComplete());
        }
    }

    @Nested
    @DisplayName("FullProductData 변환 테스트")
    class ToFullProductDataTests {

        @Test
        @DisplayName("완전한 Snapshot은 FullProductData로 변환 성공")
        void shouldConvertToFullProductDataWhenComplete() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            snapshot.updateProductName("상품");
            snapshot.updatePrice(10000L);
            snapshot.updateMainImage("https://example.com/image.jpg");
            snapshot.updateOptions(List.of(new ProductOption("색상", "빨강", 10, 1000L)));
            snapshot.updateTotalStock(100);
            snapshot.updateProductInfo(new ProductInfoModule("설명", "제조사", "한국"));
            snapshot.updateShipping(new ShippingModule(3000, "일반배송", 3));
            snapshot.updateDetailInfo(new ProductDetailInfoModule("https://example.com/detail.jpg", "<p>상세</p>"));

            // When
            FullProductData fullData = snapshot.toFullProductData();

            // Then
            assertThat(fullData).isNotNull();
            assertThat(fullData.mustItItemNo()).isEqualTo(MUST_IT_ITEM_NO);
            assertThat(fullData.productName()).isEqualTo("상품");
            assertThat(fullData.price()).isEqualTo(10000L);
        }

        @Test
        @DisplayName("불완전한 Snapshot은 FullProductData 변환 시 예외 발생")
        void shouldThrowExceptionWhenConvertIncompleteSnapshot() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            snapshot.updateProductName("상품");
            // 가격과 옵션 없음

            // When & Then
            assertThatThrownBy(() -> snapshot.toFullProductData())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("불완전한 Snapshot은 FullProductData로 변환할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("동기화 완료 기록 테스트")
    class RecordSyncCompletedTests {

        @Test
        @DisplayName("동기화 완료 기록 시 lastSyncedAt이 업데이트된다")
        void shouldUpdateLastSyncedAtWhenRecordSyncCompleted() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            LocalDateTime beforeSync = snapshot.getLastSyncedAt();

            // When
            snapshot.recordSyncCompleted();

            // Then
            assertThat(snapshot.getLastSyncedAt()).isNotNull();
            assertThat(snapshot.getLastSyncedAt()).isNotEqualTo(beforeSync);
        }

        @Test
        @DisplayName("동기화 완료 기록 시 updatedAt도 업데이트된다")
        void shouldUpdateUpdatedAtWhenRecordSyncCompleted() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            LocalDateTime beforeUpdate = snapshot.getUpdatedAt();

            // When
            snapshot.recordSyncCompleted();

            // Then
            assertThat(snapshot.getUpdatedAt()).isNotEqualTo(beforeUpdate);
        }
    }

    @Nested
    @DisplayName("Law of Demeter 테스트")
    class LawOfDemeterTests {

        @Test
        @DisplayName("getIdValue()는 ID의 value를 직접 반환한다 (Getter 체이닝 방지)")
        void shouldReturnIdValueDirectly() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.of(ID, MUST_IT_ITEM_NO, SELLER_ID);

            // When
            Long idValue = snapshot.getIdValue();

            // Then
            assertThat(idValue).isEqualTo(ID.value());
        }

        @Test
        @DisplayName("getIdValue()는 ID가 null이면 null 반환")
        void shouldReturnNullWhenIdIsNull() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);

            // When
            Long idValue = snapshot.getIdValue();

            // Then
            assertThat(idValue).isNull();
        }

        @Test
        @DisplayName("getSellerIdValue()는 SellerId의 value를 직접 반환한다")
        void shouldReturnSellerIdValueDirectly() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);

            // When
            Long sellerIdValue = snapshot.getSellerIdValue();

            // Then
            assertThat(sellerIdValue).isEqualTo(SELLER_ID.value());
        }
    }

    @Nested
    @DisplayName("Has-Check 메서드 테스트")
    class HasCheckTests {

        @Test
        @DisplayName("hasProductName()은 상품명 존재 여부를 반환한다")
        void shouldReturnTrueWhenProductNameExists() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            assertThat(snapshot.hasProductName()).isFalse();

            // When
            snapshot.updateProductName("상품");

            // Then
            assertThat(snapshot.hasProductName()).isTrue();
        }

        @Test
        @DisplayName("hasPrice()는 가격 존재 여부를 반환한다")
        void shouldReturnTrueWhenPriceExists() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            assertThat(snapshot.hasPrice()).isFalse();

            // When
            snapshot.updatePrice(10000L);

            // Then
            assertThat(snapshot.hasPrice()).isTrue();
        }

        @Test
        @DisplayName("hasOptions()는 옵션 존재 여부를 반환한다")
        void shouldReturnTrueWhenOptionsExist() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            assertThat(snapshot.hasOptions()).isFalse();

            // When
            snapshot.updateOptions(List.of(new ProductOption("색상", "빨강", 10, 0L)));

            // Then
            assertThat(snapshot.hasOptions()).isTrue();
        }
    }

    @Nested
    @DisplayName("동등성 비교 테스트")
    class EqualityTests {

        @Test
        @DisplayName("같은 ID를 가진 두 Snapshot은 같다")
        void shouldBeEqualForSameId() {
            // Given
            ProductSnapshot snapshot1 = ProductSnapshot.of(ID, MUST_IT_ITEM_NO, SELLER_ID);
            ProductSnapshot snapshot2 = ProductSnapshot.of(ID, MUST_IT_ITEM_NO, SELLER_ID);

            // When & Then
            assertThat(snapshot1).isEqualTo(snapshot2);
        }

        @Test
        @DisplayName("다른 ID를 가진 두 Snapshot은 다르다")
        void shouldNotBeEqualForDifferentId() {
            // Given
            ProductSnapshotId id1 = new ProductSnapshotId(1L);
            ProductSnapshotId id2 = new ProductSnapshotId(2L);
            ProductSnapshot snapshot1 = ProductSnapshot.of(id1, MUST_IT_ITEM_NO, SELLER_ID);
            ProductSnapshot snapshot2 = ProductSnapshot.of(id2, MUST_IT_ITEM_NO, SELLER_ID);

            // When & Then
            assertThat(snapshot1).isNotEqualTo(snapshot2);
        }

        @Test
        @DisplayName("같은 ID를 가진 두 Snapshot은 같은 hashCode를 반환한다")
        void shouldReturnSameHashCodeForSameId() {
            // Given
            ProductSnapshot snapshot1 = ProductSnapshot.of(ID, MUST_IT_ITEM_NO, SELLER_ID);
            ProductSnapshot snapshot2 = ProductSnapshot.of(ID, MUST_IT_ITEM_NO, SELLER_ID);

            // When & Then
            assertThat(snapshot1.hashCode()).isEqualTo(snapshot2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 주요 정보를 포함한다")
        void shouldContainKeyInformation() {
            // Given
            ProductSnapshot snapshot = ProductSnapshot.forNew(MUST_IT_ITEM_NO, SELLER_ID);
            snapshot.updateProductName("테스트 상품");

            // When
            String result = snapshot.toString();

            // Then
            assertThat(result).contains("ProductSnapshot");
            assertThat(result).contains("mustItItemNo=" + MUST_IT_ITEM_NO);
            assertThat(result).contains("sellerId=" + SELLER_ID);
            assertThat(result).contains("version=2");  // 초기 1 + productName 업데이트 1
            assertThat(result).contains("isComplete=false");
        }
    }
}
