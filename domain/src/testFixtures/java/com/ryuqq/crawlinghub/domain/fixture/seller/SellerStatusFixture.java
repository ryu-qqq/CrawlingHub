package com.ryuqq.crawlinghub.domain.fixture.seller;

import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;

/**
 * SellerStatusFixture
 *
 * <p>Seller 도메인에서 사용하는 상태 값 객체 생성을 담당합니다.</p>
 */
public final class SellerStatusFixture {

    private SellerStatusFixture() {
    }

    public static SellerStatus active() {
        return SellerStatus.ACTIVE;
    }

    public static SellerStatus inactive() {
        return SellerStatus.INACTIVE;
    }
}

