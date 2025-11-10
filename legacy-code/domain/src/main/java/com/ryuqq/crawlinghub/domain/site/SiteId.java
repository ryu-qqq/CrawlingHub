package com.ryuqq.crawlinghub.domain.site;

public record SiteId(Long value) {

    public SiteId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Site ID must be positive: " + value);
        }
    }

    public static SiteId of(Long value) {
        return new SiteId(value);
    }

}
