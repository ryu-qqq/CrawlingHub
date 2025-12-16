package com.ryuqq.crawlinghub.domain.product.vo;

import java.time.Instant;
import java.util.Objects;

/**
 * OPTION 크롤링 데이터 VO
 *
 * <p>CrawledProduct OPTION 업데이트에 필요한 모든 정보를 담은 불변 객체입니다. Factory 패턴을 통해 ProductOption 목록에서 변환됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public final class OptionCrawlData {

    private final ProductOptions options;
    private final Instant updatedAt;

    private OptionCrawlData(ProductOptions options, Instant updatedAt) {
        this.options = Objects.requireNonNull(options, "options must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    public static OptionCrawlData of(ProductOptions options, Instant updatedAt) {
        return new OptionCrawlData(options, updatedAt);
    }

    public ProductOptions options() {
        return options;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OptionCrawlData that = (OptionCrawlData) o;
        return Objects.equals(options, that.options) && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(options, updatedAt);
    }

    @Override
    public String toString() {
        return "OptionCrawlData{"
                + "optionsCount="
                + options.size()
                + ", updatedAt="
                + updatedAt
                + '}';
    }
}
