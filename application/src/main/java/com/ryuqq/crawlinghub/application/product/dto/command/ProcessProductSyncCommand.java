package com.ryuqq.crawlinghub.application.product.dto.command;

/**
 * SQS 메시지 기반 외부 서버 동기화 처리 Command
 *
 * <p><strong>용도</strong>: SQS Listener에서 수신한 ProductSync 메시지를 Application Layer로 전달
 *
 * @param outboxId Outbox ID (NotNull)
 * @param crawledProductId CrawledProduct ID (NotNull)
 * @param sellerId Seller ID (NotNull)
 * @param itemNo Item No (NotNull)
 * @param syncType 동기화 유형 (CREATE, UPDATE) (NotNull)
 * @param externalProductId 외부 상품 ID (UPDATE 시, nullable)
 * @param idempotencyKey 멱등성 키 (NotNull)
 * @author development-team
 * @since 1.0.0
 */
public record ProcessProductSyncCommand(
        Long outboxId,
        Long crawledProductId,
        Long sellerId,
        Long itemNo,
        String syncType,
        Long externalProductId,
        String idempotencyKey) {

    public ProcessProductSyncCommand {
        if (outboxId == null) {
            throw new IllegalArgumentException("outboxId는 null일 수 없습니다.");
        }
        if (crawledProductId == null) {
            throw new IllegalArgumentException("crawledProductId는 null일 수 없습니다.");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId는 null일 수 없습니다.");
        }
        if (itemNo == null) {
            throw new IllegalArgumentException("itemNo는 null일 수 없습니다.");
        }
        if (syncType == null || syncType.isBlank()) {
            throw new IllegalArgumentException("syncType은 null이거나 빈 문자열일 수 없습니다.");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("idempotencyKey는 null이거나 빈 문자열일 수 없습니다.");
        }
    }
}
