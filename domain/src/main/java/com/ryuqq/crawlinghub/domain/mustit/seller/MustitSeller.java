package com.ryuqq.crawlinghub.domain.mustit.seller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 머스트잇 셀러 Aggregate Root
 * 
 * <p>비즈니스 규칙:
 * <ul>
 *   <li>셀러 코드는 변경 불가능 (불변)</li>
 *   <li>DISABLED 상태에서는 크롤링 불가</li>
 *   <li>상품 수는 음수 불가</li>
 * </ul>
 */
public class MustitSeller {

    private final MustitSellerId id;
    private final String sellerCode;
    private final String sellerName;
    private SellerStatus status;
    private Integer totalProductCount;
    private LocalDateTime lastCrawledAt;
    private final Clock clock;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Private 전체 생성자 (reconstitute 전용)
     */
    private MustitSeller(
        MustitSellerId id,
        String sellerCode,
        String sellerName,
        SellerStatus status,
        Integer totalProductCount,
        LocalDateTime lastCrawledAt,
        Clock clock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.sellerCode = sellerCode;
        this.sellerName = sellerName;
        this.status = status;
        this.totalProductCount = totalProductCount;
        this.lastCrawledAt = lastCrawledAt;
        this.clock = clock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Package-private 주요 생성자 (검증 포함)
     */
    MustitSeller(
        MustitSellerId id,
        String sellerCode,
        String sellerName,
        SellerStatus status,
        Clock clock
    ) {
        validateRequiredFields(sellerCode, sellerName, status);

        this.id = id;
        this.sellerCode = sellerCode;
        this.sellerName = sellerName;
        this.status = status;
        this.totalProductCount = 0;
        this.lastCrawledAt = null;
        this.clock = clock;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 신규 셀러 생성 (ID 없음)
     */
    public static MustitSeller forNew(String sellerCode, String sellerName) {
        return new MustitSeller(
            null,
            sellerCode,
            sellerName,
            SellerStatus.ACTIVE,
            Clock.systemDefaultZone()
        );
    }

    /**
     * 기존 셀러 생성 (ID 있음)
     */
    public static MustitSeller of(
        MustitSellerId id,
        String sellerCode,
        String sellerName,
        SellerStatus status
    ) {
        if (id == null) {
            throw new IllegalArgumentException("MustitSeller ID는 필수입니다");
        }
        return new MustitSeller(id, sellerCode, sellerName, status, Clock.systemDefaultZone());
    }

    /**
     * DB reconstitute (모든 필드 포함)
     */
    public static MustitSeller reconstitute(
        MustitSellerId id,
        String sellerCode,
        String sellerName,
        SellerStatus status,
        Integer totalProductCount,
        LocalDateTime lastCrawledAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new MustitSeller(
            id,
            sellerCode,
            sellerName,
            status,
            totalProductCount,
            lastCrawledAt,
            Clock.systemDefaultZone(),
            createdAt,
            updatedAt
        );
    }

    private static void validateRequiredFields(String sellerCode, String sellerName, SellerStatus status) {
        if (sellerCode == null || sellerCode.isBlank()) {
            throw new IllegalArgumentException("셀러 코드는 필수입니다");
        }
        if (sellerName == null || sellerName.isBlank()) {
            throw new IllegalArgumentException("셀러 이름은 필수입니다");
        }
        if (status == null) {
            throw new IllegalArgumentException("셀러 상태는 필수입니다");
        }
    }

    /**
     * 셀러 활성화
     */
    public void activate() {
        this.status = SellerStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 셀러 일시정지
     */
    public void pause() {
        this.status = SellerStatus.PAUSED;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 셀러 비활성화
     */
    public void disable() {
        this.status = SellerStatus.DISABLED;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 상품 수 업데이트
     */
    public void updateProductCount(Integer count) {
        if (count == null || count < 0) {
            throw new IllegalArgumentException("상품 수는 0 이상이어야 합니다");
        }
        this.totalProductCount = count;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 크롤링 완료 기록
     */
    public void recordCrawlingComplete() {
        this.lastCrawledAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 크롤링 가능 여부 확인
     */
    public boolean canCrawl() {
        return status.canCrawl();
    }

    /**
     * 활성 상태 여부 확인
     */
    public boolean isActive() {
        return status.isActive();
    }

    /**
     * 특정 상태인지 확인
     */
    public boolean hasStatus(SellerStatus targetStatus) {
        return this.status == targetStatus;
    }

    // Law of Demeter 준수 메서드
    public Long getIdValue() {
        return id != null ? id.value() : null;
    }

    public String getSellerCode() {
        return sellerCode;
    }

    public String getSellerName() {
        return sellerName;
    }

    public SellerStatus getStatus() {
        return status;
    }

    public Integer getTotalProductCount() {
        return totalProductCount;
    }

    public LocalDateTime getLastCrawledAt() {
        return lastCrawledAt;
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
        MustitSeller that = (MustitSeller) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MustitSeller{" +
            "id=" + id +
            ", sellerCode='" + sellerCode + '\'' +
            ", sellerName='" + sellerName + '\'' +
            ", status=" + status +
            ", totalProductCount=" + totalProductCount +
            '}';
    }
}
