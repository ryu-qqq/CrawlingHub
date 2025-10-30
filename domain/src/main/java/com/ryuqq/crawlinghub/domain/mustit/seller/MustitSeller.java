package com.ryuqq.crawlinghub.domain.mustit.seller;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * MustitSeller - 셀러 Aggregate Root
 *
 * <p>MustIt 플랫폼의 셀러를 표현하는 도메인 객체입니다.</p>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ 실용적인 구조: 필요한 만큼만 캡슐화</li>
 *   <li>✅ Static Factory Methods: create(신규), reconstitute(DB 재구성)</li>
 *   <li>✅ 비즈니스 메서드: 상태 변경은 메서드를 통해서만</li>
 *   <li>❌ Lombok 금지: Pure Java 사용</li>
 * </ul>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>sellerId와 name은 필수</li>
 *   <li>크롤링 주기는 CrawlInterval로 검증</li>
 *   <li>active 상태 변경 가능</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class MustitSeller {

    private final String sellerId;
    private final String name;
    private boolean active;
    private CrawlInterval crawlInterval;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Private 생성자
     *
     * @param sellerId 셀러 ID
     * @param name 셀러 이름
     * @param active 활성화 상태
     * @param crawlInterval 크롤링 주기
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private MustitSeller(
            String sellerId,
            String name,
            boolean active,
            CrawlInterval crawlInterval,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.sellerId = sellerId;
        this.name = name;
        this.active = active;
        this.crawlInterval = crawlInterval;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 셀러 생성
     *
     * @param sellerId 셀러 ID (필수)
     * @param name 셀러 이름 (필수)
     * @param active 활성화 상태
     * @param intervalType 크롤링 주기 타입
     * @param intervalValue 크롤링 주기 값
     * @return 생성된 MustitSeller
     * @throws IllegalArgumentException 파라미터 검증 실패 시
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static MustitSeller create(
            String sellerId,
            String name,
            boolean active,
            CrawlIntervalType intervalType,
            int intervalValue
    ) {
        validateSellerId(sellerId);
        validateName(name);
        CrawlInterval interval = CrawlInterval.of(intervalType, intervalValue);
        LocalDateTime now = LocalDateTime.now();

        return new MustitSeller(sellerId, name, active, interval, now, now);
    }

    /**
     * DB에서 재구성
     *
     * @param sellerId 셀러 ID
     * @param name 셀러 이름
     * @param active 활성화 상태
     * @param crawlInterval 크롤링 주기
     * @param createdAt 생성 시각
     * @param updatedAt 수정 시각
     * @return 재구성된 MustitSeller
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static MustitSeller reconstitute(
            String sellerId,
            String name,
            boolean active,
            CrawlInterval crawlInterval,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new MustitSeller(sellerId, name, active, crawlInterval, createdAt, updatedAt);
    }

    /**
     * 크롤링 주기 변경
     *
     * @param newIntervalType 새로운 크롤링 주기 타입
     * @param newIntervalValue 새로운 크롤링 주기 값
     * @throws IllegalArgumentException 파라미터 검증 실패 시
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public void changeCrawlInterval(CrawlIntervalType newIntervalType, int newIntervalValue) {
        this.crawlInterval = CrawlInterval.of(newIntervalType, newIntervalValue);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 활성화
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 비활성화
     *
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * sellerId 검증
     *
     * @param sellerId 검증할 셀러 ID
     * @throws IllegalArgumentException sellerId가 null이거나 빈 문자열인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private static void validateSellerId(String sellerId) {
        if (sellerId == null || sellerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Seller ID must not be null or empty");
        }
    }

    /**
     * name 검증
     *
     * @param name 검증할 셀러 이름
     * @throws IllegalArgumentException name이 null이거나 빈 문자열인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Seller name must not be null or empty");
        }
    }

    /**
     * 셀러 ID 반환
     *
     * @return 셀러 ID
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public String getSellerId() {
        return sellerId;
    }

    /**
     * 셀러 이름 반환
     *
     * @return 셀러 이름
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public String getName() {
        return name;
    }

    /**
     * 활성화 상태 반환
     *
     * @return 활성화 상태
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public boolean isActive() {
        return active;
    }

    /**
     * 크롤링 주기 반환
     *
     * @return 크롤링 주기
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public CrawlInterval getCrawlInterval() {
        return crawlInterval;
    }

    /**
     * 생성 시각 반환
     *
     * @return 생성 시각
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 수정 시각 반환
     *
     * @return 수정 시각
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 동등성 비교 (sellerId 기준)
     *
     * @param o 비교할 객체
     * @return 동일하면 true
     * @author ryu-qqq
     * @since 2025-10-30
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MustitSeller that = (MustitSeller) o;
        return Objects.equals(sellerId, that.sellerId);
    }

    /**
     * 해시코드 생성
     *
     * @return 해시코드
     * @author ryu-qqq
     * @since 2025-10-30
     */
    @Override
    public int hashCode() {
        return Objects.hash(sellerId);
    }

    /**
     * 문자열 표현
     *
     * @return 문자열 표현
     * @author ryu-qqq
     * @since 2025-10-30
     */
    @Override
    public String toString() {
        return "MustitSeller{" +
                "sellerId='" + sellerId + '\'' +
                ", name='" + name + '\'' +
                ", active=" + active +
                ", crawlInterval=" + crawlInterval +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
