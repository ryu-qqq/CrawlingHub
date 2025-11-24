package com.ryuqq.crawlinghub.application.seller.dto.command;

/**
 * Register Seller Command
 *
 * <p>셀러 등록 명령 데이터
 *
 * @param mustItSellerName 머스트잇 셀러 이름 (MustIt 시스템에 등록된 이름)
 * @param sellerName 셀러 이름 (자사 커머스에 등록된 이름)
 * @author development-team
 * @since 1.0.0
 */
public record RegisterSellerCommand(String mustItSellerName, String sellerName) {}
