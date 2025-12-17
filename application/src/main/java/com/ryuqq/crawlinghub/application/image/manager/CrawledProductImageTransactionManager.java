package com.ryuqq.crawlinghub.application.image.manager;

import com.ryuqq.crawlinghub.application.product.port.out.command.CrawledProductImagePersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawledProductImage 트랜잭션 관리자
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>CrawledProductImage 저장 관리
 *   <li>이미지 S3 URL 업데이트
 *   <li>트랜잭션 경계 관리
 * </ul>
 *
 * <p><strong>SRP</strong>: CrawledProductImage Aggregate에 대한 영속성 작업만 담당
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductImageTransactionManager {

    private final CrawledProductImagePersistencePort imagePersistencePort;
    private final Clock clock;

    public CrawledProductImageTransactionManager(
            CrawledProductImagePersistencePort imagePersistencePort, Clock clock) {
        this.imagePersistencePort = imagePersistencePort;
        this.clock = clock;
    }

    /**
     * 이미지 단건 저장
     *
     * @param image 저장할 이미지
     * @return 저장된 이미지 (ID 포함)
     */
    @Transactional
    public CrawledProductImage save(CrawledProductImage image) {
        return imagePersistencePort.save(image);
    }

    /**
     * 이미지 일괄 저장
     *
     * <p>Bundle에서 생성된 이미지 목록을 저장합니다.
     *
     * @param images 저장할 이미지 목록
     * @return 저장된 이미지 목록 (ID 포함)
     */
    @Transactional
    public List<CrawledProductImage> saveAll(List<CrawledProductImage> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return imagePersistencePort.saveAll(images);
    }

    /**
     * 이미지 업로드 완료 처리 (S3 URL 및 파일 자산 ID 업데이트)
     *
     * @param image 업데이트할 이미지
     * @param s3Url 업로드된 S3 URL
     * @param fileAssetId Fileflow 파일 자산 ID
     */
    @Transactional
    public void completeUpload(CrawledProductImage image, String s3Url, String fileAssetId) {
        image.completeUpload(s3Url, fileAssetId, clock);
        imagePersistencePort.update(image);
    }
}
