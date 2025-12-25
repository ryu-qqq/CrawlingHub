package com.ryuqq.crawlinghub.application.common.tracing;

import java.util.Optional;

/**
 * TracingContext ThreadLocal 홀더
 *
 * <p>현재 스레드의 추적 컨텍스트를 관리합니다. Filter에서 설정하고 요청 처리 완료 후 정리합니다.
 *
 * <h3>사용 예시</h3>
 *
 * <pre>{@code
 * // Filter에서 설정
 * TracingContextHolder.setContext(context);
 *
 * // Service에서 사용
 * TracingContext ctx = TracingContextHolder.getContext()
 *     .orElseGet(() -> TracingContext.system("crawlinghub"));
 *
 * // 외부 호출 시 헤더 전파
 * Map<String, String> headers = ctx.toHeaders();
 *
 * // 요청 완료 후 정리
 * TracingContextHolder.clear();
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public final class TracingContextHolder {

    private static final ThreadLocal<TracingContext> CONTEXT = new ThreadLocal<>();

    private TracingContextHolder() {}

    /**
     * 현재 스레드의 TracingContext 설정
     *
     * @param context 추적 컨텍스트
     */
    public static void setContext(TracingContext context) {
        CONTEXT.set(context);
    }

    /**
     * 현재 스레드의 TracingContext 조회
     *
     * @return TracingContext Optional
     */
    public static Optional<TracingContext> getContext() {
        return Optional.ofNullable(CONTEXT.get());
    }

    /**
     * 현재 스레드의 TracingContext 조회 (없으면 시스템 컨텍스트 생성)
     *
     * @param serviceName 서비스 이름 (컨텍스트 없을 때 사용)
     * @return TracingContext
     */
    public static TracingContext getContextOrSystem(String serviceName) {
        return getContext().orElseGet(() -> TracingContext.system(serviceName));
    }

    /** 현재 스레드의 TracingContext 정리 */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 현재 traceId 조회
     *
     * @return traceId Optional
     */
    public static Optional<String> getTraceId() {
        return getContext().map(TracingContext::getTraceId);
    }

    /**
     * 현재 userId 조회
     *
     * @return userId Optional
     */
    public static Optional<String> getUserId() {
        return getContext().map(TracingContext::getUserId);
    }
}
