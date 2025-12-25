package com.ryuqq.crawlinghub.application.common.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Downstream 의존성 메트릭 수집 어노테이션
 *
 * <p>외부 의존성(Redis, DB, SQS, HTTP API) 호출 latency를 자동으로 측정합니다.
 *
 * <h3>수집 메트릭</h3>
 *
 * <ul>
 *   <li>downstream.redis.latency: Redis 작업 latency
 *   <li>downstream.db.latency: Database 쿼리 latency
 *   <li>downstream.sqs.publish.latency: SQS 메시지 발행 latency
 *   <li>downstream.external.api.latency: 외부 API 호출 latency
 *   <li>downstream.crawl.http.latency: 크롤링 HTTP 요청 latency
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DownstreamMetric {

    /**
     * 서비스 유형
     *
     * <ul>
     *   <li>redis: Redis 작업
     *   <li>db: Database 쿼리
     *   <li>sqs: SQS 메시지 발행/소비
     *   <li>external_api: 외부 API 호출
     *   <li>crawl_http: 크롤링 HTTP 요청
     * </ul>
     */
    String service();

    /**
     * 작업 유형 (선택적)
     *
     * <p>서비스별 세부 작업을 지정합니다.
     *
     * <ul>
     *   <li>redis: get, set, delete, lock 등
     *   <li>db: select, insert, update, delete 등
     *   <li>sqs: publish, consume 등
     *   <li>external_api: 호출 대상 (fileflow, session 등)
     * </ul>
     */
    String operation() default "";
}
