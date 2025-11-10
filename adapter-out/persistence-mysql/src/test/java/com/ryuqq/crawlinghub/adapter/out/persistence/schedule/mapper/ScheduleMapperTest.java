package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleEntityFixture;
import com.ryuqq.crawlinghub.domain.crawl.schedule.CrawlScheduleFixture;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ScheduleMapper 단위 테스트
 *
 * <p>Persistence Layer 컨벤션 준수:</p>
 * <ul>
 *   <li>✅ Lombok 금지 - Pure Java Mapper</li>
 *   <li>✅ Object Mother 패턴 사용 (Fixture)</li>
 *   <li>✅ Objects.requireNonNull() 검증</li>
 *   <li>✅ Static Factory Method 패턴</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@DisplayName("ScheduleMapper 단위 테스트")
class ScheduleMapperTest {

    private ScheduleMapper sut;

    @BeforeEach
    void setUp() {
        sut = new ScheduleMapper();
    }

    @Nested
    @DisplayName("toEntity 메서드는")
    class Describe_toEntity {

        @Nested
        @DisplayName("신규 CrawlSchedule이 주어지면 (ID 없음)")
        class Context_with_new_schedule {

            private CrawlSchedule schedule;

            @BeforeEach
            void setUp() {
                schedule = CrawlScheduleFixture.create();
            }

            @Test
            @DisplayName("ID가 null인 ScheduleEntity를 반환한다")
            void it_returns_entity_without_id() {
                // When
                ScheduleEntity entity = sut.toEntity(schedule);

                // Then
                assertThat(entity).isNotNull();
                assertThat(entity.getId()).isNull();
                assertThat(entity.getSellerId()).isEqualTo(schedule.getSellerIdValue());
                assertThat(entity.getCronExpression()).isEqualTo(schedule.getCronExpressionValue());
                assertThat(entity.getStatus()).isEqualTo(ScheduleEntity.ScheduleStatus.ACTIVE);
            }
        }

        @Nested
        @DisplayName("기존 CrawlSchedule이 주어지면 (ID 있음)")
        class Context_with_existing_schedule {

            private CrawlSchedule schedule;

            @BeforeEach
            void setUp() {
                schedule = CrawlScheduleFixture.createWithId(1L);
            }

            @Test
            @DisplayName("ID를 포함한 ScheduleEntity를 반환한다")
            void it_returns_entity_with_id() {
                // When
                ScheduleEntity entity = sut.toEntity(schedule);

                // Then
                assertThat(entity).isNotNull();
                assertThat(entity.getId()).isEqualTo(1L);
                assertThat(entity.getSellerId()).isEqualTo(schedule.getSellerIdValue());
                assertThat(entity.getCronExpression()).isEqualTo(schedule.getCronExpressionValue());
            }
        }

        @Nested
        @DisplayName("null이 주어지면")
        class Context_with_null {

            @Test
            @DisplayName("NullPointerException을 발생시킨다")
            void it_throws_null_pointer_exception() {
                // When & Then
                assertThatThrownBy(() -> sut.toEntity(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("schedule must not be null");
            }
        }

        @Nested
        @DisplayName("다양한 상태의 Schedule로")
        class Context_with_various_status {

            @Test
            @DisplayName("ACTIVE 상태는 올바르게 변환된다")
            void it_maps_active_status() {
                // Given
                CrawlSchedule schedule = CrawlScheduleFixture.createActive();

                // When
                ScheduleEntity entity = sut.toEntity(schedule);

                // Then
                assertThat(entity.getStatus()).isEqualTo(ScheduleEntity.ScheduleStatus.ACTIVE);
            }

            @Test
            @DisplayName("SUSPENDED 상태는 올바르게 변환된다")
            void it_maps_suspended_status() {
                // Given
                CrawlSchedule schedule = CrawlScheduleFixture.createSuspended();

                // When
                ScheduleEntity entity = sut.toEntity(schedule);

                // Then
                assertThat(entity.getStatus()).isEqualTo(ScheduleEntity.ScheduleStatus.SUSPENDED);
            }
        }
    }

    @Nested
    @DisplayName("toDomain 메서드는")
    class Describe_toDomain {

