package com.ryuqq.crawlinghub.domain.product;

import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * CrawledProduct Test Fixture
 *
 * @author windsurf
 * @since 1.0.0
 */
public class CrawledProductFixture {

    private static final Long DEFAULT_ID = 1L;
    private static final Long DEFAULT_MUSTIT_ITEM_NO = 12345L;
    private static final Long DEFAULT_SELLER_ID = 100L;
    private static final Clock DEFAULT_CLOCK = Clock.fixed(
        Instant.parse("2025-01-01T00:00:00Z"),
        ZoneId.systemDefault()
    );

    /**
     * 기본 CrawledProduct 생성 (신규)
     *
     * @return CrawledProduct
     */
    public static CrawledProduct create() {
        return CrawledProduct.forNew(
            DEFAULT_MUSTIT_ITEM_NO,
            MustitSellerId.of(DEFAULT_SELLER_ID)
        );
    }

    /**
     * ID를 가진 CrawledProduct 생성
     *
     * @param id Product ID
     * @return CrawledProduct
     */
    public static CrawledProduct createWithId(Long id) {
        return CrawledProduct.of(
            ProductId.of(id),
            DEFAULT_MUSTIT_ITEM_NO,
            MustitSellerId.of(DEFAULT_SELLER_ID)
        );
    }

    /**
     * 특정 머스트잇 상품 번호로 CrawledProduct 생성
     *
     * @param mustItItemNo 머스트잇 상품 번호
     * @return CrawledProduct
     */
    public static CrawledProduct createWithMustitItemNo(Long mustItItemNo) {
        return CrawledProduct.forNew(
            mustItItemNo,
            MustitSellerId.of(DEFAULT_SELLER_ID)
        );
    }

    /**
     * 특정 셀러 ID로 CrawledProduct 생성
     *
     * @param sellerId 셀러 ID
     * @return CrawledProduct
     */
    public static CrawledProduct createWithSellerId(Long sellerId) {
        return CrawledProduct.forNew(
            DEFAULT_MUSTIT_ITEM_NO,
            MustitSellerId.of(sellerId)
        );
    }

    /**
     * 미완성 상태의 CrawledProduct 생성
     *
     * @return CrawledProduct
     */
    public static CrawledProduct createIncomplete() {
        return CrawledProduct.forNew(
            DEFAULT_MUSTIT_ITEM_NO,
            MustitSellerId.of(DEFAULT_SELLER_ID)
        );
    }

    /**
     * 완성 상태의 CrawledProduct 생성
     *
     * @return CrawledProduct
     */
    public static CrawledProduct createComplete() {
        CrawledProduct product = CrawledProduct.of(
            ProductId.of(DEFAULT_ID),
            DEFAULT_MUSTIT_ITEM_NO,
            MustitSellerId.of(DEFAULT_SELLER_ID)
        );
        product.updateMiniShopData(ProductDataFixture.createMiniShopData());
        product.updateDetailData(ProductDataFixture.createDetailData());
        product.updateOptionData(ProductDataFixture.createOptionData());
        return product;
    }

    /**
     * 미니샵 데이터만 있는 CrawledProduct 생성
     *
     * @return CrawledProduct
     */
    public static CrawledProduct createWithMiniShopOnly() {
        CrawledProduct product = CrawledProduct.of(
            ProductId.of(DEFAULT_ID),
            DEFAULT_MUSTIT_ITEM_NO,
            MustitSellerId.of(DEFAULT_SELLER_ID)
        );
        product.updateMiniShopData(ProductDataFixture.createMiniShopData());
        return product;
    }

    /**
     * DB reconstitute용 CrawledProduct 생성
     *
     * @param id Product ID
     * @param mustItItemNo 머스트잇 상품 번호
     * @param sellerId 셀러 ID
     * @param status 완성 상태
     * @return CrawledProduct
     */
    public static CrawledProduct reconstitute(
        Long id,
        Long mustItItemNo,
        Long sellerId,
        CompletionStatus status
    ) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        ProductData miniShopData = status == CompletionStatus.COMPLETE 
            ? ProductDataFixture.createMiniShopData() : null;
        ProductData detailData = status == CompletionStatus.COMPLETE 
            ? ProductDataFixture.createDetailData() : null;
        ProductData optionData = status == CompletionStatus.COMPLETE 
            ? ProductDataFixture.createOptionData() : null;
        DataHash dataHash = status == CompletionStatus.COMPLETE 
            ? DataHashFixture.create() : null;

        return CrawledProduct.reconstitute(
            ProductId.of(id),
            mustItItemNo,
            MustitSellerId.of(sellerId),
            miniShopData,
            detailData,
            optionData,
            dataHash,
            1,
            status,
            now,
            now,
            now,
            now
        );
    }

    /**
     * 완전한 커스텀 CrawledProduct 생성
     *
     * @param id Product ID (null 가능)
     * @param mustItItemNo 머스트잇 상품 번호
     * @param sellerId 셀러 ID
     * @param miniShopData 미니샵 데이터
     * @param detailData 상세 데이터
     * @param optionData 옵션 데이터
     * @return CrawledProduct
     */
    public static CrawledProduct createCustom(
        Long id,
        Long mustItItemNo,
        Long sellerId,
        ProductData miniShopData,
        ProductData detailData,
        ProductData optionData
    ) {
        CrawledProduct product;
        if (id == null) {
            product = CrawledProduct.forNew(mustItItemNo, MustitSellerId.of(sellerId));
        } else {
            product = CrawledProduct.of(
                ProductId.of(id),
                mustItItemNo,
                MustitSellerId.of(sellerId)
            );
        }

        if (miniShopData != null) {
            product.updateMiniShopData(miniShopData);
        }
        if (detailData != null) {
            product.updateDetailData(detailData);
        }
        if (optionData != null) {
            product.updateOptionData(optionData);
        }

        return product;
    }

    /**
     * 해시값을 가진 CrawledProduct 생성
     *
     * @param dataHash 데이터 해시
     * @return CrawledProduct
     */
    public static CrawledProduct createWithHash(DataHash dataHash) {
        CrawledProduct product = createComplete();
        product.updateDataHash(dataHash);
        return product;
    }

    /**
     * 특정 버전의 CrawledProduct 생성
     *
     * @param version 버전
     * @return CrawledProduct
     */
    public static CrawledProduct createWithVersion(int version) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return CrawledProduct.reconstitute(
            ProductId.of(DEFAULT_ID),
            DEFAULT_MUSTIT_ITEM_NO,
            MustitSellerId.of(DEFAULT_SELLER_ID),
            ProductDataFixture.createMiniShopData(),
            ProductDataFixture.createDetailData(),
            ProductDataFixture.createOptionData(),
            DataHashFixture.create(),
            version,
            CompletionStatus.COMPLETE,
            now,
            now,
            now,
            now
        );
    }

    /**
     * 특정 데이터 해시를 가진 CrawledProduct 생성
     *
     * @param dataHash 데이터 해시
     * @return CrawledProduct
     */
    public static CrawledProduct createWithDataHash(DataHash dataHash) {
        LocalDateTime now = LocalDateTime.now(DEFAULT_CLOCK);
        return CrawledProduct.reconstitute(
            ProductId.of(DEFAULT_ID),
            DEFAULT_MUSTIT_ITEM_NO,
            MustitSellerId.of(DEFAULT_SELLER_ID),
            ProductDataFixture.createMiniShopData(),
            ProductDataFixture.createDetailData(),
            ProductDataFixture.createOptionData(),
            dataHash,
            1,
            CompletionStatus.COMPLETE,
            now,
            now,
            now,
            now
        );
    }
}
