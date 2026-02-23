package com.ryuqq.crawlinghub.application.product.assembler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;
import com.ryuqq.crawlinghub.domain.product.vo.ProductDetailInfo;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CrawledRaw Mapper
 *
 * <p>Domain VO → CrawledRaw 변환 전용 컴포넌트
 *
 * <ul>
 *   <li>MiniShopItem → CrawledRaw (JSON 직렬화)
 *   <li>ProductDetailInfo → CrawledRaw (JSON 직렬화)
 *   <li>ProductOption 목록 → CrawledRaw (JSON 직렬화)
 *   <li>JSON → MiniShopItem (역직렬화)
 *   <li>JSON → ProductDetailInfo (역직렬화)
 *   <li>JSON → ProductOption 목록 (역직렬화)
 *   <li>비즈니스 로직 없음 (단순 변환만)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledRawMapper {

    private static final Logger log = LoggerFactory.getLogger(CrawledRawMapper.class);

    private final ObjectMapper objectMapper;

    public CrawledRawMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * MiniShopItem 목록 → CrawledRaw 목록 변환
     *
     * @param schedulerId 스케줄러 ID
     * @param sellerId 판매자 ID
     * @param items MiniShopItem 목록
     * @param now 현재 시각
     * @return CrawledRaw 목록
     */
    public List<CrawledRaw> toMiniShopRaws(
            long schedulerId, long sellerId, List<MiniShopItem> items, Instant now) {
        List<CrawledRaw> result = new ArrayList<>(items.size());

        for (MiniShopItem item : items) {
            CrawledRaw raw =
                    toRaw(schedulerId, sellerId, item.itemNo(), CrawlType.MINI_SHOP, item, now);
            if (raw != null) {
                result.add(raw);
            }
        }

        return result;
    }

    /**
     * ProductDetailInfo → CrawledRaw 변환
     *
     * @param schedulerId 스케줄러 ID
     * @param sellerId 판매자 ID
     * @param detailInfo 상품 상세 정보
     * @param now 현재 시각
     * @return CrawledRaw (실패 시 null)
     */
    public CrawledRaw toDetailRaw(
            long schedulerId, long sellerId, ProductDetailInfo detailInfo, Instant now) {
        return toRaw(schedulerId, sellerId, detailInfo.itemNo(), CrawlType.DETAIL, detailInfo, now);
    }

    /**
     * ProductOption 목록 → CrawledRaw 변환
     *
     * @param schedulerId 스케줄러 ID
     * @param sellerId 판매자 ID
     * @param itemNo 상품 번호
     * @param options 옵션 목록
     * @param now 현재 시각
     * @return CrawledRaw (실패 시 null)
     */
    public CrawledRaw toOptionRaw(
            long schedulerId,
            long sellerId,
            long itemNo,
            List<ProductOption> options,
            Instant now) {
        return toRaw(schedulerId, sellerId, itemNo, CrawlType.OPTION, options, now);
    }

    // === 역직렬화 (JSON → Domain VO) ===

    /**
     * JSON → MiniShopItem 역직렬화 (MINI_SHOP 타입)
     *
     * @param json JSON 문자열
     * @return MiniShopItem
     * @throws IllegalStateException JSON 파싱 실패 시
     */
    public MiniShopItem toMiniShopItem(String json) {
        try {
            return objectMapper.readValue(json, MiniShopItem.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("MiniShopItem 역직렬화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * JSON → ProductDetailInfo 역직렬화 (DETAIL 타입)
     *
     * @param json JSON 문자열
     * @return ProductDetailInfo
     * @throws IllegalStateException JSON 파싱 실패 시
     */
    public ProductDetailInfo toProductDetailInfo(String json) {
        try {
            return objectMapper.readValue(json, ProductDetailInfo.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("ProductDetailInfo 역직렬화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * JSON → ProductOption 목록 역직렬화 (OPTION 타입)
     *
     * @param json JSON 문자열
     * @return ProductOption 목록
     * @throws IllegalStateException JSON 파싱 실패 시
     */
    public List<ProductOption> toProductOptions(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("ProductOption 목록 역직렬화 실패: " + e.getMessage(), e);
        }
    }

    /** 공통 변환 로직 - Object → JSON → CrawledRaw */
    private CrawledRaw toRaw(
            long schedulerId,
            long sellerId,
            long itemNo,
            CrawlType crawlType,
            Object data,
            Instant now) {
        try {
            String json = objectMapper.writeValueAsString(data);
            return CrawledRaw.forNew(schedulerId, sellerId, itemNo, crawlType, json, now);
        } catch (JsonProcessingException e) {
            log.error(
                    "JSON 변환 실패: type={}, itemNo={}, error={}", crawlType, itemNo, e.getMessage());
            return null;
        }
    }
}
