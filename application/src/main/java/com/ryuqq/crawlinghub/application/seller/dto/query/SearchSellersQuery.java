package com.ryuqq.crawlinghub.application.seller.dto.query;

import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.Instant;

/**
 * Search Sellers Query
 *
 * <p>셀러 목록 조회 조건
 *
 * @param mustItSellerName 머스트잇 셀러 이름 (부분 일치 검색, 선택)
 * @param sellerName 셀러 이름 (부분 일치 검색, 선택)
 * @param sellerStatus 셀러 상태
 * @param createdFrom 생성일 시작 (선택)
 * @param createdTo 생성일 종료 (선택)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record SearchSellersQuery(
        String mustItSellerName,
        String sellerName,
        SellerStatus sellerStatus,
        Instant createdFrom,
        Instant createdTo,
        Integer page,
        Integer size) {}
