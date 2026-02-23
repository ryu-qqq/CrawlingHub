package com.ryuqq.crawlinghub.adapter.out.http.adapter;

import com.ryuqq.crawlinghub.adapter.out.http.mapper.HttpResponseMapper;
import com.ryuqq.crawlinghub.application.common.metric.annotation.OutboundClientMetric;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.dto.HttpRequest;
import com.ryuqq.crawlinghub.application.execution.internal.crawler.dto.HttpResponse;
import com.ryuqq.crawlinghub.application.execution.port.out.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
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
public class WebClientHttpAdapter implements HttpClient {

    private static final Logger log = LoggerFactory.getLogger(WebClientHttpAdapter.class);

    private final WebClient webClient;
    private final HttpResponseMapper mapper;

    public WebClientHttpAdapter(WebClient webClient, HttpResponseMapper mapper) {
        this.webClient = webClient;
        this.mapper = mapper;
    }

    @OutboundClientMetric(system = "http_crawl", operation = "get")
    @Override
    public HttpResponse get(HttpRequest request) {
        log.debug("HTTP GET 요청: url={}", request.url());

        try {
            return webClient
                    .get()
                    .uri(request.url())
                    .headers(headers -> request.headers().forEach(headers::set))
                    .exchangeToMono(mapper::toHttpResponse)
                    .block();
        } catch (WebClientResponseException e) {
            log.warn("HTTP GET 에러 응답: url={}, status={}", request.url(), e.getStatusCode());
            return mapper.fromException(e);
        } catch (Exception e) {
            log.error("HTTP GET 요청 실패: url={}, error={}", request.url(), e.getMessage());
            return mapper.fromConnectionFailure(e);
        }
    }

    @OutboundClientMetric(system = "http_crawl", operation = "post")
    @Override
    public HttpResponse post(HttpRequest request) {
        log.debug("HTTP POST 요청: url={}", request.url());

        try {
            return webClient
                    .post()
                    .uri(request.url())
                    .headers(headers -> request.headers().forEach(headers::set))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request.body() != null ? request.body() : "")
                    .exchangeToMono(mapper::toHttpResponse)
                    .block();
        } catch (WebClientResponseException e) {
            log.warn("HTTP POST 에러 응답: url={}, status={}", request.url(), e.getStatusCode());
            return mapper.fromException(e);
        } catch (Exception e) {
            log.error("HTTP POST 요청 실패: url={}, error={}", request.url(), e.getMessage());
            return mapper.fromConnectionFailure(e);
        }
    }
}
