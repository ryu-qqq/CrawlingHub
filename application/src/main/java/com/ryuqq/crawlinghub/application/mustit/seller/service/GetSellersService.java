package com.ryuqq.crawlinghub.application.mustit.seller.service;

import com.ryuqq.crawlinghub.application.mustit.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.query.GetSellersQuery;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.SellerListResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.port.in.GetSellersUseCase;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.LoadSellerPort;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 셀러 목록 조회 UseCase 구현체
 *
 * <p>페이징 처리된 셀러 목록을 조회합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-02
 */
@Service
public class GetSellersService implements GetSellersUseCase {

    private final LoadSellerPort loadSellerPort;

    public GetSellersService(LoadSellerPort loadSellerPort) {
        this.loadSellerPort = loadSellerPort;
    }

    /**
     * 셀러 목록 조회
     *
     * <p>읽기 전용 트랜잭션에서:
     * 1. 전체 개수 조회
     * 2. 셀러 목록 조회 (페이징)
     * 3. 응답 조합
     *
     * @param query 조회 조건 (페이징, 필터)
     * @return 셀러 목록 응답 (페이징 정보 포함)
     */
    @Override
    @Transactional(readOnly = true)
    public SellerListResponse getSellers(GetSellersQuery query) {
        // 1. 전체 개수 조회
        long totalElements = loadSellerPort.countAll(query.status());

        // 2. 빈 결과 처리
        if (totalElements == 0) {
            return SellerListResponse.empty(query.page(), query.size());
        }

        // 3. 셀러 목록 조회 (offset, limit 계산)
        int offset = query.page() * query.size();
        List<MustitSeller> sellers = loadSellerPort.findAll(
            query.status(),
            offset,
            query.size()
        );

        // 4. Domain → DTO 변환
        List<SellerResponse> sellerResponses = sellers.stream()
            .map(SellerAssembler::toResponse)
            .toList();

        // 5. 응답 조합
        return SellerListResponse.of(
            sellerResponses,
            totalElements,
            query.page(),
            query.size()
        );
    }
}
