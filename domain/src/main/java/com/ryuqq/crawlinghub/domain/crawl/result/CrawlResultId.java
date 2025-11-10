package com.ryuqq.crawlinghub.domain.crawl.result;

import java.util.Objects;

/**
 * CrawlResult ID Value Object
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public record CrawlResultId(Long value) {

    public CrawlResultId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("CrawlResult ID는 양수여야 합니다");
        }
    }

    /**
     * Long 값으로부터 생성
     */
    public static CrawlResultId of(Long value) {
        return new CrawlResultId(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CrawlResultId that = (CrawlResultId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "CrawlResultId{" + value + '}';
    }
}
