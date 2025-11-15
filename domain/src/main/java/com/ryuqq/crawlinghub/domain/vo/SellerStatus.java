package com.ryuqq.crawlinghub.domain.vo;

/**
 * 셀러 상태 Enum
 *
 * <p>머스트잇 크롤링 대상 셀러의 활성화 상태를 표현합니다.</p>
 *
 * <p>상태 종류:</p>
 * <ul>
 *   <li>{@link #ACTIVE} - 활성화 (크롤링 대상)</li>
 *   <li>{@link #INACTIVE} - 비활성화 (크롤링 제외)</li>
 * </ul>
 */
public enum SellerStatus {

    /**
     * 활성화 상태 - 크롤링 대상
     */
    ACTIVE,

    /**
     * 비활성화 상태 - 크롤링 제외
     */
    INACTIVE
}
