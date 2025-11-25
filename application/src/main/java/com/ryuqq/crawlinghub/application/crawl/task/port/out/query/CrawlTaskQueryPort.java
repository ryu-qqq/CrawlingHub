package com.ryuqq.crawlinghub.application.crawl.task.port.out.query;

import com.ryuqq.crawlinghub.domain.crawl.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.crawl.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.crawl.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;

import java.util.List;
import java.util.Optional;

/**
 * CrawlTask 조회 Port (Port Out - Query)
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawlTaskQueryPort {

    /**
     * CrawlTask ID로 단건 조회
     *
     * @param crawlTaskId CrawlTask ID
     * @return CrawlTask (Optional)
     */
    Optional<CrawlTask> findById(CrawlTaskId crawlTaskId);

    /**
     * Schedule ID와 상태 목록으로 존재 여부 확인
     *
     * <p>중복 Task 생성 방지를 위해 사용
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param statuses 확인할 상태 목록
     * @return 존재 여부
     */
    boolean existsByScheduleIdAndStatusIn(CrawlSchedulerId crawlSchedulerId, List<CrawlTaskStatus> statuses);

    /**
     * Schedule ID로 CrawlTask 목록 조회
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param status 상태 필터 (null이면 전체)
     * @param offset 시작 위치
     * @param limit 조회 개수
     * @return CrawlTask 목록
     */
    List<CrawlTask> findByScheduleId(CrawlSchedulerId crawlSchedulerId, CrawlTaskStatus status, long offset, int limit);

    /**
     * Schedule ID로 CrawlTask 총 개수 조회
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param status 상태 필터 (null이면 전체)
     * @return 총 개수
     */
    long countByScheduleId(CrawlSchedulerId crawlSchedulerId, CrawlTaskStatus status);
}
