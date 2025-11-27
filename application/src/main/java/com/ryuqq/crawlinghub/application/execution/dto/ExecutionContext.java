package com.ryuqq.crawlinghub.application.execution.dto;

import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;

/**
 * CrawlTask 실행 컨텍스트
 *
 * <p>prepareExecution과 completeWith* 메서드 사이에서 컨텍스트를 전달하기 위한 DTO입니다.
 *
 * @param crawlTask 실행 대상 CrawlTask
 * @param execution 생성된 CrawlExecution
 * @author development-team
 * @since 1.0.0
 */
public record ExecutionContext(CrawlTask crawlTask, CrawlExecution execution) {}
