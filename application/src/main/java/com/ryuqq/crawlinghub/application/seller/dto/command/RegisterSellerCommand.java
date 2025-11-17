package com.ryuqq.crawlinghub.application.seller.dto.command;

/**
 * Seller 등록 Command DTO
 *
 * <p>새로운 Seller를 등록하기 위한 Command 객체입니다.</p>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Java 21 Record 사용</li>
 *   <li>✅ 비즈니스 메서드 금지 - 순수 데이터 전달 객체</li>
 *   <li>✅ Validation 금지 - REST API Layer 또는 UseCase에서 검증</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // REST API Layer에서 @Valid로 검증
 * public SellerResponse register(@Valid @RequestBody RegisterSellerCommand command) {
 *     return registerSellerUseCase.execute(command);
 * }
 *
 * // 또는 UseCase 내부에서 검증
 * public SellerResponse execute(RegisterSellerCommand command) {
 *     // 검증 로직
 *     if (command.sellerId() == null || command.sellerId().isBlank()) {
 *         throw new InvalidSellerIdException();
 *     }
 *     // ...
 * }
 * }</pre>
 *
 * @param sellerId Seller ID
 * @param name Seller 이름
 * @param crawlingIntervalDays 크롤링 주기 (일 단위)
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public record RegisterSellerCommand(
        String sellerId,
        String name,
        Integer crawlingIntervalDays
) {
}
