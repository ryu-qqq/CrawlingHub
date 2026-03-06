package com.ryuqq.crawlinghub.adapter.out.persistence.seller.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.SellerJpaEntity;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * SellerJpaEntityMapper 단위 테스트
 *
 * <p>Domain ↔ Entity 양방향 변환 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("mapper")
@DisplayName("SellerJpaEntityMapper 단위 테스트")
class SellerJpaEntityMapperTest {

    private SellerJpaEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SellerJpaEntityMapper();
    }

    @Nested
    @DisplayName("toEntity - Domain → Entity 변환")
    class ToEntityTests {

        @Test
        @DisplayName("성공 - 활성 셀러 변환")
        void shouldConvertActiveSellerToEntity() {
            // Given
            Seller domain = SellerFixture.anActiveSeller();

            // When
            SellerJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(domain.getSellerIdValue());
            assertThat(entity.getMustItSellerName()).isEqualTo(domain.getMustItSellerNameValue());
            assertThat(entity.getSellerName()).isEqualTo(domain.getSellerNameValue());
            assertThat(entity.getStatus()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(entity.getProductCount()).isEqualTo(domain.getProductCount());
            assertThat(entity.getCreatedAt()).isNotNull();
            assertThat(entity.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - 비활성 셀러 변환")
        void shouldConvertInactiveSellerToEntity() {
            // Given
            Seller domain = SellerFixture.anInactiveSeller();

            // When
            SellerJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getStatus()).isEqualTo(SellerStatus.INACTIVE);
        }

        @Test
        @DisplayName("성공 - 상품 수가 있는 셀러 변환")
        void shouldConvertSellerWithProductsToEntity() {
            // Given
            int productCount = 100;
            Seller domain = SellerFixture.anActiveSellerWithProducts(productCount);

            // When
            SellerJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getProductCount()).isEqualTo(productCount);
        }

        @Test
        @DisplayName("성공 - 신규 셀러 변환 (ID null)")
        void shouldConvertNewSellerToEntity() {
            // Given
            Seller domain = SellerFixture.aNewActiveSeller();

            // When
            SellerJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity.getId()).isNull();
            assertThat(entity.getMustItSellerName()).isNotBlank();
            assertThat(entity.getSellerName()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("toDomain - Entity → Domain 변환")
    class ToDomainTests {

        @Test
        @DisplayName("성공 - 활성 Entity를 Domain으로 변환")
        void shouldConvertEntityToActiveDomain() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            SellerJpaEntity entity =
                    SellerJpaEntity.of(
                            1L,
                            "mustit-seller",
                            "commerce-seller",
                            null,
                            SellerStatus.ACTIVE,
                            50,
                            now,
                            now);

            // When
            Seller domain = mapper.toDomain(entity);

            // Then
            assertThat(domain).isNotNull();
            assertThat(domain.getSellerIdValue()).isEqualTo(1L);
            assertThat(domain.getMustItSellerNameValue()).isEqualTo("mustit-seller");
            assertThat(domain.getSellerNameValue()).isEqualTo("commerce-seller");
            assertThat(domain.getStatus()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(domain.getProductCount()).isEqualTo(50);
            assertThat(domain.getCreatedAt()).isNotNull();
            assertThat(domain.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - 비활성 Entity를 Domain으로 변환")
        void shouldConvertEntityToInactiveDomain() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            SellerJpaEntity entity =
                    SellerJpaEntity.of(
                            2L,
                            "inactive-mustit",
                            "inactive-commerce",
                            null,
                            SellerStatus.INACTIVE,
                            0,
                            now,
                            now);

            // When
            Seller domain = mapper.toDomain(entity);

            // Then
            assertThat(domain.getStatus()).isEqualTo(SellerStatus.INACTIVE);
            assertThat(domain.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("양방향 변환 일관성")
    class RoundTripTests {

        @Test
        @DisplayName("성공 - Domain → Entity → Domain 변환 일관성")
        void shouldMaintainConsistencyInRoundTrip() {
            // Given
            Seller original = SellerFixture.anActiveSellerWithProducts(75);

            // When
            SellerJpaEntity entity = mapper.toEntity(original);
            Seller restored = mapper.toDomain(entity);

            // Then
            assertThat(restored.getSellerIdValue()).isEqualTo(original.getSellerIdValue());
            assertThat(restored.getMustItSellerNameValue())
                    .isEqualTo(original.getMustItSellerNameValue());
            assertThat(restored.getSellerNameValue()).isEqualTo(original.getSellerNameValue());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
            assertThat(restored.getProductCount()).isEqualTo(original.getProductCount());
        }
    }

    @Nested
    @DisplayName("시간 변환")
    class TimeConversionTests {

        @Test
        @DisplayName("성공 - Instant → LocalDateTime → Instant 변환 일관성")
        void shouldConvertTimesConsistently() {
            // Given
            Seller domain = SellerFixture.anActiveSeller();

            // When
            SellerJpaEntity entity = mapper.toEntity(domain);
            Seller restored = mapper.toDomain(entity);

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
        @DisplayName("성공 - null createdAt/updatedAt Entity 변환 시 null Instant 반환")
        void shouldHandleNullTimesInEntity() {
            // Given - createdAt, updatedAt이 null인 Entity (toInstant null 분기 커버)
            SellerJpaEntity entity =
                    SellerJpaEntity.of(
                            1L,
                            "mustit-seller",
                            "commerce-seller",
                            null,
                            SellerStatus.ACTIVE,
                            0,
                            null, // createdAt null
                            null); // updatedAt null

            // When
            Seller domain = mapper.toDomain(entity);

            // Then
            assertThat(domain).isNotNull();
            assertThat(domain.getCreatedAt()).isNull();
            assertThat(domain.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("성공 - null createdAt/updatedAt Domain 변환 시 null LocalDateTime 반환")
        void shouldHandleNullTimesInDomain() {
            // Given - createdAt, updatedAt이 null인 Domain (toLocalDateTime null 분기 커버)
            Seller domain =
                    Seller.reconstitute(
                            SellerId.of(1L),
                            MustItSellerName.of("mustit-seller"),
                            SellerName.of("commerce-seller"),
                            null,
                            SellerStatus.ACTIVE,
                            0,
                            null, // createdAt null
                            null); // updatedAt null

            // When
            SellerJpaEntity entity = mapper.toEntity(domain);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getCreatedAt()).isNull();
            assertThat(entity.getUpdatedAt()).isNull();
        }
    }
}
