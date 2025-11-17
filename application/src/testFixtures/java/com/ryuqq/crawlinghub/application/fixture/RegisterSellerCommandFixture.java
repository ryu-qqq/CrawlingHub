package com.ryuqq.crawlinghub.application.fixture;

import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;

/**
 * RegisterSellerCommand Fixture
 *
 * <p>테스트에서 RegisterSellerCommand 객체를 쉽게 생성하기 위한 Fixture입니다.</p>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Plain Java 사용</li>
 *   <li>✅ Builder 패턴 사용 (테스트 편의성)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public class RegisterSellerCommandFixture {

    private static final String DEFAULT_SELLER_ID = "seller_12345";
    private static final String DEFAULT_NAME = "무신사";
    private static final Integer DEFAULT_CRAWLING_INTERVAL_DAYS = 1;

    /**
     * 기본 RegisterSellerCommand 생성
     *
     * @return 기본값으로 생성된 RegisterSellerCommand
     */
    public static RegisterSellerCommand aRegisterSellerCommand() {
        return new RegisterSellerCommand(
            DEFAULT_SELLER_ID,
            DEFAULT_NAME,
            DEFAULT_CRAWLING_INTERVAL_DAYS
        );
    }

    /**
     * 커스텀 sellerId로 RegisterSellerCommand 생성
     *
     * @param sellerId Seller ID
     * @return sellerId가 지정된 RegisterSellerCommand
     */
    public static RegisterSellerCommand aRegisterSellerCommandWithSellerId(String sellerId) {
        return new RegisterSellerCommand(
            sellerId,
            DEFAULT_NAME,
            DEFAULT_CRAWLING_INTERVAL_DAYS
        );
    }

    /**
     * 커스텀 name으로 RegisterSellerCommand 생성
     *
     * @param name Seller 이름
     * @return name이 지정된 RegisterSellerCommand
     */
    public static RegisterSellerCommand aRegisterSellerCommandWithName(String name) {
        return new RegisterSellerCommand(
            DEFAULT_SELLER_ID,
            name,
            DEFAULT_CRAWLING_INTERVAL_DAYS
        );
    }

    /**
     * 커스텀 crawlingIntervalDays로 RegisterSellerCommand 생성
     *
     * @param crawlingIntervalDays 크롤링 주기
     * @return crawlingIntervalDays가 지정된 RegisterSellerCommand
     */
    public static RegisterSellerCommand aRegisterSellerCommandWithInterval(Integer crawlingIntervalDays) {
        return new RegisterSellerCommand(
            DEFAULT_SELLER_ID,
            DEFAULT_NAME,
            crawlingIntervalDays
        );
    }

    /**
     * 모든 필드를 커스텀으로 RegisterSellerCommand 생성
     *
     * @param sellerId Seller ID
     * @param name Seller 이름
     * @param crawlingIntervalDays 크롤링 주기
     * @return 커스텀 값으로 생성된 RegisterSellerCommand
     */
    public static RegisterSellerCommand aRegisterSellerCommand(
            String sellerId,
            String name,
            Integer crawlingIntervalDays
    ) {
        return new RegisterSellerCommand(sellerId, name, crawlingIntervalDays);
    }
}
