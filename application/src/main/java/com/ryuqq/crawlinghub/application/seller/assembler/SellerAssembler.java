package com.ryuqq.crawlinghub.application.seller.assembler;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.query.SearchSellersQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerQueryCriteria;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Seller Assembler
 *
 * <p>DTO ↔ Domain 변환 전용 컴포넌트
 *
 * <ul>
 *   <li>DTO → Domain: Command를 Domain 객체로 변환
 *   <li>Domain → DTO: Domain을 Response DTO로 변환
 *   <li>비즈니스 로직 없음 (단순 변환만)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerAssembler {

    private final ClockHolder clockHolder;

    public SellerAssembler(ClockHolder clockHolder) {
        this.clockHolder = clockHolder;
    }

    /**
     * RegisterSellerCommand → Seller (신규 생성)
     *
     * @param command 셀러 등록 Command
     * @return 신규 Seller Aggregate
     */
    public Seller toDomain(RegisterSellerCommand command) {
        return Seller.forNew(
                MustItSellerName.of(command.mustItSellerName()),
                SellerName.of(command.sellerName()),
                clockHolder.clock());
    }

    /**
     * UpdateSellerCommand → Seller (비교용 임시 객체)
     *
     * <p>Command의 값들을 Domain 객체로 변환하여 기존 Seller와 비교 가능하게 함
     *
     * <p>주의: 이 객체는 영속화되지 않으며, 비교 목적으로만 사용
     *
     * @param command 셀러 수정 Command
     * @return 비교용 Seller 객체
     */
    public Seller toDomain(UpdateSellerCommand command) {
        MustItSellerName mustItSellerName =
                command.mustItSellerName() != null
                        ? MustItSellerName.of(command.mustItSellerName())
                        : null;
        SellerName sellerName =
                command.sellerName() != null ? SellerName.of(command.sellerName()) : null;
        SellerStatus status =
                command.active() != null
                        ? (command.active() ? SellerStatus.ACTIVE : SellerStatus.INACTIVE)
                        : null;

        // 비교용 임시 객체 (ID만 있으면 됨, 나머지는 비교 대상)
        return Seller.of(
                SellerId.of(command.sellerId()),
                mustItSellerName,
                sellerName,
                status,
                0, // productCount (비교 불필요)
                null, // createdAt (비교 불필요)
                null, // updatedAt (비교 불필요)
                clockHolder.clock());
    }

    /**
     * Seller → SellerResponse
     *
     * @param seller Seller Aggregate
     * @return 전체 상세 응답
     */
    public SellerResponse toResponse(Seller seller) {
        return new SellerResponse(
                seller.getSellerIdValue(),
                seller.getMustItSellerNameValue(),
                seller.getSellerNameValue(),
                seller.isActive(),
                seller.getCreatedAt(),
                seller.getUpdatedAt());
    }

    /**
     * Seller → SellerSummaryResponse
     *
     * @param seller Seller Aggregate
     * @return 목록용 요약 응답
     */
    public SellerSummaryResponse toSummaryResponse(Seller seller) {
        return new SellerSummaryResponse(
                seller.getSellerIdValue(),
                seller.getMustItSellerNameValue(),
                seller.getSellerNameValue(),
                seller.isActive(),
                seller.getCreatedAt());
    }

    /**
     * Seller 목록 → SellerSummaryResponse 목록
     *
     * @param sellers Seller Aggregate 목록
     * @return 요약 응답 목록
     */
    public List<SellerSummaryResponse> toSummaryResponses(List<Seller> sellers) {
        return sellers.stream().map(this::toSummaryResponse).toList();
    }

    /**
     * SearchSellersQuery → SellerQueryCriteria (조회 조건 변환)
     *
     * <p>Application Layer Query DTO를 Domain VO로 변환
     *
     * @param query 셀러 검색 Query
     * @return Domain 조회 조건 객체
     */
    public SellerQueryCriteria toCriteria(SearchSellersQuery query) {
        MustItSellerName mustItSellerName =
                query.mustItSellerName() != null
                        ? MustItSellerName.of(query.mustItSellerName())
                        : null;
        SellerName sellerName =
                query.sellerName() != null ? SellerName.of(query.sellerName()) : null;

        return new SellerQueryCriteria(
                mustItSellerName, sellerName, query.sellerStatus(), query.page(), query.size());
    }

    /**
     * Seller 목록 + 총 개수 → PageResponse<SellerSummaryResponse>
     *
     * <p>페이징 메타데이터를 계산하여 PageResponse로 변환
     *
     * @param sellers Seller 목록
     * @param page 현재 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param totalElements 전체 데이터 개수
     * @return 페이징된 응답
     */
    public PageResponse<SellerSummaryResponse> toPageResponse(
            List<Seller> sellers, int page, int size, long totalElements) {
        List<SellerSummaryResponse> content = toSummaryResponses(sellers);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean first = (page == 0);
        boolean last = (page >= totalPages - 1);

        return PageResponse.of(content, page, size, totalElements, totalPages, first, last);
    }
}
