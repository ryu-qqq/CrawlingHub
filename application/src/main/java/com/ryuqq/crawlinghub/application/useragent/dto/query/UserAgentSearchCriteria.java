package com.ryuqq.crawlinghub.application.useragent.dto.query;

import com.ryuqq.crawlinghub.domain.common.vo.PageRequest;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;

/**
 * UserAgent 검색 조건 DTO
 *
 * <p>UserAgent 목록 조회 시 사용되는 검색 조건입니다.
 *
 * <p><strong>검색 조건:</strong>
 *
 * <ul>
 *   <li>status: UserAgent 상태 필터 (null이면 전체 조회)
 *   <li>pageRequest: 페이징 정보
 * </ul>
 *
 * @param status UserAgent 상태 필터 (null이면 전체 조회)
 * @param pageRequest 페이징 정보
 * @author development-team
 * @since 1.0.0
 */
public record UserAgentSearchCriteria(UserAgentStatus status, PageRequest pageRequest) {

    /**
     * 전체 조회 (상태 필터 없음)
     *
     * @param pageRequest 페이징 정보
     * @return 검색 조건
     */
    public static UserAgentSearchCriteria all(PageRequest pageRequest) {
        return new UserAgentSearchCriteria(null, pageRequest);
    }

    /**
     * 상태별 조회
     *
     * @param status UserAgent 상태
     * @param pageRequest 페이징 정보
     * @return 검색 조건
     */
    public static UserAgentSearchCriteria byStatus(
            UserAgentStatus status, PageRequest pageRequest) {
        return new UserAgentSearchCriteria(status, pageRequest);
    }

    /**
     * 상태 필터가 있는지 확인
     *
     * @return 상태 필터가 있으면 true
     */
    public boolean hasStatusFilter() {
        return status != null;
    }
}
