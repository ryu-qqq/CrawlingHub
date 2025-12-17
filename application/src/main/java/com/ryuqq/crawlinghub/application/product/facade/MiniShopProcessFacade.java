package com.ryuqq.crawlinghub.application.product.facade;

import com.ryuqq.crawlinghub.application.common.config.TransactionEventRegistry;
import com.ryuqq.crawlinghub.application.image.manager.ImageOutboxReadManager;
import com.ryuqq.crawlinghub.application.product.dto.bundle.MiniShopProcessBundle;
import com.ryuqq.crawlinghub.application.product.manager.ImageOutboxManager;
import com.ryuqq.crawlinghub.application.product.manager.command.CrawledProductTransactionManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ImageUploadRequestedEvent;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * MINI_SHOP 처리 Facade
 *
 * <p>CrawledProductTransactionManager, ImageOutboxManager를 조합하여 MINI_SHOP 크롤링 결과 처리를 담당합니다.
 *
 * <p><strong>Bundle 패턴 적용</strong>:
 *
 * <ul>
 *   <li>Factory에서 생성된 MiniShopProcessBundle을 받아 처리
 *   <li>Bundle 내부에서 Outbox 생성 및 Event 생성
 *   <li>N+1 문제 해결: 배치 쿼리로 기존 URL 필터링
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class MiniShopProcessFacade {

    private final CrawledProductTransactionManager crawledProductManager;
    private final ImageOutboxReadManager imageOutboxReadManager;
    private final ImageOutboxManager imageOutboxManager;
    private final TransactionEventRegistry eventRegistry;
    private final Clock clock;

    public MiniShopProcessFacade(
            CrawledProductTransactionManager crawledProductManager,
            ImageOutboxReadManager imageOutboxReadManager,
            ImageOutboxManager imageOutboxManager,
            TransactionEventRegistry eventRegistry,
            Clock clock) {
        this.crawledProductManager = crawledProductManager;
        this.imageOutboxReadManager = imageOutboxReadManager;
        this.imageOutboxManager = imageOutboxManager;
        this.eventRegistry = eventRegistry;
        this.clock = clock;
    }

    /**
     * MINI_SHOP 크롤링 Bundle로 신규 CrawledProduct 생성 및 이미지 업로드 요청
     *
     * <p>Bundle 패턴을 사용하여 다음 작업을 수행합니다:
     *
     * <ol>
     *   <li>CrawledProduct 생성
     *   <li>Bundle에 CrawledProduct ID 설정 (enrichWithProductId)
     *   <li>Bundle에서 Outbox 생성 및 저장 (신규는 필터링 불필요)
     *   <li>Bundle에서 Event 생성 및 등록 (커밋 후 발행)
     * </ol>
     *
     * @param bundle MINI_SHOP 처리 Bundle
     * @return 저장된 CrawledProduct
     */
    @Transactional
    public CrawledProduct createAndRequestImageUpload(MiniShopProcessBundle bundle) {
        // 1. CrawledProduct 생성
        CrawledProduct product =
                crawledProductManager.createFromMiniShopCrawlData(bundle.crawlData());

        // 2. Bundle에 CrawledProduct ID 설정
        CrawledProductId productId = product.getId();
        MiniShopProcessBundle enrichedBundle = bundle.enrichWithProductId(productId);

        // 3. Bundle에서 Outbox 생성 및 저장 (신규 상품이므로 필터링 불필요)
        if (enrichedBundle.hasImageUpload()) {
            List<CrawledProductImageOutbox> outboxes = enrichedBundle.createOutboxes(clock);
            imageOutboxManager.persistAll(outboxes);

            // 4. Bundle에서 Event 생성 및 등록 (커밋 후 발행)
            Optional<ImageUploadRequestedEvent> eventOpt = enrichedBundle.createEvent(clock);
            eventOpt.ifPresent(eventRegistry::registerForPublish);
        }

        return product;
    }

    /**
     * MINI_SHOP 크롤링 Bundle로 기존 CrawledProduct 업데이트 및 이미지 업로드 요청
     *
     * <p>Bundle 패턴을 사용하여 다음 작업을 수행합니다:
     *
     * <ol>
     *   <li>CrawledProduct 업데이트
     *   <li>Bundle에 CrawledProduct ID 설정 (enrichWithProductId)
     *   <li>기존 URL 필터링 (N+1 해결)
     *   <li>Bundle에서 Outbox 생성 및 저장
     *   <li>Bundle에서 Event 생성 및 등록 (커밋 후 발행)
     * </ol>
     *
     * @param existing 기존 CrawledProduct
     * @param bundle MINI_SHOP 처리 Bundle
     * @return 업데이트된 CrawledProduct
     */
    @Transactional
    public CrawledProduct updateAndRequestImageUpload(
            CrawledProduct existing, MiniShopProcessBundle bundle) {
        // 1. CrawledProduct 업데이트
        CrawledProduct product =
                crawledProductManager.updateFromMiniShop(
                        existing,
                        bundle.crawlData().itemName(),
                        bundle.crawlData().brandName(),
                        bundle.crawlData().price(),
                        bundle.crawlData().images(),
                        bundle.crawlData().freeShipping());

        // 2. Bundle에 CrawledProduct ID 설정
        CrawledProductId productId = product.getId();
        MiniShopProcessBundle enrichedBundle = bundle.enrichWithProductId(productId);

        // 3. 새로운 이미지 URL만 필터링 (N+1 해결)
        if (enrichedBundle.hasImageUpload()) {
            List<String> newImageUrls =
                    imageOutboxReadManager.filterNewImageUrls(
                            productId, enrichedBundle.getImageUrls());

            // 필터링된 URL로 Bundle 갱신
            enrichedBundle = enrichedBundle.withFilteredImageUrls(newImageUrls);
        }

        // 4. Bundle에서 Outbox 생성 및 저장
        if (enrichedBundle.hasImageUpload()) {
            List<CrawledProductImageOutbox> outboxes = enrichedBundle.createOutboxes(clock);
            imageOutboxManager.persistAll(outboxes);

            // 5. Bundle에서 Event 생성 및 등록 (커밋 후 발행)
            Optional<ImageUploadRequestedEvent> eventOpt = enrichedBundle.createEvent(clock);
            eventOpt.ifPresent(eventRegistry::registerForPublish);
        }

        return product;
    }
}
