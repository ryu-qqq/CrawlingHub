package com.ryuqq.crawlinghub.application.useragent.manager;

import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 세션 발급 후 DB 상태 동기화 매니저
 *
 * <p>세션 발급 스케줄러에서 Redis 상태 업데이트 후, DB 상태도 동기화하기 위해 사용합니다.
 *
 * <p><strong>분리 이유</strong>:
 *
 * <ul>
 *   <li>스케줄러에서 외부 API 호출 후 같은 트랜잭션에서 DB 저장 금지
 *   <li>Spring 프록시 제약: 같은 클래스 내 @Transactional 호출 시 작동 안함
 *   <li>배치 처리로 효율성 확보
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SessionDbStatusManager {

    private static final Logger log = LoggerFactory.getLogger(SessionDbStatusManager.class);

    private final UserAgentReadManager readManager;
    private final UserAgentCommandManager transactionManager;

    public SessionDbStatusManager(
            UserAgentReadManager readManager, UserAgentCommandManager transactionManager) {
        this.readManager = readManager;
        this.transactionManager = transactionManager;
    }

    /**
     * 세션 발급 성공한 UserAgent들의 DB 상태를 IDLE로 업데이트
     *
     * <p>배치 처리로 한 번의 트랜잭션에서 모든 UserAgent를 업데이트합니다.
     *
     * @param userAgentIds 세션 발급 성공한 UserAgent ID 목록
     * @return 업데이트된 UserAgent 수
     */
    @Transactional
    public int updateStatusToIdle(List<UserAgentId> userAgentIds) {
        if (userAgentIds == null || userAgentIds.isEmpty()) {
            return 0;
        }

        List<UserAgent> userAgents = readManager.findByIds(userAgentIds);
        if (userAgents.isEmpty()) {
            log.warn("DB에서 UserAgent를 찾을 수 없음: ids={}", userAgentIds);
            return 0;
        }

        if (userAgents.size() < userAgentIds.size()) {
            Set<UserAgentId> foundIds =
                    userAgents.stream().map(UserAgent::getId).collect(Collectors.toSet());
            List<UserAgentId> notFoundIds =
                    userAgentIds.stream()
                            .filter(id -> !foundIds.contains(id))
                            .collect(Collectors.toList());
            log.warn("DB에서 일부 UserAgent를 찾을 수 없음. 누락된 ID: {}", notFoundIds);
        }

        Instant now = Instant.now();
        List<UserAgent> toPersist = new ArrayList<>();

        for (UserAgent userAgent : userAgents) {
            if (userAgent.isStatusDifferentFrom(UserAgentStatus.IDLE)) {
                userAgent.changeStatus(UserAgentStatus.IDLE, now);
                toPersist.add(userAgent);
            }
        }

        if (!toPersist.isEmpty()) {
            transactionManager.persistAll(toPersist);
            log.info("DB 상태 업데이트 완료: {}건 → IDLE", toPersist.size());
        }
        return userAgents.size();
    }
}
