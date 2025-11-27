package com.ryuqq.crawlinghub.adapter.out.persistence.execution.entity;

import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * CrawlExecutionJpaEntity - CrawlExecution JPA Entity
 *
 * <p>Persistence Layer의 JPA Entity로서 crawl_execution 테이블과 매핑됩니다.
 *
 * <p><strong>Long FK 전략:</strong>
 *
 * <ul>
 *   <li>JPA 관계 어노테이션 사용 금지
 *   <li>crawlTaskId, crawlSchedulerId, sellerId는 Long 타입으로 직접 관리
 * </ul>
 *
 * <p><strong>Lombok 금지:</strong>
 *
 * <ul>
 *   <li>Plain Java getter 사용
 *   <li>Setter 제공 금지
 *   <li>명시적 생성자 제공
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Entity
@Table(name = "crawl_execution")
public class CrawlExecutionJpaEntity {

    /** 기본 키 - AUTO_INCREMENT */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** CrawlTask ID (Long FK 전략) */
    @Column(name = "crawl_task_id", nullable = false)
    private Long crawlTaskId;

    /** CrawlScheduler ID (Long FK 전략) */
    @Column(name = "crawl_scheduler_id", nullable = false)
    private Long crawlSchedulerId;

    /** Seller ID (Long FK 전략) */
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    /** 실행 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CrawlExecutionStatus status;

    /** 응답 본문 (성공 시 또는 에러 응답) */
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    /** HTTP 상태 코드 */
    @Column(name = "http_status_code")
    private Integer httpStatusCode;

    /** 에러 메시지 */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /** 실행 시작 시각 */
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    /** 실행 완료 시각 */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** 실행 소요 시간 (밀리초) */
    @Column(name = "duration_ms")
    private Long durationMs;

    /** 생성 일시 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** JPA 기본 생성자 (protected) */
    protected CrawlExecutionJpaEntity() {}

    /** 전체 필드 생성자 (private) */
    private CrawlExecutionJpaEntity(
            Long id,
            Long crawlTaskId,
            Long crawlSchedulerId,
            Long sellerId,
            CrawlExecutionStatus status,
            String responseBody,
            Integer httpStatusCode,
            String errorMessage,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            Long durationMs,
            LocalDateTime createdAt) {
        this.id = id;
        this.crawlTaskId = crawlTaskId;
        this.crawlSchedulerId = crawlSchedulerId;
        this.sellerId = sellerId;
        this.status = status;
        this.responseBody = responseBody;
        this.httpStatusCode = httpStatusCode;
        this.errorMessage = errorMessage;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.durationMs = durationMs;
        this.createdAt = createdAt;
    }

    /**
     * of() 스태틱 팩토리 메서드 (Mapper 전용)
     *
     * <p>Entity 생성은 반드시 이 메서드를 통해서만 가능합니다.
     *
     * <p>Mapper에서 Domain → Entity 변환 시 사용합니다.
     *
     * @param id 기본 키
     * @param crawlTaskId CrawlTask ID
     * @param crawlSchedulerId CrawlScheduler ID
     * @param sellerId Seller ID
     * @param status 실행 상태
     * @param responseBody 응답 본문
     * @param httpStatusCode HTTP 상태 코드
     * @param errorMessage 에러 메시지
     * @param startedAt 실행 시작 시각
     * @param completedAt 실행 완료 시각
     * @param durationMs 실행 소요 시간 (밀리초)
     * @param createdAt 생성 일시
     * @return CrawlExecutionJpaEntity 인스턴스
     */
    public static CrawlExecutionJpaEntity of(
            Long id,
            Long crawlTaskId,
            Long crawlSchedulerId,
            Long sellerId,
            CrawlExecutionStatus status,
            String responseBody,
            Integer httpStatusCode,
            String errorMessage,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            Long durationMs,
            LocalDateTime createdAt) {
        return new CrawlExecutionJpaEntity(
                id,
                crawlTaskId,
                crawlSchedulerId,
                sellerId,
                status,
                responseBody,
                httpStatusCode,
                errorMessage,
                startedAt,
                completedAt,
                durationMs,
                createdAt);
    }

    // ===== Getters (Setter 제공 금지) =====

    public Long getId() {
        return id;
    }

    public Long getCrawlTaskId() {
        return crawlTaskId;
    }

    public Long getCrawlSchedulerId() {
        return crawlSchedulerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public CrawlExecutionStatus getStatus() {
        return status;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
