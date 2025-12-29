package com.ryuqq.crawlinghub.application.image.manager.query;

import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductImageQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawledProductImage 조회 관리자
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>CrawledProductImage 조회 전용
 *   <li>업로드 완료 이미지 필터링
 *   <li>중복 URL 체크
 * </ul>
 *
 * <p><strong>CQRS</strong>: Query 담당 (CrawledProductImageTransactionManager = Command)
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductImageReadManager {

    private final CrawledProductImageQueryPort imageQueryPort;

    public CrawledProductImageReadManager(CrawledProductImageQueryPort imageQueryPort) {
        this.imageQueryPort = imageQueryPort;
    }

    /**
     * 이미지 ID로 이미지 조회
     *
     * @param imageId 이미지 ID
     * @return CrawledProductImage (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<CrawledProductImage> findById(Long imageId) {
        return imageQueryPort.findById(imageId);
    }

    /**
     * CrawledProduct ID로 이미지 목록 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return 이미지 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductImage> findByCrawledProductId(CrawledProductId crawledProductId) {
        return imageQueryPort.findByCrawledProductId(crawledProductId);
    }

    /**
     * CrawledProduct ID와 원본 URL로 이미지 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrl 원본 URL
     * @return CrawledProductImage (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<CrawledProductImage> findByCrawledProductIdAndOriginalUrl(
            CrawledProductId crawledProductId, String originalUrl) {
        return imageQueryPort.findByCrawledProductIdAndOriginalUrl(crawledProductId, originalUrl);
    }

    /**
     * 새로운 이미지 URL 목록 필터링 (중복 체크용 배치 쿼리 사용)
     *
     * <p>주어진 URL 목록에서 이미 저장된 URL을 제외하고 새로운 URL만 반환합니다.
     *
     * @param crawledProductId CrawledProduct ID
     * @param imageUrls 확인할 이미지 URL 목록
     * @return 새로운 이미지 URL 목록 (기존에 없는 URL만)
     */
    @Transactional(readOnly = true)
    public List<String> filterNewImageUrls(
            CrawledProductId crawledProductId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }

        List<String> existingUrls =
                imageQueryPort.findExistingOriginalUrls(crawledProductId, imageUrls);
        Set<String> existingSet = new HashSet<>(existingUrls);

        return imageUrls.stream().filter(url -> !existingSet.contains(url)).toList();
    }

    /**
     * CrawledProduct ID로 S3 업로드 완료된 이미지 목록 조회
     *
     * <p>s3Url이 존재하는 이미지만 반환합니다.
     *
     * @param crawledProductId CrawledProduct ID
     * @return 업로드 완료된 이미지 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductImage> findUploadedImagesByCrawledProductId(
            CrawledProductId crawledProductId) {
        return imageQueryPort.findByCrawledProductId(crawledProductId).stream()
                .filter(CrawledProductImage::isUploaded)
                .toList();
    }

    /**
     * CrawledProduct ID로 S3 업로드 완료된 썸네일 이미지 목록 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return 업로드 완료된 썸네일 이미지 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductImage> findUploadedThumbnailsByCrawledProductId(
            CrawledProductId crawledProductId) {
        return imageQueryPort
                .findByCrawledProductIdAndImageType(crawledProductId, ImageType.THUMBNAIL)
                .stream()
                .filter(CrawledProductImage::isUploaded)
                .toList();
    }

    /**
     * CrawledProduct ID로 S3 업로드 완료된 상세 이미지 목록 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return 업로드 완료된 상세 이미지 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductImage> findUploadedDescriptionImagesByCrawledProductId(
            CrawledProductId crawledProductId) {
        return imageQueryPort
                .findByCrawledProductIdAndImageType(crawledProductId, ImageType.DESCRIPTION)
                .stream()
                .filter(CrawledProductImage::isUploaded)
                .toList();
    }
}
