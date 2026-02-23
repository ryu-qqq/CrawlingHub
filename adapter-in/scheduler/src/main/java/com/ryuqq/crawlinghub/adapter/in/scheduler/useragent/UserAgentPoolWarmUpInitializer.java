package com.ryuqq.crawlinghub.adapter.in.scheduler.useragent;

import com.ryuqq.crawlinghub.application.useragent.port.in.command.WarmUpUserAgentPoolUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * UserAgent Pool WarmUp Initializer
 *
 * <p>Scheduler 서버 시작 시 DB → Redis Pool 초기 로딩을 수행합니다.
 *
 * <p><strong>분산 환경 안전성</strong>:
 *
 * <ul>
 *   <li>Redis SETNX 기반 분산 락으로 ECS 다중 인스턴스 중 최초 1개만 실행
 *   <li>WarmUp 완료 플래그로 이후 인스턴스의 중복 실행 방지
 *   <li>Redis 데이터가 유지되는 한 재배포 시에도 중복 WarmUp 없음
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
public class UserAgentPoolWarmUpInitializer {

    private static final Logger log = LoggerFactory.getLogger(UserAgentPoolWarmUpInitializer.class);

    private final WarmUpUserAgentPoolUseCase warmUpUseCase;

    public UserAgentPoolWarmUpInitializer(WarmUpUserAgentPoolUseCase warmUpUseCase) {
        this.warmUpUseCase = warmUpUseCase;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("[WarmUp] Scheduler 서버 시작 → UserAgent Pool WarmUp 시도");
        try {
            int warmedUp = warmUpUseCase.execute();
            if (warmedUp > 0) {
                log.info("[WarmUp] UserAgent Pool 초기화 완료: {}건", warmedUp);
            }
        } catch (Exception e) {
            log.error("[WarmUp] UserAgent Pool 초기화 실패 (Housekeeper가 보완 처리)", e);
        }
    }
}
