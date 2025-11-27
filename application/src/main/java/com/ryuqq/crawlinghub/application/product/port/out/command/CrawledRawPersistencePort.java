package com.ryuqq.crawlinghub.application.product.port.out.command;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledRawId;

import java.util.List;

/**
 * CrawledRaw 저장 Port (Port Out - Command)
 *
 * <p>CrawledRawManager에서만 사용됩니다.
 * <p>트랜잭션 경계 내에서 CrawledRaw Aggregate의 저장을 담당합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawledRawPersistencePort {

    /**
     * CrawledRaw 저장 (신규 생성 또는 업데이트)
     *
     * @param crawledRaw 저장할 CrawledRaw
     * @return 저장된 CrawledRaw의 ID
     */
    CrawledRawId persist(CrawledRaw crawledRaw);

    /**
     * CrawledRaw 벌크 저장 (크롤링 결과 저장 시 사용)
     *
     * @param crawledRaws 저장할 CrawledRaw 목록
     * @return 저장된 CrawledRaw ID 목록
     */
    List<CrawledRawId> persistAll(List<CrawledRaw> crawledRaws);
}
