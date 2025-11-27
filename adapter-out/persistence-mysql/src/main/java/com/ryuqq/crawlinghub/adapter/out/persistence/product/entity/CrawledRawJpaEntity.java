package com.ryuqq.crawlinghub.adapter.out.persistence.product.entity;

import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import com.ryuqq.crawlinghub.domain.product.vo.RawDataStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * CrawledRawJpaEntity - CrawledRaw JPA Entity
 *
 * <p>Persistence Layer의 JPA Entity로서 crawled_raw 테이블과 매핑됩니다.
 *
 * <p><strong>Long FK 전략:</strong>
 *
 * <ul>
 *   <li>JPA 관계 어노테이션 사용 금지 (@ManyToOne, @OneToMany 등)
 *   <li>모든 외래키는 Long 타입으로 직접 관리
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
 * <p><strong>시간 필드:</strong>
 *
 * <ul>
 *   <li>createdAt, processedAt: Instant 타입 (Domain과 일치)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Entity
@Table(name = "crawled_raw")
public class CrawledRawJpaEntity {

    /** 기본 키 - AUTO_INCREMENT */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 스케줄러 ID (FK) */
    @Column(name = "crawl_scheduler_id", nullable = false)
    private long crawlSchedulerId;

    /** 셀러 ID (FK) */
    @Column(name = "seller_id", nullable = false)
    private long sellerId;

    /** 상품 번호 */
    @Column(name = "item_no", nullable = false)
    private long itemNo;

    /** 크롤링 타입 (MINI_SHOP/DETAIL/OPTION) */
    @Enumerated(EnumType.STRING)
    @Column(name = "crawl_type", nullable = false, length = 20)
    private CrawlType crawlType;

    /** JSON 형태의 파싱 결과 */
    @Lob
    @Column(name = "raw_data", nullable = false, columnDefinition = "LONGTEXT")
    private String rawData;

    /** 처리 상태 (PENDING/PROCESSED/FAILED) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RawDataStatus status;

    /** 처리 실패 시 에러 메시지 */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /** 생성 일시 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** 처리 완료 일시 */
    @Column(name = "processed_at")
    private Instant processedAt;

    /**
     * JPA 기본 생성자 (protected)
     *
     * <p>JPA 스펙 요구사항으로 반드시 필요합니다.
     */
    protected CrawledRawJpaEntity() {}

    /**
     * 전체 필드 생성자 (private)
     *
     * <p>직접 호출 금지, of() 스태틱 메서드로만 생성하세요.
     */
    private CrawledRawJpaEntity(
            Long id,
            long crawlSchedulerId,
            long sellerId,
            long itemNo,
            CrawlType crawlType,
            String rawData,
            RawDataStatus status,
            String errorMessage,
            Instant createdAt,
            Instant processedAt) {
        this.id = id;
        this.crawlSchedulerId = crawlSchedulerId;
        this.sellerId = sellerId;
        this.itemNo = itemNo;
        this.crawlType = crawlType;
        this.rawData = rawData;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    /**
     * of() 스태틱 팩토리 메서드 (Mapper 전용)
     *
     * <p>Entity 생성은 반드시 이 메서드를 통해서만 가능합니다.
     *
     * @param id 기본 키
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 셀러 ID
     * @param itemNo 상품 번호
     * @param crawlType 크롤링 타입
     * @param rawData JSON 파싱 결과
     * @param status 처리 상태
     * @param errorMessage 에러 메시지
     * @param createdAt 생성 일시
     * @param processedAt 처리 완료 일시
     * @return CrawledRawJpaEntity 인스턴스
     */
    public static CrawledRawJpaEntity of(
            Long id,
            long crawlSchedulerId,
            long sellerId,
            long itemNo,
            CrawlType crawlType,
            String rawData,
            RawDataStatus status,
            String errorMessage,
            Instant createdAt,
            Instant processedAt) {
        return new CrawledRawJpaEntity(
                id, crawlSchedulerId, sellerId, itemNo, crawlType,
                rawData, status, errorMessage, createdAt, processedAt);
    }

    // ===== Getters (Setter 제공 금지) =====

    public Long getId() {
        return id;
    }

    public long getCrawlSchedulerId() {
        return crawlSchedulerId;
    }

    public long getSellerId() {
        return sellerId;
    }

    public long getItemNo() {
        return itemNo;
    }

    public CrawlType getCrawlType() {
        return crawlType;
    }

    public String getRawData() {
        return rawData;
    }

    public RawDataStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
