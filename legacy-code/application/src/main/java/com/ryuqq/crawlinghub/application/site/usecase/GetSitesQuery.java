package com.ryuqq.crawlinghub.application.site.usecase;

/**
 * Query object for site list retrieval
 * Immutable record for CQRS Query pattern
 * Used by GetSiteUseCase for pagination
 *
 * @param page page number (0-based)
 * @param size page size
 * @param isActive filter by active status (nullable - no filter if null)
 * @param siteType filter by site type (nullable - no filter if null)
 */
public record GetSitesQuery(
        int page,
        int size,
        Boolean isActive,
        String siteType
) {

    /**
     * Create query with default pagination
     *
     * @param page page number
     * @param size page size
     * @return query object
     */
    public static GetSitesQuery of(int page, int size) {
        return new GetSitesQuery(page, size, null, null);
    }

    /**
     * Create query with filters
     *
     * @param page page number
     * @param size page size
     * @param isActive filter by active status
     * @param siteType filter by site type
     * @return query object
     */
    public static GetSitesQuery of(int page, int size, Boolean isActive, String siteType) {
        return new GetSitesQuery(page, size, isActive, siteType);
    }
}
