package com.ryuqq.crawlinghub.adapter.in.scheduler.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 스케줄러 작업 메서드에 적용하는 AOP 어노테이션
 *
 * <p><strong>용도</strong>: TraceId 자동 주입 + 시작/종료/결과 로깅
 *
 * @author development-team
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SchedulerJob {

    /**
     * 스케줄러 작업 이름 (로깅용)
     *
     * @return 작업 이름
     */
    String value();
}
