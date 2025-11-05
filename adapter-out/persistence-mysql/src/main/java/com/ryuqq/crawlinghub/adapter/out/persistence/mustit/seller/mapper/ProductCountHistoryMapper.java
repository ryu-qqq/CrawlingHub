package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.entity.ProductCountHistoryEntity;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.mustit.seller.history.ProductCountHistory;
import com.ryuqq.crawlinghub.domain.mustit.seller.history.ProductCountHistoryId;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * ProductCountHistoryMapper - Entity ↔ Domain 변환 (MapStruct)
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductCountHistoryMapper {

    /**
     * Domain → Entity 변환
     *
     * @param domain Domain 객체
     * @return Entity
     */
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "sellerId", source = "sellerId.value")
    @Mapping(target = "productCount", source = "productCount")
    @Mapping(target = "executedDate", source = "executedDate")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductCountHistoryEntity toEntity(ProductCountHistory domain);

    /**
     * Entity → Domain 변환
     *
     * @param entity Entity
     * @return Domain 객체
     */
    default ProductCountHistory toDomain(ProductCountHistoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProductCountHistory.reconstitute(
            entity.getId() != null ? ProductCountHistoryId.of(entity.getId()) : null,
            MustitSellerId.of(entity.getSellerId()),
            entity.getProductCount(),
            entity.getExecutedDate()
        );
    }
}

