package com.ryuqq.crawlinghub.application.useragent.scheduler;

import com.ryuqq.crawlinghub.application.useragent.config.SessionSchedulerProperties;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.session.SessionToken;
import com.ryuqq.crawlinghub.application.useragent.port.out.cache.UserAgentPoolCachePort;
import com.ryuqq.crawlinghub.application.useragent.port.out.session.SessionTokenPort;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 세션 토큰 발급 Scheduler
 *
 * <p>세션이 필요하거나 곧 만료되는 UserAgent들에게 세션 토큰을 발급합니다.
 *
 * <p><strong>처리 대상</strong>:
 *
 * <ul>
 *   <li>SESSION_REQUIRED 상태인 UserAgent (매 분)
 *   <li>세션 만료 임박 UserAgent (선제적 갱신, 5분 전)
 * </ul>
 *
 * <p><strong>동작 방식</strong>:
 *
 * <ul>
 *   <li>1분 주기로 SESSION_REQUIRED 목록 조회 및 발급
 *   <li>세션 만료 5분 전에 선제적으로 갱신
 *   <li>성공 시 Redis READY 상태 전환 + DB 상태 동기화
 *   <li>실패 시 다음 사이클에서 재시도
 * </ul>
 *
 * <p><strong>상태 동기화</strong>:
 *
 * <ul>
 *   <li>Redis: 즉시 업데이트 (세션 토큰 발급 직후)
 *   <li>DB: 배치 처리로 동기화 (별도 @Transactional 컴포넌트 사용)
 * </ul>
 *
 * <p><strong>세션 만료 시간</strong>:
 *
 * <ul>
 *   <li>mustit.co.kr 세션: 30분 만료
 *   <li>선제적 갱신: 만료 5분 전 (25분 사용 후)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@EnableConfigurationProperties(SessionSchedulerProperties.class)
