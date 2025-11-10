package com.ryuqq.crawlinghub.domain.crawl.result;

import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.task.TaskId;
import com.ryuqq.crawlinghub.domain.task.TaskType;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 크롤링 결과 Aggregate Root
 *
 * <p>역할: 크롤링 API 응답을 JSON 문자열로 저장
 *
 * <p>특징:
 * <ul>
 *   <li>원본 JSON 데이터 보존 (rawData)</li>
 *   <li>데이터 출처 추적 (taskId, taskType, sellerId)</li>
 *   <li>크롤링 메타데이터 저장 (crawledAt)</li>
 * </ul>
 *
 * <p>사용처:
 * <ul>
 *   <li>META Task 결과 (전체 상품 개수)</li>
 *   <li>MINI_SHOP Task 결과 (상품 목록)</li>
 *   <li>PRODUCT_DETAIL Task 결과 (상품 상세)</li>
 *   <li>PRODUCT_OPTION Task 결과 (상품 옵션)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public class CrawlResult {

    private final CrawlResultId id;
    private final TaskId taskId;
    private final TaskType taskType;
    private final MustitSellerId sellerId;
    private final String rawData;
    private final LocalDateTime crawledAt;
    private final LocalDateTime createdAt;

    /**
     * Private 전체 생성자 (reconstitute 전용)
     */
    private CrawlResult(
        CrawlResultId id,
        TaskId taskId,
        TaskType taskType,
        MustitSellerId sellerId,
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
     * 신규 크롤링 결과 생성
     *
     * @param taskId Task ID
     * @param taskType Task 타입 (META, MINI_SHOP, PRODUCT_DETAIL, PRODUCT_OPTION)
     * @param sellerId Seller ID
     * @param rawData 크롤링 원본 JSON 데이터
     * @param crawledAt 크롤링 수행 시각
     * @return CrawlResult 인스턴스
     */
    public static CrawlResult create(
        TaskId taskId,
        TaskType taskType,
        MustitSellerId sellerId,
        String rawData,
        LocalDateTime crawledAt
    ) {
        validateRequiredFields(taskId, taskType, sellerId, rawData, crawledAt);

        return new CrawlResult(
            null,
            taskId,
            taskType,
            sellerId,
            rawData,
            crawledAt,
            LocalDateTime.now()
        );
    }

    /**
     * DB reconstitute (모든 필드 포함)
     */
    public static CrawlResult reconstitute(
        CrawlResultId id,
        TaskId taskId,
        TaskType taskType,
        MustitSellerId sellerId,
        String rawData,
        LocalDateTime crawledAt,
        LocalDateTime createdAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new CrawlResult(
            id,
            taskId,
            taskType,
            sellerId,
            rawData,
            crawledAt,
            createdAt
        );
    }

    private static void validateRequiredFields(
        TaskId taskId,
        TaskType taskType,
        MustitSellerId sellerId,
        String rawData,
        LocalDateTime crawledAt
    ) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID는 필수입니다");
        }
        if (taskType == null) {
            throw new IllegalArgumentException("Task 타입은 필수입니다");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("Seller ID는 필수입니다");
        }
        if (rawData == null || rawData.isBlank()) {
            throw new IllegalArgumentException("크롤링 데이터는 필수입니다");
        }
        if (crawledAt == null) {
            throw new IllegalArgumentException("크롤링 시각은 필수입니다");
        }
    }

    // Law of Demeter 준수 메서드
    public Long getIdValue() {
        return id != null ? id.value() : null;
    }

    public Long getTaskIdValue() {
        return taskId != null ? taskId.value() : null;
    }

    public TaskId getTaskId() {
        return taskId;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public Long getSellerIdValue() {
        return sellerId != null ? sellerId.value() : null;
    }

    public MustitSellerId getSellerId() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CrawlResult that = (CrawlResult) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CrawlResult{" +
            "id=" + id +
            ", taskId=" + taskId +
            ", taskType=" + taskType +
            ", sellerId=" + sellerId +
            ", crawledAt=" + crawledAt +
            ", createdAt=" + createdAt +
            '}';
    }
}
