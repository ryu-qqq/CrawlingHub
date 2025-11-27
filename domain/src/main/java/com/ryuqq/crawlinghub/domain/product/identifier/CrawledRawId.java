package com.ryuqq.crawlinghub.domain.product.identifier;

/**
 * CrawledRaw ID Value Object
 *
 * <p><strong>생성 패턴</strong>:
 *
 * <ul>
 *   <li>{@code unassigned()} - 미할당 상태 (ID = null, Auto Increment 대비)
 *   <li>{@code of(Long value)} - 값 기반 생성 (null 체크 필수)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public record CrawledRawId(Long value) {

    public CrawledRawId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("CrawledRawId 값은 양수여야 합니다: " + value);
        }
    }

    public static CrawledRawId unassigned() {
        return new CrawledRawId(null);
    }

    public static CrawledRawId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("CrawledRawId 값은 null일 수 없습니다.");
        }
        return new CrawledRawId(value);
    }

    public boolean isAssigned() {
        return value != null;
    }
}
