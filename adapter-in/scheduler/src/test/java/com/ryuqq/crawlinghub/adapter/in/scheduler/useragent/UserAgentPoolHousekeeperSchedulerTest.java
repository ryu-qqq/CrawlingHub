package com.ryuqq.crawlinghub.adapter.in.scheduler.useragent;

import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.crawlinghub.application.useragent.dto.command.IssueUserAgentSessionCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.command.IssueUserAgentSessionCommand.SessionIssueType;
import com.ryuqq.crawlinghub.application.useragent.dto.command.RecoverLeakedUserAgentCommand;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.IssueUserAgentSessionUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RecoverCooldownUserAgentUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RecoverLeakedUserAgentUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RecoverSuspendedPoolUserAgentUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.SyncUserAgentCacheUseCase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UserAgentPoolHousekeeperScheduler 단위 테스트
 *
 * <p>HikariCP Housekeeper 패턴으로 동작하는 단일 유지보수 스케줄러를 검증합니다.
 *
 * <ul>
 *   <li>모든 유지보수 작업(recoverCooldown, recoverSuspended, renewSessions, issueSessions, recoverLeaked,
 *       periodicDbSync)이 올바르게 호출되는지 확인합니다.
 *   <li>개별 작업이 예외를 던져도 다음 작업이 계속 실행되는지 확인합니다.
 *   <li>dbSyncInterval 기반의 DB 동기화 주기 로직을 검증합니다.
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAgentPoolHousekeeperScheduler 단위 테스트")
class UserAgentPoolHousekeeperSchedulerTest {

    @Mock private RecoverCooldownUserAgentUseCase recoverCooldownUseCase;
    @Mock private RecoverSuspendedPoolUserAgentUseCase recoverSuspendedUseCase;
    @Mock private IssueUserAgentSessionUseCase issueSessionUseCase;
    @Mock private RecoverLeakedUserAgentUseCase recoverLeakedUseCase;
    @Mock private SyncUserAgentCacheUseCase syncCacheUseCase;

    /**
     * 테스트용 기본 설정
     *
     * <p>dbSyncInterval=3 으로 설정하여 3회 호출 시 DB 동기화가 발생하도록 합니다.
     */
    private static final SchedulerProperties.UserAgentHousekeeper DEFAULT_CONFIG =
            new SchedulerProperties.UserAgentHousekeeper(
                    true,
                    5000, // fixedDelayMs
                    10, // renewalBufferMinutes
                    20, // maxSessionBatchSize
                    500L, // sessionDelayMillis
                    60000L, // leakThresholdMillis
                    3 // dbSyncInterval
                    );

    private UserAgentPoolHousekeeperScheduler sut;

    @BeforeEach
    void setUp() {
        SchedulerProperties properties = buildSchedulerPropertiesWith(DEFAULT_CONFIG);
        sut =
                new UserAgentPoolHousekeeperScheduler(
                        recoverCooldownUseCase,
                        recoverSuspendedUseCase,
                        issueSessionUseCase,
                        recoverLeakedUseCase,
                        syncCacheUseCase,
                        properties);
    }

    private SchedulerProperties buildSchedulerPropertiesWith(
            SchedulerProperties.UserAgentHousekeeper housekeeperConfig) {
        SchedulerProperties.Jobs jobs =
                new SchedulerProperties.Jobs(
                        buildCrawlSchedulerOutbox(),
                        buildCrawlTaskOutbox(),
                        buildCrawlTask(),
                        buildCrawledRawProcessing(),
                        housekeeperConfig,
                        buildSyncOutbox(),
                        buildProductRefresh());
        return new SchedulerProperties(jobs);
    }

    private SchedulerProperties.ProductRefresh buildProductRefresh() {
        SchedulerProperties.RefreshStale refreshStale =
                new SchedulerProperties.RefreshStale(
                        true, "0 0 10,14,18 * * *", "Asia/Seoul", 3000);
        return new SchedulerProperties.ProductRefresh(refreshStale);
    }

