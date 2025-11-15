package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.vo.CrawlingInterval;
import com.ryuqq.crawlinghub.domain.vo.SellerId;
import com.ryuqq.crawlinghub.domain.vo.SellerStatus;

import java.time.LocalDateTime;

/**
 * Seller Aggregate Root
 *
 * <p>머스트잇 셀러를 표현하는 Aggregate Root입니다.</p>
 *
 * <p>Zero-Tolerance Rules 준수:</p>
 * <ul>
 *   <li>Lombok 금지 - Plain Java 사용</li>
 *   <li>Law of Demeter - Getter 체이닝 금지 (getCrawlingIntervalDays() 제공)</li>
 *   <li>Tell, Don't Ask - 비즈니스 로직은 Seller 내부에 캡슐화</li>
 *   <li>Long FK 전략 - JPA 관계 어노테이션 없음</li>
 * </ul>
 *
 * <p>비즈니스 규칙:</p>
 * <ul>
 *   <li>생성 시 상태는 항상 ACTIVE</li>
 *   <li>생성 시 totalProductCount는 0</li>
 *   <li>크롤링 주기는 1-30일 범위 (CrawlingInterval VO가 검증)</li>
 * </ul>
 */
public class Seller {

    private final SellerId sellerId;
    private final String name;
    private CrawlingInterval crawlingInterval;
    private SellerStatus status;
    private Integer totalProductCount;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Private constructor - 정적 팩토리 메서드를 통해서만 생성
     *
     * @param sellerId 셀러 식별자
     * @param name 셀러 이름
     * @param crawlingInterval 크롤링 주기
     */
    private Seller(SellerId sellerId, String name, CrawlingInterval crawlingInterval) {
        this.sellerId = sellerId;
        this.name = name;
        this.crawlingInterval = crawlingInterval;
        this.status = SellerStatus.ACTIVE;
        this.totalProductCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 새로운 Seller 등록
     *
     * <p>비즈니스 규칙:</p>
     * <ul>
     *   <li>초기 상태: ACTIVE</li>
     *   <li>초기 상품 수: 0</li>
     * </ul>
     *
     * @param sellerId 셀러 식별자
     * @param name 셀러 이름
     * @param intervalDays 크롤링 주기 (1-30일)
     * @return 새로 생성된 Seller
     * @throws IllegalArgumentException intervalDays가 1-30 범위를 벗어나는 경우
     */
    public static Seller register(SellerId sellerId, String name, Integer intervalDays) {
        return new Seller(sellerId, name, new CrawlingInterval(intervalDays));
    }

    /**
     * 크롤링 주기 변경
     *
     * <p>비즈니스 규칙:</p>
     * <ul>
     *   <li>크롤링 주기는 1-30일 범위 (CrawlingInterval VO가 검증)</li>
     *   <li>변경 시 updatedAt 갱신</li>
     * </ul>
     *
     * @param newIntervalDays 새로운 크롤링 주기 (일수)
     * @throws IllegalArgumentException intervalDays가 1-30 범위를 벗어나는 경우
     */
    public void updateInterval(Integer newIntervalDays) {
        this.crawlingInterval = new CrawlingInterval(newIntervalDays);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Seller 활성화
     *
     * <p>비즈니스 규칙:</p>
     * <ul>
     *   <li>상태를 ACTIVE로 변경</li>
     *   <li>변경 시 updatedAt 갱신</li>
     * </ul>
     */
    public void activate() {
        this.status = SellerStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
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
        this.updatedAt = LocalDateTime.now();
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
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 크롤링 주기 일수 조회
     *
     * <p>Law of Demeter 준수:</p>
     * <ul>
     *   <li>seller.getCrawlingInterval().days() ❌ (Getter 체이닝)</li>
     *   <li>seller.getCrawlingIntervalDays() ✅ (캡슐화)</li>
     * </ul>
     *
     * @return 크롤링 주기 (일수)
     */
    public Integer getCrawlingIntervalDays() {
        return crawlingInterval.days();
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
}
