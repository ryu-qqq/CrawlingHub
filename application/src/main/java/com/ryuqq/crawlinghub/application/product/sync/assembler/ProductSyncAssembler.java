package com.ryuqq.crawlinghub.application.product.sync.assembler;

import com.ryuqq.crawlinghub.application.product.sync.dto.response.ChangeDetectionResponse;
import com.ryuqq.crawlinghub.domain.product.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.DataHash;

/**
 * 상품 동기화 Assembler
 *
 * <p>Domain ↔ DTO 변환 담당
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public final class ProductSyncAssembler {

    private ProductSyncAssembler() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * 변경 감지 결과로 변환
     *
     * @param product 크롤링 상품
     * @param previousHash 이전 해시
     * @param currentHash 현재 해시
     * @param hasChanged 변경 여부
     * @return ChangeDetectionResponse
     */
    public static ChangeDetectionResponse toChangeDetectionResponse(
        CrawledProduct product,
        DataHash previousHash,
        DataHash currentHash,
        boolean hasChanged
    ) {
        Integer previousVersion = hasChanged ? product.getVersion() - 1 : product.getVersion();

        return new ChangeDetectionResponse(
            product.getIdValue(),
            hasChanged,
            previousHash != null ? previousHash.getValue() : null,
            currentHash.getValue(),
            previousVersion,
            product.getVersion()
        );
    }

    /**
     * 내부 API용 JSON Payload 생성
     *
     * @param product 크롤링 상품
     * @return JSON 형식 문자열
     */
    public static String toInternalApiPayload(CrawledProduct product) {
        return String.format(
            "{\"productId\":%d,\"mustitItemNo\":\"%s\",\"sellerId\":%d,\"version\":%d,\"miniShopData\":%s,\"detailData\":%s,\"optionData\":%s}",
            product.getIdValue(),
            product.getMustitItemNo(),
            product.getSellerIdValue(),
            product.getVersion(),
            escapeJson(product.getMiniShopDataValue()),
            escapeJson(product.getDetailDataValue()),
            escapeJson(product.getOptionDataValue())
        );
    }

    /**
     * JSON 이스케이프 처리
     */
    private static String escapeJson(String data) {
        if (data == null) {
            return "null";
        }
        return data.replace("\"", "\\\"");
    }
}
