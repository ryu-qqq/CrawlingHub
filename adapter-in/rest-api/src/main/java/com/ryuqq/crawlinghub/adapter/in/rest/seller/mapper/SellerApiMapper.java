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
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerStatusCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.ProductCountHistoryResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.ScheduleHistoryResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.ScheduleInfoResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;

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
    public RegisterSellerCommand toCommand(RegisterSellerApiRequest request) {
        if (request == null) {
            return null;
        }

        // RegisterSellerApiRequest는 sellerId를 sellerCode로 사용
        return new RegisterSellerCommand(
                request.sellerId(), // sellerCode로 사용
                request.name()      // sellerName으로 사용
        );
    }

    /**
     * Domain Aggregate → API Response 변환
     *
     * @param seller Domain Aggregate
     * @return API Response
     */
    public RegisterSellerApiResponse toResponse(SellerResponse seller) {
        if (seller == null) {
            return null;
        }

        return new RegisterSellerApiResponse(
                seller.sellerId().toString(),
                seller.sellerName(),
                seller.status() == com.ryuqq.crawlinghub.domain.seller.SellerStatus.ACTIVE,
                "DAILY", // TODO: intervalType은 현재 구조에 없음, 기본값 사용
                1,        // TODO: intervalValue는 현재 구조에 없음, 기본값 사용
                seller.createdAt()
        );
    }

    /**
     * Update API Request → Application Command 변환
     *
     * @param sellerId 셀러 ID
     * @param request Update API Request
     * @return Update Application Command
     */
    public UpdateSellerStatusCommand toUpdateCommand(Long sellerId, UpdateSellerApiRequest request) {
        if (request == null) {
            return null;
        }

        // UpdateSellerApiRequest의 isActive를 SellerStatus로 변환
        com.ryuqq.crawlinghub.domain.seller.SellerStatus status;
        if (request.isActive() == null) {
            throw new IllegalArgumentException("isActive 필드는 필수입니다");
        }
        status = request.isActive() 
            ? com.ryuqq.crawlinghub.domain.seller.SellerStatus.ACTIVE
            : com.ryuqq.crawlinghub.domain.seller.SellerStatus.PAUSED;

        return new UpdateSellerStatusCommand(
                sellerId,
                status
        );
    }

    /**
     * Domain Aggregate → Update API Response 변환
     *
     * @param seller Domain Aggregate
     * @return Update API Response
     */
    public UpdateSellerApiResponse toUpdateResponse(SellerResponse seller) {
        if (seller == null) {
            return null;
        }

        return new UpdateSellerApiResponse(
                seller.sellerId().toString(),
                seller.sellerName(),
                seller.status() == com.ryuqq.crawlinghub.domain.seller.SellerStatus.ACTIVE,
                "DAILY", // TODO: intervalType은 현재 구조에 없음, 기본값 사용
                1,        // TODO: intervalValue는 현재 구조에 없음, 기본값 사용
                seller.updatedAt()
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
            toProductCountHistoryPageApiResponse(response.productCountHistories()), // ⭐
            toScheduleInfoApiResponse(response.scheduleInfo()), // ⭐
            toScheduleHistoryPageApiResponse(response.scheduleHistories()) // ⭐
        );
    }

    /**
     * PageResponse<ProductCountHistoryResponse> → PageApiResponse<ProductCountHistoryApiResponse> 변환 ⭐
     *
     * @param pageResponse Application Layer PageResponse
     * @return REST API PageApiResponse
     */
    public PageApiResponse<ProductCountHistoryApiResponse> toProductCountHistoryPageApiResponse(
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
    public PageApiResponse<ScheduleHistoryApiResponse> toScheduleHistoryPageApiResponse(
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
