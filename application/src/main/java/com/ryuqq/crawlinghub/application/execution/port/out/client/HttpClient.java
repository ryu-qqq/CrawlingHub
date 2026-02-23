package com.ryuqq.crawlinghub.application.execution.port.out.client;

import com.ryuqq.crawlinghub.application.execution.internal.crawler.dto.HttpRequest;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.dto.HttpResponse;

/**
 * HTTP 클라이언트 포트
 *
 * <p>크롤링을 위한 HTTP 요청을 처리하는 아웃바운드 포트. 실제 구현체는 adapter-out 레이어에서 WebClient 등으로 구현.
 *
 * <p><strong>트랜잭션 경계</strong>:
 *
 * <ul>
 *   <li>이 포트를 통한 HTTP 호출은 반드시 @Transactional 외부에서 실행
 *   <li>트랜잭션 내 외부 API 호출 금지 원칙 준수
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface HttpClient {

    /**
     * HTTP GET 요청 실행
     *
     * @param request HTTP 요청 정보
     * @return HTTP 응답
     */
    HttpResponse get(HttpRequest request);

    /**
     * HTTP POST 요청 실행
     *
     * @param request HTTP 요청 정보
     * @return HTTP 응답
     */
    HttpResponse post(HttpRequest request);
}
