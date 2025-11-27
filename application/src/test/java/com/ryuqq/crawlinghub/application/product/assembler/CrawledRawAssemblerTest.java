package com.ryuqq.crawlinghub.application.product.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;
import com.ryuqq.crawlinghub.domain.product.vo.ProductDetailInfo;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.product.vo.RawDataStatus;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawledRawAssembler 단위 테스트
 *
 * <p>Mockist 스타일 테스트: ObjectMapper 의존성 Spy
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledRawAssembler 테스트")
class CrawledRawAssemblerTest {

    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks private CrawledRawAssembler assembler;

    @Nested
    @DisplayName("toMiniShopRaws() 테스트")
    class ToMiniShopRaws {

        @Test
        @DisplayName("[성공] MiniShopItem 목록 → CrawledRaw 목록 변환")
        void shouldConvertMiniShopItemsToRaws() {
            // Given
            long schedulerId = 1L;
            long sellerId = 100L;
            List<MiniShopItem> items =
                    List.of(
                            createMiniShopItem(12345L, "상품1"),
                            createMiniShopItem(12346L, "상품2"),
                            createMiniShopItem(12347L, "상품3"));

            // When
            List<CrawledRaw> result = assembler.toMiniShopRaws(schedulerId, sellerId, items);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getCrawlSchedulerId()).isEqualTo(schedulerId);
            assertThat(result.get(0).getSellerId()).isEqualTo(sellerId);
            assertThat(result.get(0).getItemNo()).isEqualTo(12345L);
            assertThat(result.get(0).getCrawlType()).isEqualTo(CrawlType.MINI_SHOP);
            assertThat(result.get(0).getStatus()).isEqualTo(RawDataStatus.PENDING);
            assertThat(result.get(0).getRawData()).contains("상품1");
        }

