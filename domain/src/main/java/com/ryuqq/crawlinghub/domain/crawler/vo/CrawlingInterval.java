package com.ryuqq.crawlinghub.domain.crawler.vo;

import java.time.temporal.ChronoUnit;

/**
 * CrawlingInterval - 크롤링 주기 Value Object
 *
 * <p>크롤링 스케줄의 실행 주기를 나타냅니다.</p>
 *
 * <p><strong>지원하는 단위:</strong></p>
 * <ul>
 *   <li>✅ HOURS: 시간 단위</li>
 *   <li>✅ DAYS: 일 단위</li>
 * </ul>
 *
 * <p><strong>검증 규칙:</strong></p>
 * <ul>
 *   <li>✅ amount: 1 이상</li>
 *   <li>✅ unit: HOURS 또는 DAYS만 허용</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 (Record 패턴 사용)</li>
 *   <li>✅ 불변성 (Immutable)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public record CrawlingInterval(long amount, ChronoUnit unit) {

    /**
     * Compact Constructor - 크롤링 주기 검증
     *
     * <p>amount는 1 이상이어야 하고, unit은 HOURS 또는 DAYS만 허용합니다.</p>
     *
     * @throws IllegalArgumentException amount가 1 미만이거나 지원하지 않는 unit일 때
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public CrawlingInterval {
        if (amount < 1) {
            throw new IllegalArgumentException("크롤링 주기는 1 이상이어야 합니다");
        }

        if (unit != ChronoUnit.HOURS && unit != ChronoUnit.DAYS) {
            throw new IllegalArgumentException("크롤링 주기는 HOURS 또는 DAYS만 지원합니다");
        }
    }
}
