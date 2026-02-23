package com.ryuqq.crawlinghub.application.task.port.out.query;

import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.query.CrawlTaskOutboxCriteria;
import java.util.List;
import java.util.Optional;

/**
 * CrawlTask Outbox 조회 Port (Port Out)
 *
 * <p><strong>용도</strong>: 재시도 스케줄러에서 PENDING/FAILED 상태 Outbox 조회
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawlTaskOutboxQueryPort {

    /**
     * CrawlTask ID로 Outbox 조회
     *
     * @param crawlTaskId CrawlTask ID
     * @return Outbox (Optional)
     */
    Optional<CrawlTaskOutbox> findByCrawlTaskId(CrawlTaskId crawlTaskId);

    /**
     * 조건으로 Outbox 목록 조회
     *
     * <p>Criteria 객체를 통해 다양한 조건을 조합하여 조회합니다.
     *
     * @param criteria 조회 조건 (CrawlTaskOutboxCriteria)
     * @return Outbox 목록
     */
    List<CrawlTaskOutbox> findByCriteria(CrawlTaskOutboxCriteria criteria);

    /**
     * 조건에 맞는 Outbox 개수 조회
     *
     * <p>페이징 처리를 위한 전체 개수 조회에 사용됩니다.
     *
     * @param criteria 조회 조건 (CrawlTaskOutboxCriteria)
     * @return 조건에 맞는 Outbox 개수
     */
    long countByCriteria(CrawlTaskOutboxCriteria criteria);

    /**
     * delaySeconds 이상 경과한 PENDING 상태 Outbox 조회
     *
     * <p>스케줄러에서 배치 처리할 Outbox를 조회합니다. 오래된 것부터 처리하기 위해 createdAt 오름차순으로 정렬합니다.
     *
     * @param limit 최대 조회 건수
     * @param delaySeconds 생성 후 경과해야 할 최소 시간 (초)
     * @return PENDING 상태의 Outbox 목록
     */
    List<CrawlTaskOutbox> findPendingOlderThan(int limit, int delaySeconds);

    /**
     * timeoutSeconds 이상 PROCESSING 상태인 좀비 Outbox 조회
     *
     * <p>좀비 복구 스케줄러에서 타임아웃된 PROCESSING Outbox를 조회합니다. processedAt 오름차순으로 정렬합니다.
     *
     * @param limit 최대 조회 건수
     * @param timeoutSeconds PROCESSING 상태 타임아웃 기준 (초)
     * @return PROCESSING 좀비 Outbox 목록
     */
    List<CrawlTaskOutbox> findStaleProcessing(int limit, long timeoutSeconds);

    /**
     * FAILED 상태에서 delaySeconds 이상 경과한 재시도 가능 Outbox 조회
     *
     * <p>FAILED 복구 스케줄러에서 자동 재처리 대상 Outbox를 조회합니다. retryCount가 최대 재시도 횟수 미만인 것만 조회합니다.
     *
     * @param limit 최대 조회 건수
     * @param delaySeconds FAILED 후 경과해야 할 최소 시간 (초)
     * @return FAILED 상태의 재시도 가능 Outbox 목록
     */
    List<CrawlTaskOutbox> findFailedOlderThan(int limit, int delaySeconds);
}
