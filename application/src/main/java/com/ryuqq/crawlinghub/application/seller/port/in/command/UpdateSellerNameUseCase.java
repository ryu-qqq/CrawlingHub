package com.ryuqq.crawlinghub.application.seller.port.in.command;

import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerNameCommand;

/**
 * Seller 이름 변경 UseCase 인터페이스
 *
 * <p>기존 Seller의 이름을 변경하는 UseCase입니다.</p>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Input Port는 인터페이스여야 함 (port.in.command 패키지)</li>
 *   <li>✅ @Transactional 금지 (구현체에서만 사용)</li>
 *   <li>✅ Command DTO 입력, void 출력 (비즈니스 규칙에 따라)</li>
 *   <li>✅ Domain 객체 직접 반환 금지 (Response DTO 사용)</li>
 *   <li>✅ Output Port 직접 의존 금지 (구현체에서만)</li>
 * </ul>
 *
 * <p><strong>비즈니스 로직:</strong></p>
 * <ol>
 *   <li>Seller 조회 (SellerQueryPort.findById)</li>
 *   <li>이름 변경 (Seller.updateName)</li>
 *   <li>DB 저장 (SellerPersistencePort.persist)</li>
 * </ol>
 *
 * <p><strong>Transaction 경계:</strong></p>
 * <ul>
 *   <li>✅ 구현체에서 @Transactional 사용 (단일 트랜잭션)</li>
 *   <li>✅ DB 작업만 트랜잭션 내부 (외부 API 호출 없음)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public interface UpdateSellerNameUseCase {

    /**
     * Seller 이름 변경
     *
     * @param command UpdateSellerNameCommand (sellerId, newName)
     * @throws com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException Seller가 존재하지 않는 경우
     * @throws IllegalArgumentException 이름이 유효하지 않은 경우
     */
    void execute(UpdateSellerNameCommand command);
}

