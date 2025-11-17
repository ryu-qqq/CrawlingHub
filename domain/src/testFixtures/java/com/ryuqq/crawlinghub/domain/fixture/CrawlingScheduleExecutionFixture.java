package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.crawler.aggregate.execution.CrawlingScheduleExecution;
import com.ryuqq.crawlinghub.domain.crawler.vo.ScheduleId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

/**
 * CrawlingScheduleExecution 관련 테스트 데이터 생성 Fixture
 *
 * <p>CrawlingScheduleExecution Aggregate와 관련 Value Object의 테스트 데이터를 제공합니다.</p>
 *
 * <p><strong>표준 메서드:</strong></p>
 * <ul>
 *   <li>{@link #forNew()} - 새 CrawlingScheduleExecution 생성 (표준 패턴)</li>
 *   <li>{@link #of()} - 기본 CrawlingScheduleExecution 생성 (표준 패턴)</li>
 *   <li>{@link #reconstitute} - DB에서 복원 (표준 패턴)</li>
 * </ul>
 *
 * <p><strong>헬퍼 메서드:</strong></p>
 * <ul>
 *   <li>{@link #pendingExecution()} - PENDING 상태의 기본 실행 인스턴스</li>
 *   <li>{@link #runningExecution()} - RUNNING 상태의 실행 인스턴스</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public class CrawlingScheduleExecutionFixture {

    private static final SellerId DEFAULT_SELLER_ID = new SellerId(1L);

    /**
     * 새로운 CrawlingScheduleExecution 생성 (표준 패턴)
     *
     * @return 새로 생성된 CrawlingScheduleExecution
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static CrawlingScheduleExecution forNew() {
        return pendingExecution();
    }

    /**
     * 기본 CrawlingScheduleExecution 생성 (표준 패턴)
     *
     * @return 기본 CrawlingScheduleExecution
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static CrawlingScheduleExecution of() {
        return pendingExecution();
    }

    /**
     * DB에서 복원된 CrawlingScheduleExecution 생성 (표준 패턴)
     *
     * <p>Aggregate의 reconstitute() 메서드를 호출하여 영속화된 상태를 복원합니다.</p>
     *
     * @param executionId 실행 ID
     * @param scheduleId 스케줄 ID
     * @param sellerId Seller ID
     * @param status 실행 상태
     * @param totalTasksCreated 생성된 총 작업 수
     * @param completedTasks 완료된 작업 수
     * @param failedTasks 실패한 작업 수
     * @param startedAt 시작 일시
     * @param completedAt 완료 일시
     * @param createdAt 생성 일시
     * @return 복원된 CrawlingScheduleExecution
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static CrawlingScheduleExecution reconstitute(
            com.ryuqq.crawlinghub.domain.crawler.vo.ExecutionId executionId,
            ScheduleId scheduleId,
            SellerId sellerId,
            com.ryuqq.crawlinghub.domain.crawler.vo.ExecutionStatus status,
            Integer totalTasksCreated,
            Integer completedTasks,
            Integer failedTasks,
            java.time.LocalDateTime startedAt,
            java.time.LocalDateTime completedAt,
            java.time.LocalDateTime createdAt
    ) {
        return CrawlingScheduleExecution.reconstitute(
                executionId,
                scheduleId,
                sellerId,
                status,
                totalTasksCreated,
                completedTasks,
                failedTasks,
                startedAt,
                completedAt,
                createdAt
        );
    }

    /**
     * PENDING 상태의 CrawlingScheduleExecution 생성
     *
     * <p><strong>설정:</strong></p>
     * <ul>
     *   <li>SellerId: seller_12345</li>
     *   <li>Status: PENDING</li>
     *   <li>TotalTasksCreated: 0</li>
     *   <li>CompletedTasks: 0</li>
     *   <li>FailedTasks: 0</li>
     * </ul>
     *
     * @return PENDING 상태의 CrawlingScheduleExecution
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static CrawlingScheduleExecution pendingExecution() {
        ScheduleId scheduleId = ScheduleId.forNew();
        return CrawlingScheduleExecution.create(scheduleId, DEFAULT_SELLER_ID);
    }

    /**
     * RUNNING 상태의 CrawlingScheduleExecution 생성
     *
     * <p><strong>설정:</strong></p>
     * <ul>
     *   <li>SellerId: seller_12345</li>
     *   <li>Status: RUNNING</li>
     *   <li>TotalTasksCreated: 100</li>
     *   <li>CompletedTasks: 0</li>
     *   <li>FailedTasks: 0</li>
     * </ul>
     *
     * @return RUNNING 상태의 CrawlingScheduleExecution
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static CrawlingScheduleExecution runningExecution() {
        CrawlingScheduleExecution execution = pendingExecution();
        execution.start(100);
        return execution;
    }
}
