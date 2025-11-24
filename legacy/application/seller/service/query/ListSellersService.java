package com.ryuqq.crawlinghub.application.seller.service.query;

import com.ryuqq.crawlinghub.application.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.seller.dto.query.ListSellersQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.application.seller.port.in.query.ListSellersUseCase;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryCriteria;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** 셀러 목록 조회 UseCase 구현체. */
@Service
public class ListSellersService implements ListSellersUseCase {

    private final SellerQueryPort sellerQueryPort;
    private final SchedulerQueryPort schedulerQueryPort;
    private final SellerAssembler sellerAssembler;

    public ListSellersService(
            SellerQueryPort sellerQueryPort,
            SchedulerQueryPort schedulerQueryPort,
            SellerAssembler sellerAssembler) {
        this.sellerQueryPort = sellerQueryPort;
        this.schedulerQueryPort = schedulerQueryPort;
        this.sellerAssembler = sellerAssembler;
    }

    @Override
    public PageResponse<SellerSummaryResponse> listSellers(ListSellersQuery query) {
        SellerQueryCriteria criteria = toCriteria(query);
        List<Seller> sellers = sellerQueryPort.findByCriteria(criteria);
        long totalElements = sellerQueryPort.countByCriteria(criteria);

        // N+1 문제: 각 Seller마다 스케줄러 카운트를 조회합니다.
        // Persistence Adapter에서 Seller와 Scheduler를 JOIN하여 한 번에 조회하도록 최적화해야 합니다.
        List<SellerSummaryResponse> content =
                sellers.stream()
                        .map(
                                seller -> {
                                    int totalSchedulerCount =
                                            schedulerQueryPort.countTotalSchedulersBySellerId(
                                                    seller.getSellerId().value());
                                    return sellerAssembler.toSellerSummaryResponse(
                                            seller, totalSchedulerCount);
                                })
                        .collect(Collectors.toList());

        int totalPages = calculateTotalPages(totalElements, query.size());
        boolean isFirst = query.page() == 0;
        boolean isLast = query.page() >= totalPages - 1;

        return PageResponse.of(
                content, query.page(), query.size(), totalElements, totalPages, isFirst, isLast);
    }

    private SellerQueryCriteria toCriteria(ListSellersQuery query) {
        return new SellerQueryCriteria(query.status(), null, null);
    }

    /**
     * 전체 페이지 수를 계산합니다.
     *
     * @param totalElements 전체 요소 수
     * @param size 페이지 크기
     * @return 전체 페이지 수 (0 이상)
     */
    private int calculateTotalPages(long totalElements, int size) {
        if (totalElements == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / size);
    }
}
