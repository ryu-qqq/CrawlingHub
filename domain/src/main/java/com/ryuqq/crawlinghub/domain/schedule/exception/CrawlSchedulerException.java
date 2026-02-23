package com.ryuqq.crawlinghub.domain.schedule.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import com.ryuqq.crawlinghub.domain.common.exception.ErrorCode;
import java.util.Map;

/**
 * CrawlScheduler BC 예외의 베이스 클래스
 *
 * <p>CrawlScheduler 바운디드 컨텍스트의 모든 예외는 이 클래스를 상속해야 합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public abstract class CrawlSchedulerException extends DomainException {

    protected CrawlSchedulerException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected CrawlSchedulerException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    protected CrawlSchedulerException(
            ErrorCode errorCode, String message, Map<String, Object> args) {
        super(errorCode, message, args);
    }

    protected CrawlSchedulerException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
