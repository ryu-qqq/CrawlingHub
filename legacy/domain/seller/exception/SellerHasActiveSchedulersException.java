package com.ryuqq.crawlinghub.domain.seller.exception;

import com.ryuqq.crawlinghub.domain.common.DomainException;
import java.util.Map;

/** 활성화 상태의 스케줄러가 존재하여 셀러를 비활성화할 수 없을 때 발생하는 예외입니다. */
public final class SellerHasActiveSchedulersException extends DomainException {

    private static final SellerErrorCode ERROR_CODE = SellerErrorCode.SELLER_HAS_ACTIVE_SCHEDULERS;

    private final Map<String, Object> args;

    public SellerHasActiveSchedulersException(long sellerId, int activeSchedulerCount) {
        super(ERROR_CODE.getMessage());
        this.args =
                Map.of(
                        "sellerId", sellerId,
                        "activeSchedulerCount", activeSchedulerCount);
    }

    @Override
    public String code() {
        return ERROR_CODE.getCode();
    }

    @Override
    public Map<String, Object> args() {
        return args;
    }
}
