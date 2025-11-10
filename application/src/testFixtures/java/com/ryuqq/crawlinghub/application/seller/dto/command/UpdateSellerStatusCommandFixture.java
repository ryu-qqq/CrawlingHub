package com.ryuqq.crawlinghub.application.seller.dto.command;

import com.ryuqq.crawlinghub.domain.seller.SellerStatus;

/**
 * UpdateSellerStatusCommand Test Fixture
 *
 * @author Cascade
 * @since 2025-10-31
 */
public class UpdateSellerStatusCommandFixture {

    private static final Long DEFAULT_SELLER_ID = 1L;
    private static final SellerStatus DEFAULT_STATUS = SellerStatus.ACTIVE;

    /**
     * 기본 UpdateSellerStatusCommand 생성 (ACTIVE 상태)
     *
     * @return UpdateSellerStatusCommand
     */
    public static UpdateSellerStatusCommand create() {
        return new UpdateSellerStatusCommand(
            DEFAULT_SELLER_ID,
            DEFAULT_STATUS
        );
    }

    /**
     * 특정 셀러 ID로 UpdateSellerStatusCommand 생성
     *
     * @param sellerId 셀러 ID
     * @return UpdateSellerStatusCommand
     */
    public static UpdateSellerStatusCommand createWithId(Long sellerId) {
        return new UpdateSellerStatusCommand(
            sellerId,
            DEFAULT_STATUS
        );
    }

    /**
     * ACTIVE 상태로 변경하는 UpdateSellerStatusCommand 생성
     *
     * @return UpdateSellerStatusCommand
     */
    public static UpdateSellerStatusCommand createActive() {
        return new UpdateSellerStatusCommand(
            DEFAULT_SELLER_ID,
            SellerStatus.ACTIVE
        );
    }

    /**
     * PAUSED 상태로 변경하는 UpdateSellerStatusCommand 생성
     *
     * @return UpdateSellerStatusCommand
     */
    public static UpdateSellerStatusCommand createPaused() {
        return new UpdateSellerStatusCommand(
            DEFAULT_SELLER_ID,
            SellerStatus.PAUSED
        );
    }

    /**
     * DISABLED 상태로 변경하는 UpdateSellerStatusCommand 생성
     *
     * @return UpdateSellerStatusCommand
     */
    public static UpdateSellerStatusCommand createDisabled() {
        return new UpdateSellerStatusCommand(
            DEFAULT_SELLER_ID,
            SellerStatus.DISABLED
        );
    }

    /**
     * 완전한 커스텀 UpdateSellerStatusCommand 생성
     *
     * @param sellerId 셀러 ID
     * @param status 변경할 상태
     * @return UpdateSellerStatusCommand
     */
    public static UpdateSellerStatusCommand createCustom(
        Long sellerId,
        SellerStatus status
    ) {
        return new UpdateSellerStatusCommand(sellerId, status);
    }
}
