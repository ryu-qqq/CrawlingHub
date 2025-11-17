package com.ryuqq.crawlinghub.application.seller.port.in.command;

import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;

/**
 * Seller 등록 UseCase (Input Port - Command)
 *
 * <p>새로운 Seller를 시스템에 등록합니다.</p>
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ Input Port는 port.in.command 패키지에 위치</li>
 *   <li>✅ Command DTO 입력, Response DTO 출력</li>
 *   <li>✅ UseCase 네이밍 (동사 + UseCase 접미사)</li>
 *   <li>✅ Transaction 경계는 구현체에서 관리</li>
 * </ul>
 *
 * <p><strong>구현 책임:</strong></p>
 * <ul>
 *   <li>중복 Seller ID 검증</li>
 *   <li>Seller Domain 생성 및 저장</li>
 *   <li>EventBridge Rule 생성 (크롤링 스케줄)</li>
 *   <li>SellerResponse 반환</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
public interface RegisterSellerUseCase {

    /**
     * Seller 등록
     *
     * @param command Seller 등록 Command DTO
     * @return 등록된 Seller 정보
     * @throws IllegalArgumentException sellerId가 이미 존재하는 경우
     */
    SellerResponse execute(RegisterSellerCommand command);
}