    private SchedulerProperties.CrawlSchedulerOutbox buildCrawlSchedulerOutbox() {
        SchedulerProperties.ProcessPending processPending =
                new SchedulerProperties.ProcessPending(true, "0 * * * * *", "Asia/Seoul", 10, 5);
        SchedulerProperties.RecoverTimeout recoverTimeout =
                new SchedulerProperties.RecoverTimeout(true, "0 * * * * *", "Asia/Seoul", 10, 60L);
        return new SchedulerProperties.CrawlSchedulerOutbox(processPending, recoverTimeout);
    }

    private SchedulerProperties.CrawlTaskOutbox buildCrawlTaskOutbox() {
        SchedulerProperties.ProcessPending processPending =
                new SchedulerProperties.ProcessPending(true, "0 * * * * *", "Asia/Seoul", 10, 5);
        SchedulerProperties.RecoverTimeout recoverTimeout =
                new SchedulerProperties.RecoverTimeout(true, "0 * * * * *", "Asia/Seoul", 10, 60L);
        SchedulerProperties.RecoverFailed recoverFailed =
                new SchedulerProperties.RecoverFailed(true, "0 * * * * *", "Asia/Seoul", 10, 30);
        return new SchedulerProperties.CrawlTaskOutbox(
                processPending, recoverTimeout, recoverFailed);
    }

    private SchedulerProperties.CrawlTask buildCrawlTask() {
        SchedulerProperties.RecoverStuck recoverStuck =
                new SchedulerProperties.RecoverStuck(true, "0 * * * * *", "Asia/Seoul", 10, 120L);
        return new SchedulerProperties.CrawlTask(recoverStuck);
    }

    private SchedulerProperties.CrawledRawProcessing buildCrawledRawProcessing() {
        SchedulerProperties.ProcessCrawledRaw miniShop =
                new SchedulerProperties.ProcessCrawledRaw(true, "0 * * * * *", "Asia/Seoul", 50);
        SchedulerProperties.ProcessCrawledRaw detail =
                new SchedulerProperties.ProcessCrawledRaw(true, "0 * * * * *", "Asia/Seoul", 30);
        SchedulerProperties.ProcessCrawledRaw option =
                new SchedulerProperties.ProcessCrawledRaw(true, "0 * * * * *", "Asia/Seoul", 20);
        return new SchedulerProperties.CrawledRawProcessing(miniShop, detail, option);
    }

    private SchedulerProperties.CrawledProductSyncOutbox buildSyncOutbox() {
        SchedulerProperties.CrawledProductSyncOutboxPublishPending publishPending =
                new SchedulerProperties.CrawledProductSyncOutboxPublishPending(
                        true, "0 * * * * *", "Asia/Seoul", 100, 3);
        SchedulerProperties.RecoverTimeout recoverTimeout =
                new SchedulerProperties.RecoverTimeout(true, "0 * * * * *", "Asia/Seoul", 50, 300L);
        SchedulerProperties.RecoverFailed recoverFailed =
                new SchedulerProperties.RecoverFailed(true, "0 * * * * *", "Asia/Seoul", 50, 60);
        return new SchedulerProperties.CrawledProductSyncOutbox(
                publishPending, recoverTimeout, recoverFailed);
    }

    @Nested
    @DisplayName("maintain() 메서드 - 정상 실행 테스트")
    class MaintainSuccessTest {

