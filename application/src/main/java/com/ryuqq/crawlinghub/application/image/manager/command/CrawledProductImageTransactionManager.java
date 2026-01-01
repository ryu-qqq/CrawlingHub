package com.ryuqq.crawlinghub.application.image.manager.command;

import com.ryuqq.crawlinghub.application.product.port.out.command.CrawledProductImagePersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawledProductImage 트랜잭션 관리자
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>CrawledProductImage 저장 관리 (upsert)
 *   <li>트랜잭션 경계 관리
 * </ul>
 *
 * <p><strong>SRP</strong>: CrawledProductImage Aggregate에 대한 영속성 작업만 담당
 *
 * <p><strong>주의</strong>: 비즈니스 로직(domain method 호출)은 Service 레이어에서 수행해야 합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductImageTransactionManager {

    private final CrawledProductImagePersistencePort imagePersistencePort;

    public CrawledProductImageTransactionManager(
            CrawledProductImagePersistencePort imagePersistencePort) {
        this.imagePersistencePort = imagePersistencePort;
    }

    /**
     * 이미지 단건 저장 (upsert)
     *
     * @param image 저장할 이미지
     * @return 저장된 이미지 (ID 포함)
     */
    @Transactional
    public CrawledProductImage persist(CrawledProductImage image) {
        return imagePersistencePort.persist(image);
    }

    /**
     * 이미지 일괄 저장 (upsert)
     *
     * <p>Bundle에서 생성된 이미지 목록을 저장합니다.
     *
     * @param images 저장할 이미지 목록
     * @return 저장된 이미지 목록 (ID 포함)
     */
    @Transactional
    public List<CrawledProductImage> persistAll(List<CrawledProductImage> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return imagePersistencePort.persistAll(images);
    }
}
