package com.ryuqq.crawlinghub.application.product.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.product.vo.DetailCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;
import com.ryuqq.crawlinghub.domain.product.vo.OptionCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.ProductDetailInfo;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawledProductFactory 단위 테스트
 *
 * <p>순수 변환 팩토리이므로 Mock 없이 직접 인스턴스화하여 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@DisplayName("CrawledProductFactory 단위 테스트")
class CrawledProductFactoryTest {

    private CrawledProductFactory sut;

    @BeforeEach
    void setUp() {
        sut = new CrawledProductFactory();
    }

    @Nested
    @DisplayName("createMiniShopCrawlData() 메서드 테스트")
    class CreateMiniShopCrawlDataTest {

        @Test
        @DisplayName("[성공] MiniShopItem을 MiniShopCrawlData로 변환")
        void shouldConvertMiniShopItemToMiniShopCrawlData() {
            // Given
            SellerId sellerId = SellerId.of(100L);
            MiniShopItem item =
                    new MiniShopItem(
                            12345L,
                            List.of(
                                    "https://example.com/image1.jpg",
                                    "https://example.com/image2.jpg"),
                            "TestBrand",
                            "Test Product",
                            100000,
                            120000,
                            120000,
                            20,
                            25,
                            95000,
                            List.of());

            // When
            MiniShopCrawlData result = sut.createMiniShopCrawlData(sellerId, item);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.sellerId()).isEqualTo(sellerId);
            assertThat(result.itemNo()).isEqualTo(12345L);
            assertThat(result.itemName()).isEqualTo("Test Product");
            assertThat(result.brandName()).isEqualTo("TestBrand");
            assertThat(result.freeShipping()).isFalse();
        }

        @Test
        @DisplayName("[성공] 무료배송 태그가 있는 MiniShopItem 변환")
        void shouldSetFreeShippingWhenTagExists() {
            // Given
            SellerId sellerId = SellerId.of(100L);
            MiniShopItem item =
                    new MiniShopItem(
                            12345L,
                            List.of("https://example.com/image.jpg"),
                            "TestBrand",
                            "Free Shipping Product",
                            50000,
                            60000,
                            60000,
                            10,
                            15,
                            47500,
                            List.of(
                                    com.ryuqq.crawlinghub.domain.product.vo.ItemTag.of(
                                            "무료배송", "#000000", "#ffffff", "#dddddd")));

            // When
            MiniShopCrawlData result = sut.createMiniShopCrawlData(sellerId, item);

            // Then
            assertThat(result.freeShipping()).isTrue();
        }
    }

    @Nested
    @DisplayName("createDetailCrawlData() 메서드 테스트")
    class CreateDetailCrawlDataTest {

        @Test
        @DisplayName("[성공] ProductDetailInfo를 DetailCrawlData로 변환")
        void shouldConvertProductDetailInfoToDetailCrawlData() {
            // Given
            ProductDetailInfo detailInfo =
                    ProductDetailInfo.builder()
                            .sellerNo(1L)
                            .sellerId("seller001")
                            .itemNo(12345L)
                            .itemName("Test Product")
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

            // When
            DetailCrawlData result = sut.createDetailCrawlData(detailInfo);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.originCountry()).isEqualTo("Korea");
            assertThat(result.itemStatus()).isEqualTo("NEW");
            assertThat(result.descriptionImages()).contains("https://example.com/detail.jpg");
        }
    }

    @Nested
    @DisplayName("createOptionCrawlData() 메서드 테스트")
    class CreateOptionCrawlDataTest {

        @Test
        @DisplayName("[성공] ProductOption 목록을 OptionCrawlData로 변환")
        void shouldConvertProductOptionsToOptionCrawlData() {
            // Given
            List<ProductOption> options =
                    List.of(
                            ProductOption.of(1L, 12345L, "Black", "M", 10, ""),
                            ProductOption.of(2L, 12345L, "Black", "L", 5, ""),
                            ProductOption.of(3L, 12345L, "White", "M", 0, ""));

            // When
            OptionCrawlData result = sut.createOptionCrawlData(options);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.options().getAll()).hasSize(3);
        }

        @Test
        @DisplayName("[성공] 빈 옵션 목록을 OptionCrawlData로 변환")
        void shouldConvertEmptyOptionsToOptionCrawlData() {
            // Given
            List<ProductOption> options = List.of();

            // When
            OptionCrawlData result = sut.createOptionCrawlData(options);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.options().getAll()).isEmpty();
        }
    }
}
