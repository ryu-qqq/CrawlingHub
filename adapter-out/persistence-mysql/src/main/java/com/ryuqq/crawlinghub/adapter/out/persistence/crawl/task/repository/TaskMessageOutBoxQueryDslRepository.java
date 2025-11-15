package com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.entity.TaskMessageOutBoxEntity;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

import static com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.entity.QTaskMessageOutBoxEntity.taskMessageOutBoxEntity;

/**
 * TaskMessageOutbox QueryDSL Repository
 *
 * <p>복잡한 쿼리와 Pessimistic Lock을 지원하는 QueryDSL 기반 Repository입니다.</p>
 *
 * <p><strong>주요 기능:</strong></p>
 * <ul>
 *   <li>Pessimistic Lock을 사용한 상태별 조회 (동시성 제어)</li>
 *   <li>Scheduler가 PENDING 레코드를 조회할 때 중복 처리 방지</li>
 *   <li>다중 Scheduler 인스턴스 실행 시 Race Condition 방지</li>
 * </ul>
 *
 * <p><strong>Pessimistic Lock 전략:</strong></p>
 * <ul>
 *   <li>LockModeType.PESSIMISTIC_WRITE - 배타적 잠금</li>
 *   <li>조회한 레코드는 트랜잭션이 끝날 때까지 다른 트랜잭션에서 읽기/쓰기 불가</li>
 *   <li>동일 레코드에 대한 동시 처리 완전 차단</li>
 * </ul>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - 명시적 생성자 주입</li>
 *   <li>✅ QueryDSL 타입 세이프 쿼리</li>
 *   <li>✅ @Repository - Spring Bean 등록</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-12
 */
@Repository
public class TaskMessageOutBoxQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 생성자 주입
     *
     * @param queryFactory JPAQueryFactory
     */
    public TaskMessageOutBoxQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = Objects.requireNonNull(queryFactory, "queryFactory must not be null");
    }

    /**
     * 상태별 Outbox 조회 (Pessimistic Lock)
     *
     * <p>특정 상태의 Outbox 레코드를 조회하면서 배타적 잠금을 획득합니다.</p>
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <pre>
     * // Scheduler에서 PENDING 레코드를 조회하여 SQS 발행
     * List&lt;TaskMessageOutBoxEntity&gt; pending =
     *     repository.findByStatusWithLock("PENDING");
     *
     * for (TaskMessageOutBoxEntity outbox : pending) {
     *     // SQS 발행 시도
     *     sqsService.send(outbox);
     *
     *     // 성공 시 SENT로 변경
     *     outbox.setStatus("SENT");
     *     outbox.setSentAt(LocalDateTime.now());
     * }
     * </pre>
     *
     * <p><strong>동시성 보장:</strong></p>
     * <ul>
     *   <li>Scheduler A가 레코드 ID=1을 Lock으로 조회</li>
     *   <li>Scheduler B는 ID=1에 접근 불가 (대기 또는 건너뜀)</li>
     *   <li>Scheduler A가 트랜잭션 완료 후 Lock 해제</li>
     *   <li>Scheduler B가 이미 SENT 상태로 변경된 레코드는 조회하지 않음</li>
     * </ul>
     *
     * @param status Outbox 상태 (String - "PENDING", "SENT")
     * @return List&lt;TaskMessageOutBoxEntity&gt; Lock이 걸린 Outbox 목록
     */
    public List<TaskMessageOutBoxEntity> findByStatusWithLock(String status) {
        return queryFactory
            .selectFrom(taskMessageOutBoxEntity)
            .where(taskMessageOutBoxEntity.status.eq(status))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .fetch();
    }
}
