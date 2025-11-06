package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleOutboxEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleOutboxEntityFixture;
import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutbox;
import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutboxFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ScheduleOutboxMapper 단위 테스트
 *
 * <p>Persistence Layer 컨벤션 준수:</p>
 * <ul>
 *   <li>✅ Lombok 금지 - Pure Java Mapper</li>
 *   <li>✅ Object Mother 패턴 사용 (Fixture)</li>
 *   <li>✅ Objects.requireNonNull() 검증</li>
 *   <li>✅ Domain Enum 직접 사용 (Entity 내부 Enum 금지)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@DisplayName("ScheduleOutboxMapper 단위 테스트")
class ScheduleOutboxMapperTest {

    private ScheduleOutboxMapper sut;

    @BeforeEach
    void setUp() {
        sut = new ScheduleOutboxMapper();
    }

    @Nested
    @DisplayName("toEntity 메서드는")
    class Describe_toEntity {

        @Nested
        @DisplayName("유효한 ScheduleOutbox가 주어지면")
        class Context_with_valid_outbox {

            private ScheduleOutbox outbox;

            @BeforeEach
            void setUp() {
                outbox = ScheduleOutboxFixture.createWithId(1L);
            }

            @Test
            @DisplayName("ScheduleOutboxEntity를 반환한다")
            void it_returns_entity() {
                // When
                ScheduleOutboxEntity entity = sut.toEntity(outbox);

                // Then
                assertThat(entity).isNotNull();
                assertThat(entity.getOpId()).isEqualTo(outbox.getOpId());
                assertThat(entity.getSellerId()).isEqualTo(outbox.getSellerId());
                assertThat(entity.getIdemKey()).isEqualTo(outbox.getIdemKey());
                assertThat(entity.getDomain()).isEqualTo(outbox.getDomain());
                assertThat(entity.getEventType()).isEqualTo(outbox.getEventType());
                assertThat(entity.getBizKey()).isEqualTo(outbox.getBizKey());
                assertThat(entity.getPayload()).isEqualTo(outbox.getPayload());
                assertThat(entity.getOperationState()).isEqualTo(outbox.getOperationState());
                assertThat(entity.getWalState()).isEqualTo(outbox.getWalState());
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
                    .hasMessageContaining("domain must not be null");
            }
        }

        @Nested
        @DisplayName("다양한 Operation State로")
        class Context_with_various_operation_states {

            @Test
            @DisplayName("PENDING 상태는 올바르게 변환된다")
            void it_maps_pending_state() {
                // Given
                ScheduleOutbox outbox = ScheduleOutboxFixture.createPending();

                // When
                ScheduleOutboxEntity entity = sut.toEntity(outbox);

                // Then
                assertThat(entity.getOperationState())
                    .isEqualTo(ScheduleOutbox.OperationState.PENDING);
            }

            @Test
            @DisplayName("IN_PROGRESS 상태는 올바르게 변환된다")
            void it_maps_in_progress_state() {
                // Given
                ScheduleOutbox outbox = ScheduleOutboxFixture.createInProgress();

                // When
                ScheduleOutboxEntity entity = sut.toEntity(outbox);

                // Then
                assertThat(entity.getOperationState())
                    .isEqualTo(ScheduleOutbox.OperationState.IN_PROGRESS);
            }

            @Test
            @DisplayName("COMPLETED 상태는 올바르게 변환된다")
            void it_maps_completed_state() {
                // Given
                ScheduleOutbox outbox = ScheduleOutboxFixture.createCompleted();

                // When
                ScheduleOutboxEntity entity = sut.toEntity(outbox);

                // Then
                assertThat(entity.getOperationState())
                    .isEqualTo(ScheduleOutbox.OperationState.COMPLETED);
            }

            @Test
            @DisplayName("FAILED 상태는 올바르게 변환된다")
            void it_maps_failed_state() {
                // Given
                ScheduleOutbox outbox = ScheduleOutboxFixture.createFailed();

                // When
                ScheduleOutboxEntity entity = sut.toEntity(outbox);

                // Then
                assertThat(entity.getOperationState())
                    .isEqualTo(ScheduleOutbox.OperationState.FAILED);
            }
        }

        @Nested
        @DisplayName("다양한 WAL State로")
        class Context_with_various_wal_states {

            @Test
            @DisplayName("PENDING WAL 상태는 올바르게 변환된다")
            void it_maps_pending_wal_state() {
                // Given
                ScheduleOutbox outbox = ScheduleOutboxFixture.createPending();

                // When
                ScheduleOutboxEntity entity = sut.toEntity(outbox);

                // Then
                assertThat(entity.getWalState())
                    .isEqualTo(ScheduleOutbox.WriteAheadState.PENDING);
            }

            @Test
            @DisplayName("COMPLETED WAL 상태는 올바르게 변환된다")
            void it_maps_completed_wal_state() {
                // Given
                ScheduleOutbox outbox = ScheduleOutboxFixture.createCompleted();

                // When
                ScheduleOutboxEntity entity = sut.toEntity(outbox);

                // Then
                assertThat(entity.getWalState())
                    .isEqualTo(ScheduleOutbox.WriteAheadState.COMPLETED);
            }
        }
    }

    @Nested
    @DisplayName("toDomain 메서드는")
    class Describe_toDomain {

        @Nested
        @DisplayName("유효한 ScheduleOutboxEntity가 주어지면")
        class Context_with_valid_entity {

