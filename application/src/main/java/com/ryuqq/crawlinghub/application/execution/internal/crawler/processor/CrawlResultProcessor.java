package com.ryuqq.crawlinghub.application.execution.internal.crawler.processor;

import com.ryuqq.crawlinghub.domain.execution.vo.CrawlResult;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;

/**
 * 크롤링 결과 처리기 인터페이스
 *
 * <p>각 CrawlTaskType별로 크롤링 결과를 처리하는 전략 패턴 인터페이스.
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>크롤링 응답 파싱
 *   <li>비즈니스 데이터 저장 (상품, 옵션 등)
 *   <li>후속 CrawlTask 생성 요청
 * </ul>
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <pre>
 * CrawlTaskExecutionFacade.completeWithSuccess()
 *     → CrawlResultProcessorProvider.getProcessor(taskType)
 *     → processor.process(result, task)
 *         → 파싱 + 저장 + 후속 Task 생성
 *     → ProcessingResult (후속 Task 커맨드 목록)
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawlResultProcessor {

    /**
     * 지원하는 태스크 타입 반환
     *
     * @return 지원하는 CrawlTaskType
     */
    CrawlTaskType supportedType();

    /**
     * 크롤링 결과 처리
     *
     * <p>응답 파싱, 비즈니스 데이터 저장, 후속 Task 커맨드 생성을 수행합니다.
     *
     * @param crawlResult 크롤링 결과 (응답 본문 포함)
     * @param crawlTask 처리 대상 CrawlTask (스케줄러/셀러 정보 포함)
     * @return 처리 결과 (후속 Task 커맨드 목록 포함)
     */
    ProcessingResult process(CrawlResult crawlResult, CrawlTask crawlTask);
}
