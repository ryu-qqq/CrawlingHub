package com.ryuqq.crawlinghub.application.seller.component;

import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.application.seller.manager.SellerReadManager;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateMustItSellerIdException;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateSellerNameException;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerHasActiveSchedulersException;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerUpdateData;
import org.springframework.stereotype.Component;

/**
 * Seller 저장 전 검증기
 *
 * <p><strong>검증 항목</strong>:
 *
 * <ul>
 *   <li>MustItSellerName 중복 검증 (등록/수정)
 *   <li>SellerName 중복 검증 (등록/수정)
 *   <li>비활성화 시 활성 스케줄러 존재 여부 검증
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerPersistenceValidator {

    private final SellerReadManager sellerReadManager;
    private final CrawlSchedulerReadManager crawlSchedulerReadManager;

    public SellerPersistenceValidator(
            SellerReadManager sellerReadManager,
            CrawlSchedulerReadManager crawlSchedulerReadManager) {
        this.sellerReadManager = sellerReadManager;
        this.crawlSchedulerReadManager = crawlSchedulerReadManager;
    }

    /**
     * 등록 시 중복 검증 (MustItSellerName + SellerName)
     *
     * @param seller 등록할 Seller
     * @throws DuplicateMustItSellerIdException 머스트잇 셀러명 중복 시
     * @throws DuplicateSellerNameException 셀러명 중복 시
     */
    public void validateForRegistration(Seller seller) {
        if (sellerReadManager.existsByMustItSellerName(seller.getMustItSellerName())) {
            throw new DuplicateMustItSellerIdException(seller.getMustItSellerNameValue());
        }
        if (sellerReadManager.existsBySellerName(seller.getSellerName())) {
            throw new DuplicateSellerNameException(seller.getSellerNameValue());
        }
    }

    /**
     * 수정 시 검증 (이름 중복 + 비활성화 스케줄러 검증)
     *
     * @param existingSeller 기존 Seller
     * @param updateData 수정 데이터
     * @throws DuplicateMustItSellerIdException 머스트잇 셀러명 중복 시
     * @throws DuplicateSellerNameException 셀러명 중복 시
     * @throws SellerHasActiveSchedulersException 비활성화 시 활성 스케줄러 존재
     */
    public void validateForUpdate(Seller existingSeller, SellerUpdateData updateData) {
        validateNameDuplication(existingSeller, updateData);
        validateDeactivation(existingSeller, updateData);
    }

    private void validateNameDuplication(Seller existingSeller, SellerUpdateData updateData) {
        SellerId sellerId = existingSeller.getSellerId();
        MustItSellerName newMustItSellerName = updateData.mustItSellerName();
        SellerName newSellerName = updateData.sellerName();

        if (!existingSeller.getMustItSellerName().equals(newMustItSellerName)) {
            if (sellerReadManager.existsByMustItSellerNameExcludingId(
                    newMustItSellerName, sellerId)) {
                throw new DuplicateMustItSellerIdException(newMustItSellerName.value());
            }
        }

        if (!existingSeller.getSellerName().equals(newSellerName)) {
            if (sellerReadManager.existsBySellerNameExcludingId(newSellerName, sellerId)) {
                throw new DuplicateSellerNameException(newSellerName.value());
            }
        }
    }

    private void validateDeactivation(Seller existingSeller, SellerUpdateData updateData) {
        if (!existingSeller.isBeingDeactivatedBy(updateData)) {
            return;
        }

        long activeCount =
                crawlSchedulerReadManager.countActiveSchedulersBySellerId(
                        existingSeller.getSellerId());
        if (activeCount > 0) {
            throw new SellerHasActiveSchedulersException(
                    existingSeller.getSellerIdValue(), (int) activeCount);
        }
    }
}
