package com.ryuqq.crawlinghub.application.site.usecase;

/**
 * Exception thrown when attempting to create a site with duplicate name
 * Business exception for domain rule violation
 */
public class DuplicateSiteException extends RuntimeException {

    public DuplicateSiteException(String message) {
        super(message);
    }
}