@ConditionalOnProperty(
        name = "scheduler.session-issuance.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class SessionIssuanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(SessionIssuanceScheduler.class);

    private static final int MAX_BATCH_SIZE = 10;

    private final SessionTokenPort sessionTokenPort;
    private final UserAgentPoolCachePort cachePort;
    private final SessionDbStatusUpdater dbStatusUpdater;
    private final int renewalBufferMinutes;

    public SessionIssuanceScheduler(
            SessionTokenPort sessionTokenPort,
            UserAgentPoolCachePort cachePort,
            SessionDbStatusUpdater dbStatusUpdater,
            SessionSchedulerProperties properties) {
        this.sessionTokenPort = sessionTokenPort;
        this.cachePort = cachePort;
        this.dbStatusUpdater = dbStatusUpdater;
        this.renewalBufferMinutes = properties.getRenewalBufferMinutes();

        log.info("SessionIssuanceScheduler 초기화: renewalBufferMinutes={}", renewalBufferMinutes);
    }

    /**
     * 매 분마다 SESSION_REQUIRED UserAgent에 세션 발급
     *
     * <p>cron 표현식: 초 분 시 일 월 요일
     *
     * <p>"0 * * * * *" = 매 분 0초
     */
    @Scheduled(cron = "0 * * * * *")
    public void issueSessionTokens() {
        log.debug("세션 발급 스케줄러 시작");

        try {
            List<UserAgentId> sessionRequiredIds = cachePort.getSessionRequiredUserAgents();

            if (sessionRequiredIds.isEmpty()) {
                log.debug("세션 발급이 필요한 UserAgent 없음");
                return;
            }

            log.info("세션 발급 대상: {}건", sessionRequiredIds.size());

            processSessionIssuance(sessionRequiredIds, "신규 발급");

        } catch (Exception e) {
            log.error("세션 발급 스케줄러 실행 중 오류 발생", e);
        }
    }

    /**
     * 세션 만료 임박 UserAgent에 대해 선제적으로 세션 갱신
     *
     * <p>30초마다 실행하여 만료 N분 전인 세션을 미리 갱신합니다.
     *
     * <p>mustit.co.kr 세션은 30분 만료이므로, 설정값에 따라 갱신됩니다.
     *
     * <p>cron 표현식: "30 * * * * *" = 매 분 30초
     */
    @Scheduled(cron = "30 * * * * *")
    public void renewExpiringSessionTokens() {
        log.debug("세션 선제적 갱신 스케줄러 시작");

        try {
            List<UserAgentId> expiringIds =
                    cachePort.getSessionExpiringUserAgents(renewalBufferMinutes);

            if (expiringIds.isEmpty()) {
                log.debug("세션 갱신이 필요한 UserAgent 없음");
                return;
            }

            log.info("세션 선제적 갱신 대상: {}건 (만료 {}분 이내)", expiringIds.size(), renewalBufferMinutes);

            processSessionIssuance(expiringIds, "선제적 갱신");

        } catch (Exception e) {
            log.error("세션 선제적 갱신 스케줄러 실행 중 오류 발생", e);
        }
    }

    /**
     * 공통 세션 발급 처리 로직
     *
     * <p>Redis 상태는 즉시 업데이트하고, DB 상태는 배치 처리로 동기화합니다.
     *
     * @param userAgentIds 처리할 UserAgent ID 목록
     * @param operationType 작업 유형 (로깅용)
     */
    private void processSessionIssuance(List<UserAgentId> userAgentIds, String operationType) {
        List<UserAgentId> successIds = new ArrayList<>();
        int failCount = 0;
        int processedCount = 0;

        for (UserAgentId userAgentId : userAgentIds) {
            if (processedCount >= MAX_BATCH_SIZE) {
                log.info(
                        "[{}] 배치 크기 제한 도달: {}건 처리됨, 나머지는 다음 사이클에서 처리",
                        operationType,
                        processedCount);
                break;
            }

            boolean success = issueSessionForUserAgent(userAgentId);
            if (success) {
                successIds.add(userAgentId);
            } else {
                failCount++;
            }
            processedCount++;

            sleepBetweenRequests();
        }

        if (!successIds.isEmpty()) {
            try {
                int dbUpdated = dbStatusUpdater.updateStatusToReady(successIds);
                log.info("[{}] DB 상태 동기화 완료: {}건", operationType, dbUpdated);
            } catch (Exception e) {
                log.error("[{}] DB 상태 동기화 실패: {}", operationType, e.getMessage());
            }
        }

        log.info(
                "[{}] 완료: 성공={}, 실패={}, 총 처리={}",
                operationType,
                successIds.size(),
                failCount,
                processedCount);
    }

    private boolean issueSessionForUserAgent(UserAgentId userAgentId) {
        try {
            Optional<CachedUserAgent> cachedOpt = cachePort.findById(userAgentId);
            if (cachedOpt.isEmpty()) {
                log.warn("캐시에서 UserAgent를 찾을 수 없음: userAgentId={}", userAgentId.value());
                return false;
            }

            CachedUserAgent cached = cachedOpt.get();
            String userAgentValue = cached.userAgentValue();

            if (userAgentValue == null || userAgentValue.isBlank()) {
                log.warn("UserAgent 값이 없음: userAgentId={}", userAgentId.value());
                return false;
            }

            Optional<SessionToken> sessionTokenOpt =
                    sessionTokenPort.issueSessionToken(userAgentValue);

            if (sessionTokenOpt.isPresent()) {
                SessionToken sessionToken = sessionTokenOpt.get();
                cachePort.updateSession(
                        userAgentId, sessionToken.token(), sessionToken.expiresAt());

                log.info(
                        "세션 발급 성공: userAgentId={}, expiresAt={}",
                        userAgentId.value(),
                        sessionToken.expiresAt());
                return true;
            } else {
                log.warn("세션 발급 실패 (응답 없음): userAgentId={}", userAgentId.value());
                return false;
            }

        } catch (Exception e) {
            log.error("세션 발급 중 오류: userAgentId={}, error={}", userAgentId.value(), e.getMessage());
            return false;
        }
    }

    private void sleepBetweenRequests() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("세션 발급 대기 중 인터럽트 발생");
        }
    }
}
