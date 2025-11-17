package com.ryuqq.crawlinghub.domain.crawler.aggregate.execution;

import com.ryuqq.crawlinghub.domain.crawler.exception.CrawlingScheduleExecutionInvalidStateException;
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
        this.executionId = ExecutionId.forNew();
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

    /**
     * DB에서 조회한 CrawlingScheduleExecution 재구성 (reconstitute 패턴)
     *
     * <p>reconstitute() 패턴: DB에서 조회한 엔티티 재구성</p>
     * <p>⚠️ 주의: 현재 구현은 임시입니다. 모든 필드를 받는 private 생성자가 필요합니다.</p>
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
     * @return 재구성된 CrawlingScheduleExecution
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static CrawlingScheduleExecution reconstitute(
            ExecutionId executionId,
            ScheduleId scheduleId,
            SellerId sellerId,
            ExecutionStatus status,
            Integer totalTasksCreated,
            Integer completedTasks,
            Integer failedTasks,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            LocalDateTime createdAt
    ) {
        // TODO: 모든 필드를 받는 private 생성자 추가 필요 (struct: 리팩토링 예정)
        CrawlingScheduleExecution execution = new CrawlingScheduleExecution(scheduleId, sellerId);

        // 임시 구현: 상태 전환 메서드를 호출하여 상태 재현 (Green Phase)
        if (status == ExecutionStatus.RUNNING || status == ExecutionStatus.COMPLETED || status == ExecutionStatus.FAILED) {
            execution.start(totalTasksCreated);

            // 완료/실패 작업 수만큼 카운터 증가
            for (int i = 0; i < completedTasks; i++) {
                execution.completeTask();
            }
            for (int i = 0; i < failedTasks; i++) {
                execution.failTask();
            }

            // 최종 상태 전환
            if (status == ExecutionStatus.COMPLETED) {
                execution.complete();
            } else if (status == ExecutionStatus.FAILED) {
                execution.fail();
            }
        }

        return execution;
    }

    // ===== Business Methods =====

    /**
     * 스케줄 실행 시작
     *
     * <p><strong>시작 조건:</strong></p>
     * <ul>
     *   <li>✅ PENDING 상태에서만 시작 가능</li>
     *   <li>✅ totalTasksCreated 설정</li>
     *   <li>✅ RUNNING 상태로 전환</li>
     * </ul>
     *
     * @param totalTasksCreated 생성할 총 작업 수
     * @throws IllegalStateException PENDING 상태가 아닐 때
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public void start(int totalTasksCreated) {
        if (status != ExecutionStatus.PENDING) {
            throw new CrawlingScheduleExecutionInvalidStateException(
                executionId.value(),
                status.name(),
                "start",
                "Execution must be in PENDING status to start"
            );
        }
        this.totalTasksCreated = totalTasksCreated;
        this.status = ExecutionStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    /**
     * 작업 완료 처리
     *
     * <p>completedTasks를 1 증가시킵니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public void completeTask() {
        this.completedTasks++;
    }

    /**
     * 작업 실패 처리
     *
     * <p>failedTasks를 1 증가시킵니다.</p>
     *
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public void failTask() {
        this.failedTasks++;
    }

    /**
     * 스케줄 실행 완료
     *
     * <p><strong>완료 조건:</strong></p>
     * <ul>
     *   <li>✅ RUNNING 상태에서만 완료 가능</li>
     *   <li>✅ COMPLETED 상태로 전환</li>
     *   <li>✅ completedAt 타임스탬프 설정</li>
     * </ul>
     *
     * @throws IllegalStateException RUNNING 상태가 아닐 때
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public void complete() {
        if (status != ExecutionStatus.RUNNING) {
            throw new CrawlingScheduleExecutionInvalidStateException(
                executionId.value(),
                status.name(),
                "complete",
                "Execution must be in RUNNING status to complete"
            );
        }
        this.status = ExecutionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 스케줄 실행 실패
     *
     * <p><strong>실패 조건:</strong></p>
     * <ul>
     *   <li>✅ RUNNING 상태에서만 실패 가능</li>
     *   <li>✅ FAILED 상태로 전환</li>
     *   <li>✅ completedAt 타임스탬프 설정</li>
     * </ul>
     *
     * @throws IllegalStateException RUNNING 상태가 아닐 때
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public void fail() {
        if (status != ExecutionStatus.RUNNING) {
            throw new CrawlingScheduleExecutionInvalidStateException(
                executionId.value(),
                status.name(),
                "fail",
                "Execution must be in RUNNING status to fail"
            );
        }
        this.status = ExecutionStatus.FAILED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 진행률 계산 (Tell Don't Ask)
     *
     * <p><strong>계산 방식:</strong></p>
     * <ul>
     *   <li>✅ (completedTasks + failedTasks) / totalTasksCreated * 100</li>
     *   <li>✅ totalTasksCreated가 0이면 0.0 반환</li>
     * </ul>
     *
     * <p><strong>Tell Don't Ask 원칙:</strong></p>
     * <ul>
     *   <li>❌ 외부: getTotalTasksCreated(), getCompletedTasks() 가져와서 계산</li>
     *   <li>✅ 내부: getProgressRate()로 객체가 스스로 계산</li>
     * </ul>
     *
     * @return 진행률 (0.0 ~ 100.0)
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public double getProgressRate() {
        if (totalTasksCreated == 0) {
            return 0.0;
        }
        int processed = completedTasks + failedTasks;
        return (double) processed / totalTasksCreated * 100;
    }

    /**
     * 성공률 계산 (Tell Don't Ask)
     *
     * <p><strong>계산 방식:</strong></p>
     * <ul>
     *   <li>✅ completedTasks / (completedTasks + failedTasks) * 100</li>
     *   <li>✅ 처리된 작업이 0이면 0.0 반환</li>
     * </ul>
     *
     * <p><strong>Tell Don't Ask 원칙:</strong></p>
     * <ul>
     *   <li>❌ 외부: getCompletedTasks(), getFailedTasks() 가져와서 계산</li>
     *   <li>✅ 내부: getSuccessRate()로 객체가 스스로 계산</li>
     * </ul>
     *
     * @return 성공률 (0.0 ~ 100.0)
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public double getSuccessRate() {
        int processed = completedTasks + failedTasks;
        if (processed == 0) {
            return 0.0;
        }
        return (double) completedTasks / processed * 100;
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
