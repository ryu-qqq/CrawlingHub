package com.ryuqq.crawlinghub.application.product.assembler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import com.ryuqq.crawlinghub.domain.product.vo.MiniShopItem;
import com.ryuqq.crawlinghub.domain.product.vo.ProductDetailInfo;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOption;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CrawledRaw Assembler
 *
 * <p>Domain VO → CrawledRaw 변환 전용 컴포넌트
 *
 * <ul>
 *   <li>MiniShopItem → CrawledRaw (JSON 직렬화)
 *   <li>ProductDetailInfo → CrawledRaw (JSON 직렬화)
 *   <li>ProductOption 목록 → CrawledRaw (JSON 직렬화)
 *   <li>비즈니스 로직 없음 (단순 변환만)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledRawAssembler {

    private static final Logger log = LoggerFactory.getLogger(CrawledRawAssembler.class);

    private final ObjectMapper objectMapper;

    public CrawledRawAssembler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * MiniShopItem 목록 → CrawledRaw 목록 변환
     *
     * @param schedulerId 스케줄러 ID
     * @param sellerId 판매자 ID
     * @param items MiniShopItem 목록
     * @return CrawledRaw 목록
     */
    public List<CrawledRaw> toMiniShopRaws(
            long schedulerId, long sellerId, List<MiniShopItem> items) {
        List<CrawledRaw> result = new ArrayList<>(items.size());

        for (MiniShopItem item : items) {
            CrawledRaw raw = toRaw(schedulerId, sellerId, item.itemNo(), CrawlType.MINI_SHOP, item);
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
     * @return CrawledRaw (실패 시 null)
     */
    public CrawledRaw toDetailRaw(long schedulerId, long sellerId, ProductDetailInfo detailInfo) {
        return toRaw(schedulerId, sellerId, detailInfo.itemNo(), CrawlType.DETAIL, detailInfo);
    }

    /**
     * ProductOption 목록 → CrawledRaw 변환
     *
     * @param schedulerId 스케줄러 ID
     * @param sellerId 판매자 ID
     * @param itemNo 상품 번호
     * @param options 옵션 목록
     * @return CrawledRaw (실패 시 null)
     */
    public CrawledRaw toOptionRaw(
            long schedulerId, long sellerId, long itemNo, List<ProductOption> options) {
        return toRaw(schedulerId, sellerId, itemNo, CrawlType.OPTION, options);
    }

    /** 공통 변환 로직 - Object → JSON → CrawledRaw */
    private CrawledRaw toRaw(
            long schedulerId, long sellerId, long itemNo, CrawlType crawlType, Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            return CrawledRaw.create(schedulerId, sellerId, itemNo, crawlType, json);
        } catch (JsonProcessingException e) {
            log.error(
                    "JSON 변환 실패: type={}, itemNo={}, error={}", crawlType, itemNo, e.getMessage());
            return null;
        }
    }
}
