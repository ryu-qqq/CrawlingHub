package com.ryuqq.crawlinghub.application.product.sync.port.out;

import com.ryuqq.crawlinghub.domain.product.DataHash;

/**
 * 상품 데이터 해시 계산 Port
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface ProductHashCalculatorPort {

    /**
     * 상품 데이터로부터 해시값 계산
     *
     * @param miniShopData 미니샵 데이터
     * @param detailData 상세 데이터
     * @param optionData 옵션 데이터
     * @return 계산된 해시
     */
    DataHash calculateHash(String miniShopData, String detailData, String optionData);

    /**
     * 단일 데이터 해시 계산
     */
    String calculateSingleHash(String data);
}
