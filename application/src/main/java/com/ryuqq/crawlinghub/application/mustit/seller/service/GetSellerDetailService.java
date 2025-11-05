package com.ryuqq.crawlinghub.application.mustit.seller.service;

import com.ryuqq.crawlinghub.application.common.dto.PageResponse;
import com.ryuqq.crawlinghub.application.crawl.schedule.port.out.LoadScheduleHistoryPort;
import com.ryuqq.crawlinghub.application.crawl.schedule.port.out.LoadSchedulePort;
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
import com.ryuqq.crawlinghub.domain.crawl.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.crawl.schedule.CrawlScheduleId;
import com.ryuqq.crawlinghub.domain.crawl.schedule.history.CrawlScheduleHistory;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.mustit.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.mustit.seller.history.ProductCountHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 셀러 상세 조회 UseCase 구현체
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
 * <p>⚠️ Transaction 경계:
 * <ul>
 *   <li>읽기 전용 트랜잭션 (readOnly = true)</li>
 *   <li>순수 조회만 수행하므로 트랜잭션 안전</li>
 *   <li>외부 API 호출 없음</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Service
public class GetSellerDetailService implements GetSellerDetailUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetSellerDetailService.class);

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    private final LoadSellerPort loadSellerPort;
    private final LoadSellerStatsPort loadSellerStatsPort;
    private final LoadProductCountHistoryPort loadHistoryPort;
    private final LoadSchedulePort loadSchedulePort;
    private final LoadScheduleHistoryPort loadScheduleHistoryPort;
    private final SellerAssembler sellerAssembler;

    /**
     * 생성자
     *
     * @param loadSellerPort 셀러 조회 Port
     * @param loadSellerStatsPort 셀러 통계 조회 Port
     * @param loadHistoryPort 상품 수 변경 이력 조회 Port
     * @param loadSchedulePort 스케줄 조회 Port
     * @param loadScheduleHistoryPort 스케줄 히스토리 조회 Port
     * @param sellerAssembler 셀러 Assembler
     */
    public GetSellerDetailService(
        LoadSellerPort loadSellerPort,
        LoadSellerStatsPort loadSellerStatsPort,
        LoadProductCountHistoryPort loadHistoryPort,
        LoadSchedulePort loadSchedulePort,
        LoadScheduleHistoryPort loadScheduleHistoryPort,
        SellerAssembler sellerAssembler
    ) {
        this.loadSellerPort = loadSellerPort;
        this.loadSellerStatsPort = loadSellerStatsPort;
        this.loadHistoryPort = loadHistoryPort;
        this.loadSchedulePort = loadSchedulePort;
        this.loadScheduleHistoryPort = loadScheduleHistoryPort;
        this.sellerAssembler = sellerAssembler;
    }

    /**
     * 셀러 상세 조회 (기존 메서드 - 호환성 유지)
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
        log.debug("Getting seller detail: sellerId={}", query.sellerId());

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
    @Transactional(readOnly = true)
    public SellerDetailResponse getDetail(Long sellerId) {
        log.info("Getting seller detail (extended): sellerId={}", sellerId);

        // 1. 셀러 기본 정보 조회
        MustitSellerId mustitSellerId = MustitSellerId.of(sellerId);
        MustitSeller seller = loadSellerPort.findById(mustitSellerId)
            .orElseThrow(() -> new SellerNotFoundException(sellerId));

        // 2. 총 상품 수 조회
        Integer totalProductCount = seller.getTotalProductCount();

        // 3. 통계 조회
        LoadSellerStatsPort.SellerStats stats = loadSellerStatsPort.getSellerStats(mustitSellerId);

        // 4. 상품 수 변경 이력 조회 (PageResponse) ⭐
        PageResponse<ProductCountHistoryResponse> historyPage = getProductCountHistories(
            mustitSellerId,
            DEFAULT_PAGE,
            DEFAULT_SIZE
        );

        // 5. 크롤링 스케줄 정보 조회 ⭐
        Optional<ScheduleInfoResponse> scheduleInfo = getScheduleInfo(mustitSellerId);

        // 6. 크롤링 실행 이력 조회 (PageResponse) ⭐
        PageResponse<ScheduleHistoryResponse> scheduleHistoryPage = getScheduleHistories(
            mustitSellerId,
            DEFAULT_PAGE,
            DEFAULT_SIZE
        );

        // 7. Assembler를 통한 DTO 변환
        return sellerAssembler.toSellerDetailResponse(
            seller,
            totalProductCount,
            historyPage,
            scheduleInfo.orElse(null),
            scheduleHistoryPage,
            stats
        );
    }

    /**
     * 상품 수 변경 이력 조회 (PageResponse) ⭐
     *
     * <p>Application Layer에서 페이징 처리합니다.
     *
     * @param sellerId 셀러 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 상품 수 변경 이력 (PageResponse)
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
     * <p>셀러의 활성 스케줄을 조회합니다.
     *
     * @param sellerId 셀러 ID
     * @return 스케줄 정보 (없으면 Optional.empty())
     */
    private Optional<ScheduleInfoResponse> getScheduleInfo(MustitSellerId sellerId) {
        return loadSchedulePort.findActiveBySellerId(sellerId)
            .map(sellerAssembler::toScheduleInfoResponse);
    }

    /**
     * 크롤링 실행 이력 조회 (PageResponse) ⭐
     *
     * <p>셀러의 활성 스케줄의 실행 이력을 조회합니다.
     * 활성 스케줄이 없으면 빈 PageResponse를 반환합니다.
     *
     * @param sellerId 셀러 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 크롤링 실행 이력 (PageResponse)
     */
    private PageResponse<ScheduleHistoryResponse> getScheduleHistories(
        MustitSellerId sellerId,
        int page,
        int size
    ) {
        // 1. 활성 스케줄 조회
        Optional<CrawlSchedule> activeSchedule = loadSchedulePort.findActiveBySellerId(sellerId);
        if (activeSchedule.isEmpty()) {
            log.debug("No active schedule found for seller: sellerId={}", sellerId.value());
            return PageResponse.empty(page, size);
        }

        // 2. 스케줄의 히스토리 조회
        CrawlScheduleId scheduleId = CrawlScheduleId.of(activeSchedule.get().getIdValue());
        List<CrawlScheduleHistory> histories = loadScheduleHistoryPort.findByScheduleId(scheduleId);

        // 3. Application Layer에서 페이징 처리
        int totalElements = histories.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        int end = Math.min(start + size, totalElements);

        List<CrawlScheduleHistory> pagedHistories = histories.subList(
            Math.min(start, totalElements),
            end
        );

        // 4. Assembler를 통한 변환
        List<ScheduleHistoryResponse> responses = pagedHistories.stream()
            .map(sellerAssembler::toScheduleHistoryResponse)
            .toList();

        return PageResponse.of(
            responses,
            page,
            size,
            totalElements,
            totalPages,
            page == 0,
            page >= totalPages - 1
        );
    }
}
