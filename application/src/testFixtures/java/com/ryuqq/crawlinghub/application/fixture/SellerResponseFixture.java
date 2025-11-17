package com.ryuqq.crawlinghub.application.fixture;

import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;

import java.time.LocalDateTime;

/**
 * SellerResponse Fixture
 *
 * <p>테스트에서 SellerResponse 객체를 쉽게 생성하기 위한 Fixture입니다.</p>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Plain Java 사용</li>
 *   <li>✅ Factory 메서드 패턴 (테스트 편의성)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public class SellerResponseFixture {

    private static final String DEFAULT_SELLER_ID = "seller_test_001";
    private static final String DEFAULT_NAME = "테스트 셀러";
    private static final SellerStatus DEFAULT_STATUS = SellerStatus.ACTIVE;
    private static final Integer DEFAULT_CRAWLING_INTERVAL_DAYS = 1;
    private static final Integer DEFAULT_TOTAL_PRODUCT_COUNT = 0;
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.now();
    private static final LocalDateTime DEFAULT_UPDATED_AT = LocalDateTime.now();

    /**
     * 기본 SellerResponse 생성
     *
     * @return 기본값으로 생성된 SellerResponse
     */
    public static SellerResponse aSellerResponse() {
        return new SellerResponse(
            DEFAULT_SELLER_ID,
            DEFAULT_NAME,
            DEFAULT_STATUS,
            DEFAULT_CRAWLING_INTERVAL_DAYS,
            DEFAULT_TOTAL_PRODUCT_COUNT,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * 커스텀 sellerId로 SellerResponse 생성
     *
     * @param sellerId Seller ID
     * @return sellerId가 지정된 SellerResponse
     */
    public static SellerResponse aSellerResponseWithSellerId(String sellerId) {
        return new SellerResponse(
            sellerId,
            DEFAULT_NAME,
            DEFAULT_STATUS,
            DEFAULT_CRAWLING_INTERVAL_DAYS,
            DEFAULT_TOTAL_PRODUCT_COUNT,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * 커스텀 name으로 SellerResponse 생성
     *
     * @param name Seller 이름
     * @return name이 지정된 SellerResponse
     */
    public static SellerResponse aSellerResponseWithName(String name) {
        return new SellerResponse(
            DEFAULT_SELLER_ID,
            name,
            DEFAULT_STATUS,
            DEFAULT_CRAWLING_INTERVAL_DAYS,
            DEFAULT_TOTAL_PRODUCT_COUNT,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * 커스텀 status로 SellerResponse 생성
     *
     * @param status Seller 상태
     * @return status가 지정된 SellerResponse
     */
    public static SellerResponse aSellerResponseWithStatus(SellerStatus status) {
        return new SellerResponse(
            DEFAULT_SELLER_ID,
            DEFAULT_NAME,
            status,
            DEFAULT_CRAWLING_INTERVAL_DAYS,
            DEFAULT_TOTAL_PRODUCT_COUNT,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * 커스텀 totalProductCount로 SellerResponse 생성
     *
     * @param totalProductCount 총 상품 수
     * @return totalProductCount가 지정된 SellerResponse
     */
    public static SellerResponse aSellerResponseWithProductCount(Integer totalProductCount) {
        return new SellerResponse(
            DEFAULT_SELLER_ID,
            DEFAULT_NAME,
            DEFAULT_STATUS,
            DEFAULT_CRAWLING_INTERVAL_DAYS,
            totalProductCount,
            DEFAULT_CREATED_AT,
            DEFAULT_UPDATED_AT
        );
    }

    /**
     * 모든 필드를 커스텀으로 SellerResponse 생성
     *
     * @param sellerId Seller ID
     * @param name Seller 이름
     * @param status Seller 상태
     * @param crawlingIntervalDays 크롤링 주기
     * @param totalProductCount 총 상품 수
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @return 커스텀 값으로 생성된 SellerResponse
     */
    public static SellerResponse aSellerResponse(
            String sellerId,
            String name,
            SellerStatus status,
            Integer crawlingIntervalDays,
            Integer totalProductCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new SellerResponse(
            sellerId,
            name,
            status,
            crawlingIntervalDays,
            totalProductCount,
            createdAt,
            updatedAt
        );
    }
}
