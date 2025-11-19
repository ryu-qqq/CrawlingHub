package com.ryuqq.crawlinghub.application.seller.dto.command;

import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;

/**
 * 셀러 상태 변경 UseCase 입력 DTO.
 *
 * @param sellerId 대상 셀러 ID
 * @param targetStatus 목표 상태
 */
public record ChangeSellerStatusCommand(
    Long sellerId,
    SellerStatus targetStatus
) {
}

