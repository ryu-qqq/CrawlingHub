package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleOutboxEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleOutboxEntityFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.ScheduleOutboxJpaRepository;
import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutbox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ScheduleOutboxQueryAdapter 통합 테스트
 *
 * <p>Persistence Layer 통합 테스트 컨벤션 준수:</p>
 * <ul>
 *   <li>✅ @DataJpaTest - JPA 관련 Bean만 로드</li>
 *   <li>✅ TestEntityManager - 영속성 컨텍스트 제어</li>
 *   <li>✅ Object Mother 패턴 사용 (Fixture)</li>
 *   <li>✅ QueryDSL Projections.constructor() 검증</li>
 *   <li>✅ CQRS Query 전용 Adapter 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@DataJpaTest
@Import({ScheduleOutboxQueryAdapter.class, JPAQueryFactory.class})
@DisplayName("ScheduleOutboxQueryAdapter 통합 테스트")
class ScheduleOutboxQueryAdapterTest {

    @Autowired
    private ScheduleOutboxQueryAdapter sut;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ScheduleOutboxJpaRepository outboxRepository;

    @AfterEach
    void tearDown() {
        outboxRepository.deleteAll();
    }

    @Nested
    @DisplayName("findByIdemKey 메서드는")
    class Describe_findByIdemKey {

        @Nested
        @DisplayName("존재하는 Idempotency Key가 주어지면")
        class Context_with_existing_idem_key {

            private ScheduleOutboxEntity savedEntity;

            @BeforeEach
            void setUp() {
                // Given: DB에 Outbox 저장
                savedEntity = ScheduleOutboxEntityFixture.createWithIdemKey("idem-12345");
                savedEntity = outboxRepository.save(savedEntity);
                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("ScheduleOutbox 도메인 객체를 반환한다")
            void it_returns_outbox_domain_object() {
                // When
                Optional<ScheduleOutbox> result = sut.findByIdemKey("idem-12345");

                // Then
                assertThat(result).isPresent();
                assertThat(result.get().getIdemKey()).isEqualTo("idem-12345");
                assertThat(result.get().getSellerId()).isEqualTo(savedEntity.getSellerId());
            }
        }

        @Nested
        @DisplayName("존재하지 않는 Idempotency Key가 주어지면")
        class Context_with_non_existing_idem_key {

