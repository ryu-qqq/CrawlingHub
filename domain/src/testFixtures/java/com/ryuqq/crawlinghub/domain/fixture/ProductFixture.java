package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.vo.ItemNo;
import com.ryuqq.crawlinghub.domain.vo.ProductId;

/**
 * Product 관련 테스트 데이터 생성 Fixture
 *
 * <p>Product와 관련된 Value Object의 기본값을 제공합니다.</p>
 *
 * <p>제공 메서드:</p>
 * <ul>
 *   <li>{@link #defaultProductId()} - 새로운 ProductId 생성</li>
 *   <li>{@link #defaultItemNo()} - 기본 상품 번호 (123456L)</li>
 * </ul>
 */
public class ProductFixture {

    /**
     * 기본 ProductId 생성
     *
     * @return 새로운 UUID 기반 ProductId
     */
    public static ProductId defaultProductId() {
        return ProductId.generate();
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
