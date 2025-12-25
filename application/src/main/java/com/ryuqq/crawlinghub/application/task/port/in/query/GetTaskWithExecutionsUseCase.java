package com.ryuqq.crawlinghub.application.task.port.in.query;

import com.ryuqq.crawlinghub.application.task.dto.query.GetTaskWithExecutionsQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.TaskWithExecutionsResponse;

/**
 * Task 상세 + Execution 이력 조회 UseCase
 *
 * <p>어드민용 Task 상세 조회입니다. Task 정보와 최근 실행 이력을 함께 반환합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface GetTaskWithExecutionsUseCase {

    /**
     * Task 상세 + Execution 이력 조회
     *
     * @param query Task ID와 조회 옵션
     * @return Task 상세 + Execution 이력 응답
     */
    TaskWithExecutionsResponse execute(GetTaskWithExecutionsQuery query);
}
