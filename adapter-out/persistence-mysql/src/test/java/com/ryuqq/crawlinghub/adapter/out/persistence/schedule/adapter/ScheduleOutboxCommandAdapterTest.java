package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleOutboxEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleOutboxEntityFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper.ScheduleOutboxMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.ScheduleOutboxJpaRepository;
import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutbox;
import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutboxFixture;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ScheduleOutboxCommandAdapter 통합 테스트
 *
 * <p>Persistence Layer 통합 테스트 컨벤션 준수:</p>
 * <ul>
 *   <li>✅ @DataJpaTest - JPA 관련 Bean만 로드</li>
 *   <li>✅ TestEntityManager - 영속성 컨텍스트 제어</li>
 *   <li>✅ Object Mother 패턴 사용 (Fixture)</li>
 *   <li>✅ CQRS Command 전용 Adapter 검증</li>
 *   <li>✅ Query 작업은 QueryAdapter에 위임 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@DataJpaTest
@Import({
    ScheduleOutboxCommandAdapter.class,
    ScheduleOutboxQueryAdapter.class,
    ScheduleOutboxMapper.class,
    JPAQueryFactory.class
})
@DisplayName("ScheduleOutboxCommandAdapter 통합 테스트")
class ScheduleOutboxCommandAdapterTest {

    @Autowired
    private ScheduleOutboxCommandAdapter sut;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ScheduleOutboxJpaRepository outboxRepository;

    @AfterEach
    void tearDown() {
        outboxRepository.deleteAll();
    }

    @Nested
    @DisplayName("save 메서드는")
    class Describe_save {

        @Nested
        @DisplayName("신규 ScheduleOutbox가 주어지면 (ID 없음)")
        class Context_with_new_outbox {

            private ScheduleOutbox newOutbox;

            @BeforeEach
            void setUp() {
                // Given: 신규 Outbox
                newOutbox = ScheduleOutboxFixture.create();
            }

            @Test
            @DisplayName("Outbox를 저장하고 ID를 포함한 도메인 객체를 반환한다")
            void it_saves_outbox_and_returns_with_id() {
                // When
                ScheduleOutbox saved = sut.save(newOutbox);

                // Then
                assertThat(saved).isNotNull();
                assertThat(saved.getId()).isNotNull();
                assertThat(saved.getSellerId()).isEqualTo(newOutbox.getSellerId());
                assertThat(saved.getIdemKey()).isEqualTo(newOutbox.getIdemKey());

                // And: DB에 실제 저장되었는지 확인
                entityManager.flush();
                entityManager.clear();

                Optional<ScheduleOutboxEntity> found = outboxRepository.findById(saved.getId());
                assertThat(found).isPresent();
                assertThat(found.get().getIdemKey()).isEqualTo(newOutbox.getIdemKey());
            }
        }

        @Nested
        @DisplayName("기존 ScheduleOutbox가 주어지면 (ID 있음)")
        class Context_with_existing_outbox {

            private ScheduleOutbox existingOutbox;

            @BeforeEach
            void setUp() {
                // Given: 기존 Outbox 저장
                ScheduleOutboxEntity entity = ScheduleOutboxEntityFixture.createPending();
                entity = outboxRepository.save(entity);
                entityManager.flush();
                entityManager.clear();

                existingOutbox = ScheduleOutboxFixture.reconstitute(
                    entity.getId(),
                    ScheduleOutbox.OperationState.IN_PROGRESS,
                    ScheduleOutbox.WriteAheadState.PENDING
                );
            }

            @Test
            @DisplayName("Outbox를 업데이트하고 도메인 객체를 반환한다")
            void it_updates_outbox_and_returns() {
                // When
                ScheduleOutbox updated = sut.save(existingOutbox);

                // Then
                assertThat(updated).isNotNull();
                assertThat(updated.getId()).isEqualTo(existingOutbox.getId());
                assertThat(updated.getOperationState())
                    .isEqualTo(ScheduleOutbox.OperationState.IN_PROGRESS);

                // And: DB에 실제 업데이트되었는지 확인
                entityManager.flush();
                entityManager.clear();

                Optional<ScheduleOutboxEntity> found = outboxRepository.findById(updated.getId());
                assertThat(found).isPresent();
                assertThat(found.get().getOperationState())
                    .isEqualTo(ScheduleOutbox.OperationState.IN_PROGRESS);
            }
        }

        @Nested
        @DisplayName("null이 주어지면")
        class Context_with_null {

            @Test
            @DisplayName("NullPointerException을 발생시킨다")
            void it_throws_null_pointer_exception() {
                // When & Then
                assertThatThrownBy(() -> sut.save(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("outbox must not be null");
            }
        }
    }

    @Nested
    @DisplayName("delete 메서드는")
    class Describe_delete {

        @Nested
        @DisplayName("ID가 있는 ScheduleOutbox가 주어지면")
        class Context_with_outbox_having_id {

            private ScheduleOutbox savedOutbox;

            @BeforeEach
            void setUp() {
                // Given: Outbox 저장
                ScheduleOutboxEntity entity = ScheduleOutboxEntityFixture.createPending();
                entity = outboxRepository.save(entity);
                entityManager.flush();
                entityManager.clear();

                savedOutbox = ScheduleOutboxFixture.createWithId(entity.getId());
            }

            @Test
            @DisplayName("Outbox를 삭제한다")
            void it_deletes_outbox() {
                // When
                sut.delete(savedOutbox);

                // Then
                entityManager.flush();
                entityManager.clear();

                Optional<ScheduleOutboxEntity> found = outboxRepository.findById(savedOutbox.getId());
                assertThat(found).isEmpty();
            }
        }

