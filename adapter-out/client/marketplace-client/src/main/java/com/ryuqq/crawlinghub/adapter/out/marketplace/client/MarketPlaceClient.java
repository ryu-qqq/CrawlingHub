package com.ryuqq.crawlinghub.adapter.out.marketplace.client;

import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.CreateProductRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.response.CreateProductResponse;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.response.MarketPlaceApiResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * 외부몰 HTTP 클라이언트
 *
 * <p>외부몰 상품 서버 API를 호출합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class MarketPlaceClient {

    private final WebClient webClient;

    public MarketPlaceClient(WebClient marketPlaceWebClient) {
        this.webClient = marketPlaceWebClient;
    }

    /**
     * 상품 등록 API 호출
     *
     * @param request 등록 요청
     * @return 등록 응답
     * @throws MarketPlaceClientException API 호출 실패 시
     */
    public CreateProductResponse createProduct(CreateProductRequest request) {
        try {
            MarketPlaceApiResponse<CreateProductResponse> response =
                    webClient
                            .post()
                            .uri("/api/v1/products")
                            .bodyValue(request)
                            .retrieve()
                            .bodyToMono(
                                    new ParameterizedTypeReference<
                                            MarketPlaceApiResponse<CreateProductResponse>>() {})
                            .block();

            if (response == null || response.data() == null) {
                throw new MarketPlaceClientException("상품 등록 응답이 비어있습니다");
            }

            return response.data();
        } catch (WebClientResponseException e) {
            throw new MarketPlaceClientException(
                    String.format(
                            "상품 등록 API 호출 실패: status=%d, body=%s",
                            e.getStatusCode().value(), e.getResponseBodyAsString()),
                    e);
        }
    }
}
