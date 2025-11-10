package com.ryuqq.crawlinghub.application.task.dto.response;

/**
 * 크롤링 결과 (성공/실패 + 데이터)
 *
 * <p>CrawlerFacade의 크롤링 결과를 감싸는 Wrapper 클래스
 *
 * <p>사용 예시:
 * <pre>
 * CrawlResult&lt;MiniShopOutput&gt; result = crawlerFacade.crawl(task, MiniShopOutput.class);
 * if (result.isSuccess()) {
 *     MiniShopOutput output = result.getData();
 *     // 성공 처리
 * } else {
 *     String error = result.getError();
 *     Integer statusCode = result.getStatusCode();
 *     // 실패 처리
 * }
 * </pre>
 *
 * @param <T> Output 타입
 * @author ryu-qqq
 * @since 2025-11-06
 */
public class CrawlResult<T> {
    private final boolean success;
    private final T data;
    private final String error;
    private final Integer statusCode;

    private CrawlResult(boolean success, T data, String error, Integer statusCode) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.statusCode = statusCode;
    }

    /**
     * 성공 결과 생성
     *
     * @param data 크롤링 데이터
     * @param <T> Output 타입
     * @return 성공 결과
     */
    public static <T> CrawlResult<T> success(T data) {
        return new CrawlResult<>(true, data, null, null);
    }

    /**
     * 실패 결과 생성
     *
     * @param error 에러 메시지
     * @param statusCode HTTP 상태 코드
     * @param <T> Output 타입
     * @return 실패 결과
     */
    public static <T> CrawlResult<T> failure(String error, Integer statusCode) {
        return new CrawlResult<>(false, null, error, statusCode);
    }

    /**
     * 성공 여부
     *
     * @return true: 성공, false: 실패
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 크롤링 데이터 가져오기
     *
     * @return 크롤링 데이터
     * @throws IllegalStateException 실패 상태에서 호출 시
     */
    public T getData() {
        if (!success) {
            throw new IllegalStateException("실패한 결과에서 데이터를 가져올 수 없습니다");
        }
        return data;
    }

    /**
     * 에러 메시지
     *
     * @return 에러 메시지 (성공 시 null)
     */
    public String getError() {
        return error;
    }

    /**
     * HTTP 상태 코드
     *
     * @return 상태 코드 (성공 시 null)
     */
    public Integer getStatusCode() {
        return statusCode;
    }
}
