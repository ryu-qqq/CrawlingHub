package com.ryuqq.crawlinghub.adapter.out.persistence.crawl.result.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.result.entity.CrawlResultEntity;
import com.ryuqq.crawlinghub.domain.crawl.result.CrawlResult;
import com.ryuqq.crawlinghub.domain.crawl.result.CrawlResultId;
import com.ryuqq.crawlinghub.domain.seller.MustItSellerId;
import com.ryuqq.crawlinghub.domain.task.TaskId;
import com.ryuqq.crawlinghub.domain.task.TaskType;
import org.springframework.stereotype.Component;

/**
 * CrawlResult Mapper (Domain ↔ Entity 변환)
 *
 * <p>역할:
 * <ul>
 *   <li>Domain → Entity 변환 (toEntity)</li>
 *   <li>Entity → Domain 변환 (toDomain)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Component
public class CrawlResultMapper {

    /**
     * Domain → Entity 변환
     *
     * @param domain CrawlResult Domain
     * @return CrawlResultEntity
     */
    public CrawlResultEntity toEntity(CrawlResult domain) {
        if (domain == null) {
            return null;
        }

        return new CrawlResultEntity(
            domain.getIdValue(),
            domain.getTaskIdValue(),
            domain.getTaskType().name(),
            domain.getSellerIdValue(),
            domain.getRawData(),
            domain.getCrawledAt(),
            domain.getCreatedAt()
        );
    }

    /**
     * Entity → Domain 변환 (Reconstitute)
     *
     * @param entity CrawlResultEntity
     * @return CrawlResult Domain
     */
    public CrawlResult toDomain(CrawlResultEntity entity) {
        if (entity == null) {
            return null;
        }

        return CrawlResult.reconstitute(
            CrawlResultId.of(entity.getId()),
            TaskId.of(entity.getTaskId()),
            TaskType.valueOf(entity.getTaskType()),
            MustItSellerId.of(entity.getSellerId()),
            entity.getRawData(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
