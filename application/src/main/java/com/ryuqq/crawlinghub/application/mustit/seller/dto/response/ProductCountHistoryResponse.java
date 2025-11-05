package com.ryuqq.crawlinghub.application.mustit.seller.dto.response;

import java.time.LocalDateTime;

/**
 * ProductCountHistoryResponse - 상품 수 이력 응답 DTO
 *
 * <p><strong>🆕 변경사항 (v2):</strong></p>
 * <ul>
 *   <li>❌ previousCount 제거</li>
 *   <li>✅ executedDate + productCount만 반환</li>
 * </ul>
 *
 * @param historyId 이력 ID
 * @param executedDate 실행 날짜
 * @param productCount 카운트 된 수
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record ProductCountHistoryResponse(
    Long historyId,
    LocalDateTime executedDate,
    Integer productCount
) {}

