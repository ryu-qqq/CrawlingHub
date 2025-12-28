package com.ryuqq.crawlinghub.application.useragent.manager.query;

import com.ryuqq.crawlinghub.application.useragent.port.out.query.UserAgentQueryPort;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * UserAgent 조회 전용 Manager
 *
 * <p><strong>책임</strong>: UserAgent 조회 작업 위임
 *
 * <p><strong>규칙</strong>: 단일 QueryPort만 의존, 트랜잭션 없음
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentReadManager {

    private final UserAgentQueryPort userAgentQueryPort;

    public UserAgentReadManager(UserAgentQueryPort userAgentQueryPort) {
        this.userAgentQueryPort = userAgentQueryPort;
    }

    /**
     * 활성화된 UserAgent 전체 조회 (Pool 초기화용)
     *
     * @return 활성화된 UserAgent 목록
     */
    public List<UserAgent> findAllAvailable() {
        return userAgentQueryPort.findAllAvailable();
    }

    /**
     * ID로 UserAgent 조회
     *
     * @param userAgentId UserAgent ID
     * @return UserAgent (없으면 empty)
     */
    public Optional<UserAgent> findById(UserAgentId userAgentId) {
        return userAgentQueryPort.findById(userAgentId);
    }

    /**
     * 상태별 UserAgent 개수 조회
     *
     * @param status UserAgent 상태
     * @return 해당 상태의 UserAgent 개수
     */
    public long countByStatus(UserAgentStatus status) {
        return userAgentQueryPort.countByStatus(status);
    }

    /**
     * 전체 UserAgent 개수 조회
     *
     * @return 전체 UserAgent 개수
     */
    public long countAll() {
        return userAgentQueryPort.countAll();
    }

    /**
     * 상태별 UserAgent 목록 조회
     *
     * @param status UserAgent 상태
     * @return 해당 상태의 UserAgent 목록
     */
    public List<UserAgent> findByStatus(UserAgentStatus status) {
        return userAgentQueryPort.findByStatus(status);
    }

    /**
     * 여러 ID로 UserAgent 조회 (배치 처리용)
     *
     * @param userAgentIds UserAgent ID 목록
     * @return UserAgent 목록
     */
    public List<UserAgent> findByIds(List<UserAgentId> userAgentIds) {
        return userAgentQueryPort.findByIds(userAgentIds);
    }
}
