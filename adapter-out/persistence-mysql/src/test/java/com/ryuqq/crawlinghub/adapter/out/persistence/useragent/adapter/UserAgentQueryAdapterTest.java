package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.UserAgentJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.mapper.UserAgentJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.repository.UserAgentQueryDslRepository;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UserAgentQueryAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("UserAgentQueryAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class UserAgentQueryAdapterTest {

    @Mock private UserAgentQueryDslRepository queryDslRepository;

    @Mock private UserAgentJpaEntityMapper mapper;

    private UserAgentQueryAdapter queryAdapter;

    @BeforeEach
    void setUp() {
        queryAdapter = new UserAgentQueryAdapter(queryDslRepository, mapper);
    }

    @Nested
    @DisplayName("findById 테스트")
    class FindByIdTests {

        @Test
        @DisplayName("성공 - ID로 UserAgent 조회")
        void shouldFindById() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            LocalDateTime now = LocalDateTime.now();
            UserAgentJpaEntity entity =
                    UserAgentJpaEntity.of(
                            1L,
                            "encrypted-token",
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
                            null,
                            0,
                            now,
                            now);
            UserAgent domain = UserAgentFixture.anAvailableUserAgent();

            given(queryDslRepository.findById(1L)).willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // When
            Optional<UserAgent> result = queryAdapter.findById(userAgentId);

            // Then
            assertThat(result).isPresent();
            verify(queryDslRepository).findById(1L);
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 ID 조회 시 빈 Optional 반환")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(999L);
            given(queryDslRepository.findById(999L)).willReturn(Optional.empty());

            // When
            Optional<UserAgent> result = queryAdapter.findById(userAgentId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllAvailable 테스트")
    class FindAllAvailableTests {

        @Test
        @DisplayName("성공 - 사용 가능한 UserAgent 전체 조회")
        void shouldFindAllAvailable() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            UserAgentJpaEntity entity =
                    UserAgentJpaEntity.of(
                            1L,
                            "encrypted-token",
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
                            null,
                            0,
                            now,
                            now);
            UserAgent domain = UserAgentFixture.anAvailableUserAgent();

            given(queryDslRepository.findByStatus(UserAgentStatus.IDLE))
                    .willReturn(List.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // When
            List<UserAgent> result = queryAdapter.findAllAvailable();

            // Then
            assertThat(result).hasSize(1);
            verify(queryDslRepository).findByStatus(UserAgentStatus.IDLE);
        }
    }

    @Nested
    @DisplayName("countByStatus 테스트")
    class CountByStatusTests {

        @Test
        @DisplayName("성공 - 상태별 개수 조회")
        void shouldCountByStatus() {
            // Given
            given(queryDslRepository.countByStatus(UserAgentStatus.IDLE)).willReturn(5L);

            // When
            long result = queryAdapter.countByStatus(UserAgentStatus.IDLE);

            // Then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("countAll 테스트")
    class CountAllTests {

        @Test
        @DisplayName("성공 - 전체 개수 조회")
        void shouldCountAll() {
            // Given
            given(queryDslRepository.countAll()).willReturn(10L);

            // When
            long result = queryAdapter.countAll();

            // Then
            assertThat(result).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("findByStatus 테스트")
    class FindByStatusTests {

        @Test
        @DisplayName("성공 - 상태별 목록 조회")
        void shouldFindByStatus() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            UserAgentJpaEntity entity =
                    UserAgentJpaEntity.of(
                            1L,
                            "encrypted-token",
                            "Mozilla/5.0",
                            "DESKTOP",
                            "GENERIC",
                            "LINUX",
                            "5.10",
                            "CHROME",
                            "120.0.0.0",
                            UserAgentStatus.BLOCKED,
                            50,
                            now,
                            3,
                            null,
                            0,
                            now,
                            now);
            UserAgent domain = UserAgentFixture.aBlockedUserAgent();

            given(queryDslRepository.findByStatus(UserAgentStatus.BLOCKED))
                    .willReturn(List.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // When
            List<UserAgent> result = queryAdapter.findByStatus(UserAgentStatus.BLOCKED);

            // Then
            assertThat(result).hasSize(1);
        }
    }
}