        @Test
        @DisplayName("[성공] 빈 목록 → 빈 목록 반환")
        void shouldReturnEmptyListForEmptyItems() {
            // Given
            long schedulerId = 1L;
            long sellerId = 100L;
            List<MiniShopItem> items = List.of();

            // When
            List<CrawledRaw> result = assembler.toMiniShopRaws(schedulerId, sellerId, items);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[성공] 각 아이템이 별도의 CrawledRaw로 변환")
        void shouldCreateSeparateRawForEachItem() {
            // Given
            long schedulerId = 1L;
            long sellerId = 100L;
            List<MiniShopItem> items =
                    List.of(createMiniShopItem(111L, "첫번째"), createMiniShopItem(222L, "두번째"));

            // When
            List<CrawledRaw> result = assembler.toMiniShopRaws(schedulerId, sellerId, items);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getItemNo()).isEqualTo(111L);
            assertThat(result.get(1).getItemNo()).isEqualTo(222L);
            assertThat(result.get(0).getRawData()).isNotEqualTo(result.get(1).getRawData());
        }
    }

    @Nested
    @DisplayName("toDetailRaw() 테스트")
    class ToDetailRaw {

        @Test
        @DisplayName("[성공] ProductDetailInfo → CrawledRaw 변환")
        void shouldConvertDetailInfoToRaw() {
            // Given
            long schedulerId = 1L;
            long sellerId = 100L;
            ProductDetailInfo detailInfo = createProductDetailInfo(12345L, "테스트 상품");

            // When
            CrawledRaw result = assembler.toDetailRaw(schedulerId, sellerId, detailInfo);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCrawlSchedulerId()).isEqualTo(schedulerId);
            assertThat(result.getSellerId()).isEqualTo(sellerId);
            assertThat(result.getItemNo()).isEqualTo(12345L);
            assertThat(result.getCrawlType()).isEqualTo(CrawlType.DETAIL);
            assertThat(result.getStatus()).isEqualTo(RawDataStatus.PENDING);
            assertThat(result.getRawData()).contains("테스트 상품");
        }

        @Test
        @DisplayName("[성공] 상세 정보의 모든 필드가 JSON에 포함")
        void shouldIncludeAllFieldsInJson() {
            // Given
            long schedulerId = 1L;
            long sellerId = 100L;
            ProductDetailInfo detailInfo = createProductDetailInfo(12345L, "브랜드상품");

            // When
            CrawledRaw result = assembler.toDetailRaw(schedulerId, sellerId, detailInfo);

            // Then
            assertThat(result.getRawData()).contains("sellerNo");
            assertThat(result.getRawData()).contains("itemName");
            assertThat(result.getRawData()).contains("브랜드상품");
        }
    }

    @Nested
    @DisplayName("toOptionRaw() 테스트")
    class ToOptionRaw {

        @Test
        @DisplayName("[성공] ProductOption 목록 → CrawledRaw 변환")
        void shouldConvertOptionsToRaw() {
            // Given
            long schedulerId = 1L;
            long sellerId = 100L;
            long itemNo = 12345L;
            List<ProductOption> options =
                    List.of(
                            ProductOption.of(1L, itemNo, "Black", "M", 10, ""),
                            ProductOption.of(2L, itemNo, "Black", "L", 5, ""),
                            ProductOption.of(3L, itemNo, "White", "M", 0, ""));

            // When
            CrawledRaw result = assembler.toOptionRaw(schedulerId, sellerId, itemNo, options);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCrawlSchedulerId()).isEqualTo(schedulerId);
            assertThat(result.getSellerId()).isEqualTo(sellerId);
            assertThat(result.getItemNo()).isEqualTo(itemNo);
            assertThat(result.getCrawlType()).isEqualTo(CrawlType.OPTION);
            assertThat(result.getStatus()).isEqualTo(RawDataStatus.PENDING);
        }

        @Test
        @DisplayName("[성공] 옵션 목록이 JSON 배열로 직렬화")
        void shouldSerializeOptionsAsJsonArray() {
            // Given
            long schedulerId = 1L;
            long sellerId = 100L;
            long itemNo = 12345L;
            List<ProductOption> options =
                    List.of(ProductOption.of(1L, itemNo, "Black", "M", 10, ""));

            // When
            CrawledRaw result = assembler.toOptionRaw(schedulerId, sellerId, itemNo, options);

            // Then
            assertThat(result.getRawData()).startsWith("[");
            assertThat(result.getRawData()).endsWith("]");
            assertThat(result.getRawData()).contains("Black");
            assertThat(result.getRawData()).contains("optionNo");
        }

        @Test
        @DisplayName("[성공] 빈 옵션 목록 → 빈 배열 JSON")
        void shouldReturnEmptyArrayForEmptyOptions() {
            // Given
            long schedulerId = 1L;
            long sellerId = 100L;
            long itemNo = 12345L;
            List<ProductOption> options = List.of();

            // When
            CrawledRaw result = assembler.toOptionRaw(schedulerId, sellerId, itemNo, options);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRawData()).isEqualTo("[]");
        }
    }

    // === Helper Methods ===

    private MiniShopItem createMiniShopItem(long itemNo, String name) {
        return new MiniShopItem(
                itemNo,
                List.of("https://example.com/image.jpg"),
                "TestBrand",
                name,
                100000,
                120000,
                120000,
                20,
                25,
                95000,
                List.of());
    }

    private ProductDetailInfo createProductDetailInfo(long itemNo, String itemName) {
        return ProductDetailInfo.builder()
                .sellerNo(1L)
                .sellerId("seller001")
                .itemNo(itemNo)
                .itemName(itemName)
                .brandName("TestBrand")
                .brandNameKr("테스트브랜드")
                .brandCode(100L)
                .normalPrice(120000)
                .sellingPrice(100000)
                .discountPrice(100000)
                .discountRate(17)
                .stock(50)
                .isSoldOut(false)
                .bannerImages(List.of("https://example.com/banner.jpg"))
                .detailImages(List.of("https://example.com/detail.jpg"))
                .originCountry("Korea")
                .itemStatus("NEW")
                .build();
    }
}
