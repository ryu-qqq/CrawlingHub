package com.ryuqq.crawlinghub.application.common.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * UserAgent 메트릭 수집 어노테이션
 *
 * <p>UserAgent Pool 관련 메트릭을 자동으로 수집합니다.
 *
 * <h3>수집 메트릭</h3>
 *
 * <ul>
 *   <li>useragent.consume.total: UserAgent 소비 횟수
 *   <li>useragent.consume.latency: 소비 지연 시간
 *   <li>useragent.success.total: 성공 횟수
 *   <li>useragent.failure.total: 실패 횟수 (by HTTP status)
 *   <li>useragent.rate_limited.total: 429 응답 횟수
 *   <li>useragent.suspended.total: SUSPENDED 전환 횟수
 *   <li>useragent.recovered.total: 복구 횟수
 *   <li>useragent.circuit_breaker.total: Circuit Breaker 발동 횟수
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserAgentMetric {

    /**
     * 작업 유형
     *
     * <ul>
     *   <li>consume: UserAgent 토큰 소비
     *   <li>record_result: 결과 기록 (성공/실패/429)
     *   <li>recover: SUSPENDED UserAgent 복구
     * </ul>
     */
    String operation();
}
