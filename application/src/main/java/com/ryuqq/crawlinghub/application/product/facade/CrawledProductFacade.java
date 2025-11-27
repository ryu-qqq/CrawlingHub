package com.ryuqq.crawlinghub.application.product.facade;

import com.ryuqq.crawlinghub.application.product.manager.CrawledProductManager;
import com.ryuqq.crawlinghub.application.product.manager.ImageOutboxManager;
import com.ryuqq.crawlinghub.application.product.manager.SyncOutboxManager;
import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductQueryPort;
import com.ryuqq.crawlinghub.application.product.port.out.query.ImageOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ExternalSyncRequestedEvent;
import com.ryuqq.crawlinghub.domain.product.event.ImageUploadRequestedEvent;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCategory;
import com.ryuqq.crawlinghub.domain.product.vo.ProductDetailInfo;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.product.vo.ShippingInfo;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * CrawledProduct Facade
 *
 * <p><strong>책임</strong>:
 * <ul>
 *   <li>QueryPort를 통한 조회 로직 조율
 *   <li>Manager들을 통한 저장/업데이트 조율
 *   <li>이벤트 발행을 통한 비동기 처리 트리거
 *   <li>이미지 업로드/외부 동기화 Outbox 생성
 * </ul>
 *
 * <p><strong>트랜잭션 전략</strong>:
 * <ul>
 *   <li>조회는 readOnly 트랜잭션
 *   <li>저장/업데이트 시 Outbox도 같은 트랜잭션에서 저장
 *   <li>이벤트 발행은 트랜잭션 커밋 후 처리 (리스너에서)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */

public class CrawledProductFacade {

