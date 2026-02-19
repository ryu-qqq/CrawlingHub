package com.ryuqq.crawlinghub.domain.product.event;

import com.ryuqq.crawlinghub.domain.common.event.DomainEvent;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;

/**
 * 외부 상품 서버 동기화 요청 이벤트
 *
 * <p>MINI_SHOP, DETAIL, OPTION 크롤링이 모두 완료되고 변경이 감지되었을 때 발행됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public record ExternalSyncRequestedEvent(
        CrawledProductId crawledProductId,
        SellerId sellerId,
        long itemNo,
        String idempotencyKey,
        SyncType syncType,
        Instant occurredAt)
        implements DomainEvent {

    public ExternalSyncRequestedEvent {
        if (crawledProductId == null) {
            throw new IllegalArgumentException("crawledProductId는 필수입니다.");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId는 필수입니다.");
        }
        if (itemNo <= 0) {
            throw new IllegalArgumentException("itemNo는 양수여야 합니다.");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("idempotencyKey는 필수입니다.");
        }
        if (syncType == null) {
            throw new IllegalArgumentException("syncType은 필수입니다.");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt은 필수입니다.");
        }
    }

    /** 신규 등록용 이벤트 생성 */
    public static ExternalSyncRequestedEvent forCreate(
            CrawledProductId crawledProductId,
            SellerId sellerId,
            long itemNo,
            String idempotencyKey,
            Instant now) {
        return new ExternalSyncRequestedEvent(
                crawledProductId, sellerId, itemNo, idempotencyKey, SyncType.CREATE, now);
    }

    /** 갱신용 이벤트 생성 */
    public static ExternalSyncRequestedEvent forUpdate(
            CrawledProductId crawledProductId,
            SellerId sellerId,
            long itemNo,
            String idempotencyKey,
            Instant now) {
        return new ExternalSyncRequestedEvent(
                crawledProductId, sellerId, itemNo, idempotencyKey, SyncType.UPDATE, now);
    }

    /** 동기화 타입 */
    public enum SyncType {
        /** 신규 상품 등록 */
        CREATE,
        /** 기존 상품 갱신 */
        UPDATE
    }
}
