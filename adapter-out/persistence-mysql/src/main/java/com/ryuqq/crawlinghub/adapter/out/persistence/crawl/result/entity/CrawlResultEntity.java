package com.ryuqq.crawlinghub.adapter.out.persistence.crawl.result.entity;

import com.ryuqq.crawlinghub.adapter.out.persistence.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 크롤링 결과 Entity (Persistence Layer)
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ BaseAuditEntity 상속 - createdAt, updatedAt 자동 관리</li>
 *   <li>✅ Lombok 금지 - Pure Java</li>
 *   <li>✅ Long FK 전략 - taskId, sellerId는 Long 타입</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Entity
@Table(name = "crawl_result")
public class CrawlResultEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "task_type", nullable = false, length = 50)
    private String taskType;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "raw_data", nullable = false, columnDefinition = "JSON")
    private String rawData;

    /**
     * JPA 전용 기본 생성자
     */
    protected CrawlResultEntity() {
        super();
    }

    /**
     * 전체 생성자 (Reconstitute 전용)
     *
     * @param id ID
     * @param taskId 작업 ID
     * @param taskType 작업 타입
     * @param sellerId 셀러 ID
     * @param rawData 원본 데이터 (JSON)
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     */
    public CrawlResultEntity(
        Long id,
        Long taskId,
        String taskType,
        Long sellerId,
        String rawData,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        super(createdAt, updatedAt);
        this.id = id;
        this.taskId = taskId;
        this.taskType = taskType;
        this.sellerId = sellerId;
        this.rawData = rawData;
    }

    /**
     * 신규 생성용 생성자 (ID 없음)
     *
     * @param taskId 작업 ID
     * @param taskType 작업 타입
     * @param sellerId 셀러 ID
     * @param rawData 원본 데이터 (JSON)
     */
    public CrawlResultEntity(
        Long taskId,
        String taskType,
        Long sellerId,
        String rawData
    ) {
        super();
        this.taskId = taskId;
        this.taskType = taskType;
        this.sellerId = sellerId;
        this.rawData = rawData;
        initializeAuditFields();
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
}
