package com.ryuqq.crawlinghub.application.crawl.processing.port.out;

/**
 * HTTP 크롤링 Port (외부 API 호출)
 *
 * <p>⚠️ 트랜잭션 경계 주의:
 * <ul>
 *   <li>이 Port는 외부 API 호출이므로 트랜잭션 밖에서 실행해야 합니다</li>
 *   <li>ProcessCrawlTaskUseCase에서 트랜잭션 없이 호출됩니다</li>
 * </ul>
 *
 * <p>HTTP Client Adapter에 의해 구현됩니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface HttpCrawlerPort {

    /**
     * HTTP GET 요청 실행
     *
     * <p>⚠️ 외부 API 호출 - 트랜잭션 밖에서 실행
     *
     * @param url       요청 URL
     * @param userAgent User-Agent 헤더 값
     * @param token     인증 토큰 (nullable)
     * @return API 응답 결과
     */
    CrawlResponse execute(String url, String userAgent, String token);

    /**
     * 크롤링 응답 DTO
     *
     * @param statusCode HTTP 상태 코드
     * @param body       응답 본문 (JSON)
     * @param error      에러 메시지 (성공 시 null)
     */
    record CrawlResponse(
        int statusCode,
        String body,
        String error
    ) {
        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
    }
}