        @Nested
        @DisplayName("ID가 없는 ScheduleOutbox가 주어지면")
        class Context_with_outbox_without_id {

            @Test
            @DisplayName("IllegalArgumentException을 발생시킨다")
            void it_throws_illegal_argument_exception() {
                // Given: ID 없는 Outbox
                ScheduleOutbox outbox = ScheduleOutboxFixture.create();

                // When & Then
                assertThatThrownBy(() -> sut.delete(outbox))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Outbox ID가 없어 삭제할 수 없습니다");
            }
        }

        @Nested
        @DisplayName("null이 주어지면")
        class Context_with_null {

            @Test
            @DisplayName("NullPointerException을 발생시킨다")
            void it_throws_null_pointer_exception() {
                // When & Then
                assertThatThrownBy(() -> sut.delete(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("outbox must not be null");
            }
        }
    }

    @Nested
    @DisplayName("Query 작업 위임 검증")
    class Describe_query_delegation {

        @Nested
        @DisplayName("findByIdemKey 호출 시")
        class Context_when_calling_find_by_idem_key {

            @BeforeEach
            void setUp() {
                // Given: Outbox 저장
                ScheduleOutboxEntity entity = ScheduleOutboxEntityFixture.createWithIdemKey("idem-12345");
                outboxRepository.save(entity);
                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("QueryAdapter에 위임하여 조회한다")
            void it_delegates_to_query_adapter() {
                // When
                Optional<ScheduleOutbox> result = sut.findByIdemKey("idem-12345");

                // Then
                assertThat(result).isPresent();
                assertThat(result.get().getIdemKey()).isEqualTo("idem-12345");
            }
        }

        @Nested
        @DisplayName("existsByIdemKey 호출 시")
        class Context_when_calling_exists_by_idem_key {

            @BeforeEach
            void setUp() {
                // Given: Outbox 저장
                ScheduleOutboxEntity entity = ScheduleOutboxEntityFixture.createWithIdemKey("idem-exists");
                outboxRepository.save(entity);
                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("QueryAdapter에 위임하여 존재 여부를 확인한다")
            void it_delegates_to_query_adapter_for_existence_check() {
                // When
                boolean exists = sut.existsByIdemKey("idem-exists");

                // Then
                assertThat(exists).isTrue();
            }
        }

        @Nested
        @DisplayName("findByWalStatePending 호출 시")
        class Context_when_calling_find_by_wal_state_pending {

            @BeforeEach
            void setUp() {
                // Given: PENDING 상태 Outbox 저장
                outboxRepository.save(ScheduleOutboxEntityFixture.createPending());
                outboxRepository.save(
                    ScheduleOutboxEntityFixture.createCustom(
                        null, 200L, "idem-2",
                        ScheduleOutbox.OperationState.PENDING,
                        ScheduleOutbox.WriteAheadState.PENDING
                    )
                );
                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("QueryAdapter에 위임하여 PENDING 목록을 조회한다")
            void it_delegates_to_query_adapter_for_pending_list() {
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
        @DisplayName("findByOperationStateFailed 호출 시")
        class Context_when_calling_find_by_operation_state_failed {

            @BeforeEach
            void setUp() {
                // Given: FAILED 상태 Outbox 저장
                outboxRepository.save(ScheduleOutboxEntityFixture.createFailed());
                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("QueryAdapter에 위임하여 FAILED 목록을 조회한다")
            void it_delegates_to_query_adapter_for_failed_list() {
                // When
                List<ScheduleOutbox> results = sut.findByOperationStateFailed();

                // Then
                assertThat(results).hasSize(1);
                assertThat(results.get(0).getOperationState())
                    .isEqualTo(ScheduleOutbox.OperationState.FAILED);
            }
        }

        @Nested
        @DisplayName("findByWalStateCompleted 호출 시")
        class Context_when_calling_find_by_wal_state_completed {

            @BeforeEach
            void setUp() {
                // Given: COMPLETED 상태 Outbox 저장
                outboxRepository.save(ScheduleOutboxEntityFixture.createCompleted());
                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("QueryAdapter에 위임하여 COMPLETED 목록을 조회한다")
            void it_delegates_to_query_adapter_for_completed_list() {
                // When
                List<ScheduleOutbox> results = sut.findByWalStateCompleted();

                // Then
                assertThat(results).hasSize(1);
                assertThat(results.get(0).getWalState())
                    .isEqualTo(ScheduleOutbox.WriteAheadState.COMPLETED);
            }
        }
    }

    @Nested
    @DisplayName("CQRS 패턴 준수 검증")
    class Describe_cqrs_pattern_compliance {

        @Test
        @DisplayName("Command Adapter는 CUD 작업만 직접 수행하고 Read는 Query Adapter에 위임한다")
        void it_handles_cud_directly_and_delegates_read_to_query_adapter() {
            // Given: 신규 Outbox
            ScheduleOutbox newOutbox = ScheduleOutboxFixture.create();

            // When: Command 작업 (Create)
            ScheduleOutbox saved = sut.save(newOutbox);
            entityManager.flush();
            entityManager.clear();

            // Then: Query 작업은 QueryAdapter에 위임
            Optional<ScheduleOutbox> found = sut.findByIdemKey(saved.getIdemKey());
            assertThat(found).isPresent();

            // When: Command 작업 (Delete)
            sut.delete(saved);
            entityManager.flush();
            entityManager.clear();

            // Then: Query 작업은 QueryAdapter에 위임
            Optional<ScheduleOutbox> deleted = sut.findByIdemKey(saved.getIdemKey());
            assertThat(deleted).isEmpty();
        }
    }
}
