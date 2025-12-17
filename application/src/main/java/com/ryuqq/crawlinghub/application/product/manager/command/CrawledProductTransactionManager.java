package com.ryuqq.crawlinghub.application.product.manager.command;

import com.ryuqq.crawlinghub.application.product.port.out.command.CrawledProductPersistencePort;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.DetailCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.OptionCrawlData;
import com.ryuqq.crawlinghub.domain.product.vo.ProductCategory;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.product.vo.ShippingInfo;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawledProduct 트랜잭션 관리자
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>CrawledProduct 영속성 관리 (저장/삭제)
 *   <li>도메인 메서드 호출을 통한 상태 변경
 *   <li>트랜잭션 경계 관리
 * </ul>
 *
 * <p><strong>주의</strong>:
 *
 * <ul>
 *   <li>QueryPort는 사용하지 않음 (Facade에서 사용)
 *   <li>외부 API 호출 금지 (트랜잭션 내)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductTransactionManager {

    private final CrawledProductPersistencePort crawledProductPersistencePort;
    private final ClockHolder clockHolder;

    public CrawledProductTransactionManager(
            CrawledProductPersistencePort crawledProductPersistencePort, ClockHolder clockHolder) {
        this.crawledProductPersistencePort = crawledProductPersistencePort;
        this.clockHolder = clockHolder;
    }

    // === 생성 ===

    /**
     * MINI_SHOP 크롤링 데이터 VO로 신규 CrawledProduct 생성 및 저장
     *
     * @param crawlData MINI_SHOP 크롤링 데이터 VO
     * @return 저장된 CrawledProduct
     */
    @Transactional
    public CrawledProduct createFromMiniShopCrawlData(MiniShopCrawlData crawlData) {
        CrawledProduct product = CrawledProduct.fromMiniShopCrawlData(crawlData);

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
        product.updateFromMiniShop(
                itemName, brandName, price, images, freeShipping, clockHolder.getClock());
        crawledProductPersistencePort.persist(product);
        return product;
    }

    // === DETAIL 업데이트 ===

    /**
     * DETAIL 크롤링 결과 반환 record
     *
     * @param product 업데이트된 CrawledProduct
     * @param newImageUrls 새로 업로드가 필요한 이미지 URL 목록
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "CrawledProduct is a domain aggregate passed by reference intentionally")
    public record DetailUpdateResult(CrawledProduct product, List<String> newImageUrls) {
        /** 방어적 복사를 통해 불변성 보장 */
        public DetailUpdateResult {
            newImageUrls = newImageUrls == null ? List.of() : List.copyOf(newImageUrls);
        }
    }

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
     * @return 업데이트 결과 (CrawledProduct + 새 이미지 URL 목록)
     */
    @Transactional
    public DetailUpdateResult updateFromDetail(
            CrawledProduct product,
            ProductCategory category,
            ShippingInfo shippingInfo,
            String descriptionMarkUp,
            String itemStatus,
            String originCountry,
            String shippingLocation,
            List<String> descriptionImages) {
        List<String> newImageUrls =
                product.updateFromDetail(
                        category,
                        shippingInfo,
                        descriptionMarkUp,
                        itemStatus,
                        originCountry,
                        shippingLocation,
                        descriptionImages,
                        clockHolder.getClock());
        crawledProductPersistencePort.persist(product);
        return new DetailUpdateResult(product, newImageUrls);
    }

    /**
     * DETAIL 크롤링 데이터 VO로 CrawledProduct 업데이트
     *
     * @param product 업데이트할 CrawledProduct
     * @param crawlData DETAIL 크롤링 데이터 VO
     * @return 업데이트 결과 (CrawledProduct + 새 이미지 URL 목록)
     */
    @Transactional
    public DetailUpdateResult updateFromDetailCrawlData(
            CrawledProduct product, DetailCrawlData crawlData) {
        List<String> newImageUrls = product.updateFromDetailCrawlData(crawlData);
        crawledProductPersistencePort.persist(product);
        return new DetailUpdateResult(product, newImageUrls);
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
        product.updateFromOption(options, clockHolder.getClock());
        crawledProductPersistencePort.persist(product);
        return product;
    }

    /**
     * OPTION 크롤링 데이터 VO로 CrawledProduct 업데이트
     *
     * @param product 업데이트할 CrawledProduct
     * @param crawlData OPTION 크롤링 데이터 VO
     * @return 업데이트된 CrawledProduct
     */
    @Transactional
    public CrawledProduct updateFromOptionCrawlData(
            CrawledProduct product, OptionCrawlData crawlData) {
        product.updateFromOptionCrawlData(crawlData);
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
            CrawledProduct product, String originalUrl, String s3Url) {
        product.markImageAsUploaded(originalUrl, s3Url, clockHolder.getClock());
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
        product.markAsSynced(externalProductId, clockHolder.getClock());
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
        product.markSyncFailed(clockHolder.getClock());
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
