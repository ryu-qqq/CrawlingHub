package com.ryuqq.crawlinghub.application.useragent.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.useragent.DeviceTypeFixture;
import com.ryuqq.cralwinghub.domain.fixture.useragent.HealthScoreFixture;
import com.ryuqq.cralwinghub.domain.fixture.useragent.TokenFixture;
import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentMetadataFixture;
import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentStringFixture;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentTransactionManager;
import com.ryuqq.crawlinghub.application.useragent.manager.query.UserAgentReadManager;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SessionDbStatusUpdater 단위 테스트
 *
 * <p>Mockist 스타일 테스트: ReadManager, TransactionManager, ClockHolder Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
@DisplayName("SessionDbStatusUpdater 테스트")
class SessionDbStatusUpdaterTest {

    @Mock private UserAgentReadManager readManager;

    @Mock private UserAgentTransactionManager transactionManager;

    @Mock private ClockHolder clockHolder;

    @Captor private ArgumentCaptor<List<UserAgent>> userAgentListCaptor;

    private SessionDbStatusUpdater updater;
    private Clock fixedClock;
    private Instant fixedTime;

    @BeforeEach
    void setUp() {
        updater = new SessionDbStatusUpdater(readManager, transactionManager, clockHolder);
        fixedClock = Clock.fixed(Instant.parse("2024-01-15T10:00:00Z"), ZoneId.of("UTC"));
        fixedTime = fixedClock.instant();
    }

    @Nested
    @DisplayName("updateStatusToReady() 테스트")
    class UpdateStatusToReady {

        @Test
        @DisplayName("[성공] 빈 목록 전달 시 → 0 반환")
        void shouldReturnZeroWhenEmptyList() {
            // When
            int result = updater.updateStatusToReady(Collections.emptyList());

            // Then
            assertThat(result).isZero();
            verify(readManager, never()).findByIds(Collections.emptyList());
            verify(transactionManager, never()).persistAll(Collections.emptyList());
        }

        @Test
        @DisplayName("[성공] SESSION_REQUIRED 상태 UserAgent를 READY로 변경 및 저장")
        void shouldUpdateStatusToReadyAndPersist() {
            // Given
            UserAgentId userAgentId = new UserAgentId(1L);
            List<UserAgentId> userAgentIds = List.of(userAgentId);
            UserAgent userAgent = aSessionRequiredUserAgent(1L);

            given(clockHolder.getClock()).willReturn(fixedClock);
            given(readManager.findByIds(userAgentIds)).willReturn(List.of(userAgent));

            // When
            int result = updater.updateStatusToReady(userAgentIds);

            // Then
            assertThat(result).isEqualTo(1);
            verify(readManager).findByIds(userAgentIds);
            verify(transactionManager).persistAll(userAgentListCaptor.capture());

            List<UserAgent> capturedList = userAgentListCaptor.getValue();
            assertThat(capturedList).hasSize(1);
            assertThat(capturedList.get(0).getStatus()).isEqualTo(UserAgentStatus.READY);
        }

        @Test
        @DisplayName("[성공] 여러 SESSION_REQUIRED UserAgent 상태 일괄 업데이트")
        void shouldUpdateMultipleUserAgentsStatus() {
            // Given
            UserAgentId userAgentId1 = new UserAgentId(1L);
            UserAgentId userAgentId2 = new UserAgentId(2L);
            List<UserAgentId> userAgentIds = List.of(userAgentId1, userAgentId2);

            UserAgent userAgent1 = aSessionRequiredUserAgent(1L);
            UserAgent userAgent2 = aSessionRequiredUserAgent(2L);

            given(clockHolder.getClock()).willReturn(fixedClock);
            given(readManager.findByIds(userAgentIds)).willReturn(List.of(userAgent1, userAgent2));

            // When
            int result = updater.updateStatusToReady(userAgentIds);

            // Then
            assertThat(result).isEqualTo(2);
            verify(transactionManager).persistAll(userAgentListCaptor.capture());

            List<UserAgent> capturedList = userAgentListCaptor.getValue();
            assertThat(capturedList).hasSize(2);
            assertThat(capturedList).allMatch(ua -> ua.getStatus() == UserAgentStatus.READY);
        }

        @Test
        @DisplayName("[실패] DB에서 UserAgent를 찾을 수 없는 경우 → 0 반환")
        void shouldReturnZeroWhenUserAgentsNotFoundInDb() {
            // Given
            UserAgentId userAgentId = new UserAgentId(999L);
            List<UserAgentId> userAgentIds = List.of(userAgentId);

            given(readManager.findByIds(userAgentIds)).willReturn(Collections.emptyList());

            // When
            int result = updater.updateStatusToReady(userAgentIds);

            // Then
            assertThat(result).isZero();
            verify(readManager).findByIds(userAgentIds);
            verify(transactionManager, never()).persistAll(userAgentListCaptor.capture());
        }
    }

    /**
     * SESSION_REQUIRED 상태의 UserAgent 생성
     *
     * <p>세션 발급이 필요한 상태로, 이후 READY로 전환 가능
     *
     * @param id UserAgent ID
     * @return SESSION_REQUIRED 상태의 UserAgent
     */
    private UserAgent aSessionRequiredUserAgent(Long id) {
        return UserAgent.reconstitute(
                UserAgentIdFixture.anAssignedId(id),
                TokenFixture.aDefaultToken(),
                UserAgentStringFixture.aDefaultUserAgentString(),
                DeviceTypeFixture.aDefaultDeviceType(),
                UserAgentMetadataFixture.aDefaultMetadata(),
                UserAgentStatus.SESSION_REQUIRED,
                HealthScoreFixture.initial(),
                fixedTime,
                0,
                fixedTime,
                fixedTime);
    }
}
