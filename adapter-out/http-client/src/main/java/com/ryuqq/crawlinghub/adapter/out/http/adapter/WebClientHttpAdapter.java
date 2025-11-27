package com.ryuqq.crawlinghub.adapter.out.http.adapter;

import com.ryuqq.crawlinghub.application.crawl.dto.HttpRequest;
import com.ryuqq.crawlinghub.application.crawl.dto.HttpResponse;
import com.ryuqq.crawlinghub.application.crawl.port.out.client.HttpClientPort;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * WebClient 기반 HTTP 클라이언트 어댑터
 *
 * <p>크롤링을 위한 HTTP 요청을 WebClient로 처리하는 아웃바운드 어댑터.
 *
 * <p><strong>특징</strong>:
 *
 * <ul>
 *   <li>동기식 블로킹 호출 (block())
 *   <li>User-Agent, Cookie 헤더 지원
 *   <li>HTTP 에러 응답도 HttpResponse로 변환 (예외 발생 X)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class WebClientHttpAdapter implements HttpClientPort {

    private static final Logger log = LoggerFactory.getLogger(WebClientHttpAdapter.class);

    private final WebClient webClient;

    public WebClientHttpAdapter(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public HttpResponse get(HttpRequest request) {
        log.debug("HTTP GET 요청: url={}", request.getUrl());

        try {
            return webClient
                    .get()
                    .uri(request.getUrl())
                    .headers(headers -> request.getHeaders().forEach(headers::set))
                    .exchangeToMono(this::toHttpResponse)
                    .block();
        } catch (WebClientResponseException e) {
            log.warn("HTTP GET 에러 응답: url={}, status={}", request.getUrl(), e.getStatusCode());
            return HttpResponse.of(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("HTTP GET 요청 실패: url={}, error={}", request.getUrl(), e.getMessage());
            return HttpResponse.of(0, "Connection failed: " + e.getMessage());
        }
    }

    @Override
    public HttpResponse post(HttpRequest request) {
        log.debug("HTTP POST 요청: url={}", request.getUrl());

        try {
            return webClient
                    .post()
                    .uri(request.getUrl())
                    .headers(headers -> request.getHeaders().forEach(headers::set))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request.getBody() != null ? request.getBody() : "")
                    .exchangeToMono(this::toHttpResponse)
                    .block();
        } catch (WebClientResponseException e) {
            log.warn("HTTP POST 에러 응답: url={}, status={}", request.getUrl(), e.getStatusCode());
            return HttpResponse.of(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("HTTP POST 요청 실패: url={}, error={}", request.getUrl(), e.getMessage());
            return HttpResponse.of(0, "Connection failed: " + e.getMessage());
        }
    }

    /**
     * ClientResponse를 HttpResponse로 변환
     *
     * <p>성공/실패 모두 HttpResponse로 변환하여 반환합니다. 예외를 발생시키지 않고 상태 코드로 판단할 수 있게 합니다.
     *
     * @param clientResponse WebClient 응답
     * @return HttpResponse Mono
     */
    private reactor.core.publisher.Mono<HttpResponse> toHttpResponse(
            ClientResponse clientResponse) {
        HttpStatusCode statusCode = clientResponse.statusCode();
        Map<String, String> headers = extractHeaders(clientResponse);

        return clientResponse
                .bodyToMono(String.class)
                .defaultIfEmpty("")
                .map(body -> HttpResponse.of(statusCode.value(), body, headers));
    }

    /**
     * ClientResponse에서 헤더 추출
     *
     * @param clientResponse WebClient 응답
     * @return 헤더 맵
     */
    private Map<String, String> extractHeaders(ClientResponse clientResponse) {
        Map<String, String> headers = new HashMap<>();
        clientResponse
                .headers()
                .asHttpHeaders()
                .forEach((key, values) -> headers.put(key, String.join(", ", values)));
        return headers;
    }
}
