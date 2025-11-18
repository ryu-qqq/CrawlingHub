package com.ryuqq.crawlinghub.application.seller.port.in.command;

import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerIntervalCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;

/**
 * Seller 크롤링 주기 업데이트 UseCase (Command Port)
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Seller 크롤링 주기 변경</li>
 *   <li>EventBridge Rule 업데이트 (외부 API 호출, 트랜잭션 밖)</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ Command 접미사 (UpdateSellerIntervalCommand)</li>
 *   <li>✅ Response 반환 (SellerResponse)</li>
 *   <li>✅ UseCase 접미사 사용</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
public interface UpdateSellerIntervalUseCase {

    /**
     * Seller 크롤링 주기 업데이트
     *
     * @param command Seller 주기 업데이트 Command
     * @return 업데이트된 Seller 정보
     */
    SellerResponse execute(UpdateSellerIntervalCommand command);
}
