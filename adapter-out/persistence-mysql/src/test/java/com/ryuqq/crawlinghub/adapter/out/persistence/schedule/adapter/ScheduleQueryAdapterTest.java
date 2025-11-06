package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleEntityFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.ScheduleJpaRepository;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.CrawlScheduleId;
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
 * ScheduleQueryAdapter 통합 테스트
 *
 * <p>Persistence Layer 통합 테스트 컨벤션 준수:</p>
 * <ul>
 *   <li>✅ @DataJpaTest - JPA 관련 Bean만 로드</li>
 *   <li>✅ TestEntityManager - 영속성 컨텍스트 제어</li>
 *   <li>✅ Object Mother 패턴 사용 (Fixture)</li>
 *   <li>✅ QueryDSL Projections.constructor() 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@DataJpaTest
@Import({ScheduleQueryAdapter.class, JPAQueryFactory.class})
@DisplayName("ScheduleQueryAdapter 통합 테스트")
class ScheduleQueryAdapterTest {

    @Autowired
    private ScheduleQueryAdapter sut;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ScheduleJpaRepository scheduleRepository;

    @AfterEach
    void tearDown() {
        scheduleRepository.deleteAll();
    }

    @Nested
    @DisplayName("findById 메서드는")
    class Describe_findById {

        @Nested
        @DisplayName("존재하는 Schedule ID가 주어지면")
        class Context_with_existing_schedule_id {

            private ScheduleEntity savedEntity;

            @BeforeEach
            void setUp() {
                // Given: DB에 Schedule 저장
                savedEntity = ScheduleEntityFixture.createActive();
                savedEntity = scheduleRepository.save(savedEntity);
                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("CrawlSchedule 도메인 객체를 반환한다")
            void it_returns_schedule_domain_object() {
                // When
                Optional<CrawlSchedule> result = sut.findById(
                    CrawlScheduleId.of(savedEntity.getId())
                );

                // Then
                assertThat(result).isPresent();
                assertThat(result.get().getIdValue()).isEqualTo(savedEntity.getId());
                assertThat(result.get().getSellerIdValue()).isEqualTo(savedEntity.getSellerId());
                assertThat(result.get().getCronExpressionValue()).isEqualTo(savedEntity.getCronExpression());
            }
        }

        @Nested
        @DisplayName("존재하지 않는 Schedule ID가 주어지면")
        class Context_with_non_existing_schedule_id {

            @Test
            @DisplayName("빈 Optional을 반환한다")
            void it_returns_empty_optional() {
                // When
                Optional<CrawlSchedule> result = sut.findById(
                    CrawlScheduleId.of(999L)
                );

                // Then
                assertThat(result).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("findActiveBySellerId 메서드는")
    class Describe_findActiveBySellerId {

        @Nested
        @DisplayName("ACTIVE 상태의 Schedule이 존재하면")
        class Context_with_active_schedule {

            private ScheduleEntity activeSchedule;
            private ScheduleEntity suspendedSchedule;

            @BeforeEach
            void setUp() {
                // Given: 같은 Seller의 ACTIVE와 SUSPENDED Schedule 저장
                activeSchedule = ScheduleEntityFixture.createCustom(
                    null, 100L, "0 0 * * * ?",
                    ScheduleEntity.ScheduleStatus.ACTIVE
                );
                suspendedSchedule = ScheduleEntityFixture.createCustom(
                    null, 100L, "0 0 0 * * ?",
                    ScheduleEntity.ScheduleStatus.SUSPENDED
                );

                scheduleRepository.save(activeSchedule);
                scheduleRepository.save(suspendedSchedule);
                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("ACTIVE 상태의 Schedule만 반환한다")
            void it_returns_only_active_schedule() {
                // When
                Optional<CrawlSchedule> result = sut.findActiveBySellerId(100L);

                // Then
                assertThat(result).isPresent();
                assertThat(result.get().getSellerIdValue()).isEqualTo(100L);
                assertThat(result.get().getCronExpressionValue()).isEqualTo("0 0 * * * ?");
            }
        }

        @Nested
        @DisplayName("ACTIVE 상태의 Schedule이 없으면")
        class Context_without_active_schedule {

            @BeforeEach
            void setUp() {
                // Given: SUSPENDED Schedule만 저장
                ScheduleEntity suspendedSchedule = ScheduleEntityFixture.createCustom(
                    null, 100L, "0 0 0 * * ?",
                    ScheduleEntity.ScheduleStatus.SUSPENDED
                );
                scheduleRepository.save(suspendedSchedule);
                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("빈 Optional을 반환한다")
            void it_returns_empty_optional() {
                // When
                Optional<CrawlSchedule> result = sut.findActiveBySellerId(100L);

                // Then
                assertThat(result).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("findAllBySellerId 메서드는")
    class Describe_findAllBySellerId {

        @Nested
        @DisplayName("여러 개의 Schedule이 존재하면")
        class Context_with_multiple_schedules {

            @BeforeEach
            void setUp() {
                // Given: 같은 Seller의 여러 Schedule 저장
                scheduleRepository.save(
                    ScheduleEntityFixture.createCustom(
                        null, 100L, "0 0 * * * ?",
                        ScheduleEntity.ScheduleStatus.ACTIVE
                    )
                );
                scheduleRepository.save(
                    ScheduleEntityFixture.createCustom(
                        null, 100L, "0 0 0 * * ?",
                        ScheduleEntity.ScheduleStatus.SUSPENDED
                    )
                );
                scheduleRepository.save(
                    ScheduleEntityFixture.createCustom(
                        null, 200L, "0 0 12 * * ?",
                        ScheduleEntity.ScheduleStatus.ACTIVE
                    )
                );

                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("해당 Seller의 모든 Schedule을 반환한다")
            void it_returns_all_schedules_for_seller() {
                // When
                List<CrawlSchedule> results = sut.findAllBySellerId(100L);

                // Then
                assertThat(results).hasSize(2);
                assertThat(results).allMatch(
                    schedule -> schedule.getSellerIdValue().equals(100L)
                );
            }
        }

        @Nested
        @DisplayName("Schedule이 존재하지 않으면")
        class Context_without_schedules {

            @Test
            @DisplayName("빈 리스트를 반환한다")
            void it_returns_empty_list() {
                // When
                List<CrawlSchedule> results = sut.findAllBySellerId(999L);

                // Then
                assertThat(results).isEmpty();
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
                // Given: Schedule 저장
                ScheduleEntity entity = ScheduleEntityFixture.createActive();
                scheduleRepository.save(entity);
                entityManager.flush();
                entityManager.clear();
            }

            @Test
            @DisplayName("DTO로 직접 프로젝션하여 조회한다")
            void it_projects_directly_to_dto() {
                // When
                Optional<CrawlSchedule> result = sut.findActiveBySellerId(100L);

                // Then
                // QueryDSL Projections.constructor()를 통해 직접 DTO 조회
                assertThat(result).isPresent();

                // 영속성 컨텍스트에 엔티티가 로드되지 않음 확인
                assertThat(entityManager.getEntityManager().contains(result.get()))
                    .isFalse();
            }
        }
    }
}
