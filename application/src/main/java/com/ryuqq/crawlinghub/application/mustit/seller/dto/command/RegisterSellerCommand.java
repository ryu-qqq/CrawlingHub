package com.ryuqq.crawlinghub.application.mustit.seller.dto.command;

/**
 * 셀러 등록 Command
 *
 * @param sellerCode 셀러 코드 (필수, 불변)
 * @param sellerName 셀러 이름 (필수)
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record RegisterSellerCommand(
    String sellerCode,
    String sellerName
) {
    public RegisterSellerCommand {
        // 검증 순서: null → blank → 비즈니스 규칙
        if (sellerCode == null) {
            throw new IllegalArgumentException("셀러 코드는 null일 수 없습니다");
        }
        if (sellerCode.isBlank()) {
            throw new IllegalArgumentException("셀러 코드는 비어있을 수 없습니다");
        }

        if (sellerName == null) {
            throw new IllegalArgumentException("셀러 이름은 null일 수 없습니다");
        }
        if (sellerName.isBlank()) {
            throw new IllegalArgumentException("셀러 이름은 비어있을 수 없습니다");
        }
    }
}
