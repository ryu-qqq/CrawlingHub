package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.UserAgentJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.mapper.UserAgentJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.repository.UserAgentQueryDslRepository;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentSearchCriteria;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentSummaryResponse;
import com.ryuqq.crawlinghub.domain.common.vo.PageRequest;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
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
                            UserAgentStatus.READY,
                            100,
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
                            UserAgentStatus.READY,
                            100,
                            null,
                            0,
                            now,
                            now);
            UserAgent domain = UserAgentFixture.anAvailableUserAgent();

            given(queryDslRepository.findByStatus(UserAgentStatus.READY))
                    .willReturn(List.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // When
            List<UserAgent> result = queryAdapter.findAllAvailable();

            // Then
            assertThat(result).hasSize(1);
            verify(queryDslRepository).findByStatus(UserAgentStatus.READY);
        }
    }

    @Nested
    @DisplayName("countByStatus 테스트")
    class CountByStatusTests {

        @Test
        @DisplayName("성공 - 상태별 개수 조회")
        void shouldCountByStatus() {
            // Given
            given(queryDslRepository.countByStatus(UserAgentStatus.READY)).willReturn(5L);

            // When
            long result = queryAdapter.countByStatus(UserAgentStatus.READY);

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

    @Nested
    @DisplayName("findByCriteria 테스트")
    class FindByCriteriaTests {

        @Test
        @DisplayName("성공 - 전체 조회 (상태 필터 없음)")
        void shouldFindAllWithPaging() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 20);
            UserAgentSearchCriteria criteria = UserAgentSearchCriteria.all(pageRequest);

            LocalDateTime now = LocalDateTime.now();
            UserAgentJpaEntity entity1 =
                    UserAgentJpaEntity.of(
                            1L,
                            "encrypted-token-1",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                            "DESKTOP",
                            "GENERIC",
                            "LINUX",
                            "5.10",
                            "CHROME",
                            "120.0.0.0",
                            UserAgentStatus.READY,
                            95,
                            now,
                            150,
                            now,
                            now);
            UserAgentJpaEntity entity2 =
                    UserAgentJpaEntity.of(
                            2L,
                            "encrypted-token-2",
                            "Mozilla/5.0 (iPhone; CPU iPhone OS 15_0)",
                            "MOBILE",
                            "GENERIC",
                            "LINUX",
                            "5.10",
                            "CHROME",
                            "120.0.0.0",
                            UserAgentStatus.SUSPENDED,
                            60,
                            now,
                            80,
                            now,
                            now);

            given(queryDslRepository.findByCriteria(criteria))
                    .willReturn(List.of(entity1, entity2));
            given(queryDslRepository.countByCriteria(criteria)).willReturn(2L);

            // When
            PageResponse<UserAgentSummaryResponse> result = queryAdapter.findByCriteria(criteria);

            // Then
            assertThat(result.content()).hasSize(2);
            assertThat(result.totalElements()).isEqualTo(2);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isTrue();
        }

        @Test
        @DisplayName("성공 - 상태별 조회 (AVAILABLE)")
        void shouldFindByStatusWithPaging() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 10);
            UserAgentSearchCriteria criteria =
                    UserAgentSearchCriteria.byStatus(UserAgentStatus.READY, pageRequest);

            LocalDateTime now = LocalDateTime.now();
            UserAgentJpaEntity entity =
                    UserAgentJpaEntity.of(
                            1L,
                            "encrypted-token",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                            "DESKTOP",
                            "GENERIC",
                            "LINUX",
                            "5.10",
                            "CHROME",
                            "120.0.0.0",
                            UserAgentStatus.READY,
                            95,
                            now,
                            150,
                            now,
                            now);

            given(queryDslRepository.findByCriteria(criteria)).willReturn(List.of(entity));
            given(queryDslRepository.countByCriteria(criteria)).willReturn(1L);

            // When
            PageResponse<UserAgentSummaryResponse> result = queryAdapter.findByCriteria(criteria);

            // Then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).status()).isEqualTo(UserAgentStatus.READY);
            assertThat(result.totalElements()).isEqualTo(1);
            verify(queryDslRepository).findByCriteria(criteria);
            verify(queryDslRepository).countByCriteria(criteria);
        }

        @Test
        @DisplayName("성공 - 빈 결과 반환")
        void shouldReturnEmptyResult() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 20);
            UserAgentSearchCriteria criteria =
                    UserAgentSearchCriteria.byStatus(UserAgentStatus.BLOCKED, pageRequest);

            given(queryDslRepository.findByCriteria(criteria)).willReturn(List.of());
            given(queryDslRepository.countByCriteria(criteria)).willReturn(0L);

            // When
            PageResponse<UserAgentSummaryResponse> result = queryAdapter.findByCriteria(criteria);

            // Then
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
            assertThat(result.totalPages()).isZero();
        }

        @Test
        @DisplayName("성공 - 페이징 정보 확인 (2페이지)")
        void shouldReturnCorrectPagingInfo() {
            // Given
            PageRequest pageRequest = PageRequest.of(1, 10);
            UserAgentSearchCriteria criteria = UserAgentSearchCriteria.all(pageRequest);

            LocalDateTime now = LocalDateTime.now();
            UserAgentJpaEntity entity =
                    UserAgentJpaEntity.of(
                            11L,
                            "encrypted-token",
                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)",
                            "DESKTOP",
                            "GENERIC",
                            "LINUX",
                            "5.10",
                            "CHROME",
                            "120.0.0.0",
                            UserAgentStatus.READY,
                            88,
                            now,
                            120,
                            now,
                            now);

            given(queryDslRepository.findByCriteria(criteria)).willReturn(List.of(entity));
            given(queryDslRepository.countByCriteria(criteria)).willReturn(25L);

            // When
            PageResponse<UserAgentSummaryResponse> result = queryAdapter.findByCriteria(criteria);

            // Then
            assertThat(result.page()).isEqualTo(1);
            assertThat(result.totalElements()).isEqualTo(25);
            assertThat(result.totalPages()).isEqualTo(3);
            assertThat(result.first()).isFalse();
            assertThat(result.last()).isFalse();
        }

        @Test
        @DisplayName("성공 - Entity → SummaryResponse 변환 확인")
        void shouldMapEntityToSummaryResponse() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 10);
            UserAgentSearchCriteria criteria = UserAgentSearchCriteria.all(pageRequest);

            LocalDateTime now = LocalDateTime.now();
            UserAgentJpaEntity entity =
                    UserAgentJpaEntity.of(
                            1L,
                            "encrypted-token",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0",
                            "DESKTOP",
                            "GENERIC",
                            "LINUX",
                            "5.10",
                            "CHROME",
                            "120.0.0.0",
                            UserAgentStatus.READY,
                            95,
                            now,
                            150,
                            now,
                            now);

            given(queryDslRepository.findByCriteria(criteria)).willReturn(List.of(entity));
            given(queryDslRepository.countByCriteria(criteria)).willReturn(1L);

            // When
            PageResponse<UserAgentSummaryResponse> result = queryAdapter.findByCriteria(criteria);

            // Then
            UserAgentSummaryResponse response = result.content().get(0);
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.userAgentValue())
                    .isEqualTo("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0");
            assertThat(response.deviceType().type().name()).isEqualTo("DESKTOP");
            assertThat(response.status()).isEqualTo(UserAgentStatus.READY);
            assertThat(response.healthScore()).isEqualTo(95);
            assertThat(response.requestsPerDay()).isEqualTo(150);
        }
    }
}
