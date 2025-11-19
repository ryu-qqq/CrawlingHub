package com.ryuqq.crawlinghub.application.common.pagination;

import java.util.List;

/**
 * Simple immutable page result abstraction used by Query Ports.
 *
 * @param <T> content type
 */
public interface PageResult<T> {

    List<T> content();

    int page();

    int size();

    long totalElements();

    default boolean hasNext() {
        long total = totalElements();
        if (total < 0) {
            return false;
        }
        long consumed = (long) (page() + 1) * size();
        return consumed < total;
    }
}

