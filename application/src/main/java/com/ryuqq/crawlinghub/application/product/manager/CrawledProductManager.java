package com.ryuqq.crawlinghub.application.product.manager;

import com.ryuqq.crawlinghub.application.product.port.out.command.CrawledProductPersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCategory;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.product.vo.ShippingInfo;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CrawledProduct 트랜잭션 관리자
 *
 * <p><strong>책임</strong>:
 * <ul>
 *   <li>CrawledProduct 영속성 관리 (저장/삭제)
 *   <li>도메인 메서드 호출을 통한 상태 변경
 *   <li>트랜잭션 경계 관리
 * </ul>
 *
 * <p><strong>주의</strong>:
 * <ul>
 *   <li>QueryPort는 사용하지 않음 (Facade에서 사용)
 *   <li>외부 API 호출 금지 (트랜잭션 내)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductManager {

    private final CrawledProductPersistencePort crawledProductPersistencePort;

    public CrawledProductManager(CrawledProductPersistencePort crawledProductPersistencePort) {
        this.crawledProductPersistencePort = crawledProductPersistencePort;
    }

    // === 생성 ===

    /**
     * MINI_SHOP 크롤링 결과로 신규 CrawledProduct 생성 및 저장
     *
     * @param sellerId 판매자 ID
     * @param itemNo 상품 번호
     * @param itemName 상품명
     * @param brandName 브랜드명
     * @param price 가격 정보
     * @param images 이미지 목록
     * @param freeShipping 무료배송 여부
     * @return 저장된 CrawledProduct
     */
    @Transactional
    public CrawledProduct createFromMiniShop(
            SellerId sellerId,
            long itemNo,
            String itemName,
            String brandName,
            ProductPrice price,
            ProductImages images,
            boolean freeShipping) {
        CrawledProduct product = CrawledProduct.fromMiniShop(
                sellerId, itemNo, itemName, brandName, price, images, freeShipping);
        CrawledProductId savedId = crawledProductPersistencePort.persist(product);
        return CrawledProduct.reconstitute(
                savedId,
                product.getSellerId(),
                product.getItemNo(),
                product.getItemName(),
                product.getBrandName(),
                product.getPrice(),
                product.getImages(),
                product.isFreeShipping(),
                product.getCategory(),
                product.getShippingInfo(),
                product.getDescriptionMarkUp(),
                product.getItemStatus(),
                product.getOriginCountry(),
                product.getShippingLocation(),
                product.getOptions(),
                product.getCrawlCompletionStatus(),
                product.getExternalProductId(),
                product.getLastSyncedAt(),
                product.isNeedsSync(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }

    // === MINI_SHOP 업데이트 ===

    /**
     * MINI_SHOP 크롤링 결과로 기존 CrawledProduct 업데이트
     *
     * @param product 업데이트할 CrawledProduct
     * @param itemName 상품명
     * @param brandName 브랜드명
     * @param price 가격 정보
     * @param images 이미지 목록
     * @param freeShipping 무료배송 여부
     * @return 업데이트된 CrawledProduct
     */
    @Transactional
    public CrawledProduct updateFromMiniShop(
            CrawledProduct product,
            String itemName,
            String brandName,
            ProductPrice price,
            ProductImages images,
            boolean freeShipping) {
        product.updateFromMiniShop(itemName, brandName, price, images, freeShipping);
        crawledProductPersistencePort.persist(product);
        return product;
    }

    // === DETAIL 업데이트 ===

    /**
     * DETAIL 크롤링 결과로 CrawledProduct 업데이트
     *
     * @param product 업데이트할 CrawledProduct
     * @param category 카테고리 정보
     * @param shippingInfo 배송 정보
     * @param descriptionMarkUp 상세 설명 HTML
     * @param itemStatus 상품 상태
     * @param originCountry 원산지
     * @param shippingLocation 배송 출발지
     * @param descriptionImages 상세 설명 내 이미지 URL 목록
     * @return 업데이트된 CrawledProduct
     */
    @Transactional
    public CrawledProduct updateFromDetail(
            CrawledProduct product,
            ProductCategory category,
            ShippingInfo shippingInfo,
            String descriptionMarkUp,
            String itemStatus,
            String originCountry,
            String shippingLocation,
            List<String> descriptionImages) {
        product.updateFromDetail(
                category, shippingInfo, descriptionMarkUp,
                itemStatus, originCountry, shippingLocation, descriptionImages);
        crawledProductPersistencePort.persist(product);
        return product;
    }

    // === OPTION 업데이트 ===

    /**
     * OPTION 크롤링 결과로 CrawledProduct 업데이트
     *
     * @param product 업데이트할 CrawledProduct
     * @param options 옵션 목록
     * @return 업데이트된 CrawledProduct
     */
    @Transactional
    public CrawledProduct updateFromOption(CrawledProduct product, ProductOptions options) {
        product.updateFromOption(options);
        crawledProductPersistencePort.persist(product);
        return product;
    }

    // === 이미지 업로드 완료 처리 ===

    /**
     * 이미지 S3 업로드 완료 처리
     *
     * @param product 업데이트할 CrawledProduct
     * @param originalUrl 원본 URL
     * @param s3Url S3 URL
     * @return 업데이트된 CrawledProduct
     */
    @Transactional
    public CrawledProduct markImageAsUploaded(
            CrawledProduct product,
            String originalUrl,
            String s3Url) {
        product.markImageAsUploaded(originalUrl, s3Url);
        crawledProductPersistencePort.persist(product);
        return product;
    }

    // === 외부 서버 동기화 처리 ===

    /**
     * 외부 서버 동기화 완료 처리
     *
     * @param product 업데이트할 CrawledProduct
     * @param externalProductId 외부 상품 ID
     * @return 업데이트된 CrawledProduct
     */
    @Transactional
    public CrawledProduct markAsSynced(CrawledProduct product, Long externalProductId) {
        product.markAsSynced(externalProductId);
        crawledProductPersistencePort.persist(product);
        return product;
    }

    /**
     * 외부 서버 동기화 실패 처리
     *
     * @param product 업데이트할 CrawledProduct
     * @return 업데이트된 CrawledProduct
     */
    @Transactional
    public CrawledProduct markSyncFailed(CrawledProduct product) {
        product.markSyncFailed();
        crawledProductPersistencePort.persist(product);
        return product;
    }

    // === 삭제 ===

    /**
     * CrawledProduct 삭제
     *
     * @param crawledProductId 삭제할 CrawledProduct ID
     */
    @Transactional
    public void delete(CrawledProductId crawledProductId) {
        crawledProductPersistencePort.delete(crawledProductId);
    }
}
