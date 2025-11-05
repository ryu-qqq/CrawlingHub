package com.ryuqq.crawlinghub.domain.crawl.schedule;

import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSellerId;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 크롤링 스케줄 Aggregate Root
 * 
 * <p>비즈니스 규칙:
 * <ul>
 *   <li>한 셀러는 하나의 활성 스케줄만 가능</li>
 *   <li>Cron 표현식은 유효해야 함</li>
 *   <li>최소 크롤링 주기: 1시간</li>
 * </ul>
 */
public class CrawlSchedule {

    private final CrawlScheduleId id;
    private final MustitSellerId sellerId;
    private CronExpression cronExpression;
    private ScheduleStatus status;
    private LocalDateTime nextExecutionTime;
    private LocalDateTime lastExecutedAt;
    private final Clock clock;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Private 전체 생성자 (reconstitute 전용)
     */
    private CrawlSchedule(
        CrawlScheduleId id,
        MustitSellerId sellerId,
        CronExpression cronExpression,
        ScheduleStatus status,
        LocalDateTime nextExecutionTime,
        LocalDateTime lastExecutedAt,
        Clock clock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.sellerId = sellerId;
        this.cronExpression = cronExpression;
        this.status = status;
        this.nextExecutionTime = nextExecutionTime;
        this.lastExecutedAt = lastExecutedAt;
        this.clock = clock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Package-private 주요 생성자 (검증 포함)
     */
    CrawlSchedule(
        CrawlScheduleId id,
        MustitSellerId sellerId,
        CronExpression cronExpression,
        ScheduleStatus status,
        Clock clock
    ) {
        validateRequiredFields(sellerId, cronExpression, status);

        this.id = id;
        this.sellerId = sellerId;
        this.cronExpression = cronExpression;
        this.status = status;
        this.nextExecutionTime = null;
        this.lastExecutedAt = null;
        this.clock = clock;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 신규 스케줄 생성 (ID 없음)
     */
    public static CrawlSchedule forNew(MustitSellerId sellerId, CronExpression cronExpression) {
        return new CrawlSchedule(
            null,
            sellerId,
            cronExpression,
            ScheduleStatus.ACTIVE,
            Clock.systemDefaultZone()
        );
    }

    /**
     * 기존 스케줄 생성 (ID 있음)
     */
    public static CrawlSchedule of(
        CrawlScheduleId id,
        MustitSellerId sellerId,
        CronExpression cronExpression,
        ScheduleStatus status
    ) {
        if (id == null) {
            throw new IllegalArgumentException("CrawlSchedule ID는 필수입니다");
        }
        return new CrawlSchedule(id, sellerId, cronExpression, status, Clock.systemDefaultZone());
    }

    /**
     * DB reconstitute (모든 필드 포함)
     */
    public static CrawlSchedule reconstitute(
        CrawlScheduleId id,
        MustitSellerId sellerId,
        CronExpression cronExpression,
        ScheduleStatus status,
        LocalDateTime nextExecutionTime,
        LocalDateTime lastExecutedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new CrawlSchedule(
            id,
            sellerId,
            cronExpression,
            status,
            nextExecutionTime,
            lastExecutedAt,
            Clock.systemDefaultZone(),
            createdAt,
            updatedAt
        );
    }

    private static void validateRequiredFields(
        MustitSellerId sellerId,
        CronExpression cronExpression,
        ScheduleStatus status
    ) {
        if (sellerId == null) {
            throw new IllegalArgumentException("셀러 ID는 필수입니다");
        }
        if (cronExpression == null) {
            throw new IllegalArgumentException("Cron 표현식은 필수입니다");
        }
        if (status == null) {
            throw new IllegalArgumentException("스케줄 상태는 필수입니다");
        }
    }

    /**
     * 스케줄 업데이트
     */
    public void updateSchedule(CronExpression newExpression) {
        if (newExpression == null) {
            throw new IllegalArgumentException("Cron 표현식은 null일 수 없습니다");
        }
        this.cronExpression = newExpression;
        this.nextExecutionTime = null; // 재계산 필요
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 다음 실행 시간 계산
     */
    public void calculateNextExecution(LocalDateTime nextTime) {
        if (nextTime == null) {
            throw new IllegalArgumentException("다음 실행 시간은 null일 수 없습니다");
        }
        this.nextExecutionTime = nextTime;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 실행 완료 기록
     */
    public void markExecuted() {
        this.lastExecutedAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 실행 시간 확인
     */
    public boolean isTimeToExecute() {
        if (nextExecutionTime == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now(clock);
        return !now.isBefore(nextExecutionTime);
    }

    /**
     * 스케줄 활성화
     */
    public void activate() {
        this.status = ScheduleStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 스케줄 일시정지
     */
    public void suspend() {
        this.status = ScheduleStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 활성 상태 여부
     */
    public boolean isActive() {
        return status.isActive();
    }

    /**
     * 특정 상태인지 확인
     */
    public boolean hasStatus(ScheduleStatus targetStatus) {
        return this.status == targetStatus;
    }

    // Law of Demeter 준수 메서드
    public Long getIdValue() {
        return id != null ? id.value() : null;
    }

    public Long getSellerIdValue() {
        return sellerId != null ? sellerId.value() : null;
    }

    public String getCronExpressionValue() {
        return cronExpression.getValue();
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public LocalDateTime getNextExecutionTime() {
        return nextExecutionTime;
    }

    public LocalDateTime getLastExecutedAt() {
        return lastExecutedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * EventBridge 페이로드 생성 (Tell, Don't Ask)
     *
     * <p>Domain 객체가 스스로 외부 시스템 요청 데이터를 생성합니다.
     *
     * @return EventBridge 등록에 필요한 데이터
     */
    public EventBridgePayload toEventBridgePayload() {
        if (id == null) {
            throw new IllegalStateException("스케줄 ID가 없어 EventBridge 페이로드를 생성할 수 없습니다");
        }
        return new EventBridgePayload(
            id.value(),
            sellerId.value(),
            cronExpression.getValue()
        );
    }

    /**
     * EventBridge 페이로드를 담는 내부 레코드
     *
     * <p>Domain Layer에서 외부 시스템 요청 데이터를 표현합니다.
     *
     * @param scheduleId 스케줄 ID
     * @param sellerId 셀러 ID
     * @param cronExpression Cron 표현식
     */
    public record EventBridgePayload(
        Long scheduleId,
        Long sellerId,
        String cronExpression
    ) {
        public EventBridgePayload {
            if (scheduleId == null) {
                throw new IllegalArgumentException("스케줄 ID는 필수입니다");
            }
            if (sellerId == null) {
                throw new IllegalArgumentException("셀러 ID는 필수입니다");
            }
            if (cronExpression == null || cronExpression.isBlank()) {
                throw new IllegalArgumentException("Cron 표현식은 필수입니다");
            }
        }
    }

    /**
     * ScheduleResponse 생성 (Tell, Don't Ask)
     *
     * <p>Domain 객체가 스스로 Response DTO를 생성합니다.
     * Application Layer의 Assembler를 제거하고 Domain이 책임을 가집니다.
     *
     * @return ScheduleResponse 데이터
     */
    public ScheduleResponseData toResponse() {
        if (id == null) {
            throw new IllegalStateException("스케줄 ID가 없어 Response를 생성할 수 없습니다");
        }
        return new ScheduleResponseData(
            id.value(),
            sellerId.value(),
            cronExpression.getValue(),
            status,
            nextExecutionTime,
            lastExecutedAt,
            createdAt,
            updatedAt
        );
    }

    /**
     * Schedule Response 데이터를 담는 레코드
     *
     * <p>Domain이 Application Layer에 전달하는 Response 데이터입니다.
     */
    public record ScheduleResponseData(
        Long scheduleId,
        Long sellerId,
        String cronExpression,
        ScheduleStatus status,
        java.time.LocalDateTime nextExecutionTime,
        java.time.LocalDateTime lastExecutedAt,
        java.time.LocalDateTime createdAt,
        java.time.LocalDateTime updatedAt
    ) {}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CrawlSchedule that = (CrawlSchedule) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CrawlSchedule{" +
            "id=" + id +
            ", sellerId=" + sellerId +
            ", cronExpression=" + cronExpression +
            ", status=" + status +
            ", nextExecutionTime=" + nextExecutionTime +
            '}';
    }
}
