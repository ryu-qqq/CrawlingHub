package com.ryuqq.crawlinghub.application.monitoring.port.in;

import com.ryuqq.crawlinghub.application.monitoring.dto.query.TaskProgressQuery;
import com.ryuqq.crawlinghub.application.monitoring.dto.response.TaskProgressResponse;

/**
 * 태스크 진행 상황 조회 UseCase
 *
 * <p>특정 셀러의 크롤링 태스크 진행 상황을 조회합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface GetTaskProgressUseCase {

    /**
     * 태스크 진행 상황 조회
     *
     * @param query 진행 상황 조회 Query
     * @return 태스크 진행 상황
     */
    TaskProgressResponse execute(TaskProgressQuery query);
}
