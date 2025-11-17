package com.ryuqq.crawlinghub.application.dto.command;

/**
 * Seller 등록 Command DTO
 *
 * <p>새로운 Seller를 등록하기 위한 Command 객체입니다.</p>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Java 21 Record 사용</li>
 *   <li>✅ Validation - Compact Constructor에서 검증</li>
 * </ul>
 *
 * @param sellerId Seller ID (빈 값 불가)
 * @param name Seller 이름
 * @param crawlingIntervalDays 크롤링 주기 (일 단위, 양수)
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public record RegisterSellerCommand(
        String sellerId,
        String name,
        Integer crawlingIntervalDays
) {
    /**
     * Compact Constructor - 유효성 검증
     *
     * @throws IllegalArgumentException sellerId가 빈 값이거나 crawlingIntervalDays가 0 이하일 때
     */
    public RegisterSellerCommand {
        validateSellerId(sellerId);
        validateCrawlingIntervalDays(crawlingIntervalDays);
    }

    private void validateSellerId(String sellerId) {
        if (sellerId == null || sellerId.isBlank()) {
            throw new IllegalArgumentException("sellerId는 빈 값일 수 없습니다");
        }
    }

    private void validateCrawlingIntervalDays(Integer crawlingIntervalDays) {
        if (crawlingIntervalDays == null || crawlingIntervalDays <= 0) {
            throw new IllegalArgumentException("crawlingIntervalDays는 0보다 커야 합니다");
        }
    }
}
