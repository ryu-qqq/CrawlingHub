package com.ryuqq.crawlinghub.adapter.out.http.mapper;

import com.ryuqq.crawlinghub.application.execution.internal.crawler.dto.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * HTTP 응답 변환 Mapper
 *
 * <p>WebClient의 ClientResponse를 application 레이어의 HttpResponse로 변환합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class HttpResponseMapper {

    /**
     * ClientResponse → HttpResponse 변환
     *
     * @param clientResponse WebClient 응답
     * @return HttpResponse Mono
     */
    public Mono<HttpResponse> toHttpResponse(ClientResponse clientResponse) {
        HttpStatusCode statusCode = clientResponse.statusCode();
        Map<String, String> headers = extractHeaders(clientResponse);
        return clientResponse
                .bodyToMono(String.class)
                .defaultIfEmpty("")
                .map(body -> HttpResponse.of(statusCode.value(), body, headers));
    }

    /**
     * WebClientResponseException → HttpResponse 변환
     *
     * @param e WebClient 응답 예외
     * @return HttpResponse
     */
    public HttpResponse fromException(WebClientResponseException e) {
        return HttpResponse.of(e.getStatusCode().value(), e.getResponseBodyAsString());
    }

    /**
     * 연결 실패 → HttpResponse 변환
     *
     * @param e 예외
     * @return HttpResponse (statusCode=0)
     */
    public HttpResponse fromConnectionFailure(Exception e) {
        return HttpResponse.of(0, "Connection failed: " + e.getMessage());
    }

    private Map<String, String> extractHeaders(ClientResponse clientResponse) {
        Map<String, String> headers = new HashMap<>();
        clientResponse
                .headers()
                .asHttpHeaders()
                .forEach((key, values) -> headers.put(key, String.join(", ", values)));
        return headers;
    }
}
