package com.ryuqq.crawlinghub.domain.task.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import java.util.Map;

/**
 * CrawlTask 재시도 실패 시 발생하는 예외
 *
 * <p><strong>발생 조건:</strong>
 *
 * <ul>
 *   <li>재시도 가능한 상태(FAILED, TIMEOUT)가 아닐 때
 *   <li>최대 재시도 횟수(3회)를 초과했을 때
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public final class CrawlTaskRetryException extends DomainException {

    private static final CrawlTaskErrorCode ERROR_CODE = CrawlTaskErrorCode.RETRY_LIMIT_EXCEEDED;

    /**
     * 재시도 실패 예외 생성
     *
     * @param crawlTaskId CrawlTask ID
     * @param currentStatus 현재 상태
     * @param retryCount 현재 재시도 횟수
     */
    public CrawlTaskRetryException(
            Long crawlTaskId, CrawlTaskStatus currentStatus, int retryCount) {
        super(
                ERROR_CODE,
                String.format(
                        "재시도할 수 없습니다. Task ID: %d, 현재 상태: %s, 재시도 횟수: %d",
                        crawlTaskId, currentStatus, retryCount),
                Map.of(
                        "crawlTaskId", crawlTaskId,
                        "currentStatus", currentStatus.name(),
                        "retryCount", retryCount));
    }
}
