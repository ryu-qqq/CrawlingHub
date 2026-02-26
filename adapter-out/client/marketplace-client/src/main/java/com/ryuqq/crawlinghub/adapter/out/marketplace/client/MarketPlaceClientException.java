package com.ryuqq.crawlinghub.adapter.out.marketplace.client;

/**
 * 외부몰 API 호출 실패 예외
 *
 * @author development-team
 * @since 1.0.0
 */
public class MarketPlaceClientException extends RuntimeException {

    public MarketPlaceClientException(String message) {
        super(message);
    }

    public MarketPlaceClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
