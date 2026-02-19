package com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.mapper;

import static com.ryuqq.crawlinghub.adapter.in.rest.common.util.DateTimeFormatUtils.format;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.query.SearchProductImageOutboxApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.query.SearchProductSyncOutboxApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.response.ProductImageOutboxApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.response.ProductSyncOutboxApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchProductImageOutboxQuery;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchProductSyncOutboxQuery;
import com.ryuqq.crawlinghub.application.product.dto.response.ProductImageOutboxWithImageResponse;
import com.ryuqq.crawlinghub.application.product.dto.response.ProductSyncOutboxResponse;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.util.List;
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
        List<ProductOutboxStatus> statuses = parseStatuses(request.statuses());
        return new SearchProductSyncOutboxQuery(
                request.crawledProductId(),
                request.sellerId(),
                request.itemNos(),
                statuses,
                request.createdFrom(),
                request.createdTo(),
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
        List<ProductOutboxStatus> statuses = parseStatuses(request.statuses());
        return new SearchProductImageOutboxQuery(
                request.crawledProductImageId(),
                request.crawledProductId(),
                statuses,
                request.createdFrom(),
                request.createdTo(),
                request.page(),
                request.size());
    }

    /**
     * SyncOutbox PageResponse를 PageApiResponse로 변환
     *
     * @param pageResponse Application PageResponse
     * @return API PageApiResponse
     */
    public PageApiResponse<ProductSyncOutboxApiResponse> toSyncPageApiResponse(
            PageResponse<ProductSyncOutboxResponse> pageResponse) {
        List<ProductSyncOutboxApiResponse> content =
                pageResponse.content().stream().map(this::toSyncApiResponse).toList();
        return PageApiResponse.of(
                content, pageResponse.page(), pageResponse.size(), pageResponse.totalElements());
    }

    /**
     * ProductSyncOutboxResponse → ProductSyncOutboxApiResponse 변환
     *
     * @param appResponse Application Response
     * @return API Response
     */
    private ProductSyncOutboxApiResponse toSyncApiResponse(ProductSyncOutboxResponse appResponse) {
        return new ProductSyncOutboxApiResponse(
                appResponse.id(),
                appResponse.crawledProductId(),
                appResponse.sellerId(),
                appResponse.itemNo(),
                appResponse.syncType(),
                appResponse.idempotencyKey(),
                appResponse.externalProductId(),
                appResponse.status() != null ? appResponse.status().name() : null,
                appResponse.retryCount(),
                appResponse.errorMessage(),
                appResponse.canRetry(),
                format(appResponse.createdAt()),
                format(appResponse.updatedAt()),
                format(appResponse.processedAt()));
    }

    /**
     * ImageOutbox PageResponse를 PageApiResponse로 변환 (이미지 정보 포함)
     *
     * @param pageResponse Application PageResponse
     * @return API PageApiResponse
     */
    public PageApiResponse<ProductImageOutboxApiResponse> toImagePageApiResponse(
            PageResponse<ProductImageOutboxWithImageResponse> pageResponse) {
        List<ProductImageOutboxApiResponse> content =
                pageResponse.content().stream().map(this::toImageApiResponse).toList();
        return PageApiResponse.of(
                content, pageResponse.page(), pageResponse.size(), pageResponse.totalElements());
    }

    /**
     * ProductImageOutboxWithImageResponse → ProductImageOutboxApiResponse 변환
     *
     * @param appResponse Application Response
     * @return API Response
     */
    private ProductImageOutboxApiResponse toImageApiResponse(
            ProductImageOutboxWithImageResponse appResponse) {
        return new ProductImageOutboxApiResponse(
                appResponse.id(),
                appResponse.crawledProductImageId(),
                appResponse.idempotencyKey(),
                appResponse.status() != null ? appResponse.status().name() : null,
                appResponse.retryCount(),
                appResponse.errorMessage(),
                appResponse.canRetry(),
                format(appResponse.createdAt()),
                format(appResponse.updatedAt()),
                format(appResponse.processedAt()),
                appResponse.crawledProductId(),
                appResponse.originalUrl(),
                appResponse.s3Url(),
                appResponse.imageType() != null ? appResponse.imageType().name() : null);
    }

    private List<ProductOutboxStatus> parseStatuses(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        return statuses.stream().map(this::parseStatus).filter(s -> s != null).toList();
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
