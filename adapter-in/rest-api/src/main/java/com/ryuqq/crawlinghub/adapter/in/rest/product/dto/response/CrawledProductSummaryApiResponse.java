package com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response;

/**
 * CrawledProduct Summary API Response
 *
 * <p>크롤링 상품 요약 정보 API 응답 DTO
 *
 * <p><strong>응답 필드:</strong>
 *
 * <ul>
 *   <li>기본 정보: id, sellerId, itemNo, itemName, brandName
 *   <li>가격 정보: price, discountRate
 *   <li>크롤링 상태: completedCrawlCount, pendingCrawlTypes
 *   <li>동기화 상태: needsSync, externalProductId, lastSyncedAt
 *   <li>이미지/재고: allImagesUploaded, totalStock
 *   <li>시간 정보: createdAt, updatedAt
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public record CrawledProductSummaryApiResponse(
        Long id,
        Long sellerId,
        Long itemNo,
        String itemName,
        String brandName,
        int price,
        int discountRate,
        int completedCrawlCount,
        String pendingCrawlTypes,
        boolean needsSync,
        Long externalProductId,
        String lastSyncedAt,
        boolean allImagesUploaded,
        int totalStock,
        String createdAt,
        String updatedAt) {}
