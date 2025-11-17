package com.ryuqq.crawlinghub.domain.crawler.aggregate.execution;

import com.ryuqq.crawlinghub.domain.crawler.vo.ExecutionId;
import com.ryuqq.crawlinghub.domain.crawler.vo.ExecutionStatus;
import com.ryuqq.crawlinghub.domain.crawler.vo.ScheduleId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

import java.time.LocalDateTime;

/**
 * CrawlingScheduleExecution - 스케줄 실행 Aggregate Root
 *
 * <p>CrawlingSchedule의 단일 실행 인스턴스를 관리합니다.</p>
 *
 * <p><strong>핵심 책임:</strong></p>
 * <ul>
 *   <li>✅ 스케줄 실행 생명주기 관리 (PENDING → RUNNING → COMPLETED/FAILED)</li>
 *   <li>✅ 크롤링 작업 진행률 추적 (totalTasksCreated, completedTasks, failedTasks)</li>
 *   <li>✅ Tell Don't Ask 패턴 적용 (진행률 계산 내부화)</li>
 * </ul>
 *
 * <p><strong>상태 전환:</strong></p>
 * <pre>
 * PENDING (생성 직후)
 *    ↓ start()
 * RUNNING (크롤링 진행)
 *    ↓ complete() or fail()
 * COMPLETED / FAILED
 * </pre>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지</li>
 *   <li>✅ Law of Demeter 준수</li>
 *   <li>✅ Tell Don't Ask 패턴</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public class CrawlingScheduleExecution {

    private final ExecutionId executionId;
    private final ScheduleId scheduleId;
    private final SellerId sellerId;
    private ExecutionStatus status;
    private Integer totalTasksCreated;
    private Integer completedTasks;
    private Integer failedTasks;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private final LocalDateTime createdAt;

    /**
     * Private Constructor - Factory Method 패턴
     *
     * @param scheduleId 스케줄 ID
     * @param sellerId Seller ID
     * @author ryu-qqq
     * @since 2025-11-17
     */
    private CrawlingScheduleExecution(ScheduleId scheduleId, SellerId sellerId) {
        this.executionId = ExecutionId.generate();
        this.scheduleId = scheduleId;
        this.sellerId = sellerId;
        this.status = ExecutionStatus.PENDING;
        this.totalTasksCreated = 0;
        this.completedTasks = 0;
        this.failedTasks = 0;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * CrawlingScheduleExecution 생성 Factory Method
     *
     * <p>초기 상태: PENDING</p>
     * <p>작업 카운터 초기화: 0</p>
     *
     * @param scheduleId 스케줄 ID
     * @param sellerId Seller ID
     * @return 생성된 CrawlingScheduleExecution
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static CrawlingScheduleExecution create(ScheduleId scheduleId, SellerId sellerId) {
        return new CrawlingScheduleExecution(scheduleId, sellerId);
    }

    // ===== Getters =====

    public ExecutionId getExecutionId() {
        return executionId;
    }

    public ScheduleId getScheduleId() {
        return scheduleId;
    }

    public SellerId getSellerId() {
        return sellerId;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public Integer getTotalTasksCreated() {
        return totalTasksCreated;
    }

    public Integer getCompletedTasks() {
        return completedTasks;
    }

    public Integer getFailedTasks() {
        return failedTasks;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
