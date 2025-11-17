package com.ryuqq.crawlinghub.domain.seller.vo;

/**
 * 크롤링 주기 Value Object
 *
 * <p>머스트잇 셀러 크롤링의 실행 주기를 표현합니다.</p>
 *
 * <p>비즈니스 규칙:</p>
 * <ul>
 *   <li>최소 1일, 최대 30일</li>
 *   <li>범위 외 값은 IllegalArgumentException 발생</li>
 * </ul>
 *
 * @param days 크롤링 주기 (1-30일)
 * @throws IllegalArgumentException days가 1-30 범위를 벗어난 경우
 */
public record CrawlingInterval(Integer days) {

    private static final int MIN_DAYS = 1;
    private static final int MAX_DAYS = 30;

    /**
     * 생성자 - Compact Constructor로 범위 검증 수행
     */
    public CrawlingInterval {
        if (days == null || days < MIN_DAYS || days > MAX_DAYS) {
            throw new IllegalArgumentException("크롤링 주기는 1-30일 사이여야 합니다");
        }
    }
}
