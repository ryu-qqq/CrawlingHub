package com.ryuqq.crawlinghub.adapter.in.scheduler.useragent;

import com.ryuqq.crawlinghub.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.crawlinghub.adapter.in.scheduler.config.SchedulerProperties;
import com.ryuqq.crawlinghub.application.useragent.dto.command.IssueUserAgentSessionCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.command.RecoverLeakedUserAgentCommand;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.IssueUserAgentSessionUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RecoverCooldownUserAgentUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RecoverLeakedUserAgentUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RecoverSuspendedPoolUserAgentUseCase;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.SyncUserAgentCacheUseCase;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * UserAgent Pool Housekeeper 스케줄러 (HikariCP Housekeeper 패턴)
 *
 * <p>단일 스레드에서 모든 풀 유지보수 작업을 순차 실행합니다.
 *
 * <p><strong>유지보수 작업</strong>:
 *
 * <ul>
 *   <li>COOLDOWN 만료 → IDLE/SESSION_REQUIRED 복구
 *   <li>SUSPENDED → SESSION_REQUIRED 복구
 *   <li>세션 만료 임박 → 선제적 갱신 (RENEW)
 *   <li>신규 등록 UA → 세션 발급 (NEW)
 *   <li>Leak Detection (BORROWED 초과)
 *   <li>Redis → DB 상태 스냅샷 (주기적)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        prefix = "scheduler.jobs.user-agent-housekeeper",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false)
public class UserAgentPoolHousekeeperScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(UserAgentPoolHousekeeperScheduler.class);

    private final RecoverCooldownUserAgentUseCase recoverCooldownUseCase;
    private final RecoverSuspendedPoolUserAgentUseCase recoverSuspendedUseCase;
    private final IssueUserAgentSessionUseCase issueSessionUseCase;
    private final RecoverLeakedUserAgentUseCase recoverLeakedUseCase;
    private final SyncUserAgentCacheUseCase syncCacheUseCase;
    private final SchedulerProperties.UserAgentHousekeeper config;

    private final AtomicInteger dbSyncCounter = new AtomicInteger(0);

    public UserAgentPoolHousekeeperScheduler(
            RecoverCooldownUserAgentUseCase recoverCooldownUseCase,
            RecoverSuspendedPoolUserAgentUseCase recoverSuspendedUseCase,
            IssueUserAgentSessionUseCase issueSessionUseCase,
            RecoverLeakedUserAgentUseCase recoverLeakedUseCase,
            SyncUserAgentCacheUseCase syncCacheUseCase,
            SchedulerProperties properties) {
        this.recoverCooldownUseCase = recoverCooldownUseCase;
        this.recoverSuspendedUseCase = recoverSuspendedUseCase;
        this.issueSessionUseCase = issueSessionUseCase;
        this.recoverLeakedUseCase = recoverLeakedUseCase;
        this.syncCacheUseCase = syncCacheUseCase;
        this.config = properties.jobs().userAgentHousekeeper();

        log.info("UserAgentPoolHousekeeperScheduler 초기화 완료");
    }

    @Scheduled(fixedDelayString = "${scheduler.jobs.user-agent-housekeeper.fixed-delay-ms}")
    @SchedulerJob("UserAgentPool-Housekeeper")
    public void maintain() {
        log.debug("Housekeeper 유지보수 시작");

        recoverCooldown();
        recoverSuspended();
        renewExpiringSessions();
        issueNewSessions();
        recoverLeaked();
        periodicDbSync();
    }

    private void recoverCooldown() {
        try {
            recoverCooldownUseCase.execute();
        } catch (Exception e) {
            log.error("COOLDOWN 복구 실패", e);
        }
    }

    private void recoverSuspended() {
        try {
            recoverSuspendedUseCase.execute();
        } catch (Exception e) {
            log.error("SUSPENDED 복구 실패", e);
        }
    }

    private void renewExpiringSessions() {
        try {
            IssueUserAgentSessionCommand command =
                    IssueUserAgentSessionCommand.ofRenew(
                            config.renewalBufferMinutes(),
                            config.maxSessionBatchSize(),
                            config.sessionDelayMillis());
            issueSessionUseCase.execute(command);
        } catch (Exception e) {
            log.error("세션 갱신 실패", e);
        }
    }

    private void issueNewSessions() {
        try {
            IssueUserAgentSessionCommand command =
                    IssueUserAgentSessionCommand.ofNew(
                            config.maxSessionBatchSize(), config.sessionDelayMillis());
            issueSessionUseCase.execute(command);
        } catch (Exception e) {
            log.error("세션 발급 실패", e);
        }
    }

    private void recoverLeaked() {
        try {
            RecoverLeakedUserAgentCommand command =
                    RecoverLeakedUserAgentCommand.of(config.leakThresholdMillis());
            recoverLeakedUseCase.execute(command);
        } catch (Exception e) {
            log.error("Leak Detection 실패", e);
        }
    }

    private void periodicDbSync() {
        int count = dbSyncCounter.incrementAndGet();
        if (count >= config.dbSyncInterval()) {
            dbSyncCounter.set(0);
            try {
                int synced = syncCacheUseCase.execute();
                log.info("Redis -> DB 상태 동기화: {}건", synced);
            } catch (Exception e) {
                log.error("DB 동기화 실패", e);
            }
        }
    }
}
