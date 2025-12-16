package com.ryuqq.crawlinghub.application.product.dto.response;

import java.time.Instant;

/**
 * CrawledProduct 요약 Response
 *
 * <p>목록 조회용 요약 정보
 *
 * @param id 상품 ID
 * @param sellerId 판매자 ID
 * @param itemNo 상품 번호
 * @param itemName 상품명
 * @param brandName 브랜드명
 * @param price 판매가
 * @param discountRate 할인율
 * @param crawlCompletedCount 크롤링 완료 개수 (0-3)
 * @param pendingCrawlTypes 미완료 크롤링 타입 (예: "DETAIL, OPTION")
 * @param needsSync 동기화 필요 여부
 * @param externalProductId 외부 상품 ID (nullable)
 * @param lastSyncedAt 마지막 동기화 시각 (nullable)
 * @param allImagesUploaded 모든 이미지 업로드 완료 여부
 * @param totalStock 총 재고
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @author development-team
 * @since 1.0.0
 */
public record CrawledProductSummaryResponse(
        Long id,
        Long sellerId,
        long itemNo,
        String itemName,
        String brandName,
        int price,
        int discountRate,
        int crawlCompletedCount,
        String pendingCrawlTypes,
        boolean needsSync,
        Long externalProductId,
        Instant lastSyncedAt,
        boolean allImagesUploaded,
        int totalStock,
        Instant createdAt,
        Instant updatedAt) {}
