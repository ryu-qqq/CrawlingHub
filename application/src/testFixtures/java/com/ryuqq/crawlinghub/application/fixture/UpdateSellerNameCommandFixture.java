package com.ryuqq.crawlinghub.application.fixture;

import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerNameCommand;

/**
 * UpdateSellerNameCommand Fixture
 *
 * <p>테스트에서 UpdateSellerNameCommand 객체를 쉽게 생성하기 위한 Fixture입니다.</p>
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
public class UpdateSellerNameCommandFixture {

    private static final Long DEFAULT_SELLER_ID = 1L;
    private static final String DEFAULT_NEW_NAME = "새로운이름";

    /**
     * 기본 UpdateSellerNameCommand 생성
     *
     * @return 기본값으로 생성된 UpdateSellerNameCommand
     */
    public static UpdateSellerNameCommand anUpdateSellerNameCommand() {
        return new UpdateSellerNameCommand(
            DEFAULT_SELLER_ID,
            DEFAULT_NEW_NAME
        );
    }

    /**
     * 커스텀 sellerId로 UpdateSellerNameCommand 생성
     *
     * @param sellerId Seller ID
     * @return sellerId가 지정된 UpdateSellerNameCommand
     */
    public static UpdateSellerNameCommand anUpdateSellerNameCommandWithSellerId(Long sellerId) {
        return new UpdateSellerNameCommand(
            sellerId,
            DEFAULT_NEW_NAME
        );
    }

    /**
     * 커스텀 newName으로 UpdateSellerNameCommand 생성
     *
     * @param newName 새로운 Seller 이름
     * @return newName이 지정된 UpdateSellerNameCommand
     */
    public static UpdateSellerNameCommand anUpdateSellerNameCommandWithNewName(String newName) {
        return new UpdateSellerNameCommand(
            DEFAULT_SELLER_ID,
            newName
        );
    }

    /**
     * 모든 필드를 커스텀으로 UpdateSellerNameCommand 생성
     *
     * @param sellerId Seller ID
     * @param newName 새로운 Seller 이름
     * @return 커스텀 값으로 생성된 UpdateSellerNameCommand
     */
    public static UpdateSellerNameCommand anUpdateSellerNameCommand(
            Long sellerId,
            String newName
    ) {
        return new UpdateSellerNameCommand(sellerId, newName);
    }
}

