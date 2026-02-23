package com.ryuqq.crawlinghub.application.product.manager;

import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledRawQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import com.ryuqq.crawlinghub.domain.product.vo.RawDataStatus;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawledRaw 조회 관리자
 *
 * <p>CrawledRawQueryPort를 래핑하여 조회 로직을 제공합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledRawReadManager {

    private final CrawledRawQueryPort crawledRawQueryPort;

    public CrawledRawReadManager(CrawledRawQueryPort crawledRawQueryPort) {
        this.crawledRawQueryPort = crawledRawQueryPort;
    }

    /**
     * PENDING 상태의 특정 타입 CrawledRaw 조회
     *
     * @param crawlType 크롤링 타입
     * @param limit 최대 조회 건수
     * @return CrawledRaw 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledRaw> findPendingByType(CrawlType crawlType, int limit) {
        return crawledRawQueryPort.findByStatusAndType(RawDataStatus.PENDING, crawlType, limit);
    }
}
