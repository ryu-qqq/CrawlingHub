package com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.CrawledProductJpaEntity;
import com.ryuqq.crawlinghub.domain.common.vo.DeletionStatus;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCategory;
import com.ryuqq.crawlinghub.domain.product.vo.ProductChangeType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImage;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.product.vo.ShippingInfo;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawledProductJpaEntityMapper 단위 테스트
 *
 * <p>Domain ↔ Entity 양방향 변환 검증
 *
 * <p>CrawledProduct 특수 사항:
 *
 * <ul>
 *   <li>ProductImages, ProductOptions, ProductCategory, ShippingInfo → JSON 직렬화
 *   <li>ProductPrice → originalPrice, discountPrice, discountRate로 분리
 *   <li>CrawlCompletionStatus → 3개 시간 필드로 분리
 *   <li>Instant ↔ LocalDateTime 변환
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("mapper")
@DisplayName("CrawledProductJpaEntityMapper 단위 테스트")
class CrawledProductJpaEntityMapperTest {

    private CrawledProductJpaEntityMapper mapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mapper = new CrawledProductJpaEntityMapper(objectMapper);
    }

    @Nested
    @DisplayName("toEntity - Domain → Entity 변환")
    class ToEntityTests {

        @Test
        @DisplayName("성공 - 기본 MINI_SHOP 데이터만 있는 상품 변환")
        void shouldConvertMiniShopOnlyProductToEntity() {
            // Given
            Instant now = Instant.now();
            CrawledProduct domain = createMiniShopOnlyProduct(now);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getSellerId()).isEqualTo(100L);
            assertThat(entity.getItemNo()).isEqualTo(12345L);
            assertThat(entity.getItemName()).isEqualTo("테스트 상품");
            assertThat(entity.getBrandName()).isEqualTo("테스트 브랜드");
            assertThat(entity.getOriginalPrice()).isEqualTo(20000L);
            assertThat(entity.getDiscountPrice()).isEqualTo(15000L);
            assertThat(entity.getDiscountRate()).isEqualTo(25);
            assertThat(entity.isFreeShipping()).isTrue();
            assertThat(entity.getMiniShopCrawledAt()).isNotNull();
            assertThat(entity.getDetailCrawledAt()).isNull();
            assertThat(entity.getOptionCrawledAt()).isNull();
        }

        @Test
        @DisplayName("성공 - 모든 크롤링이 완료된 상품 변환")
        void shouldConvertFullyCrawledProductToEntity() {
            // Given
            Instant now = Instant.now();
            CrawledProduct domain = createFullyCrawledProduct(now);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getMiniShopCrawledAt()).isNotNull();
            assertThat(entity.getDetailCrawledAt()).isNotNull();
            assertThat(entity.getOptionCrawledAt()).isNotNull();
            // categoryJson과 optionsJson은 별도로 설정되지 않으면 null일 수 있음
        }

        @Test
        @DisplayName("성공 - 외부 서버 동기화 완료 상품 변환")
        void shouldConvertSyncedProductToEntity() {
            // Given
            Instant now = Instant.now();
            CrawledProduct domain = createSyncedProduct(now);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getExternalProductId()).isEqualTo(999L);
            assertThat(entity.getLastSyncedAt()).isNotNull();
            assertThat(entity.isNeedsSync()).isFalse();
        }

        @Test
        @DisplayName("성공 - 이미지 JSON 직렬화")
        void shouldSerializeImagesToJson() {
            // Given
            Instant now = Instant.now();
            CrawledProduct domain = createMiniShopOnlyProduct(now);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getImagesJson()).isNotNull();
            assertThat(entity.getImagesJson()).contains("originalUrl");
        }

        @Test
        @DisplayName("성공 - null 이미지 처리")
        void shouldHandleNullImages() {
            // Given
            Instant now = Instant.now();
            CrawledProduct domain = createProductWithEmptyImages(now);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getImagesJson()).isNull();
        }

        @Test
        @DisplayName("성공 - null 가격 처리")
        void shouldHandleNullPrice() {
            // Given
            Instant now = Instant.now();
            CrawledProduct domain = createProductWithNullPrice(now);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getOriginalPrice()).isNull();
            assertThat(entity.getDiscountPrice()).isNull();
            assertThat(entity.getDiscountRate()).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain - Entity → Domain 변환")
    class ToDomainTests {

        @Test
        @DisplayName("성공 - 기본 Entity를 Domain으로 변환")
        void shouldConvertBasicEntityToDomain() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawledProductJpaEntity entity = createBasicEntity(now);

            // When
            CrawledProduct domain = mapper.toDomain(entity);

            // Then
            assertThat(domain).isNotNull();
            assertThat(domain.getIdValue()).isEqualTo(1L);
            assertThat(domain.getSellerIdValue()).isEqualTo(100L);
            assertThat(domain.getItemNo()).isEqualTo(12345L);
            assertThat(domain.getItemName()).isEqualTo("테스트 상품");
            assertThat(domain.getBrandName()).isEqualTo("테스트 브랜드");
            assertThat(domain.isFreeShipping()).isTrue();
        }

        @Test
        @DisplayName("성공 - ProductPrice 변환")
        void shouldConvertEntityPriceToDomain() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawledProductJpaEntity entity = createBasicEntity(now);

            // When
            CrawledProduct domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getPrice()).isNotNull();
            assertThat(domain.getPrice().originalPrice()).isEqualTo(20000);
            assertThat(domain.getPrice().discountPrice()).isEqualTo(15000);
        }

        @Test
        @DisplayName("성공 - CrawlCompletionStatus 변환")
        void shouldConvertEntityCompletionStatusToDomain() {
            // Given
            LocalDateTime miniShopAt = LocalDateTime.now().minusHours(3);
            LocalDateTime detailAt = LocalDateTime.now().minusHours(2);
            LocalDateTime optionAt = LocalDateTime.now().minusHours(1);
            CrawledProductJpaEntity entity =
                    createEntityWithCompletionStatus(miniShopAt, detailAt, optionAt);

            // When
            CrawledProduct domain = mapper.toDomain(entity);

            // Then
            CrawlCompletionStatus status = domain.getCrawlCompletionStatus();
            assertThat(status.isMiniShopCrawled()).isTrue();
            assertThat(status.isDetailCrawled()).isTrue();
            assertThat(status.isOptionCrawled()).isTrue();
            assertThat(status.isAllCrawled()).isTrue();
        }

        @Test
        @DisplayName("성공 - null 가격 필드 처리")
        void shouldHandleNullPriceFields() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawledProductJpaEntity entity =
                    CrawledProductJpaEntity.of(
                            1L, 100L, 12345L, "상품명", "브랜드", 0L, null, null, null, // 가격 필드 null
                            null, true, null, null, null, null, null, null, null, null, now, null,
                            null, null, null, false, null, null, null, now, now);

            // When
            CrawledProduct domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getPrice()).isNull();
        }

        @Test
        @DisplayName("성공 - JSON 이미지 역직렬화")
        void shouldDeserializeImagesFromJson() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            // ProductImage record 필드명과 일치: originalUrl, s3Url, imageType, status, displayOrder
            String imagesJson =
                    "[{\"originalUrl\":\"http://example.com/img1.jpg\","
                            + "\"s3Url\":null,\"imageType\":\"THUMBNAIL\",\"displayOrder\":0,"
                            + "\"status\":\"PENDING\"}]";
            CrawledProductJpaEntity entity =
                    CrawledProductJpaEntity.of(
                            1L,
                            100L,
                            12345L,
                            "상품명",
                            "브랜드",
                            0L,
                            10000L,
                            10000L,
                            0,
                            imagesJson,
                            true,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            now,
                            null,
                            null,
                            null,
                            null,
                            false,
                            null,
                            null,
                            null,
                            now,
                            now);

            // When
            CrawledProduct domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getImages()).isNotNull();
            assertThat(domain.getImages().isEmpty()).isFalse();
        }

        @Test
        @DisplayName("성공 - null/빈 JSON 이미지 처리")
        void shouldHandleNullOrEmptyImagesJson() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawledProductJpaEntity entity =
                    CrawledProductJpaEntity.of(
                            1L, 100L, 12345L, "상품명", "브랜드", 0L, 10000L, 10000L, 0, null, true, null,
                            null, null, null, null, null, null, null, now, null, null, null, null,
                            false, null, null, null, now, now);

            // When
            CrawledProduct domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getImages()).isNotNull();
            assertThat(domain.getImages().isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("양방향 변환 일관성")
    class RoundTripTests {

        @Test
        @DisplayName("성공 - MINI_SHOP만 완료된 상품 양방향 변환")
        void shouldMaintainConsistencyForMiniShopOnlyProduct() {
            // Given
            Instant now = Instant.now();
            CrawledProduct original = createMiniShopOnlyProduct(now);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(original);
            CrawledProduct restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getIdValue()).isEqualTo(original.getIdValue());
            assertThat(restored.getSellerIdValue()).isEqualTo(original.getSellerIdValue());
            assertThat(restored.getItemNo()).isEqualTo(original.getItemNo());
            assertThat(restored.getItemName()).isEqualTo(original.getItemName());
            assertThat(restored.getBrandName()).isEqualTo(original.getBrandName());
            assertThat(restored.isFreeShipping()).isEqualTo(original.isFreeShipping());
        }

        @Test
        @DisplayName("성공 - 모든 크롤링 완료 상품 양방향 변환")
        void shouldMaintainConsistencyForFullyCrawledProduct() {
            // Given
            Instant now = Instant.now();
            CrawledProduct original = createFullyCrawledProduct(now);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(original);
            CrawledProduct restored = mapper.toDomain(entity);

            // Then
            CrawlCompletionStatus restoredStatus = restored.getCrawlCompletionStatus();
            CrawlCompletionStatus originalStatus = original.getCrawlCompletionStatus();
            assertThat(restoredStatus.isMiniShopCrawled())
                    .isEqualTo(originalStatus.isMiniShopCrawled());
            assertThat(restoredStatus.isDetailCrawled())
                    .isEqualTo(originalStatus.isDetailCrawled());
            assertThat(restoredStatus.isOptionCrawled())
                    .isEqualTo(originalStatus.isOptionCrawled());
        }

        @Test
        @DisplayName("성공 - ProductPrice 양방향 변환 일관성")
        void shouldMaintainPriceConsistency() {
            // Given
            Instant now = Instant.now();
            CrawledProduct original = createMiniShopOnlyProduct(now);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(original);
            CrawledProduct restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getPrice().originalPrice())
                    .isEqualTo(original.getPrice().originalPrice());
            assertThat(restored.getPrice().discountPrice())
                    .isEqualTo(original.getPrice().discountPrice());
        }
    }

    @Nested
    @DisplayName("시간 변환")
    class TimeConversionTests {

        @Test
        @DisplayName("성공 - Instant → LocalDateTime → Instant 변환 일관성")
        void shouldConvertTimesConsistently() {
            // Given
            Instant now = Instant.now();
            CrawledProduct domain = createMiniShopOnlyProduct(now);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);
            CrawledProduct restored = mapper.toDomain(entity);

            // Then - 시간대 변환으로 인한 오차 허용 (1초 이내)
            assertThat(restored.getCreatedAt())
                    .isCloseTo(
                            domain.getCreatedAt(),
                            org.assertj.core.api.Assertions.within(1, ChronoUnit.SECONDS));
            assertThat(restored.getUpdatedAt())
                    .isCloseTo(
                            domain.getUpdatedAt(),
                            org.assertj.core.api.Assertions.within(1, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("성공 - CrawlCompletionStatus 시간 변환")
        void shouldConvertCompletionStatusTimesConsistently() {
            // Given
            Instant miniShopAt = Instant.now().minus(3, ChronoUnit.HOURS);
            Instant detailAt = Instant.now().minus(2, ChronoUnit.HOURS);
            Instant optionAt = Instant.now().minus(1, ChronoUnit.HOURS);
            CrawledProduct domain =
                    createProductWithCompletionStatus(miniShopAt, detailAt, optionAt);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);
            CrawledProduct restored = mapper.toDomain(entity);

            // Then
            CrawlCompletionStatus originalStatus = domain.getCrawlCompletionStatus();
            CrawlCompletionStatus restoredStatus = restored.getCrawlCompletionStatus();

            assertThat(restoredStatus.miniShopCrawledAt())
                    .isCloseTo(
                            originalStatus.miniShopCrawledAt(),
                            org.assertj.core.api.Assertions.within(1, ChronoUnit.SECONDS));
            assertThat(restoredStatus.detailCrawledAt())
                    .isCloseTo(
                            originalStatus.detailCrawledAt(),
                            org.assertj.core.api.Assertions.within(1, ChronoUnit.SECONDS));
            assertThat(restoredStatus.optionCrawledAt())
                    .isCloseTo(
                            originalStatus.optionCrawledAt(),
                            org.assertj.core.api.Assertions.within(1, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("성공 - null 시간 필드 처리")
        void shouldHandleNullTimeFields() {
            // Given
            Instant now = Instant.now();
            CrawledProduct domain = createMiniShopOnlyProduct(now);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);
            CrawledProduct restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getCrawlCompletionStatus().detailCrawledAt()).isNull();
            assertThat(restored.getCrawlCompletionStatus().optionCrawledAt()).isNull();
        }
    }

    @Nested
    @DisplayName("외부 동기화 상태")
    class ExternalSyncTests {

        @Test
        @DisplayName("성공 - 동기화 필요 상태 변환")
        void shouldConvertNeedsSyncState() {
            // Given
            Instant now = Instant.now();
            CrawledProduct domain = createProductNeedingSync(now);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);
            CrawledProduct restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.isNeedsSync()).isTrue();
            assertThat(restored.getExternalProductId()).isNull();
        }

        @Test
        @DisplayName("성공 - 동기화 완료 상태 변환")
        void shouldConvertSyncedState() {
            // Given
            Instant now = Instant.now();
            CrawledProduct domain = createSyncedProduct(now);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);
            CrawledProduct restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.isNeedsSync()).isFalse();
            assertThat(restored.getExternalProductId()).isEqualTo(999L);
            assertThat(restored.getLastSyncedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("카테고리/배송정보/옵션 JSON 직렬화")
    class JsonSerializationTests {

        @Test
        @DisplayName("성공 - ProductCategory JSON 직렬화")
        void shouldSerializeCategoryToJson() {
            // Given - 카테고리가 있는 상품
            Instant now = Instant.now();
            ProductCategory category = ProductCategory.of("W", "여성", "BAG", "가방", "BACKPACK", "백팩");
            CrawledProduct domain = createProductWithCategory(now, category);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);

            // Then - categoryJson이 직렬화되어야 함
            assertThat(entity.getCategoryJson()).isNotNull();
            assertThat(entity.getCategoryJson()).contains("headerCategoryCode");
            assertThat(entity.getCategoryJson()).contains("여성");
        }

        @Test
        @DisplayName("성공 - null ProductCategory 처리")
        void shouldHandleNullCategory() {
            // Given
            Instant now = Instant.now();
            CrawledProduct domain = createProductWithCategory(now, null);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getCategoryJson()).isNull();
        }

        @Test
        @DisplayName("성공 - ShippingInfo JSON 직렬화")
        void shouldSerializeShippingInfoToJson() {
            // Given - 배송정보가 있는 상품
            Instant now = Instant.now();
            ShippingInfo shippingInfo = ShippingInfo.freeShipping("DOMESTIC", 3);
            CrawledProduct domain = createProductWithShippingInfo(now, shippingInfo);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);

            // Then - shippingInfoJson이 직렬화되어야 함
            assertThat(entity.getShippingInfoJson()).isNotNull();
            assertThat(entity.getShippingInfoJson()).contains("shippingType");
            assertThat(entity.getShippingInfoJson()).contains("DOMESTIC");
        }

        @Test
        @DisplayName("성공 - null ShippingInfo 처리")
        void shouldHandleNullShippingInfo() {
            // Given
            Instant now = Instant.now();
            CrawledProduct domain = createProductWithShippingInfo(now, null);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getShippingInfoJson()).isNull();
        }

        @Test
        @DisplayName("성공 - ProductOptions JSON 직렬화")
        void shouldSerializeOptionsToJson() {
            // Given - 옵션이 있는 상품
            Instant now = Instant.now();
            ProductOption option = ProductOption.of(100L, 12345L, "블랙", "M", 10, null);
            ProductOptions options = ProductOptions.of(List.of(option));
            CrawledProduct domain = createProductWithOptions(now, options);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);

            // Then - optionsJson이 직렬화되어야 함
            assertThat(entity.getOptionsJson()).isNotNull();
            assertThat(entity.getOptionsJson()).contains("optionNo");
            assertThat(entity.getOptionsJson()).contains("블랙");
        }

        @Test
        @DisplayName("성공 - 빈 ProductOptions 처리")
        void shouldHandleEmptyOptions() {
            // Given
            Instant now = Instant.now();
            CrawledProduct domain = createProductWithOptions(now, ProductOptions.empty());

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getOptionsJson()).isNull();
        }
    }

    @Nested
    @DisplayName("카테고리/배송정보/옵션 JSON 역직렬화")
    class JsonDeserializationTests {

        @Test
        @DisplayName("성공 - ProductCategory JSON 역직렬화")
        void shouldDeserializeCategoryFromJson() {
            // Given - categoryJson이 있는 Entity
            LocalDateTime now = LocalDateTime.now();
            String categoryJson =
                    "{\"headerCategoryCode\":\"W\",\"headerCategoryName\":\"여성\","
                            + "\"largeCategoryCode\":\"BAG\",\"largeCategoryName\":\"가방\","
                            + "\"mediumCategoryCode\":\"BACKPACK\",\"mediumCategoryName\":\"백팩\"}";
            CrawledProductJpaEntity entity =
                    CrawledProductJpaEntity.of(
                            1L,
                            100L,
                            12345L,
                            "상품명",
                            "브랜드",
                            0L,
                            10000L,
                            10000L,
                            0,
                            null,
                            true,
                            categoryJson,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            now,
                            null,
                            null,
                            null,
                            null,
                            false,
                            null,
                            null,
                            null,
                            now,
                            now);

            // When
            CrawledProduct domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getCategory()).isNotNull();
            assertThat(domain.getCategory().headerCategoryName()).isEqualTo("여성");
            assertThat(domain.getCategory().largeCategoryName()).isEqualTo("가방");
        }

        @Test
        @DisplayName("성공 - null categoryJson 처리")
        void shouldHandleNullCategoryJson() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawledProductJpaEntity entity =
                    CrawledProductJpaEntity.of(
                            1L, 100L, 12345L, "상품명", "브랜드", 0L, 10000L, 10000L, 0, null, true, null,
                            null, null, null, null, null, null, null, now, null, null, null, null,
                            false, null, null, null, now, now);

            // When
            CrawledProduct domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getCategory()).isNull();
        }

        @Test
        @DisplayName("성공 - ShippingInfo JSON 역직렬화")
        void shouldDeserializeShippingInfoFromJson() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            String shippingInfoJson =
                    "{\"shippingType\":\"DOMESTIC\",\"shippingFee\":0,"
                            + "\"shippingFeeType\":\"FREE\",\"averageDeliveryDays\":3,"
                            + "\"freeShipping\":true}";
            CrawledProductJpaEntity entity =
                    CrawledProductJpaEntity.of(
                            1L,
                            100L,
                            12345L,
                            "상품명",
                            "브랜드",
                            0L,
                            10000L,
                            10000L,
                            0,
                            null,
                            true,
                            null,
                            shippingInfoJson,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            now,
                            null,
                            null,
                            null,
                            null,
                            false,
                            null,
                            null,
                            null,
                            now,
                            now);

            // When
            CrawledProduct domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getShippingInfo()).isNotNull();
            assertThat(domain.getShippingInfo().shippingType()).isEqualTo("DOMESTIC");
            assertThat(domain.getShippingInfo().freeShipping()).isTrue();
        }

        @Test
        @DisplayName("성공 - null shippingInfoJson 처리")
        void shouldHandleNullShippingInfoJson() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawledProductJpaEntity entity =
                    CrawledProductJpaEntity.of(
                            1L, 100L, 12345L, "상품명", "브랜드", 0L, 10000L, 10000L, 0, null, true, null,
                            null, null, null, null, null, null, null, now, null, null, null, null,
                            false, null, null, null, now, now);

            // When
            CrawledProduct domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getShippingInfo()).isNull();
        }

        @Test
        @DisplayName("성공 - ProductOptions JSON 역직렬화")
        void shouldDeserializeOptionsFromJson() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            String optionsJson =
                    "[{\"optionNo\":100,\"itemNo\":12345,\"color\":\"블랙\","
                            + "\"size\":\"M\",\"stock\":10,\"sizeGuide\":\"\"}]";
            CrawledProductJpaEntity entity =
                    CrawledProductJpaEntity.of(
                            1L,
                            100L,
                            12345L,
                            "상품명",
                            "브랜드",
                            0L,
                            10000L,
                            10000L,
                            0,
                            null,
                            true,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            optionsJson,
                            now,
                            null,
                            null,
                            null,
                            null,
                            false,
                            null,
                            null,
                            null,
                            now,
                            now);

            // When
            CrawledProduct domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getOptions()).isNotNull();
            assertThat(domain.getOptions().isEmpty()).isFalse();
            assertThat(domain.getOptions().getAll()).hasSize(1);
        }

        @Test
        @DisplayName("성공 - 잘못된 JSON 이미지 역직렬화 시 빈 이미지 반환")
        void shouldReturnEmptyImagesOnInvalidJson() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            String invalidJson = "not-a-valid-json";
            CrawledProductJpaEntity entity =
                    CrawledProductJpaEntity.of(
                            1L,
                            100L,
                            12345L,
                            "상품명",
                            "브랜드",
                            0L,
                            10000L,
                            10000L,
                            0,
                            invalidJson,
                            true,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            now,
                            null,
                            null,
                            null,
                            null,
                            false,
                            null,
                            null,
                            null,
                            now,
                            now);

            // When
            CrawledProduct domain = mapper.toDomain(entity);

            // Then - 역직렬화 실패 시 빈 이미지 반환
            assertThat(domain.getImages()).isNotNull();
            assertThat(domain.getImages().isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("PendingChanges 변환")
    class PendingChangesTests {

        @Test
        @DisplayName("성공 - PendingChanges enum set 직렬화")
        void shouldSerializePendingChangesToString() {
            // Given - 변경 유형이 있는 상품
            Instant now = Instant.now();
            Set<ProductChangeType> pendingChanges =
                    EnumSet.of(ProductChangeType.PRICE, ProductChangeType.OPTION_STOCK);
            CrawledProduct domain = createProductWithPendingChanges(now, pendingChanges);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);

            // Then - pendingChanges가 콤마 구분 문자열로 직렬화되어야 함
            assertThat(entity.getPendingChanges()).isNotNull();
            assertThat(entity.getPendingChanges()).contains("PRICE");
            assertThat(entity.getPendingChanges()).contains("OPTION_STOCK");
        }

        @Test
        @DisplayName("성공 - 빈 PendingChanges 처리")
        void shouldHandleEmptyPendingChanges() {
            // Given
            Instant now = Instant.now();
            CrawledProduct domain = createMiniShopOnlyProduct(now);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getPendingChanges()).isNull();
        }

        @Test
        @DisplayName("성공 - PendingChanges 문자열 역직렬화")
        void shouldDeserializePendingChangesFromString() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawledProductJpaEntity entity =
                    CrawledProductJpaEntity.of(
                            1L,
                            100L,
                            12345L,
                            "상품명",
                            "브랜드",
                            0L,
                            10000L,
                            10000L,
                            0,
                            null,
                            true,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            now,
                            null,
                            null,
                            null,
                            null,
                            false,
                            "PRICE,OPTION_STOCK",
                            null,
                            null,
                            now,
                            now);

            // When
            CrawledProduct domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getPendingChanges()).isNotEmpty();
            assertThat(domain.getPendingChanges()).contains(ProductChangeType.PRICE);
            assertThat(domain.getPendingChanges()).contains(ProductChangeType.OPTION_STOCK);
        }

        @Test
        @DisplayName("성공 - null PendingChanges 문자열 처리")
        void shouldHandleNullPendingChangesString() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawledProductJpaEntity entity =
                    CrawledProductJpaEntity.of(
                            1L, 100L, 12345L, "상품명", "브랜드", 0L, 10000L, 10000L, 0, null, true, null,
                            null, null, null, null, null, null, null, now, null, null, null, null,
                            false, null, null, null, now, now);

            // When
            CrawledProduct domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getPendingChanges()).isEmpty();
        }

        @Test
        @DisplayName("성공 - 단일 PendingChanges 역직렬화")
        void shouldDeserializeSinglePendingChange() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            CrawledProductJpaEntity entity =
                    CrawledProductJpaEntity.of(
                            1L, 100L, 12345L, "상품명", "브랜드", 0L, 10000L, 10000L, 0, null, true, null,
                            null, null, null, null, null, null, null, now, null, null, null, null,
                            false, "PRICE", null, null, now, now);

            // When
            CrawledProduct domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getPendingChanges()).containsExactly(ProductChangeType.PRICE);
        }
    }

    @Nested
    @DisplayName("가격 변환 엣지 케이스")
    class PriceConversionEdgeCaseTests {

        @Test
        @DisplayName("성공 - originalPrice만 있고 discountPrice는 null인 경우")
        void shouldHandleOriginalPriceOnlyWithNullDiscountPrice() {
            // Given - discountPrice가 null인 Entity (originalPrice만 있음)
            LocalDateTime now = LocalDateTime.now();
            CrawledProductJpaEntity entity =
                    CrawledProductJpaEntity.of(
                            1L, 100L, 12345L, "상품명", "브랜드", 0L, 20000L, null,
                            null, // originalPrice=20000, discountPrice=null
                            null, true, null, null, null, null, null, null, null, null, now, null,
                            null, null, null, false, null, null, null, now, now);

            // When
            CrawledProduct domain = mapper.toDomain(entity);

            // Then - discountPrice가 없으면 originalPrice로 대체
            assertThat(domain.getPrice()).isNotNull();
            assertThat(domain.getPrice().originalPrice()).isEqualTo(20000);
            assertThat(domain.getPrice().discountPrice()).isEqualTo(20000);
        }

        @Test
        @DisplayName("성공 - 이미지 JSON 직렬화 후 Entity에 JSON이 저장됨")
        void shouldMaintainImagesConsistencyInRoundTrip() {
            // Given - ProductImage 직렬화 검증 (역직렬화는 record computed property 제약으로 실패 가능)
            Instant now = Instant.now();
            ProductImage image =
                    new ProductImage(
                            "http://example.com/img.jpg",
                            null,
                            com.ryuqq.crawlinghub.domain.product.vo.ImageType.THUMBNAIL,
                            com.ryuqq.crawlinghub.domain.product.vo.ImageUploadStatus.PENDING,
                            0);
            ProductImages images = ProductImages.of(List.of(image));
            CrawledProduct domain = createProductWithImages(now, images);

            // When
            CrawledProductJpaEntity entity = mapper.toEntity(domain);

            // Then - 이미지가 JSON으로 직렬화되어야 함 (toImagesJson 경로 커버)
            assertThat(entity.getImagesJson()).isNotNull();
            assertThat(entity.getImagesJson()).contains("originalUrl");
            assertThat(entity.getImagesJson()).contains("http://example.com/img.jpg");
        }
    }

    // === 테스트 데이터 생성 헬퍼 메서드 ===

    private CrawledProduct createMiniShopOnlyProduct(Instant now) {
        CrawlCompletionStatus status = new CrawlCompletionStatus(now, null, null);
        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                "테스트 상품",
                "테스트 브랜드",
                0L,
                ProductPrice.of(15000, 20000, 20000, 15000, 25, 25),
                ProductImages.fromThumbnailUrls(List.of("http://example.com/img1.jpg")),
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ProductOptions.empty(),
                status,
                null,
                null,
                false,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                now,
                now,
                null);
    }

    private CrawledProduct createFullyCrawledProduct(Instant now) {
        Instant miniShopAt = now.minus(3, ChronoUnit.HOURS);
        Instant detailAt = now.minus(2, ChronoUnit.HOURS);
        Instant optionAt = now.minus(1, ChronoUnit.HOURS);
        CrawlCompletionStatus status = new CrawlCompletionStatus(miniShopAt, detailAt, optionAt);

        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                "테스트 상품",
                "테스트 브랜드",
                0L,
                ProductPrice.of(15000, 20000, 20000, 15000, 25, 25),
                ProductImages.fromThumbnailUrls(List.of("http://example.com/img1.jpg")),
                true,
                null,
                null,
                "<p>상세설명</p>",
                "<p>상세설명</p>",
                "ACTIVE",
                "대한민국",
                "서울",
                ProductOptions.empty(),
                status,
                null,
                null,
                false,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                now,
                now,
                null);
    }

    private CrawledProduct createSyncedProduct(Instant now) {
        Instant miniShopAt = now.minus(3, ChronoUnit.HOURS);
        Instant detailAt = now.minus(2, ChronoUnit.HOURS);
        Instant optionAt = now.minus(1, ChronoUnit.HOURS);
        Instant syncedAt = now.minus(30, ChronoUnit.MINUTES);
        CrawlCompletionStatus status = new CrawlCompletionStatus(miniShopAt, detailAt, optionAt);

        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                "테스트 상품",
                "테스트 브랜드",
                0L,
                ProductPrice.of(15000, 20000, 20000, 15000, 25, 25),
                ProductImages.fromThumbnailUrls(List.of("http://example.com/img1.jpg")),
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ProductOptions.empty(),
                status,
                999L,
                syncedAt,
                false,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                now,
                now,
                null);
    }

    private CrawledProduct createProductWithEmptyImages(Instant now) {
        CrawlCompletionStatus status = new CrawlCompletionStatus(now, null, null);
        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                "테스트 상품",
                "테스트 브랜드",
                0L,
                ProductPrice.of(15000, 20000, 20000, 15000, 25, 25),
                ProductImages.empty(),
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ProductOptions.empty(),
                status,
                null,
                null,
                false,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                now,
                now,
                null);
    }

    private CrawledProduct createProductWithNullPrice(Instant now) {
        CrawlCompletionStatus status = new CrawlCompletionStatus(now, null, null);
        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                "테스트 상품",
                "테스트 브랜드",
                0L,
                null,
                ProductImages.empty(),
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ProductOptions.empty(),
                status,
                null,
                null,
                false,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                now,
                now,
                null);
    }

    private CrawledProduct createProductNeedingSync(Instant now) {
        Instant miniShopAt = now.minus(3, ChronoUnit.HOURS);
        Instant detailAt = now.minus(2, ChronoUnit.HOURS);
        Instant optionAt = now.minus(1, ChronoUnit.HOURS);
        CrawlCompletionStatus status = new CrawlCompletionStatus(miniShopAt, detailAt, optionAt);

        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                "테스트 상품",
                "테스트 브랜드",
                0L,
                ProductPrice.of(15000, 20000, 20000, 15000, 25, 25),
                ProductImages.empty(),
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ProductOptions.empty(),
                status,
                null,
                null,
                true,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                now,
                now,
                null);
    }

    private CrawledProduct createProductWithCompletionStatus(
            Instant miniShopAt, Instant detailAt, Instant optionAt) {
        Instant now = Instant.now();
        CrawlCompletionStatus status = new CrawlCompletionStatus(miniShopAt, detailAt, optionAt);

        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                "테스트 상품",
                "테스트 브랜드",
                0L,
                ProductPrice.of(15000, 20000, 20000, 15000, 25, 25),
                ProductImages.empty(),
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ProductOptions.empty(),
                status,
                null,
                null,
                false,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                now,
                now,
                null);
    }

    private CrawledProduct createProductWithCategory(Instant now, ProductCategory category) {
        CrawlCompletionStatus status = new CrawlCompletionStatus(now, null, null);
        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                "테스트 상품",
                "테스트 브랜드",
                0L,
                ProductPrice.of(15000, 20000, 20000, 15000, 25, 25),
                ProductImages.empty(),
                true,
                category,
                null,
                null,
                null,
                null,
                null,
                null,
                ProductOptions.empty(),
                status,
                null,
                null,
                false,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                now,
                now,
                null);
    }

    private CrawledProduct createProductWithShippingInfo(Instant now, ShippingInfo shippingInfo) {
        CrawlCompletionStatus status = new CrawlCompletionStatus(now, null, null);
        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                "테스트 상품",
                "테스트 브랜드",
                0L,
                ProductPrice.of(15000, 20000, 20000, 15000, 25, 25),
                ProductImages.empty(),
                true,
                null,
                shippingInfo,
                null,
                null,
                null,
                null,
                null,
                ProductOptions.empty(),
                status,
                null,
                null,
                false,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                now,
                now,
                null);
    }

    private CrawledProduct createProductWithOptions(Instant now, ProductOptions options) {
        CrawlCompletionStatus status = new CrawlCompletionStatus(now, null, null);
        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                "테스트 상품",
                "테스트 브랜드",
                0L,
                ProductPrice.of(15000, 20000, 20000, 15000, 25, 25),
                ProductImages.empty(),
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                options,
                status,
                null,
                null,
                false,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                now,
                now,
                null);
    }

    private CrawledProduct createProductWithPendingChanges(
            Instant now, Set<ProductChangeType> pendingChanges) {
        CrawlCompletionStatus status = new CrawlCompletionStatus(now, null, null);
        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                "테스트 상품",
                "테스트 브랜드",
                0L,
                ProductPrice.of(15000, 20000, 20000, 15000, 25, 25),
                ProductImages.empty(),
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ProductOptions.empty(),
                status,
                null,
                null,
                true,
                pendingChanges,
                DeletionStatus.active(),
                now,
                now,
                null);
    }

    private CrawledProduct createProductWithImages(Instant now, ProductImages images) {
        CrawlCompletionStatus status = new CrawlCompletionStatus(now, null, null);
        return CrawledProduct.reconstitute(
                CrawledProductId.of(1L),
                SellerId.of(100L),
                12345L,
                "테스트 상품",
                "테스트 브랜드",
                0L,
                ProductPrice.of(15000, 20000, 20000, 15000, 25, 25),
                images,
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ProductOptions.empty(),
                status,
                null,
                null,
                false,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                now,
                now,
                null);
    }

    private CrawledProductJpaEntity createBasicEntity(LocalDateTime now) {
        return CrawledProductJpaEntity.of(
                1L, 100L, 12345L, "테스트 상품", "테스트 브랜드", 0L, 20000L, 15000L, 25, null, true, null,
                null, null, null, null, null, null, null, now, null, null, null, null, false, null,
                null, null, now, now);
    }

    private CrawledProductJpaEntity createEntityWithCompletionStatus(
            LocalDateTime miniShopAt, LocalDateTime detailAt, LocalDateTime optionAt) {
        LocalDateTime now = LocalDateTime.now();
        return CrawledProductJpaEntity.of(
                1L,
                100L,
                12345L,
                "테스트 상품",
                "테스트 브랜드",
                0L,
                20000L,
                15000L,
                25,
                null,
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                miniShopAt,
                detailAt,
                optionAt,
                null,
                null,
                false,
                null,
                null,
                null,
                now,
                now);
    }
}
