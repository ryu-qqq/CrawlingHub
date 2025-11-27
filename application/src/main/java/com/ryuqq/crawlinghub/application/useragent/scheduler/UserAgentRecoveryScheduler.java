package com.ryuqq.crawlinghub.application.useragent.scheduler;

import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * UserAgent 자동 복구 Scheduler
 *
 * <p>매 시간 정각에 SUSPENDED UserAgent를 자동으로 복구합니다.
 *
 * <p><strong>복구 조건</strong>:
 *
 * <ul>
 *   <li>SUSPENDED 상태
 *   <li>1시간 경과
 *   <li>Health Score ≥ 30
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentRecoveryScheduler {

    private static final Logger log = LoggerFactory.getLogger(UserAgentRecoveryScheduler.class);

    private final UserAgentPoolManager poolManager;

    public UserAgentRecoveryScheduler(UserAgentPoolManager poolManager) {
        this.poolManager = poolManager;
    }

    /**
     * 매 시간 정각에 SUSPENDED UserAgent 복구
     *
     * <p>cron 표현식: 초 분 시 일 월 요일
     *
     * <p>"0 0 * * * *" = 매 시간 0분 0초
     */
    @Scheduled(cron = "0 0 * * * *")
    public void recoverSuspendedUserAgents() {
        log.info("UserAgent 복구 스케줄러 시작");

        try {
            int recoveredCount = poolManager.recoverSuspendedUserAgents();
            log.info("UserAgent 복구 스케줄러 완료: {}건 복구됨", recoveredCount);
        } catch (Exception e) {
            log.error("UserAgent 복구 스케줄러 실행 중 오류 발생", e);
        }
    }
}
