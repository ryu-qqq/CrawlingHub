package com.ryuqq.crawlinghub.application.seller.dto.command;

/**
 * Seller 크롤링 주기 업데이트 Command DTO
 *
 * <p>기존 Seller의 크롤링 주기를 업데이트하기 위한 Command 객체입니다.</p>
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
 * public SellerResponse updateInterval(@Valid @RequestBody UpdateSellerIntervalCommand command) {
 *     return updateSellerIntervalUseCase.execute(command);
 * }
 *
 * // 또는 UseCase 내부에서 검증
 * public SellerResponse execute(UpdateSellerIntervalCommand command) {
 *     // 검증 로직
 *     if (command.sellerId() == null || command.sellerId().isBlank()) {
 *         throw new InvalidSellerIdException();
 *     }
 *     if (command.newIntervalDays() == null || command.newIntervalDays() <= 0) {
 *         throw new InvalidCrawlingIntervalException();
 *     }
 *     // ...
 * }
 * }</pre>
 *
 * @param sellerId Seller ID
 * @param newIntervalDays 새로운 크롤링 주기 (일 단위)
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
public record UpdateSellerIntervalCommand(
        String sellerId,
        Integer newIntervalDays
) {
}
