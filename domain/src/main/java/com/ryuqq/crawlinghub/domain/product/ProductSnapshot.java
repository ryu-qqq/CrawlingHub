package com.ryuqq.crawlinghub.domain.product;

import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 상품 스냅샷 Aggregate Root
 *
 * <p>역할: 항상 완전한 상품 데이터를 유지하는 Materialized View
 *
 * <p>비즈니스 규칙:
 * <ul>
 *   <li>부분 데이터 수신 시 점진적으로 업데이트</li>
 *   <li>완전성 검증: 필수 필드가 모두 채워진 경우만 완전함</li>
 *   <li>버전 관리: 변경 시마다 버전 증가</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public class ProductSnapshot {

    private final ProductSnapshotId id;
    private final Long mustItItemNo;
    private final MustitSellerId sellerId;

    // 미니샵 데이터
    private String productName;
    private Long price;
    private String mainImageUrl;

    // 옵션 데이터
    private List<ProductOption> options;
    private Integer totalStock;

    // 상세 데이터
    private ProductInfoModule productInfo;
    private ShippingModule shipping;
    private ProductDetailInfoModule detailInfo;

    // 메타 정보
    private LocalDateTime lastSyncedAt;
    private Integer version;
    private final Clock clock;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Private 전체 생성자 (reconstitute 전용)
     */
    private ProductSnapshot(
        ProductSnapshotId id,
        Long mustItItemNo,
        MustitSellerId sellerId,
        String productName,
        Long price,
        String mainImageUrl,
        List<ProductOption> options,
        Integer totalStock,
        ProductInfoModule productInfo,
        ShippingModule shipping,
        ProductDetailInfoModule detailInfo,
        LocalDateTime lastSyncedAt,
        Integer version,
        Clock clock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.mustItItemNo = mustItItemNo;
        this.sellerId = sellerId;
        this.productName = productName;
        this.price = price;
        this.mainImageUrl = mainImageUrl;
        this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
        this.totalStock = totalStock;
        this.productInfo = productInfo;
        this.shipping = shipping;
        this.detailInfo = detailInfo;
        this.lastSyncedAt = lastSyncedAt;
        this.version = version;
        this.clock = clock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Package-private 주요 생성자 (검증 포함)
     */
    ProductSnapshot(
        ProductSnapshotId id,
        Long mustItItemNo,
        MustitSellerId sellerId,
        Clock clock
    ) {
        validateRequiredFields(mustItItemNo, sellerId);

        LocalDateTime now = LocalDateTime.now(clock);
        this.id = id;
        this.mustItItemNo = mustItItemNo;
        this.sellerId = sellerId;
        this.productName = null;
        this.price = null;
        this.mainImageUrl = null;
        this.options = new ArrayList<>();
        this.totalStock = null;
        this.productInfo = null;
        this.shipping = null;
        this.detailInfo = null;
        this.lastSyncedAt = null;
        this.version = 1;
        this.clock = clock;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * 신규 Snapshot 생성 (ID 없음)
     */
    public static ProductSnapshot forNew(Long mustItItemNo, MustitSellerId sellerId) {
        return new ProductSnapshot(null, mustItItemNo, sellerId, Clock.systemDefaultZone());
    }

    /**
     * 기존 Snapshot 생성 (ID 있음)
     */
    public static ProductSnapshot of(ProductSnapshotId id, Long mustItItemNo, MustitSellerId sellerId) {
        if (id == null) {
            throw new IllegalArgumentException("ProductSnapshot ID는 필수입니다");
        }
        return new ProductSnapshot(id, mustItItemNo, sellerId, Clock.systemDefaultZone());
    }

    /**
     * DB reconstitute (모든 필드 포함)
     */
    public static ProductSnapshot reconstitute(
        ProductSnapshotId id,
        Long mustItItemNo,
        MustitSellerId sellerId,
        String productName,
        Long price,
        String mainImageUrl,
        List<ProductOption> options,
        Integer totalStock,
        ProductInfoModule productInfo,
        ShippingModule shipping,
        ProductDetailInfoModule detailInfo,
        LocalDateTime lastSyncedAt,
        Integer version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new ProductSnapshot(
            id, mustItItemNo, sellerId,
            productName, price, mainImageUrl,
            options, totalStock,
            productInfo, shipping, detailInfo,
            lastSyncedAt, version,
            Clock.systemDefaultZone(),
            createdAt, updatedAt
        );
    }

    private static void validateRequiredFields(Long mustItItemNo, MustitSellerId sellerId) {
        if (mustItItemNo == null) {
            throw new IllegalArgumentException("머스트잇 상품 번호는 필수입니다");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("셀러 ID는 필수입니다");
        }
    }

    /**
     * 미니샵 데이터 업데이트
     */
    public void updateProductName(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다");
        }
        this.productName = productName;
        incrementVersion();
    }

    public void updatePrice(Long price) {
        if (price == null || price < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다");
        }
        this.price = price;
        incrementVersion();
    }

    public void updateMainImage(String mainImageUrl) {
        if (mainImageUrl == null || mainImageUrl.isBlank()) {
            throw new IllegalArgumentException("메인 이미지 URL은 필수입니다");
        }
        this.mainImageUrl = mainImageUrl;
        incrementVersion();
    }

    /**
     * 옵션 데이터 업데이트
     */
    public void updateOptions(List<ProductOption> options) {
        if (options == null) {
            throw new IllegalArgumentException("옵션 리스트는 null일 수 없습니다");
        }
        this.options = new ArrayList<>(options);
        incrementVersion();
    }

    public void updateTotalStock(Integer totalStock) {
        if (totalStock == null || totalStock < 0) {
            throw new IllegalArgumentException("총 재고는 0 이상이어야 합니다");
        }
        this.totalStock = totalStock;
        incrementVersion();
    }

    /**
     * 상세 데이터 업데이트
     */
    public void updateProductInfo(ProductInfoModule productInfo) {
        if (productInfo == null) {
            throw new IllegalArgumentException("상품 정보 모듈은 필수입니다");
        }
        this.productInfo = productInfo;
        incrementVersion();
    }

    public void updateShipping(ShippingModule shipping) {
        if (shipping == null) {
            throw new IllegalArgumentException("배송 모듈은 필수입니다");
        }
        this.shipping = shipping;
        incrementVersion();
    }

    public void updateDetailInfo(ProductDetailInfoModule detailInfo) {
        if (detailInfo == null) {
            throw new IllegalArgumentException("상세 정보 모듈은 필수입니다");
        }
        this.detailInfo = detailInfo;
        incrementVersion();
    }

    /**
     * 외부 동기화 완료 기록
     */
    public void recordSyncCompleted() {
        this.lastSyncedAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 버전 증가
     */
    private void incrementVersion() {
        this.version++;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 완전성 검증
     *
     * <p>최소 필수 필드:
     * <ul>
     *   <li>productName (미니샵)</li>
     *   <li>price (미니샵)</li>
     *   <li>options (옵션)</li>
     * </ul>
     *
     * <p>상세 데이터는 선택적 (없어도 동기화 가능)
     */
    public boolean isComplete() {
        return hasProductName()
            && hasPrice()
            && hasOptions();
    }

    /**
     * 동기화 준비 여부 확인
     */
    public boolean isReadyForSync() {
        return isComplete();
    }

    /**
     * 전체 상품 데이터로 변환 (외부 동기화용)
     */
    public FullProductData toFullProductData() {
        if (!isComplete()) {
            throw new IllegalStateException("불완전한 Snapshot은 FullProductData로 변환할 수 없습니다");
        }
        return new FullProductData(
            mustItItemNo,
            productName,
            price,
            mainImageUrl,
            options,
            totalStock,
            productInfo,
            shipping,
            detailInfo
        );
    }

    // Law of Demeter 준수 메서드
    public Long getIdValue() {
        return id != null ? id.value() : null;
    }

    public Long getMustItItemNo() {
        return mustItItemNo;
    }

    public Long getSellerIdValue() {
        return sellerId != null ? sellerId.value() : null;
    }

    public String getProductName() {
        return productName;
    }

    public Long getPrice() {
        return price;
    }

    public String getMainImageUrl() {
        return mainImageUrl;
    }

    public List<ProductOption> getOptions() {
        return new ArrayList<>(options);
    }

    public Integer getTotalStock() {
        return totalStock;
    }

    public ProductInfoModule getProductInfo() {
        return productInfo;
    }

    public ShippingModule getShipping() {
        return shipping;
    }

    public ProductDetailInfoModule getDetailInfo() {
        return detailInfo;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public Integer getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // 필드 존재 여부 확인 메서드
    public boolean hasProductName() {
        return productName != null && !productName.isBlank();
    }

    public boolean hasPrice() {
        return price != null;
    }

    public boolean hasMainImage() {
        return mainImageUrl != null && !mainImageUrl.isBlank();
    }

    public boolean hasOptions() {
        return options != null && !options.isEmpty();
    }

    public boolean hasProductInfo() {
        return productInfo != null;
    }

    public boolean hasShipping() {
        return shipping != null;
    }

    public boolean hasDetailInfo() {
        return detailInfo != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductSnapshot that = (ProductSnapshot) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProductSnapshot{" +
            "id=" + id +
            ", mustItItemNo=" + mustItItemNo +
            ", sellerId=" + sellerId +
            ", version=" + version +
            ", isComplete=" + isComplete() +
            '}';
    }
}

