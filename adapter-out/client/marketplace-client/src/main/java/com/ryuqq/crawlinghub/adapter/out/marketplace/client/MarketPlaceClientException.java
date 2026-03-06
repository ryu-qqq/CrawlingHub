package com.ryuqq.crawlinghub.adapter.out.marketplace.client;

/**
 * 외부몰 API 호출 실패 예외
 *
 * @author development-team
 * @since 1.0.0
 */
public class MarketPlaceClientException extends RuntimeException {

    private final int statusCode;

    public MarketPlaceClientException(String message) {
        super(message);
        this.statusCode = 0;
    }

    public MarketPlaceClientException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
    }

    public MarketPlaceClientException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isNotYetConverted() {
        return statusCode == 422;
    }
}
