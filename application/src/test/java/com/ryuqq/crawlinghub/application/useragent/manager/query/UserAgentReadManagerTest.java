package com.ryuqq.crawlinghub.application.useragent.manager.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentReadManager;
import com.ryuqq.crawlinghub.application.useragent.port.out.query.UserAgentQueryPort;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UserAgentReadManager 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAgentReadManager 테스트")
class UserAgentReadManagerTest {

    @Mock private UserAgentQueryPort userAgentQueryPort;
    @Mock private UserAgent userAgent;

    private UserAgentReadManager manager;

    @BeforeEach
    void setUp() {
        manager = new UserAgentReadManager(userAgentQueryPort);
    }

    @Nested
    @DisplayName("findAllAvailable() 테스트")
    class FindAllAvailable {

        @Test
        @DisplayName("[성공] 활성화된 UserAgent 전체 조회")
        void shouldDelegateToQueryPort() {
            // Given
            given(userAgentQueryPort.findAllAvailable()).willReturn(List.of(userAgent));

            // When
            List<UserAgent> result = manager.findAllAvailable();

            // Then
            assertThat(result).hasSize(1).contains(userAgent);
            verify(userAgentQueryPort).findAllAvailable();
        }

        @Test
        @DisplayName("[성공] 활성화된 UserAgent 없으면 빈 리스트 반환")
        void shouldReturnEmptyListWhenNoAvailable() {
            // Given
            given(userAgentQueryPort.findAllAvailable()).willReturn(List.of());

            // When
            List<UserAgent> result = manager.findAllAvailable();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById() 테스트")
    class FindById {

        @Test
        @DisplayName("[성공] ID로 UserAgent 조회")
        void shouldDelegateToQueryPort() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            given(userAgentQueryPort.findById(userAgentId)).willReturn(Optional.of(userAgent));

            // When
            Optional<UserAgent> result = manager.findById(userAgentId);

            // Then
            assertThat(result).isPresent().contains(userAgent);
            verify(userAgentQueryPort).findById(userAgentId);
        }

        @Test
        @DisplayName("[성공] 존재하지 않는 경우 empty 반환")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(999L);
            given(userAgentQueryPort.findById(userAgentId)).willReturn(Optional.empty());

            // When
            Optional<UserAgent> result = manager.findById(userAgentId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByStatus() 테스트")
    class CountByStatus {

        @Test
        @DisplayName("[성공] 상태별 UserAgent 개수 조회")
        void shouldDelegateToQueryPort() {
            // Given
            UserAgentStatus status = UserAgentStatus.IDLE;
            given(userAgentQueryPort.countByStatus(status)).willReturn(5L);

            // When
            long result = manager.countByStatus(status);

            // Then
            assertThat(result).isEqualTo(5L);
            verify(userAgentQueryPort).countByStatus(status);
        }
    }

    @Nested
    @DisplayName("countAll() 테스트")
    class CountAll {

        @Test
        @DisplayName("[성공] 전체 UserAgent 개수 조회")
        void shouldDelegateToQueryPort() {
            // Given
            given(userAgentQueryPort.countAll()).willReturn(100L);

            // When
            long result = manager.countAll();

            // Then
            assertThat(result).isEqualTo(100L);
            verify(userAgentQueryPort).countAll();
        }
    }

    @Nested
    @DisplayName("findByStatus() 테스트")
    class FindByStatus {

        @Test
        @DisplayName("[성공] 상태별 UserAgent 목록 조회")
        void shouldDelegateToQueryPort() {
            // Given
            UserAgentStatus status = UserAgentStatus.IDLE;
            given(userAgentQueryPort.findByStatus(status)).willReturn(List.of(userAgent));

            // When
            List<UserAgent> result = manager.findByStatus(status);

            // Then
            assertThat(result).hasSize(1).contains(userAgent);
            verify(userAgentQueryPort).findByStatus(status);
        }
    }
}
