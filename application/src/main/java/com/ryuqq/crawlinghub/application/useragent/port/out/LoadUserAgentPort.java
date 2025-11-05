package com.ryuqq.crawlinghub.application.useragent.port.out;

import com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentQueryDto;
import com.ryuqq.crawlinghub.domain.useragent.TokenStatus;
import com.ryuqq.crawlinghub.domain.useragent.UserAgentId;

import java.util.List;
import java.util.Optional;

/**
 * UserAgent 조회 Port
 *
 * <p>Persistence Adapter에 의해 구현됩니다.
 *
 * <p><strong>CQRS 패턴 적용 - Query 작업만 수행 ⭐</strong></p>
 * <ul>
 *   <li>✅ Read 작업 전용 (findById, findAvailableForRotation)</li>
 *   <li>✅ QueryDSL DTO Projection으로 직접 조회 (Domain Model 거치지 않음)</li>
 *   <li>✅ N+1 문제 방지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public interface LoadUserAgentPort {

    /**
     * ID로 UserAgent 조회
     *
     * <p>QueryDSL DTO Projection으로 직접 조회하여 Domain Model을 거치지 않습니다.</p>
     *
     * @param id UserAgent ID (null 불가)
     * @return UserAgent Query DTO (없으면 Optional.empty())
     * @throws IllegalArgumentException id가 null인 경우
     */
    Optional<UserAgentQueryDto> findById(UserAgentId id);

    /**
     * 로테이션용 사용 가능한 UserAgent 조회
     *
     * <p>요청 가능한 상태의 UserAgent 중 하나를 반환합니다.</p>
     * <p>QueryDSL DTO Projection으로 직접 조회합니다.</p>
     *
     * @return 사용 가능한 UserAgent Query DTO (없으면 Optional.empty())
     */
    Optional<UserAgentQueryDto> findAvailableForRotation();

    /**
     * 상태로 UserAgent 목록 조회
     *
     * <p>특정 상태의 UserAgent 목록을 조회합니다.</p>
     * <p>QueryDSL DTO Projection으로 직접 조회합니다.</p>
     *
     * @param status TokenStatus (null 불가)
     * @return UserAgent Query DTO 목록
     * @throws IllegalArgumentException status가 null인 경우
     */
    List<UserAgentQueryDto> findByStatus(TokenStatus status);
}

