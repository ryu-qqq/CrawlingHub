package com.ryuqq.crawlinghub.domain.fixture.seller;

import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateMustItSellerIdException;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateSellerNameException;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerHasActiveSchedulersException;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;

/**
 * SellerExceptionFixture
 *
 * <p>Seller Bounded Context에서 사용하는 Domain Exception 생성을 담당합니다.</p>
 */
public final class SellerExceptionFixture {

    private SellerExceptionFixture() {
    }

    public static DuplicateMustItSellerIdException duplicateMustItSellerIdException(long mustItSellerId) {
        return new DuplicateMustItSellerIdException(mustItSellerId);
    }

    public static DuplicateSellerNameException duplicateSellerNameException(String sellerName) {
        return new DuplicateSellerNameException(sellerName);
    }

    public static SellerHasActiveSchedulersException sellerHasActiveSchedulersException(long sellerId, int activeSchedulerCount) {
        return new SellerHasActiveSchedulersException(sellerId, activeSchedulerCount);
    }

    public static SellerNotFoundException sellerNotFoundException(long sellerId) {
        return new SellerNotFoundException(sellerId);
    }
}

