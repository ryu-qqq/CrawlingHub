package com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto.SellerSchedulerSummaryDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto.SellerTaskStatisticsDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto.SellerTaskSummaryDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.mapper.SellerCompositeMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.repository.SellerCompositeQueryDslRepository;
import com.ryuqq.crawlinghub.application.seller.dto.composite.SellerDetailResult;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerCompositionQueryPort;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Seller Composite 조회 Adapter
 *
 * <p>SellerCompositionQueryPort 구현체. Repository에서 4개 쿼리를 실행하고 Mapper로 조합합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerCompositionQueryAdapter implements SellerCompositionQueryPort {

    private static final int RECENT_TASKS_LIMIT = 5;

    private final SellerCompositeQueryDslRepository compositeRepository;
    private final SellerCompositeMapper compositeMapper;

    public SellerCompositionQueryAdapter(
            SellerCompositeQueryDslRepository compositeRepository,
            SellerCompositeMapper compositeMapper) {
        this.compositeRepository = compositeRepository;
        this.compositeMapper = compositeMapper;
    }

    @Override
    public Optional<SellerDetailResult> findSellerDetailById(Long sellerId) {
        return compositeRepository
                .fetchSeller(sellerId)
                .map(
                        sellerDto -> {
                            List<SellerSchedulerSummaryDto> schedulers =
                                    compositeRepository.fetchSchedulers(sellerId);
                            List<SellerTaskSummaryDto> tasks =
                                    compositeRepository.fetchRecentTasks(
                                            sellerId, RECENT_TASKS_LIMIT);
                            List<SellerTaskStatisticsDto> stats =
                                    compositeRepository.fetchTaskStatistics(sellerId);
                            return compositeMapper.toResult(sellerDto, schedulers, tasks, stats);
                        });
    }
}
