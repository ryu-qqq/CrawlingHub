package com.ryuqq.crawlinghub.adapter.out.persistence.task.entity;

import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
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
 * CrawlTaskJpaEntity - CrawlTask JPA Entity
 *
 * <p>Persistence Layer의 JPA Entity로서 crawl_task 테이블과 매핑됩니다.
 *
 * <p><strong>Long FK 전략:</strong>
 *
 * <ul>
 *   <li>JPA 관계 어노테이션 사용 금지
 *   <li>crawlSchedulerId, sellerId는 Long 타입으로 직접 관리
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
@Table(name = "crawl_task")
public class CrawlTaskJpaEntity {

    /** 기본 키 - AUTO_INCREMENT */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** CrawlScheduler ID (Long FK 전략) */
    @Column(name = "crawl_scheduler_id", nullable = false)
    private Long crawlSchedulerId;

    /** Seller ID (Long FK 전략) */
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    /** 태스크 유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 30)
    private CrawlTaskType taskType;

    /** 크롤링 Base URL */
    @Column(name = "endpoint_base_url", nullable = false, length = 500)
    private String endpointBaseUrl;

    /** 크롤링 Path */
    @Column(name = "endpoint_path", nullable = false, length = 500)
    private String endpointPath;

    /** 크롤링 Query Params (JSON) */
    @Column(name = "endpoint_query_params", columnDefinition = "TEXT")
    private String endpointQueryParams;

    /** 현재 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CrawlTaskStatus status;

    /** 재시도 횟수 */
    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    /** 생성 일시 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 수정 일시 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** JPA 기본 생성자 (protected) */
    protected CrawlTaskJpaEntity() {}

    /** 전체 필드 생성자 (private) */
    private CrawlTaskJpaEntity(
            Long id,
            Long crawlSchedulerId,
            Long sellerId,
            CrawlTaskType taskType,
            String endpointBaseUrl,
            String endpointPath,
            String endpointQueryParams,
            CrawlTaskStatus status,
            int retryCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.crawlSchedulerId = crawlSchedulerId;
        this.sellerId = sellerId;
        this.taskType = taskType;
        this.endpointBaseUrl = endpointBaseUrl;
        this.endpointPath = endpointPath;
        this.endpointQueryParams = endpointQueryParams;
        this.status = status;
        this.retryCount = retryCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * of() 스태틱 팩토리 메서드 (Mapper 전용)
     *
     * <p>Entity 생성은 반드시 이 메서드를 통해서만 가능합니다.
     *
     * <p>Mapper에서 Domain → Entity 변환 시 사용합니다.
     *
     * @param id 기본 키
     * @param crawlSchedulerId CrawlScheduler ID
     * @param sellerId Seller ID
     * @param taskType 태스크 유형
     * @param endpointBaseUrl 엔드포인트 Base URL
     * @param endpointPath 엔드포인트 Path
     * @param endpointQueryParams 엔드포인트 Query Params (JSON)
     * @param status 현재 상태
     * @param retryCount 재시도 횟수
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @return CrawlTaskJpaEntity 인스턴스
     */
    public static CrawlTaskJpaEntity of(
            Long id,
            Long crawlSchedulerId,
            Long sellerId,
            CrawlTaskType taskType,
            String endpointBaseUrl,
            String endpointPath,
            String endpointQueryParams,
            CrawlTaskStatus status,
            int retryCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new CrawlTaskJpaEntity(
                id,
                crawlSchedulerId,
                sellerId,
                taskType,
                endpointBaseUrl,
                endpointPath,
                endpointQueryParams,
                status,
                retryCount,
                createdAt,
                updatedAt);
    }

    // ===== Getters (Setter 제공 금지) =====

    public Long getId() {
        return id;
    }

    public Long getCrawlSchedulerId() {
        return crawlSchedulerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public CrawlTaskType getTaskType() {
        return taskType;
    }

    public String getEndpointBaseUrl() {
        return endpointBaseUrl;
    }

    public String getEndpointPath() {
        return endpointPath;
    }

    public String getEndpointQueryParams() {
        return endpointQueryParams;
    }

    public CrawlTaskStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
