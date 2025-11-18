package com.ryuqq.crawlinghub.domain.seller.aggregate.seller;

import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Seller Aggregate Root
 *
 * <p>머스트잇 셀러를 표현하는 Aggregate Root입니다.</p>
 *
 * <p>Zero-Tolerance Rules 준수:</p>
 * <ul>
 *   <li>Lombok 금지 - Plain Java 사용</li>
 *   <li>Law of Demeter - Getter 체이닝 금지</li>
 *   <li>Tell, Don't Ask - 비즈니스 로직은 Seller 내부에 캡슐화</li>
 *   <li>Long FK 전략 - JPA 관계 어노테이션 없음</li>
 * </ul>
 *
 * <p>비즈니스 규칙:</p>
 * <ul>
 *   <li>생성 시 상태는 항상 ACTIVE</li>
 *   <li>생성 시 totalProductCount는 0</li>
 *   <li>이름만 변경 가능 (크롤링 주기는 EventBridge에서 관리)</li>
 * </ul>
 */
public class Seller {

    private final SellerId sellerId;
    private String name;
    private SellerStatus status;
    private Integer totalProductCount;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final Clock clock;

    /**
     * Private constructor - 정적 팩토리 메서드를 통해서만 생성 (신규)
     *
     * @param sellerId 셀러 식별자
     * @param name 셀러 이름
     * @param clock 시간 제어 (테스트 가능성)
     */
    private Seller(SellerId sellerId, String name, Clock clock) {
        this.sellerId = sellerId;
        this.name = name;
        this.status = SellerStatus.INACTIVE;
        this.totalProductCount = 0;
        this.clock = clock;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Private constructor - 정적 팩토리 메서드를 통해서만 생성 (재구성)
     *
     * @param sellerId 셀러 식별자
     * @param name 셀러 이름
     * @param status 상태
     * @param totalProductCount 총 상품 수
     * @param clock 시간 제어 (테스트 가능성)
     */
    private Seller(SellerId sellerId, String name, SellerStatus status, Integer totalProductCount, Clock clock) {
        this.sellerId = sellerId;
        this.name = name;
        this.status = status;
        this.totalProductCount = totalProductCount;
        this.clock = clock;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 새로운 Seller 생성 (표준 패턴)
     *
     * <p>forNew() 패턴: 신규 엔티티 생성</p>
     * <ul>
     *   <li>초기 상태: ACTIVE</li>
     *   <li>초기 상품 수: 0</li>
     * </ul>
     *
     * @param sellerId 셀러 식별자
     * @param name 셀러 이름
     * @return 새로 생성된 Seller
     */
    public static Seller forNew(SellerId sellerId, String name) {
        return forNew(sellerId, name, Clock.systemDefaultZone());
    }

    public static Seller forNew(SellerId sellerId, String name, Clock clock) {
        return new Seller(sellerId, name, clock);
    }

    /**
     * 불변 속성으로 Seller 재구성 (표준 패턴)
     *
     * <p>of() 패턴: 테스트용 간편 생성</p>
     * <ul>
     *   <li>초기 상태: ACTIVE</li>
     *   <li>초기 상품 수: 0</li>
     * </ul>
     *
     * @param sellerId 셀러 식별자
     * @param name 셀러 이름
     * @return 재구성된 Seller
     */
    public static Seller of(SellerId sellerId, String name) {
        return of(sellerId, name, Clock.systemDefaultZone());
    }

    public static Seller of(SellerId sellerId, String name, Clock clock) {
        return new Seller(sellerId, name, clock);
    }

    /**
     * 완전한 Seller 재구성 (표준 패턴)
     *
     * <p>reconstitute() 패턴: DB에서 조회한 엔티티 재구성</p>
     *
     * @param sellerId 셀러 식별자
     * @param name 셀러 이름
     * @param status 상태
     * @param totalProductCount 총 상품 수
     * @return 재구성된 Seller
     */
    public static Seller reconstitute(SellerId sellerId, String name, SellerStatus status, Integer totalProductCount) {
        return reconstitute(sellerId, name, status, totalProductCount, Clock.systemDefaultZone());
    }

    public static Seller reconstitute(SellerId sellerId, String name, SellerStatus status, Integer totalProductCount, Clock clock) {
        return new Seller(sellerId, name, status, totalProductCount, clock);
    }

    /**
     * Seller 활성화
     *
     * <p>비즈니스 규칙:</p>
     * <ul>
     *   <li>이미 ACTIVE 상태이면 예외 발생</li>
     *   <li>상태를 ACTIVE로 변경</li>
     *   <li>변경 시 updatedAt 갱신</li>
     * </ul>
     *
     * @throws com.ryuqq.crawlinghub.domain.seller.exception.SellerInvalidStateException 이미 ACTIVE 상태인 경우
     */
    public void activate() {
        if (this.status == SellerStatus.ACTIVE) {
            throw new com.ryuqq.crawlinghub.domain.seller.exception.SellerInvalidStateException(
                    "이미 활성화된 상태입니다"
            );
        }
        this.status = SellerStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Seller 비활성화
     *
     * <p>비즈니스 규칙:</p>
     * <ul>
     *   <li>상태를 INACTIVE로 변경</li>
     *   <li>변경 시 updatedAt 갱신</li>
     * </ul>
     */
    public void deactivate() {
        this.status = SellerStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 총 상품 수 업데이트
     *
     * <p>비즈니스 규칙:</p>
     * <ul>
     *   <li>크롤링 완료 후 상품 수 갱신</li>
     *   <li>변경 시 updatedAt 갱신</li>
     * </ul>
     *
     * @param count 총 상품 수
     */
    public void updateTotalProductCount(Integer count) {
        this.totalProductCount = count;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Seller 이름 변경
     *
     * <p>비즈니스 규칙:</p>
     * <ul>
     *   <li>이름은 null일 수 없음</li>
     *   <li>이름은 빈 값일 수 없음</li>
     *   <li>이름은 100자를 초과할 수 없음</li>
     *   <li>변경 시 updatedAt 갱신</li>
     * </ul>
     *
     * @param newName 새로운 셀러 이름
     * @throws IllegalArgumentException 이름이 null, blank, 또는 100자 초과인 경우
     */
    public void updateName(String newName) {
        if (newName == null) {
            throw new IllegalArgumentException("이름은 null일 수 없습니다");
        }
        if (newName.isBlank()) {
            throw new IllegalArgumentException("이름은 빈 값일 수 없습니다");
        }
        if (newName.length() > 100) {
            throw new IllegalArgumentException("이름은 100자를 초과할 수 없습니다");
        }
        this.name = newName;
        this.updatedAt = LocalDateTime.now(clock);
    }

    // Getters (필요한 것만)
    public SellerId getSellerId() {
        return sellerId;
    }

    public String getName() {
        return name;
    }

    public SellerStatus getStatus() {
        return status;
    }

    public Integer getTotalProductCount() {
        return totalProductCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
