package com.ryuqq.crawlinghub.domain.mustit.seller;

import java.util.Objects;

/**
 * CrawlInterval - 크롤링 주기 Value Object
 *
 * <p>셀러의 크롤링 주기 정보(타입, 값)를 캡슐화한 불변 객체입니다.</p>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Immutability: 모든 필드는 final로 선언</li>
 *   <li>✅ Self-Validation: 생성 시점에 비즈니스 규칙 검증</li>
 *   <li>✅ Static Factory Method: of() 메서드를 통한 생성</li>
 *   <li>✅ Business Logic Encapsulation: 주기 관련 비즈니스 규칙 내재화</li>
 *   <li>❌ Lombok 금지: Pure Java 사용</li>
 * </ul>
 *
 * <p><strong>검증 규칙:</strong></p>
 * <ul>
 *   <li>MINUTES: 1분 ~ 59분</li>
 *   <li>HOURS: 1시간 ~ 23시간</li>
 *   <li>DAYS: 1일 ~ 30일</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * CrawlInterval interval = CrawlInterval.of(CrawlIntervalType.HOURS, 2);
 * int hours = interval.getIntervalValue();
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public class CrawlInterval {

    private static final int MIN_MINUTES = 1;
    private static final int MAX_MINUTES = 59;
    private static final int MIN_HOURS = 1;
    private static final int MAX_HOURS = 23;
    private static final int MIN_DAYS = 1;
    private static final int MAX_DAYS = 30;

    private final CrawlIntervalType intervalType;
    private final int intervalValue;

    /**
     * Private 생성자
     *
     * <p>외부에서 직접 생성을 막고, Static Factory Method를 통해서만 생성 가능하도록 합니다.</p>
     *
     * @param intervalType 크롤링 주기 타입 (MINUTES, HOURS, DAYS)
     * @param intervalValue 크롤링 주기 값 (타입별 범위 제한 있음)
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private CrawlInterval(CrawlIntervalType intervalType, int intervalValue) {
        this.intervalType = intervalType;
        this.intervalValue = intervalValue;
    }

    /**
     * CrawlInterval 생성 Static Factory Method
     *
     * <p>크롤링 주기 객체를 생성하며, 생성 시점에 비즈니스 규칙을 검증합니다.</p>
     *
     * <p><strong>검증 규칙:</strong></p>
     * <ul>
     *   <li>intervalType은 null일 수 없습니다</li>
     *   <li>intervalValue는 타입별 허용 범위 내에 있어야 합니다</li>
     *   <li>MINUTES: 1 ~ 59</li>
     *   <li>HOURS: 1 ~ 23</li>
     *   <li>DAYS: 1 ~ 30</li>
     * </ul>
     *
     * @param intervalType 크롤링 주기 타입 (필수)
     * @param intervalValue 크롤링 주기 값 (타입별 범위 제한)
     * @return 생성된 CrawlInterval 객체
     * @throws IllegalArgumentException intervalType이 null이거나 intervalValue가 허용 범위를 벗어난 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public static CrawlInterval of(CrawlIntervalType intervalType, int intervalValue) {
        validateIntervalType(intervalType);
        validateIntervalValue(intervalType, intervalValue);
        return new CrawlInterval(intervalType, intervalValue);
    }

    /**
     * intervalType 유효성 검증
     *
     * @param intervalType 검증할 크롤링 주기 타입
     * @throws IllegalArgumentException intervalType이 null인 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private static void validateIntervalType(CrawlIntervalType intervalType) {
        if (intervalType == null) {
            throw new IllegalArgumentException("Interval type must not be null");
        }
    }

    /**
     * intervalValue 유효성 검증 (타입별 범위 체크)
     *
     * <p>각 타입별로 허용되는 값의 범위를 검증합니다:</p>
     * <ul>
     *   <li>MINUTES: 1 ~ 59</li>
     *   <li>HOURS: 1 ~ 23</li>
     *   <li>DAYS: 1 ~ 30</li>
     * </ul>
     *
     * @param intervalType 크롤링 주기 타입
     * @param intervalValue 검증할 크롤링 주기 값
     * @throws IllegalArgumentException intervalValue가 허용 범위를 벗어난 경우
     * @author ryu-qqq
     * @since 2025-10-30
     */
    private static void validateIntervalValue(CrawlIntervalType intervalType, int intervalValue) {
        switch (intervalType) {
            case MINUTES:
                if (intervalValue < MIN_MINUTES || intervalValue > MAX_MINUTES) {
                    throw new IllegalArgumentException(
                            String.format("Minutes interval must be between %d and %d", MIN_MINUTES, MAX_MINUTES)
                    );
                }
                break;
            case HOURS:
                if (intervalValue < MIN_HOURS || intervalValue > MAX_HOURS) {
                    throw new IllegalArgumentException(
                            String.format("Hours interval must be between %d and %d", MIN_HOURS, MAX_HOURS)
                    );
                }
                break;
            case DAYS:
                if (intervalValue < MIN_DAYS || intervalValue > MAX_DAYS) {
                    throw new IllegalArgumentException(
                            String.format("Days interval must be between %d and %d", MIN_DAYS, MAX_DAYS)
                    );
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported interval type: " + intervalType);
        }
    }

    /**
     * 크롤링 주기 타입 반환
     *
     * @return 크롤링 주기 타입 (MINUTES, HOURS, DAYS)
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public CrawlIntervalType getIntervalType() {
        return intervalType;
    }

    /**
     * 크롤링 주기 값 반환
     *
     * @return 크롤링 주기 값
     * @author ryu-qqq
     * @since 2025-10-30
     */
    public int getIntervalValue() {
        return intervalValue;
    }

    /**
     * 동등성 비교
     *
     * <p>intervalType과 intervalValue가 모두 동일하면 같은 객체로 간주합니다.</p>
     *
     * @param o 비교할 객체
     * @return 동일하면 true, 다르면 false
     * @author ryu-qqq
     * @since 2025-10-30
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrawlInterval that = (CrawlInterval) o;
        return intervalValue == that.intervalValue &&
                intervalType == that.intervalType;
    }

    /**
     * 해시코드 생성
     *
     * @return 해시코드 값
     * @author ryu-qqq
     * @since 2025-10-30
     */
    @Override
    public int hashCode() {
        return Objects.hash(intervalType, intervalValue);
    }

    /**
     * 문자열 표현
     *
     * @return CrawlInterval 객체의 문자열 표현
     * @author ryu-qqq
     * @since 2025-10-30
     */
    @Override
    public String toString() {
        return "CrawlInterval{" +
                "intervalType=" + intervalType +
                ", intervalValue=" + intervalValue +
                '}';
    }
}
