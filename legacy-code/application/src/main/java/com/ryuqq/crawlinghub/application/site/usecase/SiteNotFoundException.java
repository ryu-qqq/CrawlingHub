package com.ryuqq.crawlinghub.application.site.usecase;

import com.ryuqq.crawlinghub.domain.site.SiteId;

/**
 * Exception thrown when requested site is not found
 * Business exception for entity not found
 */
public class SiteNotFoundException extends RuntimeException {

    public SiteNotFoundException(SiteId siteId) {
        super("Site not found: " + siteId.value());
    }

    public SiteNotFoundException(String message) {
        super(message);
    }
}
