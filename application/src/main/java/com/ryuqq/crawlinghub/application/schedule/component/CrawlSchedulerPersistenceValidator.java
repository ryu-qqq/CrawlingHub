package com.ryuqq.crawlinghub.application.schedule.component;

import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.application.seller.manager.SellerReadManager;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.exception.DuplicateSchedulerNameException;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import org.springframework.stereotype.Component;

/**
 * CrawlScheduler 저장 전 검증기
 *
 * <p><strong>검증 항목</strong>:
 *
 * <ul>
 *   <li>셀러 존재 여부 검증
 *   <li>중복 스케줄러명 검증 (등록/수정)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerPersistenceValidator {

    private final SellerReadManager sellerReadManager;
    private final CrawlSchedulerReadManager crawlSchedulerReadManager;

    public CrawlSchedulerPersistenceValidator(
            SellerReadManager sellerReadManager,
            CrawlSchedulerReadManager crawlSchedulerReadManager) {
        this.sellerReadManager = sellerReadManager;
        this.crawlSchedulerReadManager = crawlSchedulerReadManager;
    }

    /**
     * 등록 시 검증 (셀러 존재 + 중복 스케줄러명)
     *
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @throws SellerNotFoundException 셀러가 존재하지 않는 경우
     * @throws DuplicateSchedulerNameException 동일 셀러에 중복 스케줄러명이 존재하는 경우
     */
    public void validateForRegistration(Long sellerId, String schedulerName) {
        if (!sellerReadManager.existsById(SellerId.of(sellerId))) {
            throw new SellerNotFoundException(sellerId);
        }
        SellerId sellerIdVo = SellerId.of(sellerId);
        boolean exists =
                crawlSchedulerReadManager.existsBySellerIdAndSchedulerName(
                        sellerIdVo, schedulerName);
        if (exists) {
            throw new DuplicateSchedulerNameException(sellerId, schedulerName);
        }
    }

    /**
     * 수정 시 중복 스케줄러명 검증 (이름 변경된 경우만 검증)
     *
     * @param currentScheduler 현재 스케줄러
     * @param newSchedulerName 새로운 스케줄러 이름
     * @throws DuplicateSchedulerNameException 동일 셀러에 중복 스케줄러명이 존재하는 경우
     */
    public void validateDuplicateSchedulerNameForUpdate(
            CrawlScheduler currentScheduler, String newSchedulerName) {
        if (currentScheduler.hasSameSchedulerName(newSchedulerName)) {
            return;
        }
        SellerId sellerId = currentScheduler.getSellerId();
        boolean exists =
                crawlSchedulerReadManager.existsBySellerIdAndSchedulerName(
                        sellerId, newSchedulerName);
        if (exists) {
            throw new DuplicateSchedulerNameException(sellerId.value(), newSchedulerName);
        }
    }
}
