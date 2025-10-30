package com.ryuqq.crawlinghub.domain.mustit.seller;

/**
 * CrawlIntervalType - 크롤링 주기 타입 Enum
 *
 * <p>셀러의 크롤링 주기 유형을 정의합니다.</p>
 *
 * <p><strong>지원하는 타입:</strong></p>
 * <ul>
 *   <li>MINUTES: 분 단위 주기</li>
 *   <li>HOURS: 시간 단위 주기</li>
 *   <li>DAYS: 일 단위 주기</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-30
 */
public enum CrawlIntervalType {
    /**
     * 분 단위 크롤링 주기
     * <p>최소 1분, 최대 59분까지 설정 가능</p>
     */
    MINUTES,

    /**
     * 시간 단위 크롤링 주기
     * <p>최소 1시간, 최대 23시간까지 설정 가능</p>
     */
    HOURS,

    /**
     * 일 단위 크롤링 주기
     * <p>최소 1일, 최대 30일까지 설정 가능</p>
     */
    DAYS
}
