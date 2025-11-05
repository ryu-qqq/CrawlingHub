package com.ryuqq.crawlinghub.domain.seller.exception;

/**
 * 중복된 셀러 코드 예외
 *
 * <p>이미 존재하는 셀러 코드로 신규 셀러를 등록하려 할 때 발생합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public class DuplicateSellerCodeException extends RuntimeException {

    private final String sellerCode;

    /**
     * 중복 셀러 코드 예외 생성
     *
     * @param sellerCode 중복된 셀러 코드
     */
    public DuplicateSellerCodeException(String sellerCode) {
        super(String.format("이미 존재하는 셀러 코드입니다: %s", sellerCode));
        this.sellerCode = sellerCode;
    }

    /**
     * 중복된 셀러 코드 반환
     *
     * @return 셀러 코드
     */
    public String getSellerCode() {
        return sellerCode;
    }
}
