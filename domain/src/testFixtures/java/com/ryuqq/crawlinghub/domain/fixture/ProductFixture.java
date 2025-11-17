package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.product.aggregate.product.Product;
import com.ryuqq.crawlinghub.domain.product.vo.ItemNo;
import com.ryuqq.crawlinghub.domain.product.vo.ProductId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

import java.time.Clock;

/**
 * Product 관련 테스트 데이터 생성 Fixture
 *
 * <p>Product Aggregate와 관련된 Value Object의 기본값을 제공합니다.</p>
 *
 * <p>표준 패턴 준수:</p>
 * <ul>
 *   <li>{@link #forNew()} - 새 Product 생성 (ID 자동 생성)</li>
 *   <li>{@link #of(ProductId, ItemNo, SellerId)} - 불변 속성으로 재구성</li>
 *   <li>{@link #reconstitute(ProductId, ItemNo, SellerId, String, String, String, Boolean)} - 완전한 재구성</li>
 * </ul>
 *
 * <p>레거시 호환 메서드:</p>
 * <ul>
 *   <li>{@link #defaultProduct()} - 기본 Product (레거시)</li>
 *   <li>{@link #defaultProductId()} - 새로운 ProductId 생성</li>
 *   <li>{@link #defaultItemNo()} - 기본 상품 번호 (123456L)</li>
 * </ul>
 */
public class ProductFixture {

    private static final ItemNo DEFAULT_ITEM_NO = new ItemNo(123456L);

    /**
     * 새로운 Product 생성 (표준 패턴)
     *
     * <p>forNew() 패턴: ID 자동 생성, INCOMPLETE 상태</p>
     *
     * @return 새로 생성된 Product
     */
    public static Product forNew() {
        return forNew(Clock.systemDefaultZone());
    }

    /**
     * 새로운 Product 생성 (표준 패턴 + Clock 주입)
     *
     * <p>forNew(Clock) 패턴: ID 자동 생성, INCOMPLETE 상태, Clock 주입</p>
     *
     * @param clock 시간 제어 (테스트 가능성)
     * @return 새로 생성된 Product
     */
    public static Product forNew(Clock clock) {
        SellerId sellerId = SellerFixture.defaultSellerId();
        return Product.forNew(DEFAULT_ITEM_NO, sellerId, clock);
    }

    /**
     * 불변 속성으로 Product 재구성 (표준 패턴)
     *
     * <p>of() 패턴: ID 포함, 테스트용 간편 생성</p>
     *
     * @param productId Product ID
     * @param itemNo 상품 번호
     * @param sellerId 판매자 ID
     * @return 재구성된 Product
     */
    public static Product of(ProductId productId, ItemNo itemNo, SellerId sellerId) {
        return of(productId, itemNo, sellerId, Clock.systemDefaultZone());
    }

    /**
     * 불변 속성으로 Product 재구성 (표준 패턴 + Clock 주입)
     *
     * <p>of(Clock) 패턴: ID 포함, 테스트용 간편 생성, Clock 주입</p>
     *
     * @param productId Product ID
     * @param itemNo 상품 번호
     * @param sellerId 판매자 ID
     * @param clock 시간 제어
     * @return 재구성된 Product
     */
    public static Product of(ProductId productId, ItemNo itemNo, SellerId sellerId, Clock clock) {
        return Product.of(itemNo, sellerId, clock);
    }

    /**
     * 완전한 Product 재구성 (표준 패턴)
     *
     * <p>reconstitute() 패턴: 모든 필드 포함, DB 조회 시뮬레이션</p>
     *
     * @param productId Product ID
     * @param itemNo 상품 번호
     * @param sellerId 판매자 ID
     * @param minishopDataHash 미니샵 데이터 해시
     * @param detailDataHash 상세 데이터 해시
     * @param optionDataHash 옵션 데이터 해시
     * @param isComplete 완료 상태
     * @return 재구성된 Product
     */
    public static Product reconstitute(ProductId productId, ItemNo itemNo, SellerId sellerId,
                                        String minishopDataHash, String detailDataHash, String optionDataHash, Boolean isComplete) {
        return reconstitute(productId, itemNo, sellerId, minishopDataHash, detailDataHash, optionDataHash, isComplete, Clock.systemDefaultZone());
    }

    /**
     * 완전한 Product 재구성 (표준 패턴 + Clock 주입)
     *
     * <p>reconstitute(Clock) 패턴: 모든 필드 포함, DB 조회 시뮬레이션, Clock 주입</p>
     *
     * @param productId Product ID
     * @param itemNo 상품 번호
     * @param sellerId 판매자 ID
     * @param minishopDataHash 미니샵 데이터 해시
     * @param detailDataHash 상세 데이터 해시
     * @param optionDataHash 옵션 데이터 해시
     * @param isComplete 완료 상태
     * @param clock 시간 제어
     * @return 재구성된 Product
     */
    public static Product reconstitute(ProductId productId, ItemNo itemNo, SellerId sellerId,
                                        String minishopDataHash, String detailDataHash, String optionDataHash, Boolean isComplete, Clock clock) {
        return Product.reconstitute(itemNo, sellerId, minishopDataHash, detailDataHash, optionDataHash, isComplete, clock);
    }

    /**
     * 기본 Product 생성 (레거시)
     *
     * @deprecated Use {@link #forNew()} instead
     * @return 데이터 해시가 없는 INCOMPLETE 상태의 Product
     */
    @Deprecated
    public static Product defaultProduct() {
        return forNew();
    }

    /**
     * 기본 ProductId 생성
     *
     * @return 새로운 UUID 기반 ProductId
     */
    public static ProductId defaultProductId() {
        return ProductId.forNew();
    }

    /**
     * 기본 ItemNo 반환
     *
     * @return ItemNo(123456L)
     */
    public static ItemNo defaultItemNo() {
        return new ItemNo(123456L);
    }
}
