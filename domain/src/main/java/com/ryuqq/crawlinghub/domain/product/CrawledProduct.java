package com.ryuqq.crawlinghub.domain.product;

import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSellerId;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 크롤링된 상품 Aggregate Root
 * 
 * <p>비즈니스 규칙:
 * <ul>
 *   <li>상품 완성 조건: 미니샵 + 상세 + 옵션 모두 존재</li>
 *   <li>해시 불일치 시 변경 감지</li>
 *   <li>버전은 변경 시마다 증가</li>
 * </ul>
 */
public class CrawledProduct {

    private final ProductId id;
    private final String mustitItemNo;
    private final MustitSellerId sellerId;
    private ProductData miniShopData;
    private ProductData detailData;
    private ProductData optionData;
    private DataHash dataHash;
    private Integer version;
    private CompletionStatus status;
    private final LocalDateTime firstCrawledAt;
    private LocalDateTime lastUpdatedAt;
    private final Clock clock;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Private 전체 생성자 (reconstitute 전용)
     */
    private CrawledProduct(
        ProductId id,
        String mustitItemNo,
        MustitSellerId sellerId,
        ProductData miniShopData,
        ProductData detailData,
        ProductData optionData,
        DataHash dataHash,
        Integer version,
        CompletionStatus status,
        LocalDateTime firstCrawledAt,
        LocalDateTime lastUpdatedAt,
        Clock clock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.mustitItemNo = mustitItemNo;
        this.sellerId = sellerId;
        this.miniShopData = miniShopData;
        this.detailData = detailData;
        this.optionData = optionData;
        this.dataHash = dataHash;
        this.version = version;
        this.status = status;
        this.firstCrawledAt = firstCrawledAt;
        this.lastUpdatedAt = lastUpdatedAt;
        this.clock = clock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Package-private 주요 생성자 (검증 포함)
     */
    CrawledProduct(
        ProductId id,
        String mustitItemNo,
        MustitSellerId sellerId,
        Clock clock
    ) {
        validateRequiredFields(mustitItemNo, sellerId);

        LocalDateTime now = LocalDateTime.now(clock);
        this.id = id;
        this.mustitItemNo = mustitItemNo;
        this.sellerId = sellerId;
        this.miniShopData = null;
        this.detailData = null;
        this.optionData = null;
        this.dataHash = null;
        this.version = 1;
        this.status = CompletionStatus.INCOMPLETE;
        this.firstCrawledAt = now;
        this.lastUpdatedAt = now;
        this.clock = clock;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * 신규 상품 생성 (ID 없음)
     */
    public static CrawledProduct forNew(String mustitItemNo, MustitSellerId sellerId) {
        return new CrawledProduct(null, mustitItemNo, sellerId, Clock.systemDefaultZone());
    }

    /**
     * 기존 상품 생성 (ID 있음)
     */
    public static CrawledProduct of(ProductId id, String mustitItemNo, MustitSellerId sellerId) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID는 필수입니다");
        }
        return new CrawledProduct(id, mustitItemNo, sellerId, Clock.systemDefaultZone());
    }

    /**
     * DB reconstitute (모든 필드 포함)
     */
    public static CrawledProduct reconstitute(
        ProductId id,
        String mustitItemNo,
        MustitSellerId sellerId,
        ProductData miniShopData,
        ProductData detailData,
        ProductData optionData,
        DataHash dataHash,
        Integer version,
        CompletionStatus status,
        LocalDateTime firstCrawledAt,
        LocalDateTime lastUpdatedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new CrawledProduct(
            id,
            mustitItemNo,
            sellerId,
            miniShopData,
            detailData,
            optionData,
            dataHash,
            version,
            status,
            firstCrawledAt,
            lastUpdatedAt,
            Clock.systemDefaultZone(),
            createdAt,
            updatedAt
        );
    }

    private static void validateRequiredFields(String mustitItemNo, MustitSellerId sellerId) {
        if (mustitItemNo == null || mustitItemNo.isBlank()) {
            throw new IllegalArgumentException("머스트잇 상품 번호는 필수입니다");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("셀러 ID는 필수입니다");
        }
    }

    /**
     * 미니샵 데이터 업데이트
     */
    public void updateMiniShopData(ProductData data) {
        if (data == null) {
            throw new IllegalArgumentException("미니샵 데이터는 null일 수 없습니다");
        }
        this.miniShopData = data;
        this.lastUpdatedAt = LocalDateTime.now(clock);
        updateCompletionStatus();
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 상세 데이터 업데이트
     */
    public void updateDetailData(ProductData data) {
        if (data == null) {
            throw new IllegalArgumentException("상세 데이터는 null일 수 없습니다");
        }
        this.detailData = data;
        this.lastUpdatedAt = LocalDateTime.now(clock);
        updateCompletionStatus();
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 옵션 데이터 업데이트
     */
    public void updateOptionData(ProductData data) {
        if (data == null) {
            throw new IllegalArgumentException("옵션 데이터는 null일 수 없습니다");
        }
        this.optionData = data;
        this.lastUpdatedAt = LocalDateTime.now(clock);
        updateCompletionStatus();
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 완성 상태 업데이트
     */
    private void updateCompletionStatus() {
        if (isComplete()) {
            this.status = CompletionStatus.COMPLETE;
        } else {
            this.status = CompletionStatus.INCOMPLETE;
        }
    }

    /**
     * 완성 여부 확인
     */
    public boolean isComplete() {
        return miniShopData != null && detailData != null && optionData != null;
    }

    /**
     * 데이터 변경 확인
     */
    public boolean hasDataChanged(DataHash newHash) {
        if (newHash == null) {
            return false;
        }
        if (this.dataHash == null) {
            return true;
        }
        return !this.dataHash.isSameAs(newHash);
    }

    /**
     * 데이터 해시 업데이트
     */
    public void updateDataHash(DataHash newHash) {
        if (newHash == null) {
            throw new IllegalArgumentException("데이터 해시는 null일 수 없습니다");
        }
        this.dataHash = newHash;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 버전 증가
     */
    public void incrementVersion() {
        this.version++;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 특정 상태인지 확인
     */
    public boolean hasStatus(CompletionStatus targetStatus) {
        return this.status == targetStatus;
    }

    // Law of Demeter 준수 메서드
    public Long getIdValue() {
        return id != null ? id.value() : null;
    }

    public String getMustitItemNo() {
        return mustitItemNo;
    }

    public Long getSellerIdValue() {
        return sellerId != null ? sellerId.value() : null;
    }

    public String getMiniShopDataValue() {
        return miniShopData != null ? miniShopData.getValue() : null;
    }

    public String getDetailDataValue() {
        return detailData != null ? detailData.getValue() : null;
    }

    public String getOptionDataValue() {
        return optionData != null ? optionData.getValue() : null;
    }

    public String getDataHashValue() {
        return dataHash != null ? dataHash.getValue() : null;
    }

    public Integer getVersion() {
        return version;
    }

    public CompletionStatus getStatus() {
        return status;
    }

    public LocalDateTime getFirstCrawledAt() {
        return firstCrawledAt;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CrawledProduct that = (CrawledProduct) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CrawledProduct{" +
            "id=" + id +
            ", mustitItemNo='" + mustitItemNo + '\'' +
            ", sellerId=" + sellerId +
            ", version=" + version +
            ", status=" + status +
            '}';
    }
}
