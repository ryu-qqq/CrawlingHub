package com.ryuqq.crawlinghub.application.common.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 크롤링 실행 메트릭 수집 어노테이션
 *
 * <p>실제 크롤링 수행에 대한 메트릭을 자동으로 수집합니다.
 *
 * <h3>수집 메트릭</h3>
 *
 * <ul>
 *   <li>crawl.http.latency: HTTP 요청 latency
 *   <li>crawl.http.status.total: HTTP 상태 코드별 카운트
 *   <li>crawl.success.total: 성공 횟수
 *   <li>crawl.failure.total: 실패 횟수
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CrawlMetric {

    /**
     * 크롤러 타입
     *
     * <p>빈 문자열이면 메서드에서 자동 추출을 시도합니다.
     *
     * <ul>
     *   <li>meta: 미니샵 메타 정보 크롤링
     *   <li>minishop: 미니샵 상품 목록 크롤링
     *   <li>search: Search API 상품 목록 크롤링
     *   <li>detail: 상품 상세 정보 크롤링
     *   <li>option: 상품 옵션 정보 크롤링
     * </ul>
     */
    String crawlerType() default "";
}
