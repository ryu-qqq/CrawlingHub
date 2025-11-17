package com.ryuqq.crawlinghub.domain.crawler.exception;

import com.ryuqq.crawlinghub.domain.common.DomainException;

import java.util.Map;

/**
 * InvalidRequestUrlException - RequestUrl 검증 실패 예외
 *
 * <p>RequestUrl이 CrawlerTaskType에 맞지 않는 형식일 때 발생합니다.</p>
 *
 * <p><strong>발생 시나리오:</strong></p>
 * <ul>
 *   <li>✅ MINISHOP URL에 /searchmini-shop-search 패턴 없음</li>
 *   <li>✅ PRODUCT_DETAIL URL이 /item/{숫자}/detail/top 형식 아님</li>
 *   <li>✅ PRODUCT_OPTION URL이 /auction_products/{숫자}/options 형식 아님</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public class InvalidRequestUrlException extends DomainException {

    /**
     * Constructor - 기본 에러 메시지
     *
     * @param message 에러 메시지
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public InvalidRequestUrlException(String message) {
        super(message);
    }

    /**
     * Constructor - 에러 메시지 + 원인 예외
     *
     * @param message 에러 메시지
     * @param cause 원인 예외
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public InvalidRequestUrlException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String code() {
        return CrawlerErrorCode.INVALID_REQUEST_URL.getCode();
    }

    @Override
    public Map<String, Object> args() {
        return Map.of();
    }
}
