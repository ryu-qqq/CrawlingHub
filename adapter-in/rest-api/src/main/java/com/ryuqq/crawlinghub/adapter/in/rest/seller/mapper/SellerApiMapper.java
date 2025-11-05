package com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.RegisterSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.RegisterSellerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.UpdateSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.UpdateSellerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.ProductCountHistoryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.ScheduleHistoryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.ScheduleInfoApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerDetailApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.PageResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.command.RegisterMustitSellerCommand;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.command.UpdateMustitSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.ProductCountHistoryResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.ScheduleHistoryResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.ScheduleInfoResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.domain.crawl.schedule.CrawlIntervalType;
import com.ryuqq.crawlinghub.domain.seller.MustitSeller;

import org.springframework.stereotype.Component;

/**
 * SellerApiMapper - Application DTO ↔ REST API DTO 변환
 *
 * <p><strong>PageResponse 변환 로직 추가 ⭐</strong></p>
 * <ul>
 *   <li>Application PageResponse → REST API PageApiResponse</li>
 *   <li>Domain 객체 → API DTO</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class SellerApiMapper {

    /**
     * API Request → Application Command 변환
     *
     * @param request API Request
     * @return Application Command
     */
    public RegisterMustitSellerCommand toCommand(RegisterSellerApiRequest request) {
        if (request == null) {
            return null;
        }

        CrawlIntervalType intervalType = CrawlIntervalType.valueOf(request.intervalType());

        return new RegisterMustitSellerCommand(
                request.sellerId(),
                request.name(),
                intervalType,
                request.intervalValue()
        );
    }

    /**
     * Domain Aggregate → API Response 변환
     *
     * @param seller Domain Aggregate
     * @return API Response
     */
    public RegisterSellerApiResponse toResponse(MustitSeller seller) {
        if (seller == null) {
            return null;
        }

        return new RegisterSellerApiResponse(
                seller.getSellerId(),
                seller.getName(),
                seller.isActive(),
                seller.getCrawlIntervalType().name(),
                seller.getCrawlIntervalValue(),
                seller.getCreatedAt()
        );
    }

    /**
     * Update API Request → Application Command 변환
     *
     * @param sellerId 셀러 ID
     * @param request Update API Request
     * @return Update Application Command
     */
    public UpdateMustitSellerCommand toUpdateCommand(String sellerId, UpdateSellerApiRequest request) {
        if (request == null) {
            return null;
        }

        CrawlIntervalType intervalType = null;
        if (request.intervalType() != null) {
            intervalType = CrawlIntervalType.valueOf(request.intervalType());
        }

        return new UpdateMustitSellerCommand(
                sellerId,
                request.isActive(),
                intervalType,
                request.intervalValue()
        );
    }

    /**
     * Domain Aggregate → Update API Response 변환
     *
     * @param seller Domain Aggregate
     * @return Update API Response
     */
    public UpdateSellerApiResponse toUpdateResponse(MustitSeller seller) {
        if (seller == null) {
            return null;
        }

        return new UpdateSellerApiResponse(
                seller.getSellerId(),
                seller.getName(),
                seller.isActive(),
                seller.getCrawlIntervalType().name(),
                seller.getCrawlIntervalValue(),
                seller.getUpdatedAt()
        );
    }

    /**
     * SellerDetailResponse → SellerDetailApiResponse 변환
     *
     * <p>PageResponse도 함께 변환 ⭐</p>
     *
     * @param response Application Layer SellerDetailResponse
     * @return REST API SellerDetailApiResponse
     */
    public SellerDetailApiResponse toSellerDetailApiResponse(SellerDetailResponse response) {
        if (response == null) {
            return null;
        }

        return new SellerDetailApiResponse(
            response.sellerId(),
            response.sellerCode(),
            response.sellerName(),
            response.status(),
            response.totalProductCount(),
            toPageApiResponse(response.productCountHistories()), // ⭐
            toScheduleInfoApiResponse(response.scheduleInfo()), // ⭐
            toPageApiResponse(response.scheduleHistories()) // ⭐
        );
    }

    /**
     * PageResponse<ProductCountHistoryResponse> → PageApiResponse<ProductCountHistoryApiResponse> 변환 ⭐
     *
     * @param pageResponse Application Layer PageResponse
     * @return REST API PageApiResponse
     */
    public PageApiResponse<ProductCountHistoryApiResponse> toPageApiResponse(
        PageResponse<ProductCountHistoryResponse> pageResponse
    ) {
        if (pageResponse == null) {
            return null;
        }

        return PageApiResponse.of(
            pageResponse.content().stream()
                .map(this::toProductCountHistoryApiResponse)
                .toList(),
            pageResponse.page(),
            pageResponse.size(),
            pageResponse.totalElements(),
            pageResponse.totalPages(),
            pageResponse.first(),
            pageResponse.last()
        );
    }

    /**
     * ProductCountHistoryResponse → ProductCountHistoryApiResponse 변환
     *
     * @param response Application Layer ProductCountHistoryResponse
     * @return REST API ProductCountHistoryApiResponse
     */
    public ProductCountHistoryApiResponse toProductCountHistoryApiResponse(
        ProductCountHistoryResponse response
    ) {
        if (response == null) {
            return null;
        }

        return new ProductCountHistoryApiResponse(
            response.historyId(),
            response.executedDate(),
            response.productCount()
        );
    }

    /**
     * ScheduleInfoResponse → ScheduleInfoApiResponse 변환
     *
     * @param response Application Layer ScheduleInfoResponse
     * @return REST API ScheduleInfoApiResponse
     */
    public ScheduleInfoApiResponse toScheduleInfoApiResponse(ScheduleInfoResponse response) {
        if (response == null) {
            return null;
        }

        return new ScheduleInfoApiResponse(
            response.scheduleId(),
            response.cronExpression(),
            response.status(),
            response.nextExecutionTime(),
            response.createdAt()
        );
    }

    /**
     * PageResponse<ScheduleHistoryResponse> → PageApiResponse<ScheduleHistoryApiResponse> 변환
     *
     * @param pageResponse Application Layer PageResponse
     * @return REST API PageApiResponse
     */
    public PageApiResponse<ScheduleHistoryApiResponse> toPageApiResponse(
        PageResponse<ScheduleHistoryResponse> pageResponse
    ) {
        if (pageResponse == null) {
            return null;
        }

        return PageApiResponse.of(
            pageResponse.content().stream()
                .map(this::toScheduleHistoryApiResponse)
                .toList(),
            pageResponse.page(),
            pageResponse.size(),
            pageResponse.totalElements(),
            pageResponse.totalPages(),
            pageResponse.first(),
            pageResponse.last()
        );
    }

    /**
     * ScheduleHistoryResponse → ScheduleHistoryApiResponse 변환
     *
     * @param response Application Layer ScheduleHistoryResponse
     * @return REST API ScheduleHistoryApiResponse
     */
    public ScheduleHistoryApiResponse toScheduleHistoryApiResponse(ScheduleHistoryResponse response) {
        if (response == null) {
            return null;
        }

        return new ScheduleHistoryApiResponse(
            response.historyId(),
            response.startedAt(),
            response.completedAt(),
            response.status(),
            response.message()
        );
    }
}
