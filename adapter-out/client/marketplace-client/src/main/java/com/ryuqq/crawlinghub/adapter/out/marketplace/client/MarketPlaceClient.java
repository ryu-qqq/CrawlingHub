package com.ryuqq.crawlinghub.adapter.out.marketplace.client;

import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.ReceiveInboundProductRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.UpdateDescriptionRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.UpdateImagesRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.UpdatePriceRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.UpdateProductsRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.response.InboundProductConversionResponse;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.response.MarketPlaceApiResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * MarketPlace HTTP 클라이언트
 *
 * <p>MarketPlace InboundProduct API를 호출합니다.
 */
@Component
public class MarketPlaceClient {

    private static final String INBOUND_PRODUCTS_PATH = "/api/v1/market/internal/inbound/products";

    private final WebClient webClient;

    public MarketPlaceClient(WebClient marketPlaceWebClient) {
        this.webClient = marketPlaceWebClient;
    }

    /**
     * 인바운드 상품 수신 (POST)
     *
     * @param request 수신 요청
     * @return 변환 결과 응답
     */
    public InboundProductConversionResponse receiveInboundProduct(
            ReceiveInboundProductRequest request) {
        try {
            MarketPlaceApiResponse<InboundProductConversionResponse> response =
                    webClient
                            .post()
                            .uri(INBOUND_PRODUCTS_PATH)
                            .bodyValue(request)
                            .retrieve()
                            .bodyToMono(
                                    new ParameterizedTypeReference<
                                            MarketPlaceApiResponse<
                                                    InboundProductConversionResponse>>() {})
                            .block();

            if (response == null || response.data() == null) {
                throw new MarketPlaceClientException("인바운드 상품 수신 응답이 비어있습니다");
            }

            return response.data();
        } catch (WebClientResponseException e) {
            throw new MarketPlaceClientException(
                    String.format(
                            "인바운드 상품 수신 API 호출 실패: status=%d, body=%s",
                            e.getStatusCode().value(), e.getResponseBodyAsString()),
                    e);
        }
    }

    /**
     * 가격 수정 (PATCH)
     *
     * @param inboundSourceId 인바운드 소스 ID
     * @param externalProductCode 외부 상품 코드
     * @param request 가격 수정 요청
     */
    public void updatePrice(
            long inboundSourceId, String externalProductCode, UpdatePriceRequest request) {
        try {
            webClient
                    .patch()
                    .uri(
                            INBOUND_PRODUCTS_PATH
                                    + "/{inboundSourceId}/{externalProductCode}/price",
                            inboundSourceId,
                            externalProductCode)
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            throw new MarketPlaceClientException(
                    String.format(
                            "가격 수정 API 호출 실패: status=%d, body=%s",
                            e.getStatusCode().value(), e.getResponseBodyAsString()),
                    e);
        }
    }

    /**
     * 이미지 수정 (PATCH)
     *
     * @param inboundSourceId 인바운드 소스 ID
     * @param externalProductCode 외부 상품 코드
     * @param request 이미지 수정 요청
     */
    public void updateImages(
            long inboundSourceId, String externalProductCode, UpdateImagesRequest request) {
        try {
            webClient
                    .patch()
                    .uri(
                            INBOUND_PRODUCTS_PATH
                                    + "/{inboundSourceId}/{externalProductCode}/images",
                            inboundSourceId,
                            externalProductCode)
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            throw new MarketPlaceClientException(
                    String.format(
                            "이미지 수정 API 호출 실패: status=%d, body=%s",
                            e.getStatusCode().value(), e.getResponseBodyAsString()),
                    e);
        }
    }

    /**
     * 상세설명 수정 (PATCH)
     *
     * @param inboundSourceId 인바운드 소스 ID
     * @param externalProductCode 외부 상품 코드
     * @param request 상세설명 수정 요청
     */
    public void updateDescription(
            long inboundSourceId, String externalProductCode, UpdateDescriptionRequest request) {
        try {
            webClient
                    .patch()
                    .uri(
                            INBOUND_PRODUCTS_PATH
                                    + "/{inboundSourceId}/{externalProductCode}/description",
                            inboundSourceId,
                            externalProductCode)
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            throw new MarketPlaceClientException(
                    String.format(
                            "상세설명 수정 API 호출 실패: status=%d, body=%s",
                            e.getStatusCode().value(), e.getResponseBodyAsString()),
                    e);
        }
    }

    /**
     * 상품 + 옵션 일괄 수정 (PATCH)
     *
     * @param inboundSourceId 인바운드 소스 ID
     * @param externalProductCode 외부 상품 코드
     * @param request 상품/옵션 수정 요청
     */
    public void updateProducts(
            long inboundSourceId, String externalProductCode, UpdateProductsRequest request) {
        try {
            webClient
                    .patch()
                    .uri(
                            INBOUND_PRODUCTS_PATH
                                    + "/{inboundSourceId}/{externalProductCode}/products",
                            inboundSourceId,
                            externalProductCode)
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            throw new MarketPlaceClientException(
                    String.format(
                            "상품/옵션 수정 API 호출 실패: status=%d, body=%s",
                            e.getStatusCode().value(), e.getResponseBodyAsString()),
                    e);
        }
    }
}
