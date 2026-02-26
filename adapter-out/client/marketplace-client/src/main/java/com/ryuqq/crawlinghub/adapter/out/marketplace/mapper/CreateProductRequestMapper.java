package com.ryuqq.crawlinghub.adapter.out.marketplace.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.CreateProductPayload;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.CreateProductRequest;
import com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request.OptionType;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import org.springframework.stereotype.Component;

/**
 * CrawledProduct + Seller + Outbox → CreateProductRequest 변환 Mapper
 *
 * <p>외부몰 상품 등록 API의 최종 요청 DTO를 조립합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CreateProductRequestMapper {

    private static final long INBOUND_SOURCE_ID = 1L;

    private final CreateProductPayloadMapper payloadMapper;
    private final ObjectMapper objectMapper;

    public CreateProductRequestMapper(
            CreateProductPayloadMapper payloadMapper, ObjectMapper objectMapper) {
        this.payloadMapper = payloadMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * CreateProductRequest 조립
     *
     * @param outbox 동기화 Outbox
     * @param product 크롤링된 상품
     * @param seller 셀러 정보
     * @return CreateProductRequest
     * @throws JsonProcessingException 페이로드 직렬화 실패 시
     * @throws IllegalStateException omsSellerId가 null인 경우
     */
    public CreateProductRequest toRequest(
            CrawledProductSyncOutbox outbox, CrawledProduct product, Seller seller)
            throws JsonProcessingException {

        Long omsSellerId = seller.getOmsSellerId();
        if (omsSellerId == null) {
            throw new IllegalStateException(
                    "omsSellerId가 설정되지 않은 셀러입니다: sellerId=" + seller.getSellerIdValue());
        }

        CreateProductPayload payload = payloadMapper.toPayload(product);
        String rawPayloadJson = objectMapper.writeValueAsString(payload);

        return new CreateProductRequest(
                INBOUND_SOURCE_ID,
                String.valueOf(outbox.getItemNo()),
                product.getItemName(),
                product.getBrandName(),
                extractCategoryCode(product),
                omsSellerId,
                payload.regularPrice(),
                payload.currentPrice(),
                resolveOptionType(product).name(),
                product.getDescriptionMarkUp(),
                rawPayloadJson);
    }

    private String extractCategoryCode(CrawledProduct product) {
        return product.getCategory() != null ? product.getCategory().mediumCategoryCode() : "";
    }

    private OptionType resolveOptionType(CrawledProduct product) {
        if (product.getOptions() == null || product.getOptions().isEmpty()) {
            return OptionType.NONE;
        }

        boolean hasColor =
                product.getOptions().getDistinctColors().stream()
                        .anyMatch(c -> c != null && !c.isEmpty());
        boolean hasSize =
                product.getOptions().getDistinctSizes().stream()
                        .anyMatch(s -> s != null && !s.isEmpty());

        if (hasColor && hasSize) {
            return OptionType.COMBINATION;
        }
        if (hasColor || hasSize) {
            return OptionType.SINGLE;
        }
        return OptionType.NONE;
    }
}
