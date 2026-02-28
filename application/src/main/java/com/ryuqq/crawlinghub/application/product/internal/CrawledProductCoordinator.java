package com.ryuqq.crawlinghub.application.product.internal;

import com.ryuqq.crawlinghub.application.product.manager.CrawledProductCommandManager;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductReadManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CrawledProduct 조회/영속/동기화를 조율하는 Coordinator
 *
 * <p>3개 프로세서(MiniShop, Detail, Option)의 공통 흐름(조회→변경→영속→동기화)을 조율합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductCoordinator {

    private static final Logger log = LoggerFactory.getLogger(CrawledProductCoordinator.class);

    private final CrawledProductReadManager readManager;
    private final CrawledProductCommandManager commandManager;
    private final CrawledProductCommandFacade commandFacade;

    public CrawledProductCoordinator(
            CrawledProductReadManager readManager,
            CrawledProductCommandManager commandManager,
            CrawledProductCommandFacade commandFacade) {
        this.readManager = readManager;
        this.commandManager = commandManager;
        this.commandFacade = commandFacade;
    }

    /**
     * 기존 상품을 찾아서 업데이트 + 동기화 요청 (Detail, Option용)
     *
     * <p>상품이 존재하지 않으면 아무 동작도 하지 않습니다.
     *
     * @param sellerId 판매자 ID
     * @param itemNo 상품 번호
     * @param updater 도메인 업데이트 로직
     */
    public void updateExistingAndSync(
            SellerId sellerId, long itemNo, Consumer<CrawledProduct> updater) {
        readManager
                .findBySellerIdAndItemNo(sellerId, itemNo)
                .ifPresent(
                        product -> {
                            updater.accept(product);
                            commandFacade.persistAndSync(product);
                        });
    }

    /**
     * 기존 상품이 있으면 업데이트+동기화, 없으면 신규 생성 (MiniShop용)
     *
     * @param sellerId 판매자 ID
     * @param itemNo 상품 번호
     * @param updater 기존 상품 업데이트 로직
     * @param creator 신규 상품 생성 로직
     */
    public void createOrUpdate(
            SellerId sellerId,
            long itemNo,
            Consumer<CrawledProduct> updater,
            Supplier<CrawledProduct> creator) {
        Optional<CrawledProduct> existing = readManager.findBySellerIdAndItemNo(sellerId, itemNo);

        if (existing.isPresent()) {
            CrawledProduct product = existing.get();
            updater.accept(product);
            commandFacade.persistAndSync(product);
            return;
        }

        Optional<CrawledProduct> deleted =
                readManager.findBySellerIdAndItemNoIncludingDeleted(sellerId, itemNo);

        if (deleted.isPresent()) {
            CrawledProduct product = deleted.get();
            product.restore(Instant.now());
            updater.accept(product);
            commandFacade.persistAndSync(product);
            log.info("soft-deleted 상품 복원: sellerId={}, itemNo={}", sellerId.value(), itemNo);
        } else {
            commandManager.persist(creator.get());
        }
    }

    /**
     * 기존 상품이 있으면 soft-delete 처리 (셀러 불일치 등으로 인한 정리)
     *
     * <p>상품이 존재하지 않으면 아무 동작도 하지 않습니다.
     *
     * @param sellerId 판매자 ID
     * @param itemNo 상품 번호
     */
    public void softDeleteIfExists(SellerId sellerId, long itemNo) {
        readManager
                .findBySellerIdAndItemNo(sellerId, itemNo)
                .ifPresent(
                        product -> {
                            product.delete(Instant.now());
                            commandManager.persist(product);
                        });
    }
}