        @Nested
        @DisplayName("유효한 ScheduleEntity가 주어지면")
        class Context_with_valid_entity {

            private ScheduleEntity entity;

            @BeforeEach
            void setUp() {
                entity = ScheduleEntityFixture.createWithId(1L);
            }

            @Test
            @DisplayName("CrawlSchedule 도메인 객체를 반환한다")
            void it_returns_domain_object() {
                // When
                CrawlSchedule schedule = sut.toDomain(entity);

                // Then
                assertThat(schedule).isNotNull();
                assertThat(schedule.getIdValue()).isEqualTo(entity.getId());
                assertThat(schedule.getSellerIdValue()).isEqualTo(entity.getSellerId());
                assertThat(schedule.getCronExpressionValue()).isEqualTo(entity.getCronExpression());
                assertThat(schedule.getNextExecutionTime()).isEqualTo(entity.getNextExecutionTime());
            }
        }

        @Nested
        @DisplayName("null이 주어지면")
        class Context_with_null {

            @Test
            @DisplayName("NullPointerException을 발생시킨다")
            void it_throws_null_pointer_exception() {
                // When & Then
                assertThatThrownBy(() -> sut.toDomain(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("entity must not be null");
            }
        }

        @Nested
        @DisplayName("다양한 상태의 Entity로")
        class Context_with_various_entity_status {

            @Test
            @DisplayName("ACTIVE Entity는 올바르게 변환된다")
            void it_maps_active_entity() {
                // Given
                ScheduleEntity entity = ScheduleEntityFixture.createActive();

                // When
                CrawlSchedule schedule = sut.toDomain(entity);

                // Then
                assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.ACTIVE);
            }

            @Test
            @DisplayName("SUSPENDED Entity는 올바르게 변환된다")
            void it_maps_suspended_entity() {
                // Given
                ScheduleEntity entity = ScheduleEntityFixture.createSuspended();

                // When
                CrawlSchedule schedule = sut.toDomain(entity);

                // Then
                assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.SUSPENDED);
            }

            @Test
            @DisplayName("DELETED Entity는 올바르게 변환된다")
            void it_maps_deleted_entity() {
                // Given
                ScheduleEntity entity = ScheduleEntityFixture.createDeleted();

                // When
                CrawlSchedule schedule = sut.toDomain(entity);

                // Then
                assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.SUSPENDED);
            }
        }
    }

    @Nested
    @DisplayName("양방향 변환 시")
    class Describe_bidirectional_conversion {

        @Test
        @DisplayName("Domain → Entity → Domain 변환이 정확하다")
        void it_converts_accurately_domain_to_entity_to_domain() {
            // Given
            CrawlSchedule originalSchedule = CrawlScheduleFixture.createWithId(1L);

            // When
            ScheduleEntity entity = sut.toEntity(originalSchedule);
            CrawlSchedule convertedSchedule = sut.toDomain(entity);

            // Then
            assertThat(convertedSchedule.getIdValue()).isEqualTo(originalSchedule.getIdValue());
            assertThat(convertedSchedule.getSellerIdValue()).isEqualTo(originalSchedule.getSellerIdValue());
            assertThat(convertedSchedule.getCronExpressionValue()).isEqualTo(originalSchedule.getCronExpressionValue());
            assertThat(convertedSchedule.getStatus()).isEqualTo(originalSchedule.getStatus());
        }

        @Test
        @DisplayName("Entity → Domain → Entity 변환이 정확하다")
        void it_converts_accurately_entity_to_domain_to_entity() {
            // Given
            ScheduleEntity originalEntity = ScheduleEntityFixture.createWithId(1L);

            // When
            CrawlSchedule schedule = sut.toDomain(originalEntity);
            ScheduleEntity convertedEntity = sut.toEntity(schedule);

            // Then
            assertThat(convertedEntity.getId()).isEqualTo(originalEntity.getId());
            assertThat(convertedEntity.getSellerId()).isEqualTo(originalEntity.getSellerId());
            assertThat(convertedEntity.getCronExpression()).isEqualTo(originalEntity.getCronExpression());
            assertThat(convertedEntity.getStatus()).isEqualTo(originalEntity.getStatus());
        }
    }
}
