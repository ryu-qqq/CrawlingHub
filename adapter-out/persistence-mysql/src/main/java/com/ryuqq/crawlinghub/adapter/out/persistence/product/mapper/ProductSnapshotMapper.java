package com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.ProductSnapshotEntity;
import com.ryuqq.crawlinghub.domain.product.*;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * ProductSnapshot Mapper (Pure Java)
 *
 * <p>Entity ↔ Domain 변환을 담당합니다.</p>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ MapStruct 금지 - Pure Java 사용</li>
 *   <li>✅ @Component (Spring Bean 등록)</li>
 *   <li>✅ Value Object 수동 변환</li>
 *   <li>✅ Null 체크 및 검증</li>
 *   <li>✅ JSON 필드 변환 (ObjectMapper 사용)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Component
public class ProductSnapshotMapper {

    private final ObjectMapper objectMapper;

    public ProductSnapshotMapper(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    /**
     * Domain → Entity 변환
     *
     * <p>Domain Model의 ID가 null이면 신규 생성 Entity 반환</p>
     */
    public ProductSnapshotEntity toEntity(ProductSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");

        Long id = snapshot.getIdValue();
        Long mustItItemNo = snapshot.getMustItItemNo();
        Long sellerId = snapshot.getSellerIdValue();

        String optionsJson = toJson(snapshot.getOptions());
        String productInfoJson = toJson(snapshot.getProductInfo());
        String shippingJson = toJson(snapshot.getShipping());
        String detailInfoJson = toJson(snapshot.getDetailInfo());

        if (id == null) {
            // 신규 생성 Entity
            return ProductSnapshotEntity.create(mustItItemNo, sellerId);
        } else {
            // DB reconstitute Entity
            return ProductSnapshotEntity.reconstitute(
                id,
                mustItItemNo,
                sellerId,
                snapshot.getProductName(),
                snapshot.getPrice(),
                snapshot.getMainImageUrl(),
                optionsJson,
                snapshot.getTotalStock(),
                productInfoJson,
                shippingJson,
                detailInfoJson,
                snapshot.getLastSyncedAt(),
                snapshot.getVersion(),
                snapshot.getCreatedAt(),
                snapshot.getUpdatedAt()
            );
        }
    }

    /**
     * Entity → Domain 변환
     */
    public ProductSnapshot toDomain(ProductSnapshotEntity entity) {
        Objects.requireNonNull(entity, "entity must not be null");

        List<ProductOption> options = fromJson(entity.getOptions(), new TypeReference<List<ProductOption>>() {});
        ProductInfoModule productInfo = fromJson(entity.getProductInfo(), new TypeReference<ProductInfoModule>() {});
        ShippingModule shipping = fromJson(entity.getShipping(), new TypeReference<ShippingModule>() {});
        ProductDetailInfoModule detailInfo = fromJson(entity.getDetailInfo(), new TypeReference<ProductDetailInfoModule>() {});

        return ProductSnapshot.reconstitute(
            ProductSnapshotId.of(entity.getId()),
            entity.getMustItItemNo(),
            MustitSellerId.of(entity.getSellerId()),
            entity.getProductName(),
            entity.getPrice(),
            entity.getMainImageUrl(),
            options,
            entity.getTotalStock(),
            productInfo,
            shipping,
            detailInfo,
            entity.getLastSyncedAt(),
            entity.getVersion(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 객체를 JSON 문자열로 변환
     */
    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 직렬화 실패: " + obj.getClass().getSimpleName(), e);
        }
    }

    /**
     * JSON 문자열을 객체로 변환
     */
    private <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 역직렬화 실패: " + typeRef.getType(), e);
        }
    }
}

