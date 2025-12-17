package com.ryuqq.crawlinghub.application.product.port.out.query;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import java.util.List;
import java.util.Optional;

/**
 * 크롤링된 상품 이미지 조회 Port (Port Out - Query)
 *
 * <p>이미지 데이터의 조회를 담당합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawledProductImageQueryPort {

    /**
     * ID로 이미지 조회
     *
     * @param imageId 이미지 ID
     * @return 이미지 (Optional)
     */
    Optional<CrawledProductImage> findById(Long imageId);

    /**
     * CrawledProduct ID로 이미지 목록 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return 이미지 목록
     */
    List<CrawledProductImage> findByCrawledProductId(CrawledProductId crawledProductId);

    /**
     * CrawledProduct ID와 이미지 타입으로 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @param imageType 이미지 타입
     * @return 이미지 목록
     */
    List<CrawledProductImage> findByCrawledProductIdAndImageType(
            CrawledProductId crawledProductId, ImageType imageType);

    /**
     * CrawledProduct ID와 원본 URL로 이미지 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrl 원본 URL
     * @return 이미지 (Optional)
     */
    Optional<CrawledProductImage> findByCrawledProductIdAndOriginalUrl(
            CrawledProductId crawledProductId, String originalUrl);

    /**
     * 이미 존재하는 원본 URL 목록 조회 (중복 체크용)
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrls 확인할 원본 URL 목록
     * @return 이미 존재하는 원본 URL 목록
     */
    List<String> findExistingOriginalUrls(
            CrawledProductId crawledProductId, List<String> originalUrls);

    /**
     * CrawledProduct ID와 원본 URL로 존재 여부 확인
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrl 원본 URL
     * @return 존재하면 true
     */
    boolean existsByCrawledProductIdAndOriginalUrl(
            CrawledProductId crawledProductId, String originalUrl);
}
