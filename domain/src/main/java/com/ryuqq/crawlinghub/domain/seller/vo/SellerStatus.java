package com.ryuqq.crawlinghub.domain.seller.vo;

/**
 * 셀러 상태
 *
 * <p><strong>도메인 규칙</strong>:
 *
 * <ul>
 *   <li>ACTIVE: 활성 상태 (크롤링 스케줄 실행 가능)
 *   <li>INACTIVE: 비활성 상태 (크롤링 스케줄 중지)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public enum SellerStatus {

    /** 활성 상태 */
    ACTIVE,

    /** 비활성 상태 (크롤링 스케줄 중지 필요) */
    INACTIVE
}
