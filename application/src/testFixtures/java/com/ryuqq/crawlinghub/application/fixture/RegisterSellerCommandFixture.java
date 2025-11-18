package com.ryuqq.crawlinghub.application.fixture;

import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;

/**
 * RegisterSellerCommand Fixture (Object Mother Pattern)
 *
 * <p>테스트에서 RegisterSellerCommand 객체를 쉽게 생성하기 위한 Fixture입니다.</p>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Plain Java 사용</li>
 *   <li>✅ Object Mother 패턴 사용 (테스트 편의성)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
public class RegisterSellerCommandFixture {

    private static final String DEFAULT_NAME = "무신사";

    /**
     * 기본 RegisterSellerCommand 생성
     *
     * @return 기본값으로 생성된 RegisterSellerCommand
     */
    public static RegisterSellerCommand aRegisterSellerCommand() {
        return new RegisterSellerCommand(DEFAULT_NAME);
    }

    /**
     * 커스텀 name으로 RegisterSellerCommand 생성
     *
     * @param name Seller 이름
     * @return name이 지정된 RegisterSellerCommand
     */
    public static RegisterSellerCommand aRegisterSellerCommandWithName(String name) {
        return new RegisterSellerCommand(name);
    }
}
