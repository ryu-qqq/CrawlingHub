package com.ryuqq.crawlinghub.application.useragent.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.useragent.HealthScoreFixture;
import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentFixture;
import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentIdFixture;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SessionDbStatusManager 단위 테스트
 *
 * <p>세션 발급 후 DB 상태 동기화 매니저 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SessionDbStatusManager 테스트")
class SessionDbStatusManagerTest {

    @Mock private UserAgentReadManager readManager;
    @Mock private UserAgentCommandManager transactionManager;

    @InjectMocks private SessionDbStatusManager sut;

    /**
     * SESSION_REQUIRED 상태 UserAgent (IDLE → IDLE 전환 예외 방지용)
     *
     * <p>SessionDbStatusManager는 SESSION_REQUIRED → IDLE 전환을 수행하므로, 테스트 픽스처도 SESSION_REQUIRED 상태여야
     * 합니다.
     */
    private static UserAgent aSessionRequiredUserAgent() {
        return UserAgentFixture.reconstitute(
                UserAgentStatus.SESSION_REQUIRED, HealthScoreFixture.initial());
    }

    private static UserAgent aSessionRequiredUserAgentWithId(Long id) {
        return UserAgentFixture.anUserAgentWithId(id);
    }

    @Nested
    @DisplayName("updateStatusToIdle() 테스트")
    class UpdateStatusToIdle {

        @Test
        @DisplayName("[성공] UserAgent 목록을 IDLE 상태로 업데이트")
        void shouldUpdateUserAgentsToIdle() {
            // Given
            UserAgentId id = UserAgentIdFixture.anAssignedId();
            // IDLE 상태 UserAgent는 changeStatus(IDLE)하면 예외 발생
            // SESSION_REQUIRED 상태로 사용
            UserAgent userAgent = aSessionRequiredUserAgent();
            List<UserAgentId> ids = List.of(id);

            given(readManager.findByIds(ids)).willReturn(List.of(userAgent));

            // When
            int result = sut.updateStatusToIdle(ids);

            // Then
            assertThat(result).isEqualTo(1);
            then(transactionManager).should().persistAll(List.of(userAgent));
        }

        @Test
        @DisplayName("[성공] 빈 목록이면 0 반환하고 DB 조회 없음")
        void shouldReturnZeroWhenEmptyList() {
            // When
            int result = sut.updateStatusToIdle(List.of());

            // Then
            assertThat(result).isZero();
            then(readManager).should(never()).findByIds(any());
        }

        @Test
        @DisplayName("[성공] null 목록이면 0 반환")
        void shouldReturnZeroWhenNullList() {
            // When
            int result = sut.updateStatusToIdle(null);

            // Then
            assertThat(result).isZero();
            then(readManager).should(never()).findByIds(any());
        }

        @Test
        @DisplayName("[성공] DB에서 UserAgent를 찾을 수 없으면 0 반환")
        void shouldReturnZeroWhenUserAgentsNotFound() {
            // Given
            UserAgentId id = UserAgentIdFixture.anAssignedId();
            List<UserAgentId> ids = List.of(id);

            given(readManager.findByIds(ids)).willReturn(List.of());

            // When
            int result = sut.updateStatusToIdle(ids);

            // Then
            assertThat(result).isZero();
            then(transactionManager).should(never()).persistAll(any());
        }

        @Test
        @DisplayName("[성공] 복수 UserAgent를 한 번의 트랜잭션에서 업데이트")
        void shouldUpdateMultipleUserAgentsInSingleTransaction() {
            // Given
            UserAgentId id1 = UserAgentIdFixture.anAssignedId();
            UserAgentId id2 = UserAgentIdFixture.anAssignedId(2L);
            // 두 개의 독립된 SESSION_REQUIRED 상태 UserAgent
            UserAgent ua1 =
                    UserAgentFixture.reconstitute(
                            UserAgentStatus.SESSION_REQUIRED, HealthScoreFixture.initial());
            UserAgent ua2 =
                    UserAgentFixture.reconstitute(
                            UserAgentStatus.SESSION_REQUIRED, HealthScoreFixture.initial());
            List<UserAgentId> ids = List.of(id1, id2);

            given(readManager.findByIds(ids)).willReturn(List.of(ua1, ua2));

            // When
            int result = sut.updateStatusToIdle(ids);

            // Then
            assertThat(result).isEqualTo(2);
            then(transactionManager).should().persistAll(List.of(ua1, ua2));
        }

        @Test
        @DisplayName("[성공] 요청 ID보다 DB 조회 수가 적으면 누락 경고 후 조회된 것만 업데이트")
        void shouldUpdateFoundUserAgentsWhenPartiallyFound() {
            // Given
            UserAgentId id1 = UserAgentIdFixture.anAssignedId();
            UserAgentId id2 = UserAgentIdFixture.anAssignedId(2L);
            UserAgent ua1 =
                    UserAgentFixture.reconstitute(
                            UserAgentStatus.SESSION_REQUIRED, HealthScoreFixture.initial());
            List<UserAgentId> ids = List.of(id1, id2);

            given(readManager.findByIds(ids)).willReturn(List.of(ua1)); // ua2만 못 찾음

            // When
            int result = sut.updateStatusToIdle(ids);

            // Then
            assertThat(result).isEqualTo(1); // ua1만 업데이트됨
            then(transactionManager).should().persistAll(List.of(ua1));
        }

        @Test
        @DisplayName("[성공] 이미 IDLE인 UserAgent는 건너뛰고 persist 안 함 (선제적 갱신 시나리오)")
        void shouldSkipWhenAlreadyIdle() {
            // Given: 이미 IDLE인 UserAgent (선제적 갱신 시 세션만 갱신, DB 상태는 이미 IDLE)
            UserAgentId id = UserAgentIdFixture.anAssignedId();
            UserAgent idleUserAgent =
                    UserAgentFixture.reconstitute(
                            UserAgentStatus.IDLE, HealthScoreFixture.initial());
            List<UserAgentId> ids = List.of(id);

            given(readManager.findByIds(ids)).willReturn(List.of(idleUserAgent));

            // When
            int result = sut.updateStatusToIdle(ids);

            // Then: 반환값은 1이지만 persist는 호출되지 않음 (IDLE→IDLE 전환 시도 안 함)
            assertThat(result).isEqualTo(1);
            then(transactionManager).should(never()).persistAll(any());
        }
    }
}
