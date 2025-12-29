package com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.List;

/**
 * Search UserAgents API Request
 *
 * <p>UserAgent 목록 조회 API 요청 DTO
 *
 * <p><strong>Validation 규칙:</strong>
 *
 * <ul>
 *   <li>statuses: 선택, UserAgent 상태 필터 (AVAILABLE, SUSPENDED, BLOCKED)
 *   <li>createdFrom: 선택, 생성일 시작 (ISO-8601 형식)
 *   <li>createdTo: 선택, 생성일 종료 (ISO-8601 형식)
 *   <li>page: 최소 0 (기본값: 0)
 *   <li>size: 1-100 (기본값: 20)
 * </ul>
 *
 * @param statuses UserAgent 상태 필터 목록 (AVAILABLE, SUSPENDED, BLOCKED)
 * @param createdFrom 생성일 시작 (선택)
 * @param createdTo 생성일 종료 (선택)
 * @param page 페이지 번호 (0부터 시작, 기본값: 0)
 * @param size 페이지 크기 (기본값: 20, 최대: 100)
 * @author development-team
 * @since 1.0.0
 */
public record SearchUserAgentsApiRequest(
        List<String> statuses,
        Instant createdFrom,
        Instant createdTo,
        @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다") Integer page,
        @Min(value = 1, message = "페이지 크기는 최소 1이어야 합니다")
                @Max(value = 100, message = "페이지 크기는 최대 100까지 허용됩니다")
                Integer size) {

    /** 기본값 적용 생성자 */
    public SearchUserAgentsApiRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
    }
}
