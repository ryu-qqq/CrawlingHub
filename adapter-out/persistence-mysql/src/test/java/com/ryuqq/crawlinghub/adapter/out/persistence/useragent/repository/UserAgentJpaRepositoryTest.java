package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.UserAgentEntity;
import com.ryuqq.crawlinghub.domain.useragent.TokenStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserAgentJpaRepository 통합 테스트
 *
 * <p>JPA Repository의 기본 CRUD 동작을 검증합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("UserAgentJpaRepository 통합 테스트")
class UserAgentJpaRepositoryTest {

    @Autowired
    private UserAgentJpaRepository jpaRepository;

    @Nested
    @DisplayName("save 메서드는")
    class Describe_save {

        @Test
        @DisplayName("신규 UserAgent Entity를 저장하면 ID가 생성된다")
        void it_generates_id_when_saving_new_entity() {
            // Given
            UserAgentEntity entity = UserAgentEntity.create(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                null,
                TokenStatus.IDLE,
                80,
                null,
                null
            );

            // When
            UserAgentEntity saved = jpaRepository.save(entity);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getUserAgentString()).isEqualTo("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            assertThat(saved.getTokenStatus()).isEqualTo(TokenStatus.IDLE);
            assertThat(saved.getRemainingRequests()).isEqualTo(80);
        }

        @Test
        @DisplayName("기존 UserAgent Entity를 수정하면 업데이트된다")
        void it_updates_existing_entity() {
            // Given: 기존 Entity 저장
            UserAgentEntity entity = UserAgentEntity.create(
                "Mozilla/5.0",
                null,
                TokenStatus.IDLE,
                80,
                null,
                null
            );
            UserAgentEntity saved = jpaRepository.save(entity);
            Long id = saved.getId();

            // When: Entity 수정
            UserAgentEntity reconstituted = UserAgentEntity.reconstitute(
                id,
                "Mozilla/5.0 (Updated)",
                "new-token",
                TokenStatus.ACTIVE,
                50,
                LocalDateTime.now(),
                null,
                saved.getCreatedAt(),  // ⭐ Audit 필드 전달
                saved.getUpdatedAt()   // ⭐ Audit 필드 전달
            );
            UserAgentEntity updated = jpaRepository.save(reconstituted);

            // Then
            assertThat(updated.getId()).isEqualTo(id);
            assertThat(updated.getUserAgentString()).isEqualTo("Mozilla/5.0 (Updated)");
            assertThat(updated.getTokenStatus()).isEqualTo(TokenStatus.ACTIVE);
            assertThat(updated.getRemainingRequests()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("findById 메서드는")
    class Describe_findById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 Entity를 반환한다")
        void it_returns_entity_when_id_exists() {
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
            Long id = saved.getId();

            // When
            Optional<UserAgentEntity> found = jpaRepository.findById(id);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUserAgentString()).isEqualTo("Mozilla/5.0");
            assertThat(found.get().getTokenStatus()).isEqualTo(TokenStatus.ACTIVE);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 Optional.empty()를 반환한다")
        void it_returns_empty_when_id_not_exists() {
            // When
            Optional<UserAgentEntity> found = jpaRepository.findById(999L);

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete 메서드는")
    class Describe_delete {

        @Test
        @DisplayName("Entity를 삭제하면 조회 시 Optional.empty()를 반환한다")
        void it_deletes_entity() {
            // Given
            UserAgentEntity entity = UserAgentEntity.create(
                "Mozilla/5.0",
                null,
                TokenStatus.IDLE,
                80,
                null,
                null
            );
            UserAgentEntity saved = jpaRepository.save(entity);
            Long id = saved.getId();

            // When
            jpaRepository.delete(saved);

            // Then
            Optional<UserAgentEntity> found = jpaRepository.findById(id);
            assertThat(found).isEmpty();
        }
    }
}

