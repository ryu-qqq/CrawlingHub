package com.ryuqq.crawlinghub.application.execution.dto.bundle;

import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlContext;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import java.time.Instant;

/**
 * CrawlTask 실행 번들
 *
 * <p>Factory에서 CrawlTask, CrawlExecution, Command, changedAt을 모두 포함하여 생성하는 불변 DTO입니다.
 *
 * <p>UserAgent 소비 후 {@link #withCrawlContext(CrawlContext)}로 CrawlContext가 포함된 번들을 생성합니다.
 *
 * @param crawlTask 실행 대상 CrawlTask
 * @param execution 생성된 CrawlExecution
 * @param command 실행 커맨드
 * @param changedAt 변경 시각
 * @param crawlContext 크롤링 컨텍스트 (UserAgent 소비 후 생성)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlTaskExecutionBundle(
        CrawlTask crawlTask,
        CrawlExecution execution,
        ExecuteCrawlTaskCommand command,
        Instant changedAt,
        CrawlContext crawlContext) {

    /**
     * Bundle 생성 (CrawlContext 없음)
     *
     * @param crawlTask 검증 완료된 CrawlTask
     * @param execution 생성된 CrawlExecution
     * @param command 실행 커맨드
     * @param changedAt 변경 시각
     * @return CrawlTaskExecutionBundle
     */
    public static CrawlTaskExecutionBundle of(
            CrawlTask crawlTask,
            CrawlExecution execution,
            ExecuteCrawlTaskCommand command,
            Instant changedAt) {
        return new CrawlTaskExecutionBundle(crawlTask, execution, command, changedAt, null);
    }

    /**
     * CrawlContext를 포함한 새 Bundle 생성
     *
     * @param context 크롤링 컨텍스트
     * @return CrawlContext가 포함된 새 Bundle
     */
    public CrawlTaskExecutionBundle withCrawlContext(CrawlContext context) {
        return new CrawlTaskExecutionBundle(crawlTask, execution, command, changedAt, context);
    }
}
