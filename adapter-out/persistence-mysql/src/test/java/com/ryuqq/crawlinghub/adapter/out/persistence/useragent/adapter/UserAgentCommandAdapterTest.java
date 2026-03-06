package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.UserAgentJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.mapper.UserAgentJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.repository.UserAgentJpaRepository;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UserAgentCommandAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("UserAgentCommandAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class UserAgentCommandAdapterTest {

    @Mock private UserAgentJpaRepository jpaRepository;

    @Mock private UserAgentJpaEntityMapper mapper;

    private UserAgentCommandAdapter commandAdapter;

    private UserAgentJpaEntity buildUserAgentEntity(Long id) {
        LocalDateTime now = LocalDateTime.now();
        return UserAgentJpaEntity.of(
                id,
                "Mozilla/5.0",
                "DESKTOP",
                "GENERIC",
                "LINUX",
                "5.10",
                "CHROME",
                "120.0.0.0",
                UserAgentStatus.IDLE,
                100,
                null,
                0,
                now,
                now);
    }

    @BeforeEach
    void setUp() {
        commandAdapter = new UserAgentCommandAdapter(jpaRepository, mapper);
    }

    @Test
    @DisplayName("성공 - UserAgent 저장 시 ID 반환")
    void shouldReturnIdWhenPersist() {
        // Given
        UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();
        LocalDateTime now = LocalDateTime.now();
        UserAgentJpaEntity entity =
                UserAgentJpaEntity.of(
                        null,
                        "Mozilla/5.0",
                        "DESKTOP",
                        "GENERIC",
                        "LINUX",
                        "5.10",
                        "CHROME",
                        "120.0.0.0",
                        UserAgentStatus.IDLE,
                        100,
                        null,
                        0,
                        now,
                        now);
        UserAgentJpaEntity savedEntity =
                UserAgentJpaEntity.of(
                        1L,
                        "Mozilla/5.0",
                        "DESKTOP",
                        "GENERIC",
                        "LINUX",
                        "5.10",
                        "CHROME",
                        "120.0.0.0",
                        UserAgentStatus.IDLE,
                        100,
                        null,
                        0,
                        now,
                        now);

        given(mapper.toEntity(userAgent)).willReturn(entity);
        given(jpaRepository.save(entity)).willReturn(savedEntity);

        // When
        UserAgentId result = commandAdapter.persist(userAgent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.value()).isEqualTo(1L);
        verify(mapper).toEntity(userAgent);
        verify(jpaRepository).save(entity);
    }

    @Test
    @DisplayName("성공 - 기존 UserAgent 수정")
    void shouldUpdateExistingUserAgent() {
        // Given
        UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();
        LocalDateTime now = LocalDateTime.now();
        UserAgentJpaEntity entity =
                UserAgentJpaEntity.of(
                        100L,
                        "Mozilla/5.0",
                        "DESKTOP",
                        "GENERIC",
                        "LINUX",
                        "5.10",
                        "CHROME",
                        "120.0.0.0",
                        UserAgentStatus.BLOCKED,
                        50,
                        null,
                        0,
                        now,
                        now);

        given(mapper.toEntity(userAgent)).willReturn(entity);
        given(jpaRepository.save(entity)).willReturn(entity);

        // When
        UserAgentId result = commandAdapter.persist(userAgent);

        // Then
        assertThat(result.value()).isEqualTo(100L);
    }

    @Nested
    @DisplayName("persistAll 메서드 테스트")
    class PersistAllTests {

        @Test
        @DisplayName("성공 - 여러 UserAgent 배치 저장")
        void shouldPersistAllUserAgents() {
            // Given
            UserAgent userAgent1 = UserAgentFixture.anAvailableUserAgent();
            UserAgent userAgent2 = UserAgentFixture.anAvailableUserAgent();
            List<UserAgent> userAgents = List.of(userAgent1, userAgent2);

            UserAgentJpaEntity entity1 = buildUserAgentEntity(null);
            UserAgentJpaEntity entity2 = buildUserAgentEntity(null);

            given(mapper.toEntity(userAgent1)).willReturn(entity1);
            given(mapper.toEntity(userAgent2)).willReturn(entity2);

            // When
            commandAdapter.persistAll(userAgents);

            // Then
            org.mockito.BDDMockito.then(jpaRepository).should().saveAll(List.of(entity1, entity2));
        }

        @Test
        @DisplayName("성공 - 빈 리스트로 배치 저장 호출")
        void shouldHandleEmptyList() {
            // Given
            List<UserAgent> emptyList = List.of();

            // When
            commandAdapter.persistAll(emptyList);

            // Then
            org.mockito.BDDMockito.then(jpaRepository).should().saveAll(List.of());
        }
    }
}
