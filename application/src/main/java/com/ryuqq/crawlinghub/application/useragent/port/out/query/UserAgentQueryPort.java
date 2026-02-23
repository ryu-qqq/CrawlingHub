package com.ryuqq.crawlinghub.application.useragent.port.out.query;

import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.util.List;
import java.util.Optional;

/**
 * UserAgent Query Port (DB 조회)
 *
 * <p>UserAgent 조회를 위한 Port입니다. Pool 초기화, 상태 조회 등에 사용됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface UserAgentQueryPort {

    /**
     * 활성화된 UserAgent 전체 조회 (Pool 초기화용)
     *
     * <p>AVAILABLE 상태인 모든 UserAgent를 조회합니다.
     *
     * @return 활성화된 UserAgent 목록
     */
    List<UserAgent> findAllAvailable();

    /**
     * ID로 UserAgent 조회
     *
     * @param userAgentId UserAgent ID
     * @return UserAgent (없으면 empty)
     */
    Optional<UserAgent> findById(UserAgentId userAgentId);

    /**
     * 상태별 UserAgent 개수 조회
     *
     * @param status UserAgent 상태
     * @return 해당 상태의 UserAgent 개수
     */
    long countByStatus(UserAgentStatus status);

    /**
     * 전체 UserAgent 개수 조회
     *
     * @return 전체 UserAgent 개수
     */
    long countAll();

    /**
     * 상태별 UserAgent 목록 조회
     *
     * @param status UserAgent 상태
     * @return 해당 상태의 UserAgent 목록
     */
    List<UserAgent> findByStatus(UserAgentStatus status);

    /**
     * 여러 ID로 UserAgent 조회 (배치 처리용)
     *
     * @param userAgentIds UserAgent ID 목록
     * @return UserAgent 목록
     */
    List<UserAgent> findByIds(List<UserAgentId> userAgentIds);
}
