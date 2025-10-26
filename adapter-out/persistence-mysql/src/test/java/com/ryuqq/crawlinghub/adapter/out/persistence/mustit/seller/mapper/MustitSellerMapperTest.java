package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.entity.MustitSellerEntity;
import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlInterval;
import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlIntervalType;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.mustit.seller.SellerBasicInfo;
import com.ryuqq.crawlinghub.domain.mustit.seller.SellerTimeInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MustitSellerMapper 단위 테스트
 *
 * @author Claude
 * @since 1.0
 */
class MustitSellerMapperTest {

    private MustitSellerMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MustitSellerMapper();
    }

    /**
     * Entity 생성 후 @PrePersist 메서드를 호출하여 시간 필드를 초기화하는 헬퍼 메서드
     */
    private void initializeEntityTimestamps(MustitSellerEntity entity) {
        try {
            Method onCreate = MustitSellerEntity.class.getDeclaredMethod("onCreate");
            onCreate.setAccessible(true);
            onCreate.invoke(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize entity timestamps", e);
        }
    }

    @Test
    @DisplayName("Domain Aggregate를 Entity로 변환 - 성공")
    void toEntity_ShouldConvertDomainToEntity() {
        // Given
        CrawlInterval crawlInterval = new CrawlInterval(CrawlIntervalType.DAILY, 1);
        MustitSeller seller = new MustitSeller(
                "SELLER001",
                "Test Seller",
                crawlInterval
        );

        // When
        MustitSellerEntity entity = mapper.toEntity(seller);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getSellerId()).isEqualTo("SELLER001");
        assertThat(entity.getName()).isEqualTo("Test Seller");
        assertThat(entity.getIsActive()).isTrue();
        assertThat(entity.getIntervalType()).isEqualTo(MustitSellerEntity.IntervalType.DAILY);
        assertThat(entity.getIntervalValue()).isEqualTo(1);
        assertThat(entity.getCronExpression()).isEqualTo("0 0 0/1 * ? *");
    }

    @Test
    @DisplayName("Entity를 Domain Aggregate로 변환 - 성공")
    void toDomain_ShouldConvertEntityToDomain() {
        // Given
        MustitSellerEntity.BasicInfo basicInfo = new MustitSellerEntity.BasicInfo(
                "SELLER001",
                "Test Seller",
                true
        );
        MustitSellerEntity.CrawlInfo crawlInfo = new MustitSellerEntity.CrawlInfo(
                MustitSellerEntity.IntervalType.WEEKLY,
                2,
                "0 0 0 ? * 1/2 *"
        );
        MustitSellerEntity entity = new MustitSellerEntity(basicInfo, crawlInfo);
        initializeEntityTimestamps(entity);

        // When
        MustitSeller seller = mapper.toDomain(entity);

        // Then
        assertThat(seller).isNotNull();
        assertThat(seller.getSellerId()).isEqualTo("SELLER001");
        assertThat(seller.getName()).isEqualTo("Test Seller");
        assertThat(seller.isActive()).isTrue();
        assertThat(seller.getCrawlInterval().getIntervalType()).isEqualTo(CrawlIntervalType.WEEKLY);
        assertThat(seller.getCrawlInterval().getIntervalValue()).isEqualTo(2);
        assertThat(seller.getCrawlInterval().getCronExpression()).isEqualTo("0 0 0 ? * 1/2 *");
    }

    @Test
    @DisplayName("null Domain을 변환하면 null Entity 반환")
    void toEntity_ShouldReturnNullWhenDomainIsNull() {
        // When
        MustitSellerEntity entity = mapper.toEntity(null);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("null Entity를 변환하면 null Domain 반환")
    void toDomain_ShouldReturnNullWhenEntityIsNull() {
        // When
        MustitSeller seller = mapper.toDomain(null);

        // Then
        assertThat(seller).isNull();
    }

    @Test
    @DisplayName("모든 IntervalType 변환 검증 - HOURLY")
    void toEntity_ShouldConvertAllIntervalTypes_Hourly() {
        // Given
        CrawlInterval crawlInterval = new CrawlInterval(CrawlIntervalType.HOURLY, 3);
        MustitSeller seller = new MustitSeller("SELLER001", "Test Seller", crawlInterval);

        // When
        MustitSellerEntity entity = mapper.toEntity(seller);

        // Then
        assertThat(entity.getIntervalType()).isEqualTo(MustitSellerEntity.IntervalType.HOURLY);
        assertThat(entity.getIntervalValue()).isEqualTo(3);
        assertThat(entity.getCronExpression()).isEqualTo("0 0/3 * * ? *");
    }

    @Test
    @DisplayName("모든 IntervalType 변환 검증 - DAILY")
    void toEntity_ShouldConvertAllIntervalTypes_Daily() {
        // Given
        CrawlInterval crawlInterval = new CrawlInterval(CrawlIntervalType.DAILY, 2);
        MustitSeller seller = new MustitSeller("SELLER001", "Test Seller", crawlInterval);

        // When
        MustitSellerEntity entity = mapper.toEntity(seller);

        // Then
        assertThat(entity.getIntervalType()).isEqualTo(MustitSellerEntity.IntervalType.DAILY);
        assertThat(entity.getIntervalValue()).isEqualTo(2);
        assertThat(entity.getCronExpression()).isEqualTo("0 0 0/2 * ? *");
    }

    @Test
    @DisplayName("모든 IntervalType 변환 검증 - WEEKLY")
    void toEntity_ShouldConvertAllIntervalTypes_Weekly() {
        // Given
        CrawlInterval crawlInterval = new CrawlInterval(CrawlIntervalType.WEEKLY, 1);
        MustitSeller seller = new MustitSeller("SELLER001", "Test Seller", crawlInterval);

        // When
        MustitSellerEntity entity = mapper.toEntity(seller);

        // Then
        assertThat(entity.getIntervalType()).isEqualTo(MustitSellerEntity.IntervalType.WEEKLY);
        assertThat(entity.getIntervalValue()).isEqualTo(1);
        assertThat(entity.getCronExpression()).isEqualTo("0 0 0 ? * 1/1 *");
    }

    @Test
    @DisplayName("양방향 변환 - Domain → Entity → Domain")
    void bidirectionalConversion_ShouldPreserveData() {
        // Given
        CrawlInterval crawlInterval = new CrawlInterval(CrawlIntervalType.DAILY, 1);
        MustitSeller originalSeller = new MustitSeller(
                "SELLER001",
                "Test Seller",
                crawlInterval
        );

        // When
        MustitSellerEntity entity = mapper.toEntity(originalSeller);
        initializeEntityTimestamps(entity);
        MustitSeller convertedSeller = mapper.toDomain(entity);

        // Then
        assertThat(convertedSeller.getSellerId()).isEqualTo(originalSeller.getSellerId());
        assertThat(convertedSeller.getName()).isEqualTo(originalSeller.getName());
        assertThat(convertedSeller.isActive()).isEqualTo(originalSeller.isActive());
        assertThat(convertedSeller.getCrawlInterval().getIntervalType())
                .isEqualTo(originalSeller.getCrawlInterval().getIntervalType());
        assertThat(convertedSeller.getCrawlInterval().getIntervalValue())
                .isEqualTo(originalSeller.getCrawlInterval().getIntervalValue());
    }

    @Test
    @DisplayName("비활성 상태 셀러 변환 - 성공")
    void toEntity_ShouldConvertInactiveSeller() {
        // Given
        CrawlInterval crawlInterval = new CrawlInterval(CrawlIntervalType.HOURLY, 6);
        LocalDateTime now = LocalDateTime.now();
        SellerBasicInfo basicInfo = SellerBasicInfo.of(
                "SELLER002",
                "Inactive Seller",
                false
        );
        SellerTimeInfo timeInfo = SellerTimeInfo.of(
                now.minusDays(10),
                now
        );
        MustitSeller seller = MustitSeller.reconstitute(basicInfo, crawlInterval, timeInfo);

        // When
        MustitSellerEntity entity = mapper.toEntity(seller);

        // Then
        assertThat(entity.getSellerId()).isEqualTo("SELLER002");
        assertThat(entity.getName()).isEqualTo("Inactive Seller");
        assertThat(entity.getIsActive()).isFalse();
        assertThat(entity.getIntervalType()).isEqualTo(MustitSellerEntity.IntervalType.HOURLY);
        assertThat(entity.getIntervalValue()).isEqualTo(6);
    }
}