            private ScheduleOutboxEntity entity;

            @BeforeEach
            void setUp() {
                entity = ScheduleOutboxEntityFixture.createWithId(1L);
            }

            @Test
            @DisplayName("ScheduleOutbox 도메인 객체를 반환한다")
            void it_returns_domain_object() {
                // When
                ScheduleOutbox outbox = sut.toDomain(entity);

                // Then
                assertThat(outbox).isNotNull();
                assertThat(outbox.getId()).isEqualTo(entity.getId());
                assertThat(outbox.getOpId()).isEqualTo(entity.getOpId());
                assertThat(outbox.getSellerId()).isEqualTo(entity.getSellerId());
                assertThat(outbox.getIdemKey()).isEqualTo(entity.getIdemKey());
                assertThat(outbox.getDomain()).isEqualTo(entity.getDomain());
                assertThat(outbox.getEventType()).isEqualTo(entity.getEventType());
                assertThat(outbox.getBizKey()).isEqualTo(entity.getBizKey());
                assertThat(outbox.getPayload()).isEqualTo(entity.getPayload());
                assertThat(outbox.getOperationState()).isEqualTo(entity.getOperationState());
                assertThat(outbox.getWalState()).isEqualTo(entity.getWalState());
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
        class Context_with_various_entity_states {

            @Test
            @DisplayName("PENDING Entity는 올바르게 변환된다")
            void it_maps_pending_entity() {
                // Given
                ScheduleOutboxEntity entity = ScheduleOutboxEntityFixture.createPending();

                // When
                ScheduleOutbox outbox = sut.toDomain(entity);

                // Then
                assertThat(outbox.getOperationState())
                    .isEqualTo(ScheduleOutbox.OperationState.PENDING);
                assertThat(outbox.getWalState())
                    .isEqualTo(ScheduleOutbox.WriteAheadState.PENDING);
            }

            @Test
            @DisplayName("COMPLETED Entity는 올바르게 변환된다")
            void it_maps_completed_entity() {
                // Given
                ScheduleOutboxEntity entity = ScheduleOutboxEntityFixture.createCompleted();

                // When
                ScheduleOutbox outbox = sut.toDomain(entity);

                // Then
                assertThat(outbox.getOperationState())
                    .isEqualTo(ScheduleOutbox.OperationState.COMPLETED);
                assertThat(outbox.getWalState())
                    .isEqualTo(ScheduleOutbox.WriteAheadState.COMPLETED);
            }

            @Test
            @DisplayName("FAILED Entity는 올바르게 변환된다")
            void it_maps_failed_entity() {
                // Given
                ScheduleOutboxEntity entity = ScheduleOutboxEntityFixture.createFailed();

                // When
                ScheduleOutbox outbox = sut.toDomain(entity);

                // Then
                assertThat(outbox.getOperationState())
                    .isEqualTo(ScheduleOutbox.OperationState.FAILED);
                assertThat(outbox.getRetryCount()).isEqualTo(1);
                assertThat(outbox.getErrorMessage()).isEqualTo("External API timeout");
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
            ScheduleOutbox originalOutbox = ScheduleOutboxFixture.createWithId(1L);

            // When
            ScheduleOutboxEntity entity = sut.toEntity(originalOutbox);
            ScheduleOutbox convertedOutbox = sut.toDomain(entity);

            // Then
            assertThat(convertedOutbox.getSellerId()).isEqualTo(originalOutbox.getSellerId());
            assertThat(convertedOutbox.getIdemKey()).isEqualTo(originalOutbox.getIdemKey());
            assertThat(convertedOutbox.getOperationState()).isEqualTo(originalOutbox.getOperationState());
            assertThat(convertedOutbox.getWalState()).isEqualTo(originalOutbox.getWalState());
        }

        @Test
        @DisplayName("Entity → Domain → Entity 변환이 정확하다")
        void it_converts_accurately_entity_to_domain_to_entity() {
            // Given
            ScheduleOutboxEntity originalEntity = ScheduleOutboxEntityFixture.createWithId(1L);

            // When
            ScheduleOutbox outbox = sut.toDomain(originalEntity);
            ScheduleOutboxEntity convertedEntity = sut.toEntity(outbox);

            // Then
            assertThat(convertedEntity.getSellerId()).isEqualTo(originalEntity.getSellerId());
            assertThat(convertedEntity.getIdemKey()).isEqualTo(originalEntity.getIdemKey());
            assertThat(convertedEntity.getOperationState()).isEqualTo(originalEntity.getOperationState());
            assertThat(convertedEntity.getWalState()).isEqualTo(originalEntity.getWalState());
        }
    }

    @Nested
    @DisplayName("Enum 변환 시")
    class Describe_enum_conversion {

        @Test
        @DisplayName("Domain Enum이 Entity에서 그대로 사용된다")
        void it_uses_domain_enum_directly() {
            // Given
            ScheduleOutbox outbox = ScheduleOutboxFixture.createPending();

            // When
            ScheduleOutboxEntity entity = sut.toEntity(outbox);

            // Then
            // Entity는 Domain Enum을 직접 사용 (별도 변환 없음)
            assertThat(entity.getOperationState()).isSameAs(outbox.getOperationState());
            assertThat(entity.getWalState()).isSameAs(outbox.getWalState());
        }
    }
}
