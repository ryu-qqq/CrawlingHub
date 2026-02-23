package com.ryuqq.crawlinghub.domain.product.aggregate;

import com.ryuqq.crawlinghub.domain.product.id.CrawledRawId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import com.ryuqq.crawlinghub.domain.product.vo.RawDataStatus;
import java.time.Instant;
import java.util.Objects;

/**
 * 크롤링 Raw 데이터 Aggregate Root
 *
 * <p>파싱된 크롤링 결과를 JSON 형태로 저장하는 단일 테이블 구조입니다. 타입(MINI_SHOP, DETAIL, OPTION)에 따라 다른 JSON 스키마를 가집니다.
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>크롤링 완료 → CrawledRaw 벌크 저장 (PENDING)
 *   <li>가공 스케줄러 → MINI_SHOP 먼저 처리 → CrawledProduct 생성
 *   <li>가공 스케줄러 → DETAIL/OPTION 처리 → CrawledProduct 업데이트
 *   <li>처리 완료 → PROCESSED / 실패 → FAILED
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
public class CrawledRaw {

    private final CrawledRawId id;
    private final long crawlSchedulerId;
    private final long sellerId;
    private final long itemNo;
    private final CrawlType crawlType;
    private final String rawData;
    private final RawDataStatus status;
    private final String errorMessage;
    private final Instant createdAt;
    private final Instant processedAt;

    private CrawledRaw(
            CrawledRawId id,
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
     * 신규 CrawledRaw 생성 (PENDING 상태)
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param sellerId 판매자 ID
     * @param itemNo 상품 번호
     * @param crawlType 크롤링 타입
     * @param rawData JSON 형태의 파싱 결과
     * @param now 현재 시각
     * @return 새 CrawledRaw
     */
    public static CrawledRaw forNew(
            long crawlSchedulerId,
            long sellerId,
            long itemNo,
            CrawlType crawlType,
            String rawData,
            Instant now) {
        if (crawlType == null) {
            throw new IllegalArgumentException("crawlType은 필수입니다.");
        }
        if (rawData == null || rawData.isBlank()) {
            throw new IllegalArgumentException("rawData는 필수입니다.");
        }
        return new CrawledRaw(
                CrawledRawId.forNew(),
                crawlSchedulerId,
                sellerId,
                itemNo,
                crawlType,
                rawData,
                RawDataStatus.PENDING,
                null,
                now,
                null);
    }

    /** DB에서 복원 */
    public static CrawledRaw reconstitute(
            CrawledRawId id,
            long crawlSchedulerId,
            long sellerId,
            long itemNo,
            CrawlType crawlType,
            String rawData,
            RawDataStatus status,
            String errorMessage,
            Instant createdAt,
            Instant processedAt) {
        return new CrawledRaw(
                id,
                crawlSchedulerId,
                sellerId,
                itemNo,
                crawlType,
                rawData,
                status,
                errorMessage,
                createdAt,
                processedAt);
    }

    /**
     * 처리 완료 상태로 변경
     *
     * @param now 현재 시각
     * @return 처리 완료된 CrawledRaw
     */
    public CrawledRaw markAsProcessed(Instant now) {
        return new CrawledRaw(
                this.id,
                this.crawlSchedulerId,
                this.sellerId,
                this.itemNo,
                this.crawlType,
                this.rawData,
                RawDataStatus.PROCESSED,
                null,
                this.createdAt,
                now);
    }

    /**
     * 처리 실패 상태로 변경
     *
     * @param errorMessage 에러 메시지
     * @param now 현재 시각
     * @return 처리 실패한 CrawledRaw
     */
    public CrawledRaw markAsFailed(String errorMessage, Instant now) {
        return new CrawledRaw(
                this.id,
                this.crawlSchedulerId,
                this.sellerId,
                this.itemNo,
                this.crawlType,
                this.rawData,
                RawDataStatus.FAILED,
                errorMessage,
                this.createdAt,
                now);
    }

    // === Getters ===

    public CrawledRawId getId() {
        return id;
    }

    public Long getIdValue() {
        return id != null ? id.value() : null;
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

    // === 상태 확인 ===

    public boolean isPending() {
        return status == RawDataStatus.PENDING;
    }

    public boolean isProcessed() {
        return status == RawDataStatus.PROCESSED;
    }

    public boolean isFailed() {
        return status == RawDataStatus.FAILED;
    }

    public boolean isMiniShop() {
        return crawlType == CrawlType.MINI_SHOP;
    }

    public boolean isDetail() {
        return crawlType == CrawlType.DETAIL;
    }

    public boolean isOption() {
        return crawlType == CrawlType.OPTION;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CrawledRaw that = (CrawledRaw) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
