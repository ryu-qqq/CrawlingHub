package com.ryuqq.crawlinghub.domain.seller.vo;

import java.time.Instant;

/**
 * Seller Query Criteria
 *
 * <p>셀러 조회 조건 VO
 *
 * @param mustItSellerName 머스트잇 셀러명 (부분 일치 검색)
 * @param sellerName 셀러명 (부분 일치 검색)
 * @param status 셀러 상태
 * @param createdFrom 생성일 시작
 * @param createdTo 생성일 종료
 * @param page 페이지 번호
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record SellerQueryCriteria(
        MustItSellerName mustItSellerName,
        SellerName sellerName,
        SellerStatus status,
        Instant createdFrom,
        Instant createdTo,
        Integer page,
        Integer size) {}
