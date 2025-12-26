package com.ryuqq.crawlinghub.application.common.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CrawlTask 메트릭 수집 어노테이션
 *
 * <p>크롤링 태스크 관련 메트릭을 자동으로 수집합니다.
 *
 * <h3>수집 메트릭</h3>
 *
 * <ul>
 *   <li>crawl.task.received.total: 수신된 태스크 수
 *   <li>crawl.task.completed.total: 완료된 태스크 수
 *   <li>crawl.task.failed.total: 실패한 태스크 수
 *   <li>crawl.task.duration: 처리 시간
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CrawlTaskMetric {

    /**
     * 작업 유형
     *
     * <ul>
     *   <li>execute: 태스크 실행 (수신, 완료/실패, 시간 측정)
     * </ul>
     */
    String operation() default "execute";
}
