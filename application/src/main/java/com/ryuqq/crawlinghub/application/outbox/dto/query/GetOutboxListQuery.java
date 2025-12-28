package com.ryuqq.crawlinghub.application.outbox.dto.query;

import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.util.List;

/**
 * Outbox 목록 조회 Query DTO
 *
 * <p><strong>페이징 파라미터</strong>:
 *
 * <ul>
 *   <li>page: 페이지 번호 (0부터 시작)
 *   <li>size: 페이지 크기 (기본값: 20, 최대: 100)
 * </ul>
 *
 * @param statuses 조회할 상태 목록 (null이면 PENDING, FAILED 조회)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record GetOutboxListQuery(List<OutboxStatus> statuses, int page, int size) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public GetOutboxListQuery {
        if (page < 0) {
            page = DEFAULT_PAGE;
        }
        if (size <= 0) {
            size = DEFAULT_SIZE;
        }
        if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
        if (statuses == null || statuses.isEmpty()) {
            statuses = List.of(OutboxStatus.PENDING, OutboxStatus.FAILED);
        } else {
            statuses = List.copyOf(statuses);
        }
    }

    /**
     * Query 생성 (정적 팩토리 메서드)
     *
     * @param statuses 상태 목록
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return GetOutboxListQuery
     */
    public static GetOutboxListQuery of(List<OutboxStatus> statuses, int page, int size) {
        return new GetOutboxListQuery(statuses, page, size);
    }

    /**
     * PENDING 또는 FAILED 상태 조회 Query 생성
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return GetOutboxListQuery
     */
    public static GetOutboxListQuery pendingOrFailed(int page, int size) {
        return new GetOutboxListQuery(
                List.of(OutboxStatus.PENDING, OutboxStatus.FAILED), page, size);
    }

    /**
     * 오프셋 계산
     *
     * @return offset 값
     */
    public int offset() {
        return page * size;
    }
}