        @Test
        @DisplayName("[성공] 모든 유지보수 작업이 순서대로 호출된다")
        void shouldCallAllMaintenanceTasksInOrder() {
            // Given: syncCacheUseCase는 3번째 호출 시 실행됨 (dbSyncInterval=3)
            // 첫 번째 maintain() 호출에서는 DB 동기화 미실행 (count=1)

            // When
            sut.maintain();

            // Then: 각 UseCase가 1회씩 호출되어야 함
            verify(recoverCooldownUseCase, times(1)).execute();
            verify(recoverSuspendedUseCase, times(1)).execute();
            // issueSessionUseCase는 RENEW + NEW = 2회 호출
            verify(issueSessionUseCase, times(2)).execute(any());
            verify(recoverLeakedUseCase, times(1)).execute(any());
            // DB 동기화는 interval(3) 미달로 미호출
            verify(syncCacheUseCase, never()).execute();
        }

        @Test
        @DisplayName("[성공] 세션 갱신(RENEW) 커맨드가 올바른 파라미터로 생성된다")
        void shouldIssueRenewCommandWithCorrectParameters() {
            // When
            sut.maintain();

            // Then
            ArgumentCaptor<IssueUserAgentSessionCommand> captor =
                    forClass(IssueUserAgentSessionCommand.class);
            verify(issueSessionUseCase, times(2)).execute(captor.capture());

            // 첫 번째 호출이 RENEW, 두 번째 호출이 NEW
            IssueUserAgentSessionCommand renewCommand = captor.getAllValues().get(0);
            Assertions.assertThat(renewCommand.issueType()).isEqualTo(SessionIssueType.RENEW);
            // 설정값: renewalBufferMinutes=10, maxSessionBatchSize=20, sessionDelayMillis=500
            Assertions.assertThat(renewCommand.renewalBufferMinutes()).isEqualTo(10);
            Assertions.assertThat(renewCommand.maxBatchSize()).isEqualTo(20);
            Assertions.assertThat(renewCommand.sessionDelayMillis()).isEqualTo(500L);
        }

        @Test
        @DisplayName("[성공] 신규 세션 발급(NEW) 커맨드가 올바른 파라미터로 생성된다")
        void shouldIssueNewCommandWithCorrectParameters() {
            // When
            sut.maintain();

            // Then
            ArgumentCaptor<IssueUserAgentSessionCommand> captor =
                    forClass(IssueUserAgentSessionCommand.class);
            verify(issueSessionUseCase, times(2)).execute(captor.capture());

            // 두 번째 호출이 NEW
            IssueUserAgentSessionCommand newCommand = captor.getAllValues().get(1);
            Assertions.assertThat(newCommand.issueType()).isEqualTo(SessionIssueType.NEW);
            // 설정값: maxSessionBatchSize=20, sessionDelayMillis=500
            Assertions.assertThat(newCommand.maxBatchSize()).isEqualTo(20);
            Assertions.assertThat(newCommand.sessionDelayMillis()).isEqualTo(500L);
        }

        @Test
        @DisplayName("[성공] Leak 감지 커맨드가 올바른 파라미터로 생성된다")
        void shouldRecoverLeakedWithCorrectCommand() {
            // When
            sut.maintain();

            // Then
            ArgumentCaptor<RecoverLeakedUserAgentCommand> captor =
                    forClass(RecoverLeakedUserAgentCommand.class);
            verify(recoverLeakedUseCase).execute(captor.capture());

            RecoverLeakedUserAgentCommand captured = captor.getValue();
            // 설정값: leakThresholdMillis=60000
            Assertions.assertThat(captured.leakThresholdMillis()).isEqualTo(60000L);
        }
    }

    @Nested
    @DisplayName("maintain() 메서드 - DB 동기화 주기 테스트")
    class PeriodicDbSyncTest {

        @Test
        @DisplayName("[성공] dbSyncInterval 미달 시 DB 동기화를 실행하지 않는다")
        void shouldNotSyncDbBeforeInterval() {
            // Given: dbSyncInterval=3, 1~2회 호출은 동기화 미실행

            // When
            sut.maintain(); // count=1
            sut.maintain(); // count=2

            // Then
            verify(syncCacheUseCase, never()).execute();
        }

