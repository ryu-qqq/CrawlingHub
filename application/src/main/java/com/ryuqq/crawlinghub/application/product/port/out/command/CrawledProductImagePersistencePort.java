package com.ryuqq.crawlinghub.application.product.port.out.command;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import java.util.List;

/**
 * 크롤링된 상품 이미지 저장 Port (Port Out - Command)
 *
 * <p>이미지 데이터의 영속화를 담당합니다.
 *
 * <p><strong>컨벤션</strong>: persist 메서드는 새 엔티티 저장 및 기존 엔티티 업데이트를 모두 처리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawledProductImagePersistencePort {

    /**
     * 이미지 저장 (upsert)
     *
     * @param image 저장할 이미지
     * @return 저장된 이미지 (ID 포함)
     */
    CrawledProductImage persist(CrawledProductImage image);

    /**
     * 이미지 일괄 저장 (upsert)
     *
     * @param images 저장할 이미지 목록
     * @return 저장된 이미지 목록 (ID 포함)
     */
    List<CrawledProductImage> persistAll(List<CrawledProductImage> images);
}
