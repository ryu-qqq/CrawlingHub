package com.ryuqq.crawlinghub.application.port.out.query;

import com.ryuqq.crawlinghub.application.common.pagination.PageResult;
import com.ryuqq.crawlinghub.domain.eventbridge.aggregate.CrawlingScheduler;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerId;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import java.util.List;
import java.util.Optional;

/** Scheduler Query Port */
public interface SchedulerQueryPort {

    Optional<CrawlingScheduler> findById(SchedulerId schedulerId);

    Optional<CrawlingScheduler> findBySellerIdAndSchedulerName(
            SellerId sellerId, String schedulerName);

    List<CrawlingScheduler> findBySellerIdAndStatus(SellerId sellerId, SchedulerStatus status);

    PageResult<CrawlingScheduler> findAllBySellerIdAndStatus(
            SellerId sellerId, SchedulerStatus status, int page, int size);

    int countActiveSchedulersBySellerId(SellerId sellerId);

    int countTotalSchedulersBySellerId(SellerId sellerId);
}