        @Test
        @DisplayName("[성공] dbSyncInterval 도달 시 DB 동기화를 실행한다")
        void shouldSyncDbWhenIntervalReached() {
            // Given: dbSyncInterval=3
            given(syncCacheUseCase.execute()).willReturn(5);

            // When
            sut.maintain(); // count=1
            sut.maintain(); // count=2
            sut.maintain(); // count=3 → DB 동기화 실행

            // Then
            verify(syncCacheUseCase, times(1)).execute();
        }

        @Test
        @DisplayName("[성공] DB 동기화 후 카운터가 초기화되어 다음 주기에 다시 실행된다")
        void shouldResetCounterAfterDbSync() {
            // Given: dbSyncInterval=3
            given(syncCacheUseCase.execute()).willReturn(3);

            // When: 6회 호출 → 3회, 6회째에 각각 DB 동기화
            for (int i = 0; i < 6; i++) {
                sut.maintain();
            }

            // Then: 총 2회 동기화 (3회, 6회)
            verify(syncCacheUseCase, times(2)).execute();
        }
    }

    @Nested
    @DisplayName("maintain() 메서드 - 예외 격리 테스트")
    class ExceptionIsolationTest {

        @Test
        @DisplayName("[성공] recoverCooldown 실패해도 이후 작업이 계속 실행된다")
        void shouldContinueWhenRecoverCooldownFails() {
            // Given
            doThrow(new RuntimeException("COOLDOWN 복구 실패")).when(recoverCooldownUseCase).execute();

            // When: 예외가 전파되지 않아야 함
            sut.maintain();

            // Then: 이후 작업들도 모두 호출
            verify(recoverSuspendedUseCase, times(1)).execute();
            verify(issueSessionUseCase, times(2)).execute(any());
            verify(recoverLeakedUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("[성공] recoverSuspended 실패해도 이후 작업이 계속 실행된다")
        void shouldContinueWhenRecoverSuspendedFails() {
            // Given
            doThrow(new RuntimeException("SUSPENDED 복구 실패"))
                    .when(recoverSuspendedUseCase)
                    .execute();

            // When
            sut.maintain();

            // Then: 이후 작업들도 호출
            verify(issueSessionUseCase, times(2)).execute(any());
            verify(recoverLeakedUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("[성공] issueSession 실패해도 recoverLeaked가 계속 실행된다")
        void shouldContinueWhenIssueSessionFails() {
            // Given: 모든 issueSession 호출이 실패
            doThrow(new RuntimeException("세션 발급 실패")).when(issueSessionUseCase).execute(any());

            // When
            sut.maintain();

            // Then: recoverLeaked도 호출
            verify(recoverLeakedUseCase, times(1)).execute(any());
        }

        @Test
        @DisplayName("[성공] recoverLeaked 실패해도 periodicDbSync가 계속 실행된다")
        void shouldContinueWhenRecoverLeakedFails() {
            // Given: dbSyncInterval=3이므로 3번 호출해야 동기화 실행
            doThrow(new RuntimeException("Leak 감지 실패")).when(recoverLeakedUseCase).execute(any());
            given(syncCacheUseCase.execute()).willReturn(0);

            // When: 3번 호출
            sut.maintain(); // count=1
            sut.maintain(); // count=2
            sut.maintain(); // count=3 → DB 동기화 실행

            // Then: DB 동기화도 실행
            verify(syncCacheUseCase, times(1)).execute();
        }

        @Test
        @DisplayName("[성공] DB 동기화 실패해도 예외가 전파되지 않는다")
        void shouldNotPropagateExceptionWhenDbSyncFails() {
            // Given: DB 동기화 시 예외 발생
            doThrow(new RuntimeException("DB 동기화 실패")).when(syncCacheUseCase).execute();

            // When: 3번 호출로 동기화 트리거
            sut.maintain(); // count=1
            sut.maintain(); // count=2
            sut.maintain(); // count=3 → 예외 발생하지만 전파되지 않아야 함 (예외 삼킴)
        }
    }
}
