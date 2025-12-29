package com.ryuqq.crawlinghub.application.useragent.dto.query;

import com.ryuqq.crawlinghub.domain.common.vo.PageRequest;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Instant;
import java.util.List;

/**
 * UserAgent 검색 조건 DTO
 *
 * <p>UserAgent 목록 조회 시 사용되는 검색 조건입니다.
 *
 * <p><strong>검색 조건:</strong>
 *
 * <ul>
 *   <li>statuses: UserAgent 상태 필터 목록 (null이나 빈 리스트면 전체 조회)
 *   <li>createdFrom: 생성일 시작 (null이면 필터 없음)
 *   <li>createdTo: 생성일 종료 (null이면 필터 없음)
 *   <li>pageRequest: 페이징 정보
 * </ul>
 *
 * @param statuses UserAgent 상태 필터 목록 (null이나 빈 리스트면 전체 조회)
 * @param createdFrom 생성일 시작 (null이면 필터 없음)
 * @param createdTo 생성일 종료 (null이면 필터 없음)
 * @param pageRequest 페이징 정보
 * @author development-team
 * @since 1.0.0
 */
public record UserAgentSearchCriteria(
        List<UserAgentStatus> statuses,
        Instant createdFrom,
        Instant createdTo,
        PageRequest pageRequest) {

    public UserAgentSearchCriteria {
        statuses = statuses != null ? List.copyOf(statuses) : null;
    }

    /**
     * 전체 조회 (상태 필터 없음)
     *
     * @param pageRequest 페이징 정보
     * @return 검색 조건
     */
    public static UserAgentSearchCriteria all(PageRequest pageRequest) {
        return new UserAgentSearchCriteria(null, null, null, pageRequest);
    }

    /**
     * 상태별 조회 (단일 상태, 하위 호환성 유지)
     *
     * @param status UserAgent 상태
     * @param pageRequest 페이징 정보
     * @return 검색 조건
     */
    public static UserAgentSearchCriteria byStatus(
            UserAgentStatus status, PageRequest pageRequest) {
        return new UserAgentSearchCriteria(
                status != null ? List.of(status) : null, null, null, pageRequest);
    }

    /**
     * 상태 목록으로 조회 (다중 상태)
     *
     * @param statuses UserAgent 상태 목록
     * @param pageRequest 페이징 정보
     * @return 검색 조건
     */
    public static UserAgentSearchCriteria byStatuses(
            List<UserAgentStatus> statuses, PageRequest pageRequest) {
        return new UserAgentSearchCriteria(statuses, null, null, pageRequest);
    }

    /**
     * 전체 조건으로 조회
     *
     * @param statuses UserAgent 상태 목록 (null이나 빈 리스트면 전체)
     * @param createdFrom 생성일 시작
     * @param createdTo 생성일 종료
     * @param pageRequest 페이징 정보
     * @return 검색 조건
     */
    public static UserAgentSearchCriteria of(
            List<UserAgentStatus> statuses,
            Instant createdFrom,
            Instant createdTo,
            PageRequest pageRequest) {
        return new UserAgentSearchCriteria(statuses, createdFrom, createdTo, pageRequest);
    }

    /**
     * 상태 필터가 있는지 확인
     *
     * @return 상태 필터가 있으면 true
     */
    public boolean hasStatusFilter() {
        return statuses != null && !statuses.isEmpty();
    }

    /**
     * 기간 필터가 있는지 확인
     *
     * @return 기간 필터가 있으면 true
     */
    public boolean hasDateFilter() {
        return createdFrom != null || createdTo != null;
    }

    /**
     * 단일 상태 반환 (하위 호환성)
     *
     * @return 첫 번째 상태 또는 null
     */
    public UserAgentStatus status() {
        return hasStatusFilter() ? statuses.get(0) : null;
    }
}