    // private final CrawledProductQueryPort crawledProductQueryPort;
    // private final ImageOutboxQueryPort imageOutboxQueryPort;
    // private final CrawledProductManager crawledProductManager;
    // private final ImageOutboxManager imageOutboxManager;
    // private final SyncOutboxManager syncOutboxManager;
    // private final ApplicationEventPublisher eventPublisher;
    //
    // public CrawledProductFacade(
    //         CrawledProductQueryPort crawledProductQueryPort,
    //         ImageOutboxQueryPort imageOutboxQueryPort,
    //         CrawledProductManager crawledProductManager,
    //         ImageOutboxManager imageOutboxManager,
    //         SyncOutboxManager syncOutboxManager,
    //         ApplicationEventPublisher eventPublisher) {
    //     this.crawledProductQueryPort = crawledProductQueryPort;
    //     this.imageOutboxQueryPort = imageOutboxQueryPort;
    //     this.crawledProductManager = crawledProductManager;
    //     this.imageOutboxManager = imageOutboxManager;
    //     this.syncOutboxManager = syncOutboxManager;
    //     this.eventPublisher = eventPublisher;
    // }
    //
    // // === 조회 ===
    //
    // /**
    //  * ID로 CrawledProduct 조회
    //  *
    //  * @param crawledProductId CrawledProduct ID
    //  * @return CrawledProduct (Optional)
    //  */
    // @Transactional(readOnly = true)
    // public Optional<CrawledProduct> findById(CrawledProductId crawledProductId) {
    //     return crawledProductQueryPort.findById(crawledProductId);
    // }
    //
    // /**
    //  * Seller ID와 Item No로 CrawledProduct 조회
    //  *
    //  * @param sellerId 판매자 ID
    //  * @param itemNo 상품 번호
    //  * @return CrawledProduct (Optional)
    //  */
    // @Transactional(readOnly = true)
    // public Optional<CrawledProduct> findBySellerIdAndItemNo(SellerId sellerId, long itemNo) {
    //     return crawledProductQueryPort.findBySellerIdAndItemNo(sellerId, itemNo);
    // }
    //
    // // === MINI_SHOP 처리 ===
    //
    // /**
    //  * MINI_SHOP 크롤링 결과 처리
    //  *
    //  * <p>기존 상품이 있으면 업데이트, 없으면 신규 생성.
    //  * <p>이미지 업로드 Outbox 생성 후 이벤트 발행.
    //  *
    //  * @param sellerId 판매자 ID
    //  * @param item MINI_SHOP 파싱 결과
    //  * @return 저장/업데이트된 CrawledProduct
    //  */
    // @Transactional
    // public CrawledProduct processMiniShopItem(SellerId sellerId, MiniShopItem item) {
    //     Optional<CrawledProduct> existingOpt = crawledProductQueryPort.findBySellerIdAndItemNo(
    //             sellerId, item.itemNo());
    //
    //     ProductPrice price = item.toProductPrice();
    //     ProductImages images = ProductImages.fromUrls(item.imageUrls());
    //     boolean freeShipping = item.hasFreeShippingTag();
    //
    //     CrawledProduct product;
    //     if (existingOpt.isPresent()) {
    //         product = crawledProductManager.updateFromMiniShop(
    //                 existingOpt.get(),
    //                 item.name(),
    //                 item.brandName(),
    //                 price,
    //                 images,
    //                 freeShipping);
    //     } else {
    //         product = crawledProductManager.createFromMiniShop(
    //                 sellerId,
    //                 item.itemNo(),
    //                 item.name(),
    //                 item.brandName(),
    //                 price,
    //                 images,
    //                 freeShipping);
    //     }
    //
    //     // 이미지 업로드 Outbox 생성 및 이벤트 발행
    //     createImageOutboxesAndPublishEvent(product, images.getPendingUploadUrls(), ImageType.THUMBNAIL);
    //
    //     return product;
    // }
    //
    // // === DETAIL 처리 ===
    //
    // /**
    //  * DETAIL 크롤링 결과 처리
    //  *
    //  * <p>상세 정보 업데이트 및 상세 이미지 업로드 Outbox 생성.
    //  *
    //  * @param crawledProductId CrawledProduct ID
    //  * @param detailInfo DETAIL 파싱 결과
    //  * @return 업데이트된 CrawledProduct (Optional)
    //  */
    // @Transactional
    // public Optional<CrawledProduct> processDetailInfo(
    //         CrawledProductId crawledProductId,
    //         ProductDetailInfo detailInfo) {
    //     Optional<CrawledProduct> productOpt = crawledProductQueryPort.findById(crawledProductId);
    //     if (productOpt.isEmpty()) {
    //         return Optional.empty();
    //     }
    //
    //     CrawledProduct product = productOpt.get();
    //
    //     ProductCategory category = detailInfo.category();
    //     ShippingInfo shippingInfo = detailInfo.shipping();
    //     List<String> detailImages = detailInfo.detailImages();
    //
    //     CrawledProduct updated = crawledProductManager.updateFromDetail(
    //             product,
    //             category,
    //             shippingInfo,
    //             null, // descriptionMarkUp은 별도 저장
    //             detailInfo.itemStatus(),
    //             detailInfo.originCountry(),
    //             null, // shippingLocation
    //             detailImages);
    //
    //     // 상세 이미지 업로드 Outbox 생성 및 이벤트 발행
    //     if (detailImages != null && !detailImages.isEmpty()) {
    //         createImageOutboxesAndPublishEvent(updated, detailImages, ImageType.DESCRIPTION);
    //     }
    //
    //     // 외부 동기화 가능 여부 확인 및 Outbox 생성
    //     checkAndCreateSyncOutbox(updated);
    //
    //     return Optional.of(updated);
    // }
    //
    // /**
    //  * DETAIL 크롤링 결과 처리 (Seller ID와 Item No 기반)
    //  *
    //  * @param sellerId 판매자 ID
    //  * @param itemNo 상품 번호
    //  * @param detailInfo DETAIL 파싱 결과
    //  * @return 업데이트된 CrawledProduct (Optional)
    //  */
    // @Transactional
    // public Optional<CrawledProduct> processDetailInfo(
    //         SellerId sellerId,
    //         long itemNo,
    //         ProductDetailInfo detailInfo) {
    //     Optional<CrawledProduct> productOpt = crawledProductQueryPort.findBySellerIdAndItemNo(sellerId, itemNo);
    //     if (productOpt.isEmpty()) {
    //         return Optional.empty();
    //     }
    //
    //     return processDetailInfo(productOpt.get().getId(), detailInfo);
    // }
    //
    // // === OPTION 처리 ===
    //
    // /**
    //  * OPTION 크롤링 결과 처리
    //  *
    //  * @param crawledProductId CrawledProduct ID
    //  * @param options 옵션 목록
    //  * @return 업데이트된 CrawledProduct (Optional)
    //  */
    // @Transactional
    // public Optional<CrawledProduct> processOptions(
    //         CrawledProductId crawledProductId,
    //         List<ProductOption> options) {
    //     Optional<CrawledProduct> productOpt = crawledProductQueryPort.findById(crawledProductId);
    //     if (productOpt.isEmpty()) {
    //         return Optional.empty();
    //     }
    //
    //     CrawledProduct product = productOpt.get();
    //     ProductOptions productOptions = ProductOptions.from(options);
    //
    //     CrawledProduct updated = crawledProductManager.updateFromOption(product, productOptions);
    //
    //     // 외부 동기화 가능 여부 확인 및 Outbox 생성
    //     checkAndCreateSyncOutbox(updated);
    //
    //     return Optional.of(updated);
    // }
    //
    // /**
    //  * OPTION 크롤링 결과 처리 (Seller ID와 Item No 기반)
    //  *
    //  * @param sellerId 판매자 ID
    //  * @param itemNo 상품 번호
    //  * @param options 옵션 목록
    //  * @return 업데이트된 CrawledProduct (Optional)
    //  */
    // @Transactional
    // public Optional<CrawledProduct> processOptions(
    //         SellerId sellerId,
    //         long itemNo,
    //         List<ProductOption> options) {
    //     Optional<CrawledProduct> productOpt = crawledProductQueryPort.findBySellerIdAndItemNo(sellerId, itemNo);
    //     if (productOpt.isEmpty()) {
    //         return Optional.empty();
    //     }
    //
    //     return processOptions(productOpt.get().getId(), options);
    // }
    //
    // // === 이미지 업로드 완료 처리 ===
    //
    // /**
    //  * 이미지 업로드 완료 처리 (웹훅에서 호출)
    //  *
    //  * @param outboxId ImageOutbox ID
    //  * @param s3Url 업로드된 S3 URL
    //  */
    // @Transactional
    // public void completeImageUpload(Long outboxId, String s3Url) {
    //     Optional<CrawledProductImageOutbox> outboxOpt = imageOutboxQueryPort.findById(outboxId);
    //     if (outboxOpt.isEmpty()) {
    //         return;
    //     }
    //
    //     CrawledProductImageOutbox outbox = outboxOpt.get();
    //     imageOutboxManager.markAsCompleted(outbox, s3Url);
    //
    //     // CrawledProduct의 이미지 URL 업데이트
    //     Optional<CrawledProduct> productOpt = crawledProductQueryPort.findById(outbox.getCrawledProductId());
    //     if (productOpt.isPresent()) {
    //         crawledProductManager.markImageAsUploaded(
    //                 productOpt.get(),
    //                 outbox.getOriginalUrl(),
    //                 s3Url);
    //     }
    // }
    //
    // // === 외부 동기화 완료 처리 ===
    //
    // /**
    //  * 외부 서버 동기화 완료 처리
    //  *
    //  * @param crawledProductId CrawledProduct ID
    //  * @param externalProductId 외부 상품 ID
    //  */
    // @Transactional
    // public void completeSyncToExternalServer(
    //         CrawledProductId crawledProductId,
    //         Long externalProductId) {
    //     Optional<CrawledProduct> productOpt = crawledProductQueryPort.findById(crawledProductId);
    //     if (productOpt.isPresent()) {
    //         crawledProductManager.markAsSynced(productOpt.get(), externalProductId);
    //     }
    // }
    //
    // // === Private Helper Methods ===
    //
    // /**
    //  * 이미지 업로드 Outbox 생성 및 이벤트 발행
    //  *
    //  * <p>TODO Phase 2: 이미지 업로드 리스너 활성화 후 이벤트 발행 로직 활성화
    //  */
    // private void createImageOutboxesAndPublishEvent(
    //         CrawledProduct product,
    //         List<String> imageUrls,
    //         ImageType imageType) {
    //     if (imageUrls == null || imageUrls.isEmpty()) {
    //         return;
    //     }
    //
    //     // 이미 존재하는 Outbox 제외
    //     List<String> newImageUrls = imageUrls.stream()
    //             .filter(url -> !imageOutboxQueryPort.existsByCrawledProductIdAndOriginalUrl(product.getId(), url))
    //             .toList();
    //
    //     if (newImageUrls.isEmpty()) {
    //         return;
    //     }
    //
    //     // Outbox 일괄 생성
    //     List<CrawledProductImageOutbox> outboxes = imageOutboxManager.createAll(
    //             product.getId(), newImageUrls, imageType);
    //
    //     // TODO Phase 2: 이벤트 발행 활성화 (리스너 완성 후)
    //     // 이벤트 발행 (비동기 처리 트리거) - 현재 리스너가 비활성화되어 있으므로 주석 처리
    //     if (!newImageUrls.isEmpty()) {
    //         ImageUploadRequestedEvent event = ImageUploadRequestedEvent.ofUrls(
    //                 product.getId(),
    //                 newImageUrls,
    //                 imageType);
    //         eventPublisher.publishEvent(event);
    //     }
    // }
    //
    // /**
    //  * 외부 동기화 가능 여부 확인 및 Outbox 생성
    //  *
    //  * <p>TODO Phase 2: 외부 동기화 리스너 활성화 후 이벤트 발행 로직 활성화
    //  */
    // private void checkAndCreateSyncOutbox(CrawledProduct product) {
    //     if (!product.needsExternalSync()) {
    //         return;
    //     }
    //
    //     if (product.isRegisteredToExternalServer()) {
    //         // 갱신 요청
    //         syncOutboxManager.createForUpdate(
    //                 product.getId(),
    //                 product.getSellerId(),
    //                 product.getItemNo(),
    //                 product.getExternalProductId());
    //
    //         // TODO Phase 2: 이벤트 발행 활성화
    //         ExternalSyncRequestedEvent event = ExternalSyncRequestedEvent.forUpdate(
    //                 product.getId(),
    //                 product.getSellerId(),
    //                 product.getItemNo());
    //         eventPublisher.publishEvent(event);
    //     } else {
    //         // 신규 등록 요청
    //         syncOutboxManager.createForCreate(
    //                 product.getId(),
    //                 product.getSellerId(),
    //                 product.getItemNo());
    //
    //         // TODO Phase 2: 이벤트 발행 활성화
    //         ExternalSyncRequestedEvent event = ExternalSyncRequestedEvent.forCreate(
    //                 product.getId(),
    //                 product.getSellerId(),
    //                 product.getItemNo());
    //         eventPublisher.publishEvent(event);
    //     }
    // }

}