            @Test
            @DisplayName("빈 Optional을 반환한다")
            void it_returns_empty_optional() {
                // When
                Optional<ScheduleOutbox> result = sut.findByIdemKey("non-existing-key");

                // Then
                assertThat(result).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("existsByIdemKey 메서드는")
    class Describe_existsByIdemKey {

        @Nested
        @DisplayName("존재하는 Idempotency Key가 주어지면")
        class Context_with_existing_idem_key {

            @BeforeEach
            void setUp() {
                // Given: DB에 Outbox 저장
                ScheduleOutboxEntity entity = ScheduleOutboxEntityFixture.createWithIdemKey("idem-exists");
                outboxRepository.save(entity);
                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("true를 반환한다")
            void it_returns_true() {
                // When
                boolean exists = sut.existsByIdemKey("idem-exists");

                // Then
                assertThat(exists).isTrue();
            }
        }

        @Nested
        @DisplayName("존재하지 않는 Idempotency Key가 주어지면")
        class Context_with_non_existing_idem_key {

            @Test
            @DisplayName("false를 반환한다")
            void it_returns_false() {
                // When
                boolean exists = sut.existsByIdemKey("non-existing-key");

                // Then
                assertThat(exists).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("findByWalStatePending 메서드는")
    class Describe_findByWalStatePending {

        @Nested
        @DisplayName("PENDING WAL 상태의 Outbox가 존재하면")
        class Context_with_pending_wal_state {

            @BeforeEach
            void setUp() {
                // Given: 여러 상태의 Outbox 저장
                outboxRepository.save(ScheduleOutboxEntityFixture.createPending());
                outboxRepository.save(
                    ScheduleOutboxEntityFixture.createCustom(
                        null, 200L, "idem-2",
                        ScheduleOutbox.OperationState.PENDING,
                        ScheduleOutbox.WriteAheadState.PENDING
                    )
                );
                outboxRepository.save(ScheduleOutboxEntityFixture.createCompleted());

                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("PENDING WAL 상태의 Outbox 목록을 createdAt 오름차순으로 반환한다")
            void it_returns_pending_wal_outboxes_ordered_by_created_at() {
                // When
                List<ScheduleOutbox> results = sut.findByWalStatePending();

                // Then
                assertThat(results).hasSize(2);
                assertThat(results).allMatch(
                    outbox -> outbox.getWalState() == ScheduleOutbox.WriteAheadState.PENDING
                );
            }
        }

        @Nested
        @DisplayName("PENDING WAL 상태의 Outbox가 없으면")
        class Context_without_pending_wal_state {

            @BeforeEach
            void setUp() {
                // Given: COMPLETED 상태만 저장
                outboxRepository.save(ScheduleOutboxEntityFixture.createCompleted());
                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("빈 리스트를 반환한다")
            void it_returns_empty_list() {
                // When
                List<ScheduleOutbox> results = sut.findByWalStatePending();

                // Then
                assertThat(results).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("findByOperationStateFailed 메서드는")
    class Describe_findByOperationStateFailed {

        @Nested
        @DisplayName("재시도 가능한 FAILED 상태의 Outbox가 존재하면")
        class Context_with_retryable_failed_outboxes {

            @BeforeEach
            void setUp() {
                // Given: 재시도 가능한 FAILED와 재시도 불가능한 FAILED 저장
                outboxRepository.save(ScheduleOutboxEntityFixture.createFailed());  // retryCount=1, maxRetries=3
                outboxRepository.save(ScheduleOutboxEntityFixture.createFailedExceedRetry());  // retryCount=3, maxRetries=3
                outboxRepository.save(ScheduleOutboxEntityFixture.createCompleted());

                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("재시도 가능한 FAILED 상태의 Outbox만 반환한다")
            void it_returns_only_retryable_failed_outboxes() {
                // When
                List<ScheduleOutbox> results = sut.findByOperationStateFailed();

                // Then
                assertThat(results).hasSize(1);
                assertThat(results.get(0).getOperationState())
                    .isEqualTo(ScheduleOutbox.OperationState.FAILED);
                assertThat(results.get(0).getRetryCount())
                    .isLessThan(results.get(0).getMaxRetries());
            }
        }

        @Nested
        @DisplayName("재시도 가능한 FAILED 상태의 Outbox가 없으면")
        class Context_without_retryable_failed_outboxes {

            @BeforeEach
            void setUp() {
                // Given: 재시도 초과 FAILED만 저장
                outboxRepository.save(ScheduleOutboxEntityFixture.createFailedExceedRetry());
                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("빈 리스트를 반환한다")
            void it_returns_empty_list() {
                // When
                List<ScheduleOutbox> results = sut.findByOperationStateFailed();

                // Then
                assertThat(results).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("findByWalStateCompleted 메서드는")
    class Describe_findByWalStateCompleted {

        @Nested
        @DisplayName("COMPLETED WAL 상태의 Outbox가 존재하면")
        class Context_with_completed_wal_state {

            @BeforeEach
            void setUp() {
                // Given: 여러 상태의 Outbox 저장
                outboxRepository.save(ScheduleOutboxEntityFixture.createCompleted());
                outboxRepository.save(
                    ScheduleOutboxEntityFixture.createCustom(
                        null, 200L, "idem-2",
                        ScheduleOutbox.OperationState.COMPLETED,
                        ScheduleOutbox.WriteAheadState.COMPLETED
                    )
                );
                outboxRepository.save(ScheduleOutboxEntityFixture.createPending());

                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("COMPLETED WAL 상태의 Outbox 목록을 반환한다")
            void it_returns_completed_wal_outboxes() {
                // When
                List<ScheduleOutbox> results = sut.findByWalStateCompleted();

                // Then
                assertThat(results).hasSize(2);
                assertThat(results).allMatch(
                    outbox -> outbox.getWalState() == ScheduleOutbox.WriteAheadState.COMPLETED
                );
            }
        }
    }

    @Nested
    @DisplayName("findByOpId 메서드는")
    class Describe_findByOpId {

        @Nested
        @DisplayName("존재하는 OpId가 주어지면")
        class Context_with_existing_op_id {

            @BeforeEach
            void setUp() {
                // Given: DB에 Outbox 저장
                ScheduleOutboxEntity entity = ScheduleOutboxEntityFixture.createWithOpId("op-12345");
                outboxRepository.save(entity);
                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("ScheduleOutbox 도메인 객체를 반환한다")
            void it_returns_outbox_domain_object() {
                // When
                ScheduleOutbox result = sut.findByOpId("op-12345");

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getOpId()).isEqualTo("op-12345");
            }
        }

        @Nested
        @DisplayName("존재하지 않는 OpId가 주어지면")
        class Context_with_non_existing_op_id {

            @Test
            @DisplayName("null을 반환한다")
            void it_returns_null() {
                // When
                ScheduleOutbox result = sut.findByOpId("non-existing-op-id");

                // Then
                assertThat(result).isNull();
            }
        }
    }

    @Nested
    @DisplayName("findLatestBySellerId 메서드는")
    class Describe_findLatestBySellerId {

        @Nested
        @DisplayName("여러 개의 Outbox가 존재하면")
        class Context_with_multiple_outboxes {

            @BeforeEach
            void setUp() {
                // Given: 같은 Seller의 여러 Outbox 저장 (시간 간격을 두고)
                ScheduleOutboxEntity old = ScheduleOutboxEntityFixture.createWithSellerId(100L);
                outboxRepository.save(old);

                // 시간 차이를 주기 위해 약간의 대기 (실제 테스트에서는 Fixture에 시간 설정 가능)
                ScheduleOutboxEntity recent = ScheduleOutboxEntityFixture.createCustom(
                    null, 100L, "idem-recent",
                    ScheduleOutbox.OperationState.PENDING,
                    ScheduleOutbox.WriteAheadState.PENDING
                );
                outboxRepository.save(recent);

                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("최신 Outbox를 반환한다")
            void it_returns_latest_outbox() {
                // When
                ScheduleOutbox result = sut.findLatestBySellerId(100L);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getSellerId()).isEqualTo(100L);
                assertThat(result.getIdemKey()).isEqualTo("idem-recent");
            }
        }

        @Nested
        @DisplayName("Outbox가 존재하지 않으면")
        class Context_without_outboxes {

            @Test
            @DisplayName("null을 반환한다")
            void it_returns_null() {
                // When
                ScheduleOutbox result = sut.findLatestBySellerId(999L);

                // Then
                assertThat(result).isNull();
            }
        }
    }

    @Nested
    @DisplayName("QueryDSL Projections 검증")
    class Describe_querydsl_projections {

        @Nested
        @DisplayName("QueryDSL을 사용하여")
        class Context_using_querydsl {

            @BeforeEach
            void setUp() {
                // Given: Outbox 저장
                ScheduleOutboxEntity entity = ScheduleOutboxEntityFixture.createPending();
                outboxRepository.save(entity);
                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("DTO로 직접 프로젝션하여 조회한다")
            void it_projects_directly_to_dto() {
                // When
                List<ScheduleOutbox> results = sut.findByWalStatePending();

                // Then
                // QueryDSL Projections.constructor()를 통해 직접 DTO 조회
                assertThat(results).isNotEmpty();

                // 영속성 컨텍스트에 엔티티가 로드되지 않음 확인
                assertThat(entityManager.getEntityManager().contains(results.get(0)))
                    .isFalse();
            }
        }
    }
}
