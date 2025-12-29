package com.ryuqq.crawlinghub.application.product.facade;

import com.ryuqq.crawlinghub.application.image.factory.ImageUploadBundleFactory;
import com.ryuqq.crawlinghub.application.image.manager.query.CrawledProductImageReadManager;
import com.ryuqq.crawlinghub.application.product.dto.bundle.DetailProcessBundle;
import com.ryuqq.crawlinghub.application.product.manager.command.CrawledProductTransactionManager;
import com.ryuqq.crawlinghub.application.sync.port.in.command.RequestSyncUseCase;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * DETAIL 처리 Facade
 *
 * <p>CrawledProductTransactionManager, ImageUploadBundleFactory, RequestSyncUseCase를 조합하여 DETAIL
 * 크롤링 결과 처리를 담당합니다.
 *
 * <p><strong>Bundle 패턴 적용</strong>:
 *
 * <ul>
 *   <li>Factory에서 생성된 DetailProcessBundle을 받아 처리
 *   <li>ImageUploadBundleFactory가 이미지/Outbox/Event 처리 담당
 *   <li>N+1 문제 해결: 배치 쿼리로 기존 URL 필터링
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class DetailProcessFacade {

    private final CrawledProductTransactionManager crawledProductManager;
    private final CrawledProductImageReadManager imageReadManager;
    private final ImageUploadBundleFactory imageUploadBundleFactory;
    private final RequestSyncUseCase requestSyncUseCase;

    public DetailProcessFacade(
            CrawledProductTransactionManager crawledProductManager,
            CrawledProductImageReadManager imageReadManager,
            ImageUploadBundleFactory imageUploadBundleFactory,
            RequestSyncUseCase requestSyncUseCase) {
        this.crawledProductManager = crawledProductManager;
        this.imageReadManager = imageReadManager;
        this.imageUploadBundleFactory = imageUploadBundleFactory;
        this.requestSyncUseCase = requestSyncUseCase;
    }

    /**
     * DETAIL 크롤링 Bundle로 CrawledProduct 업데이트 및 이미지 업로드/동기화 요청
     *
     * <p>Bundle 패턴을 사용하여 다음 작업을 수행합니다:
     *
     * <ol>
     *   <li>CrawledProduct 업데이트
     *   <li>Bundle에 CrawledProduct ID 설정 (enrichWithProductId)
     *   <li>기존 URL 필터링 (N+1 해결)
     *   <li>ImageUploadBundleFactory로 이미지 처리 위임
     *   <li>동기화 요청
     * </ol>
     *
     * @param existing 기존 CrawledProduct
     * @param bundle DETAIL 처리 Bundle
     * @return 업데이트된 CrawledProduct
     */
    @Transactional
    public CrawledProduct updateAndRequestUploadAndSync(
            CrawledProduct existing, DetailProcessBundle bundle) {
        // 1. CrawledProduct 업데이트
        CrawledProduct updated =
                crawledProductManager
                        .updateFromDetailCrawlData(existing, bundle.crawlData())
                        .product();

        // 2. Bundle에 CrawledProduct ID 설정
        CrawledProductId productId = updated.getId();
        DetailProcessBundle enrichedBundle = bundle.enrichWithProductId(productId);

        // 3. 새로운 이미지 URL만 필터링 (N+1 해결)
        if (enrichedBundle.hasImageUpload()) {
            List<String> newImageUrls =
                    imageReadManager.filterNewImageUrls(productId, enrichedBundle.getImageUrls());

            // 필터링된 URL로 Bundle 갱신
            enrichedBundle = enrichedBundle.withFilteredImageUrls(newImageUrls);
        }

        // 4. Factory로 이미지 처리 위임
        if (enrichedBundle.hasImageUpload()) {
            imageUploadBundleFactory.processImageUpload(enrichedBundle.imageUploadData());
        }

        // 5. 외부 동기화 가능 여부 확인 및 요청
        requestSyncUseCase.requestIfReady(updated);

        return updated;
    }
}
