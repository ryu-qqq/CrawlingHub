package com.ryuqq.crawlinghub.application.mustit.seller.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ryuqq.crawlinghub.application.common.dto.PageResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.ProductCountHistoryResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.ScheduleHistoryResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.ScheduleInfoResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.mustit.seller.port.in.GetSellerDetailUseCase;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.LoadProductCountHistoryPort;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.LoadSellerPort;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.LoadSellerStatsPort;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.mustit.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.mustit.seller.history.ProductCountHistory;

/**
 * GetSellerDetailService - 셀러 상세 조회 UseCase 구현
 *
 * <p><strong>확장된 기능 (v2) ⭐</strong></p>
 * <ul>
 *   <li>기본 셀러 정보</li>
 *   <li>총 상품 수</li>
 *   <li>🆕 상품 수 변경 이력 (PageResponse)</li>
 *   <li>🆕 크롤링 스케줄 정보</li>
 *   <li>🆕 크롤링 실행 이력 (PageResponse)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Service
public class GetSellerDetailService implements GetSellerDetailUseCase {

    private final LoadSellerPort loadSellerPort;
    private final LoadSellerStatsPort loadSellerStatsPort;
    private final LoadProductCountHistoryPort loadHistoryPort; // 추가 ⭐
    // TODO: 스케줄 관련 Port는 worktree에 없으므로 임시로 주석 처리
    // private final LoadSchedulePort loadSchedulePort; // 추가 ⭐
    // private final LoadScheduleHistoryPort loadScheduleHistoryPort; // 추가 ⭐
    private final SellerAssembler sellerAssembler;

    public GetSellerDetailService(
        LoadSellerPort loadSellerPort,
        LoadSellerStatsPort loadSellerStatsPort,
        LoadProductCountHistoryPort loadHistoryPort,
        // TODO: 스케줄 관련 Port는 worktree에 없으므로 임시로 주석 처리
        // LoadSchedulePort loadSchedulePort,
        // LoadScheduleHistoryPort loadScheduleHistoryPort,
        SellerAssembler sellerAssembler
    ) {
        this.loadSellerPort = loadSellerPort;
        this.loadSellerStatsPort = loadSellerStatsPort;
        this.loadHistoryPort = loadHistoryPort;
        // this.loadSchedulePort = loadSchedulePort;
        // this.loadScheduleHistoryPort = loadScheduleHistoryPort;
        this.sellerAssembler = sellerAssembler;
    }

    /**
     * 셀러 상세 조회 (기존 메서드)
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

        // 3. 응답 조합 (기존 방식 - 호환성 유지)
        return SellerAssembler.toDetailResponse(seller, stats);
    }

    /**
     * 셀러 상세 조회 (확장된 메서드) ⭐
     *
     * <p>읽기 전용 트랜잭션에서:
     * 1. 셀러 기본 정보 조회
     * 2. 총 상품 수 조회
     * 3. 상품 수 변경 이력 조회 (PageResponse)
     * 4. 크롤링 스케줄 정보 조회
     * 5. 크롤링 실행 이력 조회 (PageResponse)
     *
     * @param sellerId 셀러 ID
     * @return 셀러 상세 정보 (확장된 정보 포함)
     * @throws SellerNotFoundException 셀러를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public SellerDetailResponse getDetail(Long sellerId) {
        // 1. 셀러 기본 정보 조회
        MustitSellerId mustitSellerId = MustitSellerId.of(sellerId);
        MustitSeller seller = loadSellerPort.findById(mustitSellerId)
            .orElseThrow(() -> new SellerNotFoundException(sellerId));

        // 2. 총 상품 수 조회
        Integer totalProductCount = seller.getTotalProductCount();

        // 3. 상품 수 변경 이력 조회 (PageResponse) ⭐
        PageResponse<ProductCountHistoryResponse> historyPage = getProductCountHistories(
            mustitSellerId,
            0,  // 기본 페이지 0
            10  // 기본 10개
        );

        // 4. 크롤링 스케줄 정보 조회 ⭐
        // TODO: 다음 페이즈에서 스케줄 관련 Port 구현 후 활성화 예정
        Optional<ScheduleInfoResponse> scheduleInfo = Optional.empty();

        // 5. 크롤링 실행 이력 조회 (PageResponse) ⭐
        // TODO: 다음 페이즈에서 스케줄 관련 Port 구현 후 활성화 예정
        PageResponse<ScheduleHistoryResponse> scheduleHistoryPage = PageResponse.empty(0, 10);

        // 6. Assembler를 통한 DTO 변환
        return sellerAssembler.toSellerDetailResponse(
            seller,
            totalProductCount,
            historyPage,
            scheduleInfo.orElse(null),
            scheduleHistoryPage
        );
    }

    /**
     * 상품 수 변경 이력 조회 (PageResponse) ⭐
     */
    private PageResponse<ProductCountHistoryResponse> getProductCountHistories(
        MustitSellerId sellerId,
        int page,
        int size
    ) {
        List<ProductCountHistory> histories = loadHistoryPort.loadHistories(sellerId, page, size);
        long totalElements = loadHistoryPort.countHistories(sellerId);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PageResponse.of(
            histories.stream()
                .map(sellerAssembler::toProductCountHistoryResponse)
                .toList(),
            page,
            size,
            totalElements,
            totalPages,
            page == 0,
            page >= totalPages - 1
        );
    }

    /**
     * 크롤링 스케줄 정보 조회 ⭐
     *
     * <p>다음 페이즈에서 스케줄 관련 Port 구현 후 활성화 예정
     */
    /*
    private Optional<ScheduleInfoResponse> getScheduleInfo(MustitSellerId sellerId) {
        return loadSchedulePort.findActiveBySellerId(sellerId)
            .map(sellerAssembler::toScheduleInfoResponse);
    }
    */

    /**
     * 크롤링 실행 이력 조회 (PageResponse) ⭐
     *
     * <p>다음 페이즈에서 스케줄 관련 Port 구현 후 활성화 예정
     */
    /*
    private PageResponse<ScheduleHistoryResponse> getScheduleHistories(
        CrawlScheduleId scheduleId,
        int page,
        int size
    ) {
        List<CrawlScheduleHistory> histories = loadScheduleHistoryPort.findByScheduleId(scheduleId);
        
        int totalElements = histories.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        
        List<CrawlScheduleHistory> pagedHistories = histories.subList(
            Math.min(start, totalElements),
            end
        );

        return PageResponse.of(
            pagedHistories.stream()
                .map(sellerAssembler::toScheduleHistoryResponse)
                .toList(),
            page,
            size,
            totalElements,
            totalPages,
            page == 0,
            page >= totalPages - 1
        );
    }
    */
}
