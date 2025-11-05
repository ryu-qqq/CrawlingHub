package com.ryuqq.crawlinghub.application.seller.dto.command;

import com.ryuqq.crawlinghub.domain.seller.SellerStatus;

/**
 * 셀러 상태 변경 Command
 *
 * @param sellerId 셀러 ID (필수)
 * @param status   변경할 상태 (필수)
 * @author ryu-qqq
 * @since 2025-10-31
 */
public record UpdateSellerStatusCommand(
    Long sellerId,
    SellerStatus status
) {
    public UpdateSellerStatusCommand {
        // 검증 순서: null → 비즈니스 규칙
        if (sellerId == null) {
            throw new IllegalArgumentException("셀러 ID는 null일 수 없습니다");
        }
        if (sellerId <= 0) {
            throw new IllegalArgumentException("셀러 ID는 양수여야 합니다");
        }

        if (status == null) {
            throw new IllegalArgumentException("상태는 null일 수 없습니다");
        }
    }
}
