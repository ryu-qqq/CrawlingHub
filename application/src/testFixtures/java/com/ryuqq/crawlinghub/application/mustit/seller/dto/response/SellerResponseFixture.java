package com.ryuqq.crawlinghub.application.mustit.seller.dto.response;

import com.ryuqq.crawlinghub.domain.mustit.seller.SellerStatus;

import java.time.LocalDateTime;

/**
 * SellerResponse Test Fixture
 *
 * @author Cascade
 * @since 2025-10-31
 */
public class SellerResponseFixture {

    private static final Long DEFAULT_SELLER_ID = 1L;
    private static final String DEFAULT_SELLER_CODE = "SEL001";
    private static final String DEFAULT_SELLER_NAME = "테스트셀러";
    private static final SellerStatus DEFAULT_STATUS = SellerStatus.ACTIVE;
    private static final Integer DEFAULT_PRODUCT_COUNT = 100;
    private static final LocalDateTime DEFAULT_TIMESTAMP = LocalDateTime.of(2025, 1, 1, 0, 0, 0);

    /**
     * 기본 SellerResponse 생성
     *
     * @return SellerResponse
     */
    public static SellerResponse create() {
        return new SellerResponse(
            DEFAULT_SELLER_ID,
            DEFAULT_SELLER_CODE,
            DEFAULT_SELLER_NAME,
            DEFAULT_STATUS,
            DEFAULT_PRODUCT_COUNT,
            null,
            DEFAULT_TIMESTAMP,
            DEFAULT_TIMESTAMP
        );
    }

    /**
     * 특정 ID로 SellerResponse 생성
     *
     * @param sellerId 셀러 ID
     * @return SellerResponse
     */
    public static SellerResponse createWithId(Long sellerId) {
        return new SellerResponse(
            sellerId,
            DEFAULT_SELLER_CODE,
            DEFAULT_SELLER_NAME,
            DEFAULT_STATUS,
            DEFAULT_PRODUCT_COUNT,
            null,
            DEFAULT_TIMESTAMP,
            DEFAULT_TIMESTAMP
        );
    }

    /**
     * ACTIVE 상태의 SellerResponse 생성
     *
     * @return SellerResponse
     */
    public static SellerResponse createActive() {
        return new SellerResponse(
            DEFAULT_SELLER_ID,
            DEFAULT_SELLER_CODE,
            DEFAULT_SELLER_NAME,
            SellerStatus.ACTIVE,
            DEFAULT_PRODUCT_COUNT,
            null,
            DEFAULT_TIMESTAMP,
            DEFAULT_TIMESTAMP
        );
    }

    /**
     * PAUSED 상태의 SellerResponse 생성
     *
     * @return SellerResponse
     */
    public static SellerResponse createPaused() {
        return new SellerResponse(
            DEFAULT_SELLER_ID,
            DEFAULT_SELLER_CODE,
            DEFAULT_SELLER_NAME,
            SellerStatus.PAUSED,
            50,
            DEFAULT_TIMESTAMP.minusDays(1),
            DEFAULT_TIMESTAMP,
            DEFAULT_TIMESTAMP
        );
    }

    /**
     * DISABLED 상태의 SellerResponse 생성
     *
     * @return SellerResponse
     */
    public static SellerResponse createDisabled() {
        return new SellerResponse(
            DEFAULT_SELLER_ID,
            DEFAULT_SELLER_CODE,
            DEFAULT_SELLER_NAME,
            SellerStatus.DISABLED,
            0,
            DEFAULT_TIMESTAMP.minusDays(7),
            DEFAULT_TIMESTAMP,
            DEFAULT_TIMESTAMP
        );
    }

    /**
     * 특정 상품 수를 가진 SellerResponse 생성
     *
     * @param productCount 상품 수
     * @return SellerResponse
     */
    public static SellerResponse createWithProductCount(Integer productCount) {
        return new SellerResponse(
            DEFAULT_SELLER_ID,
            DEFAULT_SELLER_CODE,
            DEFAULT_SELLER_NAME,
            DEFAULT_STATUS,
            productCount,
            DEFAULT_TIMESTAMP,
            DEFAULT_TIMESTAMP,
            DEFAULT_TIMESTAMP
        );
    }

    /**
     * 완전한 커스텀 SellerResponse 생성
     *
     * @param sellerId          셀러 ID
     * @param sellerCode        셀러 코드
     * @param sellerName        셀러 이름
     * @param status            상태
     * @param totalProductCount 총 상품 수
     * @param lastCrawledAt     마지막 크롤링 시간
     * @param createdAt         생성 시간
     * @param updatedAt         수정 시간
     * @return SellerResponse
     */
    public static SellerResponse createCustom(
        Long sellerId,
        String sellerCode,
        String sellerName,
        SellerStatus status,
        Integer totalProductCount,
        LocalDateTime lastCrawledAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new SellerResponse(
            sellerId,
            sellerCode,
            sellerName,
            status,
            totalProductCount,
            lastCrawledAt,
            createdAt,
            updatedAt
        );
    }
}
