package com.ryuqq.crawlinghub.application.seller.dto.query;

import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;

/**
 * 셀러 목록 조회 Query DTO.
 *
 * @param status 상태 필터
 * @param page 페이지 번호
 * @param size 페이지 크기
 */
public record ListSellersQuery(SellerStatus status, int page, int size) {}
