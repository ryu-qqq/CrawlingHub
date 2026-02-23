package com.ryuqq.crawlinghub.application.seller.factory.command;

import com.ryuqq.crawlinghub.application.common.dto.command.UpdateContext;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerCommand;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerUpdateData;
import org.springframework.stereotype.Component;

/**
 * Seller CommandFactory
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>Command → Domain 변환
 * </ul>
 *
 * <p><strong>금지</strong>:
 *
 * <ul>
 *   <li>@Transactional 금지 (변환만, 트랜잭션 불필요)
 *   <li>Port 의존 금지
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerCommandFactory {

    private final TimeProvider timeProvider;

    public SellerCommandFactory(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * RegisterSellerCommand → Seller (신규 생성)
     *
     * @param command 셀러 등록 Command
     * @return 신규 Seller Aggregate
     */
    public Seller create(RegisterSellerCommand command) {
        return Seller.forNew(
                MustItSellerName.of(command.mustItSellerName()),
                SellerName.of(command.sellerName()),
                timeProvider.now());
    }

    /**
     * UpdateSellerCommand → UpdateContext (수정 컨텍스트 생성)
     *
     * @param command 셀러 수정 Command
     * @return UpdateContext (ID + UpdateData + changedAt)
     */
    public UpdateContext<SellerId, SellerUpdateData> createUpdateContext(
            UpdateSellerCommand command) {
        SellerId id = SellerId.of(command.sellerId());
        SellerUpdateData updateData =
                SellerUpdateData.of(
                        MustItSellerName.of(command.mustItSellerName()),
                        SellerName.of(command.sellerName()),
                        command.active() ? SellerStatus.ACTIVE : SellerStatus.INACTIVE);
        return new UpdateContext<>(id, updateData, timeProvider.now());
    }

    /**
     * 상품 수 업데이트 컨텍스트 생성
     *
     * @param sellerId 셀러 ID
     * @param productCount 새로운 상품 수
     * @return UpdateContext (ID + productCount + changedAt)
     */
    public UpdateContext<SellerId, Integer> createProductCountUpdateContext(
            Long sellerId, int productCount) {
        return new UpdateContext<>(SellerId.of(sellerId), productCount, timeProvider.now());
    }
}
