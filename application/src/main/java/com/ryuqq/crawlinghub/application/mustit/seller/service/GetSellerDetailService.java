package com.ryuqq.crawlinghub.application.mustit.seller.service;

import com.ryuqq.crawlinghub.application.mustit.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.port.in.GetSellerDetailUseCase;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.LoadSellerPort;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.LoadSellerStatsPort;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.mustit.seller.exception.SellerNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 셀러 상세 조회 UseCase 구현체
 *
 * <p>셀러 기본 정보와 통계 정보를 함께 조회합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Service
public class GetSellerDetailService implements GetSellerDetailUseCase {

    private final LoadSellerPort loadSellerPort;
    private final LoadSellerStatsPort loadSellerStatsPort;

    public GetSellerDetailService(
        LoadSellerPort loadSellerPort,
        LoadSellerStatsPort loadSellerStatsPort
    ) {
        this.loadSellerPort = loadSellerPort;
        this.loadSellerStatsPort = loadSellerStatsPort;
    }

    /**
     * 셀러 상세 조회
     *
     * <p>읽기 전용 트랜잭션에서:
     * 1. 셀러 조회
     * 2. 통계 조회 (스케줄, 태스크 등)
     * 3. 응답 조합
     *
     * @param query 조회할 셀러 ID
     * @return 셀러 상세 정보 (통계 포함)
     * @throws SellerNotFoundException 셀러를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public SellerDetailResponse execute(GetSellerQuery query) {
        // 1. 셀러 조회
        MustitSellerId sellerId = MustitSellerId.of(query.sellerId());
        MustitSeller seller = loadSellerPort.findById(sellerId)
            .orElseThrow(() -> new SellerNotFoundException(query.sellerId()));

        // 2. 통계 조회
        LoadSellerStatsPort.SellerStats stats = loadSellerStatsPort.getSellerStats(sellerId);

        // 3. 응답 조합
        return SellerAssembler.toDetailResponse(seller, stats);
    }
}
