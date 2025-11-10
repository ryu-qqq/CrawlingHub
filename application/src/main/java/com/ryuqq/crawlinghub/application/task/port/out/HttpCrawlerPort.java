package com.ryuqq.crawlinghub.application.task.port.out;

/**
 * HTTP Crawler Port
 * <p>
 * HTTP API 호출 포트
 * </p>
 * <p>
 * 역할:
 * - 외부 API (MustIt 등) HTTP 호출
 * - User-Agent 및 Token 헤더 포함
 * - 응답 처리 및 에러 핸들링
 * </p>
 * <p>
 * 어댑터 구현:
 * - HttpCrawlerAdapter (RestTemplate 기반)
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public interface HttpCrawlerPort {

    /**
     * HTTP API 호출 실행
     * <p>
     * 호출 흐름:
     * - User-Agent 헤더 설정
     * - Token 헤더 설정 (있는 경우)
     * - GET 요청 실행
     * - 응답을 CrawlResponse로 변환
     * </p>
     * <p>
     * Zero-Tolerance 규칙:
     * - 이 메서드는 Transactional 밖에서 호출되어야 함
     * </p>
     *
     * @param url        호출할 URL
     * @param userAgent  User-Agent 문자열
     * @param token      인증 토큰 (nullable)
     * @return CrawlResponse (성공/실패 + 응답 데이터)
     */
    CrawlResponse execute(String url, String userAgent, String token);

    /**
     * Crawl Response
     * <p>
     * HTTP 응답을 추상화한 DTO
     * </p>
     *
     * @param isSuccess   성공 여부
     * @param statusCode  HTTP 상태 코드
     * @param body        응답 본문 (JSON 문자열)
     * @param error       에러 메시지 (실패 시)
     */
    record CrawlResponse(
        boolean isSuccess,
        Integer statusCode,
        String body,
        String error
    ) {
        /**
         * 성공 응답 생성
         */
        public static CrawlResponse success(int statusCode, String body) {
            return new CrawlResponse(true, statusCode, body, null);
        }

        /**
         * 실패 응답 생성
         */
        public static CrawlResponse failure(int statusCode, String error) {
            return new CrawlResponse(false, statusCode, null, error);
        }
    }
}
