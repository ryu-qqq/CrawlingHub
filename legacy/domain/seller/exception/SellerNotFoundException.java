package com.ryuqq.crawlinghub.domain.seller.exception;

import com.ryuqq.crawlinghub.domain.common.DomainException;
import java.util.Map;

/** 존재하지 않는 셀러를 조회했을 때 발생하는 예외입니다. */
public final class SellerNotFoundException extends DomainException {

    private static final SellerErrorCode ERROR_CODE = SellerErrorCode.SELLER_NOT_FOUND;

    private final Map<String, Object> args;

    public SellerNotFoundException(long sellerId) {
        super(ERROR_CODE.getMessage());
        this.args = Map.of("sellerId", sellerId);
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
