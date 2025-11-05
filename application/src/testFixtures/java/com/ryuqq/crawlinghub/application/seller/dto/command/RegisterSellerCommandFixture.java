package com.ryuqq.crawlinghub.application.seller.dto.command;

/**
 * RegisterSellerCommand Test Fixture
 *
 * @author Cascade
 * @since 2025-10-31
 */
public class RegisterSellerCommandFixture {

    private static final String DEFAULT_SELLER_CODE = "SEL001";
    private static final String DEFAULT_SELLER_NAME = "테스트셀러";

    /**
     * 기본 RegisterSellerCommand 생성
     *
     * @return RegisterSellerCommand
     */
    public static RegisterSellerCommand create() {
        return new RegisterSellerCommand(
            DEFAULT_SELLER_CODE,
            DEFAULT_SELLER_NAME
        );
    }

    /**
     * 특정 셀러 코드로 RegisterSellerCommand 생성
     *
     * @param sellerCode 셀러 코드
     * @return RegisterSellerCommand
     */
    public static RegisterSellerCommand createWithCode(String sellerCode) {
        return new RegisterSellerCommand(
            sellerCode,
            DEFAULT_SELLER_NAME
        );
    }

    /**
     * 특정 셀러 이름으로 RegisterSellerCommand 생성
     *
     * @param sellerName 셀러 이름
     * @return RegisterSellerCommand
     */
    public static RegisterSellerCommand createWithName(String sellerName) {
        return new RegisterSellerCommand(
            DEFAULT_SELLER_CODE,
            sellerName
        );
    }

    /**
     * 완전한 커스텀 RegisterSellerCommand 생성
     *
     * @param sellerCode 셀러 코드
     * @param sellerName 셀러 이름
     * @return RegisterSellerCommand
     */
    public static RegisterSellerCommand createCustom(
        String sellerCode,
        String sellerName
    ) {
        return new RegisterSellerCommand(sellerCode, sellerName);
    }
}
