package com.ryuqq.crawlinghub.application.seller.factory.command;

import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerCommand;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
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

    private final ClockHolder clockHolder;

    public SellerCommandFactory(ClockHolder clockHolder) {
        this.clockHolder = clockHolder;
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
                clockHolder.getClock());
    }

    /**
     * UpdateSellerCommand → Seller (비교용 임시 객체)
     *
     * <p>Command의 값들을 Domain 객체로 변환하여 기존 Seller와 비교 가능하게 함
     *
     * <p>주의: 이 객체는 영속화되지 않으며, 비교 목적으로만 사용
     *
     * @param command 셀러 수정 Command
     * @return 비교용 Seller 객체
     */
    public Seller createForComparison(UpdateSellerCommand command) {
        MustItSellerName mustItSellerName =
                command.mustItSellerName() != null
                        ? MustItSellerName.of(command.mustItSellerName())
                        : null;
        SellerName sellerName =
                command.sellerName() != null ? SellerName.of(command.sellerName()) : null;
        SellerStatus status =
                command.active() != null
                        ? (command.active() ? SellerStatus.ACTIVE : SellerStatus.INACTIVE)
                        : null;

        return Seller.of(
                SellerId.of(command.sellerId()),
                mustItSellerName,
                sellerName,
                status,
                0, // productCount (비교 불필요)
                null, // createdAt (비교 불필요)
                null); // updatedAt (비교 불필요)
    }
}

