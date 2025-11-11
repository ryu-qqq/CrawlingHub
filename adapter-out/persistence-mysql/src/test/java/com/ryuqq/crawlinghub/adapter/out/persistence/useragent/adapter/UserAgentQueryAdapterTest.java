package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.UserAgentEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.repository.UserAgentJpaRepository;
import com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentQueryDto;
import com.ryuqq.crawlinghub.application.useragent.port.out.LoadUserAgentPort;
import com.ryuqq.crawlinghub.domain.useragent.TokenStatus;
import com.ryuqq.crawlinghub.domain.useragent.UserAgentId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserAgentQueryAdapter 통합 테스트
 *
 * <p>QueryDSL을 사용한 조회 쿼리를 실제 DB에서 검증합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserAgentQueryAdapter.class, UserAgentQueryAdapterTest.TestConfig.class})
@DisplayName("UserAgentQueryAdapter 통합 테스트")
class UserAgentQueryAdapterTest {

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfig {
        @org.springframework.context.annotation.Bean
        public com.querydsl.jpa.impl.JPAQueryFactory jpaQueryFactory(jakarta.persistence.EntityManager entityManager) {
            return new com.querydsl.jpa.impl.JPAQueryFactory(entityManager);
        }
    }

    @Autowired
    private UserAgentJpaRepository jpaRepository;

