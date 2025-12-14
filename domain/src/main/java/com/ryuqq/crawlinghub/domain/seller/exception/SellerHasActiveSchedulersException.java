package com.ryuqq.crawlinghub.domain.seller.exception;

import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import java.util.Map;

/** 활성화 상태의 스케줄러가 존재하여 셀러를 비활성화할 수 없을 때 발생하는 예외입니다. */
public final class SellerHasActiveSchedulersException extends DomainException {

    private static final SellerErrorCode ERROR_CODE = SellerErrorCode.SELLER_HAS_ACTIVE_SCHEDULERS;

    public SellerHasActiveSchedulersException(long sellerId, int activeSchedulerCount) {
        super(
                ERROR_CODE,
                String.format(
                        "활성화된 스케줄러가 있어 셀러를 비활성화할 수 없습니다. 셀러: %d, 활성 스케줄러: %d개",
                        sellerId, activeSchedulerCount),
                Map.of("sellerId", sellerId, "activeSchedulerCount", activeSchedulerCount));
    }
}
