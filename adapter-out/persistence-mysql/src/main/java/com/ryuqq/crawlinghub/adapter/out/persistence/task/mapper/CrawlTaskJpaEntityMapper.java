package com.ryuqq.crawlinghub.adapter.out.persistence.task.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.CrawlTaskJpaEntity;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlEndpoint;
import com.ryuqq.crawlinghub.domain.task.vo.RetryCount;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * CrawlTaskJpaEntityMapper - Entity ↔ Domain 변환 Mapper
 *
 * <p>Persistence Layer의 JPA Entity와 Domain Layer의 CrawlTask 간 변환을 담당합니다.
 *
 * <p><strong>변환 책임:</strong>
 *
 * <ul>
 *   <li>CrawlTask → CrawlTaskJpaEntity (저장용)
 *   <li>CrawlTaskJpaEntity → CrawlTask (조회용)
 *   <li>CrawlEndpoint ↔ JSON 변환
 * </ul>
 *
 * <p><strong>Hexagonal Architecture 관점:</strong>
 *
 * <ul>
 *   <li>Adapter Layer의 책임
 *   <li>Domain과 Infrastructure 기술 분리
 *   <li>Domain은 JPA 의존성 없음
 * </ul>
 *
 * <p><strong>주의:</strong>
 *
 * <ul>
 *   <li>Outbox는 별도 Entity로 관리됨 (CrawlTaskOutboxJpaEntity)
 *   <li>toDomain에서 Outbox는 null로 설정됨
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskJpaEntityMapper {

    private final ObjectMapper objectMapper;

    public CrawlTaskJpaEntityMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Domain → Entity 변환
     *
     * <p><strong>사용 시나리오:</strong>
     *
     * <ul>
     *   <li>신규 CrawlTask 저장 (ID가 null)
     *   <li>기존 CrawlTask 수정 (ID가 있음)
     * </ul>
     *
     * <p><strong>변환 규칙:</strong>
     *
     * <ul>
     *   <li>ID: Domain.getId().value() → Entity.id (null이면 신규)
     *   <li>CrawlEndpoint → endpointBaseUrl, endpointPath, endpointQueryParams(JSON)
     *   <li>RetryCount → retryCount (int)
     * </ul>
     *
     * @param domain CrawlTask 도메인
     * @return CrawlTaskJpaEntity
     */
    public CrawlTaskJpaEntity toEntity(CrawlTask domain) {
        CrawlEndpoint endpoint = domain.getEndpoint();

        return CrawlTaskJpaEntity.of(
                domain.getIdValue(),
                domain.getCrawlSchedulerIdValue(),
                domain.getSellerIdValue(),
                domain.getTaskType(),
                endpoint.baseUrl(),
                endpoint.path(),
                endpoint.toQueryParamsJson(),
                domain.getStatus(),
                domain.getRetryCountValue(),
                toLocalDateTime(domain.getCreatedAt()),
                toLocalDateTime(domain.getUpdatedAt()));
    }

    /**
     * Entity → Domain 변환
     *
     * <p><strong>사용 시나리오:</strong>
     *
     * <ul>
     *   <li>데이터베이스에서 조회한 Entity를 Domain으로 변환
     *   <li>Application Layer로 전달
     * </ul>
     *
     * <p><strong>변환 규칙:</strong>
     *
     * <ul>
     *   <li>Entity.id → CrawlTaskId
     *   <li>endpointBaseUrl, endpointPath, endpointQueryParams(JSON) → CrawlEndpoint
     *   <li>retryCount (int) → RetryCount
     *   <li>Outbox는 null (별도로 조회/연결 필요)
     * </ul>
     *
     * @param entity CrawlTaskJpaEntity
     * @return CrawlTask 도메인
     */
    public CrawlTask toDomain(CrawlTaskJpaEntity entity) {
        CrawlEndpoint endpoint =
                new CrawlEndpoint(
                        entity.getEndpointBaseUrl(),
                        entity.getEndpointPath(),
                        deserializeQueryParams(entity.getEndpointQueryParams()));

        return CrawlTask.reconstitute(
                CrawlTaskId.of(entity.getId()),
                CrawlSchedulerId.of(entity.getCrawlSchedulerId()),
                SellerId.of(entity.getSellerId()),
                entity.getTaskType(),
                endpoint,
                entity.getStatus(),
                new RetryCount(entity.getRetryCount()),
                null, // Outbox는 별도 Entity로 관리
                toInstant(entity.getCreatedAt()),
                toInstant(entity.getUpdatedAt()));
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * JSON 문자열 → Query Params Map 변환
     *
     * @param json JSON 문자열
     * @return Query Params Map (null이면 빈 Map)
     */
    private Map<String, String> deserializeQueryParams(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Query params 역직렬화 실패", e);
        }
    }
}
