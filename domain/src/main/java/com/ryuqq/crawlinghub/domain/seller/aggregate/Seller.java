package com.ryuqq.crawlinghub.domain.seller.aggregate;

import com.ryuqq.crawlinghub.domain.common.Clock;
import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import com.ryuqq.crawlinghub.domain.seller.event.SellerDeActiveEvent;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 셀러 Aggregate Root
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>MustItSellerName, SellerName은 중복 불가 (외부 검증 필요)
 *   <li>상태를 INACTIVE로 변경 시 SellerDeActiveEvent 발행
 *   <li>비활성화 시 크롤링 스케줄 중지 필요
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class Seller {

    // ==================== 필드 ====================

    private final SellerId sellerId;
    private MustItSellerName mustItSellerName;
    private SellerName sellerName;
    private SellerStatus status;
    private int productCount;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final Clock clock;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // ==================== 생성 메서드 (3종) ====================

    /**
     * 신규 생성 (Auto Increment ID)
     *
     * @param mustItSellerName 머스트잇 셀러명 (중복 불가)
     * @param sellerName 커머스 셀러명 (중복 불가)
     * @param clock 시간 제어
     * @return 신규 Seller
     */
    public static Seller forNew(
            MustItSellerName mustItSellerName, SellerName sellerName, Clock clock) {
        LocalDateTime now = LocalDateTime.ofInstant(clock.now(), ZoneId.systemDefault());
        return new Seller(
                null, // Auto Increment: ID null
                mustItSellerName,
                sellerName,
                SellerStatus.ACTIVE,
                0, // 신규 셀러는 상품 수 0
                now,
                now,
                clock);
    }

    /**
     * ID 기반 생성 (비즈니스 로직용)
     *
     * @param sellerId 셀러 ID (null 불가)
     * @param mustItSellerName 머스트잇 셀러명
     * @param sellerName 커머스 셀러명
     * @param status 셀러 상태
     * @param productCount 상품 수
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @param clock 시간 제어
     * @return Seller
     */
    public static Seller of(
            SellerId sellerId,
            MustItSellerName mustItSellerName,
            SellerName sellerName,
            SellerStatus status,
            int productCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Clock clock) {
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId는 null일 수 없습니다.");
        }
        return new Seller(
                sellerId, mustItSellerName, sellerName, status, productCount, createdAt, updatedAt, clock);
    }

    /**
     * 영속성 복원 (Mapper 전용)
     *
     * @param sellerId 셀러 ID (null 불가)
     * @param mustItSellerName 머스트잇 셀러명
     * @param sellerName 커머스 셀러명
     * @param status 셀러 상태
     * @param productCount 상품 수
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @param clock 시간 제어
     * @return Seller
     */
    public static Seller reconstitute(
            SellerId sellerId,
            MustItSellerName mustItSellerName,
            SellerName sellerName,
            SellerStatus status,
            int productCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Clock clock) {
        return of(sellerId, mustItSellerName, sellerName, status, productCount, createdAt, updatedAt, clock);
    }

    /** 생성자 (private) */
    private Seller(
            SellerId sellerId,
            MustItSellerName mustItSellerName,
            SellerName sellerName,
            SellerStatus status,
            int productCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Clock clock) {
        this.sellerId = sellerId;
        this.mustItSellerName = mustItSellerName;
        this.sellerName = sellerName;
        this.status = status;
        this.productCount = productCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.clock = clock;
    }

    // ==================== 비즈니스 메서드 ====================

    /** 셀러 활성화 */
    public void activate() {
        if (this.status == SellerStatus.ACTIVE) {
            return; // 이미 활성 상태면 무시
        }
        this.status = SellerStatus.ACTIVE;
        this.updatedAt = LocalDateTime.ofInstant(clock.now(), ZoneId.systemDefault());
    }

    /**
     * 셀러 비활성화
     *
     * <p><strong>중요</strong>: 비활성화 시 SellerDeActiveEvent를 발행하여 크롤링 스케줄 중지
     */
    public void deactivate() {
        if (this.status == SellerStatus.INACTIVE) {
            return; // 이미 비활성 상태면 무시
        }

        this.status = SellerStatus.INACTIVE;
        this.updatedAt = LocalDateTime.ofInstant(clock.now(), ZoneId.systemDefault());

        // 이벤트 발행: 크롤링 스케줄 중지
        this.domainEvents.add(SellerDeActiveEvent.of(this.sellerId));
    }

    /**
     * 셀러 정보 수정 (통합 메서드)
     *
     * <p>Tell, Don't Ask 패턴 적용: 객체가 스스로 판단하여 처리
     *
     * @param newMustItSellerName 새로운 머스트잇 셀러명 (null이면 변경 안 함)
     * @param newSellerName 새로운 셀러명 (null이면 변경 안 함)
     * @param newStatus 새로운 상태 (null이면 변경 안 함)
     */
    public void update(
            MustItSellerName newMustItSellerName,
            SellerName newSellerName,
            SellerStatus newStatus) {
        // 머스트잇 셀러명 변경 (자기 자신이 판단)
        if (newMustItSellerName != null && !this.mustItSellerName.equals(newMustItSellerName)) {
            this.mustItSellerName = newMustItSellerName;
            this.updatedAt = LocalDateTime.ofInstant(clock.now(), ZoneId.systemDefault());
        }

        // 셀러명 변경 (자기 자신이 판단)
        if (newSellerName != null && !this.sellerName.equals(newSellerName)) {
            this.sellerName = newSellerName;
            this.updatedAt = LocalDateTime.ofInstant(clock.now(), ZoneId.systemDefault());
        }

        // 상태 변경 (자기 자신이 판단)
        if (newStatus != null && this.status != newStatus) {
            if (newStatus == SellerStatus.ACTIVE) {
                activate();
            } else if (newStatus == SellerStatus.INACTIVE) {
                deactivate();
            }
        }
    }

    /**
     * 머스트잇 셀러명 변경 필요 여부
     *
     * @param newMustItSellerName 새로운 머스트잇 셀러명
     * @return 변경 필요 시 true
     */
    public boolean needsUpdateMustItSellerName(MustItSellerName newMustItSellerName) {
        return newMustItSellerName != null && !this.mustItSellerName.equals(newMustItSellerName);
    }

    /**
     * 셀러명 변경 필요 여부
     *
     * @param newSellerName 새로운 셀러명
     * @return 변경 필요 시 true
     */
    public boolean needsUpdateSellerName(SellerName newSellerName) {
        return newSellerName != null && !this.sellerName.equals(newSellerName);
    }

    /**
     * 상품 수 업데이트
     *
     * <p>META 크롤링 결과에서 파싱된 총 상품 수를 업데이트합니다.
     *
     * @param newProductCount 새로운 상품 수 (0 이상)
     */
    public void updateProductCount(int newProductCount) {
        if (newProductCount < 0) {
            throw new IllegalArgumentException("상품 수는 0 이상이어야 합니다: " + newProductCount);
        }
        if (this.productCount != newProductCount) {
            this.productCount = newProductCount;
            this.updatedAt = LocalDateTime.ofInstant(clock.now(), ZoneId.systemDefault());
        }
    }

    // ==================== Getter ====================

    public SellerId getSellerId() {
        return sellerId;
    }

    /** Law of Demeter: 원시 타입이 필요한 경우 별도 메서드 제공 */
    public Long getSellerIdValue() {
        return sellerId != null ? sellerId.value() : null;
    }

    public MustItSellerName getMustItSellerName() {
        return mustItSellerName;
    }

    /** Law of Demeter: 머스트잇 셀러명의 원시값 */
    public String getMustItSellerNameValue() {
        return mustItSellerName.value();
    }

    public SellerName getSellerName() {
        return sellerName;
    }

    /** Law of Demeter: 셀러명의 원시값 */
    public String getSellerNameValue() {
        return sellerName.value();
    }

    public SellerStatus getStatus() {
        return status;
    }

    public int getProductCount() {
        return productCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /** 활성 상태 여부 */
    public boolean isActive() {
        return this.status == SellerStatus.ACTIVE;
    }

    /** 비활성 상태 여부 */
    public boolean isInactive() {
        return this.status == SellerStatus.INACTIVE;
    }

    /** 도메인 이벤트 목록 (읽기 전용) */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /** 도메인 이벤트 초기화 */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
