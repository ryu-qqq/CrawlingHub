package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MustitSellerEntity 단위 테스트
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
class MustitSellerEntityTest {

    /**
     * Test helper: Create MustitSellerEntity with Record pattern
     */
    private MustitSellerEntity createEntity(
            String sellerId,
            String name,
            Boolean isActive,
            MustitSellerEntity.IntervalType intervalType,
            Integer intervalValue,
            String cronExpression
    ) {
        MustitSellerEntity.BasicInfo basicInfo = new MustitSellerEntity.BasicInfo(
                sellerId,
                name,
                isActive
        );
        MustitSellerEntity.CrawlInfo crawlInfo = new MustitSellerEntity.CrawlInfo(
                intervalType,
                intervalValue,
                cronExpression
        );
        return new MustitSellerEntity(basicInfo, crawlInfo);
    }

    @Test
    @DisplayName("Entity 생성 - 성공")
    void constructor_ShouldCreateEntity() {
        // Given & When
        MustitSellerEntity entity = createEntity(
                "SELLER001",
                "Test Seller",
                true,
                MustitSellerEntity.IntervalType.DAILY,
                1,
                "0 0 0/1 * ? *"
        );

        // Then
        assertThat(entity.getSellerId()).isEqualTo("SELLER001");
        assertThat(entity.getName()).isEqualTo("Test Seller");
        assertThat(entity.getIsActive()).isTrue();
        assertThat(entity.getIntervalType()).isEqualTo(MustitSellerEntity.IntervalType.DAILY);
        assertThat(entity.getIntervalValue()).isEqualTo(1);
        assertThat(entity.getCronExpression()).isEqualTo("0 0 0/1 * ? *");
    }

    @Test
    @DisplayName("Setter 메서드 - 성공")
    void setters_ShouldUpdateFields() {
        // Given
        MustitSellerEntity entity = createEntity(
                "SELLER001",
                "Old Name",
                true,
                MustitSellerEntity.IntervalType.HOURLY,
                1,
                "0 0/1 * * ? *"
        );

        // When
        MustitSellerEntity.BasicInfo newBasicInfo = new MustitSellerEntity.BasicInfo(
                "SELLER001",
                "New Name",
                false
        );
        MustitSellerEntity.CrawlInfo newCrawlInfo = new MustitSellerEntity.CrawlInfo(
                MustitSellerEntity.IntervalType.WEEKLY,
                2,
                "0 0 0 ? * 1/2 *"
        );
        MustitSellerEntity updated = entity.update(newBasicInfo, newCrawlInfo);

        // Then
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getIsActive()).isFalse();
        assertThat(updated.getIntervalType()).isEqualTo(MustitSellerEntity.IntervalType.WEEKLY);
        assertThat(updated.getIntervalValue()).isEqualTo(2);
        assertThat(updated.getCronExpression()).isEqualTo("0 0 0 ? * 1/2 *");
    }

    @Test
    @DisplayName("모든 IntervalType Enum 값 검증")
    void intervalType_ShouldHaveAllValues() {
        // Given & When
        MustitSellerEntity.IntervalType[] types = MustitSellerEntity.IntervalType.values();

        // Then
        assertThat(types).hasSize(3);
        assertThat(types).containsExactlyInAnyOrder(
                MustitSellerEntity.IntervalType.HOURLY,
                MustitSellerEntity.IntervalType.DAILY,
                MustitSellerEntity.IntervalType.WEEKLY
        );
    }

    @Test
    @DisplayName("IntervalType Enum valueOf 검증")
    void intervalType_ValueOf() {
        // When & Then
        assertThat(MustitSellerEntity.IntervalType.valueOf("HOURLY"))
                .isEqualTo(MustitSellerEntity.IntervalType.HOURLY);
        assertThat(MustitSellerEntity.IntervalType.valueOf("DAILY"))
                .isEqualTo(MustitSellerEntity.IntervalType.DAILY);
        assertThat(MustitSellerEntity.IntervalType.valueOf("WEEKLY"))
                .isEqualTo(MustitSellerEntity.IntervalType.WEEKLY);
    }

    @Test
    @DisplayName("비활성 상태 Entity 생성")
    void constructor_ShouldCreateInactiveEntity() {
        // Given & When
        MustitSellerEntity entity = createEntity(
                "SELLER002",
                "Inactive Seller",
                false,
                MustitSellerEntity.IntervalType.HOURLY,
                6,
                "0 0/6 * * ? *"
        );

        // Then
        assertThat(entity.getSellerId()).isEqualTo("SELLER002");
        assertThat(entity.getName()).isEqualTo("Inactive Seller");
        assertThat(entity.getIsActive()).isFalse();
        assertThat(entity.getIntervalType()).isEqualTo(MustitSellerEntity.IntervalType.HOURLY);
        assertThat(entity.getIntervalValue()).isEqualTo(6);
    }

    @Test
    @DisplayName("Entity ID는 초기값 null")
    void getId_ShouldBeNullInitially() {
        // Given & When
        MustitSellerEntity entity = createEntity(
                "SELLER001",
                "Test Seller",
                true,
                MustitSellerEntity.IntervalType.DAILY,
                1,
                "0 0 0/1 * ? *"
        );

        // Then
        assertThat(entity.getId()).isNull();
    }

    @Test
    @DisplayName("생성/수정 시각은 초기값 null (JPA 콜백 전)")
    void timestamps_ShouldBeNullBeforePersistence() {
        // Given & When
        MustitSellerEntity entity = createEntity(
                "SELLER001",
                "Test Seller",
                true,
                MustitSellerEntity.IntervalType.DAILY,
                1,
                "0 0 0/1 * ? *"
        );

        // Then
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }
}
