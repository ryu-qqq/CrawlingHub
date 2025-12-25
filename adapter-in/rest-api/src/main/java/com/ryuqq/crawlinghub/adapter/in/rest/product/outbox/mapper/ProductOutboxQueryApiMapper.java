package com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.query.SearchProductImageOutboxApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.query.SearchProductSyncOutboxApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.response.ProductImageOutboxApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.response.ProductSyncOutboxApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchProductImageOutboxQuery;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchProductSyncOutboxQuery;
import com.ryuqq.crawlinghub.application.product.dto.response.ProductImageOutboxResponse;
import com.ryuqq.crawlinghub.application.product.dto.response.ProductSyncOutboxResponse;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import org.springframework.stereotype.Component;

/**
 * ProductOutbox Query API Mapper
 *
 * <p>REST API 레이어와 Application 레이어 간의 DTO 변환을 담당합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ProductOutboxQueryApiMapper {

    /**
     * SyncOutbox API 요청을 Application Query로 변환
     *
     * @param request API 요청
     * @return Application Query
     */
    public SearchProductSyncOutboxQuery toSyncQuery(SearchProductSyncOutboxApiRequest request) {
        ProductOutboxStatus status = parseStatus(request.status());
        return new SearchProductSyncOutboxQuery(
                request.crawledProductId(),
                request.sellerId(),
                status,
                request.page(),
                request.size());
    }

    /**
     * ImageOutbox API 요청을 Application Query로 변환
     *
     * @param request API 요청
     * @return Application Query
     */
    public SearchProductImageOutboxQuery toImageQuery(SearchProductImageOutboxApiRequest request) {
        ProductOutboxStatus status = parseStatus(request.status());
        return new SearchProductImageOutboxQuery(
                request.crawledProductImageId(), status, request.page(), request.size());
    }

    /**
     * SyncOutbox PageResponse를 PageApiResponse로 변환
     *
     * @param pageResponse Application PageResponse
     * @return API PageApiResponse
     */
    public PageApiResponse<ProductSyncOutboxApiResponse> toSyncPageApiResponse(
            PageResponse<ProductSyncOutboxResponse> pageResponse) {
        return PageApiResponse.from(pageResponse, ProductSyncOutboxApiResponse::from);
    }

    /**
     * ImageOutbox PageResponse를 PageApiResponse로 변환
     *
     * @param pageResponse Application PageResponse
     * @return API PageApiResponse
     */
    public PageApiResponse<ProductImageOutboxApiResponse> toImagePageApiResponse(
            PageResponse<ProductImageOutboxResponse> pageResponse) {
        return PageApiResponse.from(pageResponse, ProductImageOutboxApiResponse::from);
    }

    private ProductOutboxStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return ProductOutboxStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
