package com.ryuqq.crawlinghub.application.product.builder;

import com.ryuqq.crawlinghub.domain.product.ProductSnapshot;
import com.ryuqq.crawlinghub.domain.product.event.ChangeSource;
import com.ryuqq.crawlinghub.domain.product.event.FieldChange;
import com.ryuqq.crawlinghub.domain.product.event.ProductChangeEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ProductSnapshot 빌더
 *
 * <p>역할: 부분 데이터를 받아서 Snapshot을 점진적으로 구축
 *
 * <p>처리 흐름:
 * <ol>
 *   <li>기존 Snapshot과 신규 데이터 비교 (변경 감지)</li>
 *   <li>변경된 필드만 Snapshot 업데이트</li>
 *   <li>ProductChangeEvent 발행</li>
 * </ol>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Component
public class ProductSnapshotBuilder {

    private final ApplicationEventPublisher eventPublisher;

    public ProductSnapshotBuilder(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * 미니샵 데이터 적용
     *
     * @param snapshot 기존 Snapshot
     * @param newData 신규 미니샵 데이터
     * @return 업데이트된 Snapshot
     */
    public ProductSnapshot applyMiniShopData(ProductSnapshot snapshot, MiniShopData newData) {
        Map<String, FieldChange> changes = new HashMap<>();

        // 1. 상품명 변경 감지
        if (!equals(snapshot.getProductName(), newData.productName())) {
            changes.put("productName", new FieldChange(
                "productName",
                snapshot.getProductName(),
                newData.productName()
            ));
            snapshot.updateProductName(newData.productName());
        }

        // 2. 가격 변경 감지
        if (!equals(snapshot.getPrice(), newData.price())) {
            changes.put("price", new FieldChange(
                "price",
                snapshot.getPrice(),
                newData.price()
            ));
            snapshot.updatePrice(newData.price());
        }

        // 3. 메인 이미지 변경 감지
        if (!equals(snapshot.getMainImageUrl(), newData.mainImageUrl())) {
            changes.put("mainImageUrl", new FieldChange(
                "mainImageUrl",
                snapshot.getMainImageUrl(),
                newData.mainImageUrl()
            ));
            snapshot.updateMainImage(newData.mainImageUrl());
        }

        // 4. 변경 사항이 있으면 Event 발행
        if (!changes.isEmpty()) {
            publishChangeEvent(snapshot.getIdValue(), ChangeSource.MINI_SHOP, changes);
        }

        return snapshot;
    }

    /**
     * 옵션 데이터 적용
     */
    public ProductSnapshot applyOptionData(ProductSnapshot snapshot, OptionData newData) {
        Map<String, FieldChange> changes = new HashMap<>();

        // 1. 옵션 변경 감지
        if (!equals(snapshot.getOptions(), newData.options())) {
            changes.put("options", new FieldChange(
                "options",
                snapshot.getOptions(),
                newData.options()
            ));
            snapshot.updateOptions(newData.options());
        }

        // 2. 총 재고 변경 감지
        if (!equals(snapshot.getTotalStock(), newData.totalStock())) {
            changes.put("totalStock", new FieldChange(
                "totalStock",
                snapshot.getTotalStock(),
                newData.totalStock()
            ));
            snapshot.updateTotalStock(newData.totalStock());
        }

        // 3. 변경 사항이 있으면 Event 발행
        if (!changes.isEmpty()) {
            publishChangeEvent(snapshot.getIdValue(), ChangeSource.OPTION, changes);
        }

        return snapshot;
    }

    /**
     * 상세 데이터 적용
     */
    public ProductSnapshot applyDetailData(ProductSnapshot snapshot, DetailData newData) {
        Map<String, FieldChange> changes = new HashMap<>();

        // 1. 상품 정보 모듈 변경 감지
        if (!equals(snapshot.getProductInfo(), newData.productInfo())) {
            changes.put("productInfo", new FieldChange(
                "productInfo",
                snapshot.getProductInfo(),
                newData.productInfo()
            ));
            snapshot.updateProductInfo(newData.productInfo());
        }

        // 2. 배송 모듈 변경 감지
        if (!equals(snapshot.getShipping(), newData.shipping())) {
            changes.put("shipping", new FieldChange(
                "shipping",
                snapshot.getShipping(),
                newData.shipping()
            ));
            snapshot.updateShipping(newData.shipping());
        }

        // 3. 상세 정보 모듈 변경 감지
        if (!equals(snapshot.getDetailInfo(), newData.detailInfo())) {
            changes.put("detailInfo", new FieldChange(
                "detailInfo",
                snapshot.getDetailInfo(),
                newData.detailInfo()
            ));
            snapshot.updateDetailInfo(newData.detailInfo());
        }

        // 4. 변경 사항이 있으면 Event 발행
        if (!changes.isEmpty()) {
            publishChangeEvent(snapshot.getIdValue(), ChangeSource.DETAIL, changes);
        }

        return snapshot;
    }

    /**
     * ProductChangeEvent 발행
     */
    private void publishChangeEvent(Long productId, ChangeSource source, Map<String, FieldChange> changes) {
        ProductChangeEvent event = new ProductChangeEvent(
            productId,
            source,
            changes,
            LocalDateTime.now()
        );
        eventPublisher.publishEvent(event);
    }

    /**
     * 객체 동등성 비교 (null-safe)
     */
    private boolean equals(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }
}

