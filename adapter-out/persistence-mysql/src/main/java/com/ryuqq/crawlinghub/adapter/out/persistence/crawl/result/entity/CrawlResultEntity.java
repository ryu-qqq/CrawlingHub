package com.ryuqq.crawlinghub.adapter.out.persistence.crawl.result.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 크롤링 결과 Entity (Persistence Layer)
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Entity
@Table(name = "CRAWL_RESULT")
public class CrawlResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TASK_ID", nullable = false)
    private Long taskId;

    @Column(name = "TASK_TYPE", nullable = false, length = 50)
    private String taskType;

    @Column(name = "SELLER_ID", nullable = false)
    private Long sellerId;

    @Column(name = "RAW_DATA", nullable = false, columnDefinition = "JSON")
    private String rawData;

    @Column(name = "CRAWLED_AT", nullable = false)
    private LocalDateTime crawledAt;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * JPA 전용 기본 생성자
     */
    protected CrawlResultEntity() {
    }

    /**
     * 전체 생성자 (Reconstitute 전용)
     */
    public CrawlResultEntity(
        Long id,
        Long taskId,
        String taskType,
        Long sellerId,
        String rawData,
        LocalDateTime crawledAt,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.taskId = taskId;
        this.taskType = taskType;
        this.sellerId = sellerId;
        this.rawData = rawData;
        this.crawledAt = crawledAt;
        this.createdAt = createdAt;
    }

    /**
     * 신규 생성용 생성자 (ID 없음)
     */
    public CrawlResultEntity(
        Long taskId,
        String taskType,
        Long sellerId,
        String rawData,
        LocalDateTime crawledAt,
        LocalDateTime createdAt
    ) {
        this(null, taskId, taskType, sellerId, rawData, crawledAt, createdAt);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getTaskType() {
        return taskType;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getRawData() {
        return rawData;
    }

    public LocalDateTime getCrawledAt() {
        return crawledAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
