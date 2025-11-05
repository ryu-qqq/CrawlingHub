package com.ryuqq.crawlinghub.domain.seller.history;

import java.time.LocalDateTime;

import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;

/**
 * ProductCountHistory - 상품 수 변경 이력
 *
 * <p>Pure Java 도메인 객체 (Lombok 금지)</p>
 *
 * <p><strong>🆕 변경사항 (v2):</strong></p>
 * <ul>
 *   <li>❌ previousCount 필드 제거</li>
 *   <li>✅ executedDate + productCount만 저장</li>
 *   <li>✅ 이유: 변경 전 수량은 이력 추적에 불필요</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public class ProductCountHistory {

    private final ProductCountHistoryId id;
    private final MustitSellerId sellerId;
    private final Integer productCount;         // 카운트 된 수 ⭐
    private final LocalDateTime executedDate;   // 실행 날짜 ⭐

    // Private Constructor (Factory Method 강제)
    private ProductCountHistory(
        ProductCountHistoryId id,
        MustitSellerId sellerId,
        Integer productCount,
        LocalDateTime executedDate
    ) {
        this.id = id;
        this.sellerId = sellerId;
        this.productCount = productCount;
        this.executedDate = executedDate;
    }

    /**
     * Factory Method - 새로운 이력 기록
     *
     * @param sellerId 셀러 ID
     * @param productCount 실행 시점 상품 수
     * @param executedDate 실행 날짜
     * @return ProductCountHistory
     */
    public static ProductCountHistory record(
        MustitSellerId sellerId,
        Integer productCount,
        LocalDateTime executedDate
    ) {
        validateProductCount(productCount);
        validateExecutedDate(executedDate);
        return new ProductCountHistory(
            null, // ID는 Persistence Layer에서 할당
            sellerId,
            productCount,
            executedDate
        );
    }

    /**
     * Factory Method - 기존 이력 복원 (Persistence → Domain)
     *
     * @param id 이력 ID
     * @param sellerId 셀러 ID
     * @param productCount 실행 시점 상품 수
     * @param executedDate 실행 날짜
     * @return ProductCountHistory
     */
    public static ProductCountHistory reconstitute(
        ProductCountHistoryId id,
        MustitSellerId sellerId,
        Integer productCount,
        LocalDateTime executedDate
    ) {
        validateProductCount(productCount);
        validateExecutedDate(executedDate);
        return new ProductCountHistory(id, sellerId, productCount, executedDate);
    }

    /**
     * 상품 수 검증
     *
     * @param productCount 상품 수
     */
    private static void validateProductCount(Integer productCount) {
        if (productCount == null || productCount < 0) {
            throw new IllegalArgumentException("상품 수는 0 이상이어야 합니다");
        }
    }

    /**
     * 실행 날짜 검증
     *
     * @param executedDate 실행 날짜
     */
    private static void validateExecutedDate(LocalDateTime executedDate) {
        if (executedDate == null) {
            throw new IllegalArgumentException("실행 날짜는 필수입니다");
        }
    }

    // Getters (Pure Java)
    public ProductCountHistoryId getId() {
        return id;
    }

    public MustitSellerId getSellerId() {
        return sellerId;
    }

    public Long getSellerIdValue() {
        return sellerId != null ? sellerId.value() : null;
    }

    public Integer getProductCount() {
        return productCount;
    }

    public LocalDateTime getExecutedDate() {
        return executedDate;
    }

    /**
     * 날짜 변경 체크 (동일 날짜에 중복 저장 방지)
     *
     * @param other 비교할 날짜
     * @return 동일 날짜 여부
     */
    public boolean isSameDate(LocalDateTime other) {
        if (executedDate == null || other == null) {
            return false;
        }
        return this.executedDate.toLocalDate().equals(other.toLocalDate());
    }
}

