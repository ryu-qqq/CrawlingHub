package com.ryuqq.crawlinghub.application.fixture;

import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerIntervalCommand;

/**
 * UpdateSellerIntervalCommand Fixture
 *
 * <p>테스트에서 UpdateSellerIntervalCommand 객체를 쉽게 생성하기 위한 Fixture입니다.</p>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Plain Java 사용</li>
 *   <li>✅ Builder 패턴 사용 (테스트 편의성)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
public class UpdateSellerIntervalCommandFixture {

    private static final String DEFAULT_SELLER_ID = "seller_12345";
    private static final Integer DEFAULT_NEW_INTERVAL_DAYS = 7;

    /**
     * 기본 UpdateSellerIntervalCommand 생성
     *
     * @return 기본값으로 생성된 UpdateSellerIntervalCommand
     */
    public static UpdateSellerIntervalCommand anUpdateSellerIntervalCommand() {
        return new UpdateSellerIntervalCommand(
            DEFAULT_SELLER_ID,
            DEFAULT_NEW_INTERVAL_DAYS
        );
    }

    /**
     * 커스텀 sellerId로 UpdateSellerIntervalCommand 생성
     *
     * @param sellerId Seller ID
     * @return sellerId가 지정된 UpdateSellerIntervalCommand
     */
    public static UpdateSellerIntervalCommand anUpdateSellerIntervalCommandWithSellerId(String sellerId) {
        return new UpdateSellerIntervalCommand(
            sellerId,
            DEFAULT_NEW_INTERVAL_DAYS
        );
    }

    /**
     * 커스텀 newIntervalDays로 UpdateSellerIntervalCommand 생성
     *
     * @param newIntervalDays 새로운 크롤링 주기
     * @return newIntervalDays가 지정된 UpdateSellerIntervalCommand
     */
    public static UpdateSellerIntervalCommand anUpdateSellerIntervalCommandWithInterval(Integer newIntervalDays) {
        return new UpdateSellerIntervalCommand(
            DEFAULT_SELLER_ID,
            newIntervalDays
        );
    }

    /**
     * 모든 필드를 커스텀으로 UpdateSellerIntervalCommand 생성
     *
     * @param sellerId Seller ID
     * @param newIntervalDays 새로운 크롤링 주기
     * @return 커스텀 값으로 생성된 UpdateSellerIntervalCommand
     */
    public static UpdateSellerIntervalCommand anUpdateSellerIntervalCommand(
            String sellerId,
            Integer newIntervalDays
    ) {
        return new UpdateSellerIntervalCommand(sellerId, newIntervalDays);
    }
}