    @Autowired
    private LoadUserAgentPort loadUserAgentPort;

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();
    }

    @Nested
    @DisplayName("findById 메서드는")
    class Describe_findById {

        @Test
        @DisplayName("존재하는 UserAgent ID로 조회하면 DTO를 반환한다")
        void it_returns_dto_when_id_exists() {
            // Given
            UserAgentEntity entity = UserAgentEntity.create(
                "Mozilla/5.0",
                "token-123",
                TokenStatus.ACTIVE,
                50,
                LocalDateTime.now(),
                null
            );
            UserAgentEntity saved = jpaRepository.save(entity);

            // When
            Optional<UserAgentQueryDto> found = loadUserAgentPort.findById(UserAgentId.of(saved.getId()));

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().id()).isEqualTo(saved.getId());
            assertThat(found.get().userAgentString()).isEqualTo("Mozilla/5.0");
            assertThat(found.get().tokenStatus()).isEqualTo(TokenStatus.ACTIVE);
            assertThat(found.get().remainingRequests()).isEqualTo(50);
        }

        @Test
        @DisplayName("존재하지 않는 UserAgent ID로 조회하면 Optional.empty()를 반환한다")
        void it_returns_empty_when_id_not_exists() {
            // When
            Optional<UserAgentQueryDto> found = loadUserAgentPort.findById(UserAgentId.of(999L));

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAvailableForRotation 메서드는")
    class Describe_findAvailableForRotation {

        @Test
        @DisplayName("요청 가능한 UserAgent 중 남은 요청 수가 가장 많은 것을 반환한다")
        void it_returns_user_agent_with_most_remaining_requests() {
            // Given: 여러 UserAgent 생성
            UserAgentEntity entity1 = UserAgentEntity.create(
                "Mozilla/5.0 (Chrome)",
                "token-1",
                TokenStatus.IDLE,
                30, // 남은 요청 30
                LocalDateTime.now(),
                null
            );
            UserAgentEntity entity2 = UserAgentEntity.create(
                "Mozilla/5.0 (Firefox)",
                "token-2",
                TokenStatus.ACTIVE,
                80, // 남은 요청 80 (가장 많음)
                LocalDateTime.now(),
                null
            );
            UserAgentEntity entity3 = UserAgentEntity.create(
                "Mozilla/5.0 (Safari)",
                "token-3",
                TokenStatus.RECOVERED,
                50, // 남은 요청 50
                LocalDateTime.now(),
                null
            );
            jpaRepository.save(entity1);
            jpaRepository.save(entity2);
            jpaRepository.save(entity3);

            // When
            Optional<UserAgentQueryDto> found = loadUserAgentPort.findAvailableForRotation();

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().remainingRequests()).isEqualTo(80);
            assertThat(found.get().userAgentString()).isEqualTo("Mozilla/5.0 (Firefox)");
        }

        @Test
        @DisplayName("요청 가능한 UserAgent가 없으면 Optional.empty()를 반환한다")
        void it_returns_empty_when_no_available_user_agent() {
            // Given: 요청 불가능한 UserAgent만 생성
            UserAgentEntity entity1 = UserAgentEntity.create(
                "Mozilla/5.0 (Disabled)",
                null,
                TokenStatus.DISABLED,
                0,
                null,
                null
            );
            UserAgentEntity entity2 = UserAgentEntity.create(
                "Mozilla/5.0 (RateLimited)",
                null,
                TokenStatus.RATE_LIMITED,
                0,
                null,
                LocalDateTime.now().plusHours(1)
            );
            jpaRepository.save(entity1);
            jpaRepository.save(entity2);

            // When
            Optional<UserAgentQueryDto> found = loadUserAgentPort.findAvailableForRotation();

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("remainingRequests가 0인 UserAgent는 제외한다")
        void it_excludes_user_agents_with_zero_remaining_requests() {
            // Given
            UserAgentEntity entity1 = UserAgentEntity.create(
                "Mozilla/5.0 (Available)",
                "token-1",
                TokenStatus.IDLE,
                50, // 요청 가능
                LocalDateTime.now(),
                null
            );
            UserAgentEntity entity2 = UserAgentEntity.create(
                "Mozilla/5.0 (Exhausted)",
                "token-2",
                TokenStatus.ACTIVE,
                0, // 요청 불가능
                LocalDateTime.now(),
                null
            );
            jpaRepository.save(entity1);
            jpaRepository.save(entity2);

            // When
            Optional<UserAgentQueryDto> found = loadUserAgentPort.findAvailableForRotation();

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().remainingRequests()).isGreaterThan(0);
            assertThat(found.get().userAgentString()).isEqualTo("Mozilla/5.0 (Available)");
        }
    }

    @Nested
    @DisplayName("findByStatus 메서드는")
    class Describe_findByStatus {

        @Test
        @DisplayName("특정 상태의 UserAgent 목록을 반환한다")
        void it_returns_user_agents_with_specific_status() {
            // Given
            UserAgentEntity entity1 = UserAgentEntity.create(
                "Mozilla/5.0 (Active1)",
                "token-1",
                TokenStatus.ACTIVE,
                50,
                LocalDateTime.now(),
                null
            );
            UserAgentEntity entity2 = UserAgentEntity.create(
                "Mozilla/5.0 (Active2)",
                "token-2",
                TokenStatus.ACTIVE,
                30,
                LocalDateTime.now(),
                null
            );
            UserAgentEntity entity3 = UserAgentEntity.create(
                "Mozilla/5.0 (Idle)",
                "token-3",
                TokenStatus.IDLE,
                80,
                LocalDateTime.now(),
                null
            );
            jpaRepository.save(entity1);
            jpaRepository.save(entity2);
            jpaRepository.save(entity3);

            // When
            List<UserAgentQueryDto> found = loadUserAgentPort.findByStatus(TokenStatus.ACTIVE);

            // Then
            assertThat(found).hasSize(2);
            assertThat(found).allMatch(dto -> dto.tokenStatus() == TokenStatus.ACTIVE);
        }

        @Test
        @DisplayName("해당 상태의 UserAgent가 없으면 빈 목록을 반환한다")
        void it_returns_empty_list_when_no_user_agents_with_status() {
            // Given: 다른 상태의 UserAgent만 생성
            UserAgentEntity entity = UserAgentEntity.create(
                "Mozilla/5.0",
                "token-1",
                TokenStatus.IDLE,
                80,
                LocalDateTime.now(),
                null
            );
            jpaRepository.save(entity);

            // When
            List<UserAgentQueryDto> found = loadUserAgentPort.findByStatus(TokenStatus.RATE_LIMITED);

            // Then
            assertThat(found).isEmpty();
        }
    }
}



