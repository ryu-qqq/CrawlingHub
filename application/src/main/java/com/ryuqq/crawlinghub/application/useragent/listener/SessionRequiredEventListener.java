package com.ryuqq.crawlinghub.application.useragent.listener;

import com.ryuqq.crawlinghub.application.useragent.dto.session.SessionToken;
import com.ryuqq.crawlinghub.application.useragent.port.out.cache.UserAgentPoolCachePort;
import com.ryuqq.crawlinghub.application.useragent.port.out.session.SessionTokenPort;
import com.ryuqq.crawlinghub.domain.useragent.event.SessionRequiredEvent;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 세션 발급 필요 이벤트 리스너
 *
 * <p><strong>용도</strong>: UserAgent에 세션 토큰 발급이 필요할 때 외부 사이트에 접속하여 세션을 발급받고 캐시에 저장합니다.
 *
 * <p><strong>트랜잭션 단계</strong>: AFTER_COMMIT - 데이터 저장 확정 후 외부 시스템 호출
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>이벤트 수신 (트랜잭션 커밋 후, 비동기)
 *   <li>외부 사이트에 User-Agent 헤더로 접속
 *   <li>Cookie에서 세션 토큰 추출
 *   <li>성공 시: Redis 캐시에 세션 정보 저장 → READY 상태
 *   <li>실패 시: 로그 기록 (재시도는 별도 스케줄러에서 처리)
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SessionRequiredEventListener {

    private static final Logger log = LoggerFactory.getLogger(SessionRequiredEventListener.class);

    private final SessionTokenPort sessionTokenPort;
    private final UserAgentPoolCachePort cachePort;

    public SessionRequiredEventListener(
            SessionTokenPort sessionTokenPort, UserAgentPoolCachePort cachePort) {
        this.sessionTokenPort = sessionTokenPort;
        this.cachePort = cachePort;
    }

    /**
     * 세션 발급 필요 이벤트 처리
     *
     * <p>트랜잭션 커밋 후 비동기로 세션 토큰을 발급받아 캐시에 저장합니다.
     *
     * @param event 세션 발급 필요 이벤트
     */
    @Async
    @EventListener
    public void handleSessionRequired(SessionRequiredEvent event) {
        UserAgentId userAgentId = event.userAgentId();
        String userAgentValue = event.userAgentValue();

        log.info("세션 발급 이벤트 처리 시작: userAgentId={}", userAgentId.value());

        try {
            Optional<SessionToken> sessionTokenOpt =
                    sessionTokenPort.issueSessionToken(userAgentValue);

            if (sessionTokenOpt.isPresent()) {
                SessionToken sessionToken = sessionTokenOpt.get();
                cachePort.updateSession(
                        userAgentId, sessionToken.token(), sessionToken.expiresAt());

                log.info(
                        "세션 발급 완료: userAgentId={}, expiresAt={}",
                        userAgentId.value(),
                        sessionToken.expiresAt());
            } else {
                log.warn("세션 발급 실패 (응답 없음): userAgentId={}", userAgentId.value());
            }
        } catch (Exception e) {
            log.error(
                    "세션 발급 중 오류 발생: userAgentId={}, error={}",
                    userAgentId.value(),
                    e.getMessage(),
                    e);
        }
    }
}
