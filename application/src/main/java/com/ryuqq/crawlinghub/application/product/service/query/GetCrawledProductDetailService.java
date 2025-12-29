package com.ryuqq.crawlinghub.application.product.service.query;

import com.ryuqq.crawlinghub.application.image.manager.query.CrawledProductImageReadManager;
import com.ryuqq.crawlinghub.application.product.assembler.CrawledProductAssembler;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductDetailResponse;
import com.ryuqq.crawlinghub.application.product.manager.query.CrawledProductReadManager;
import com.ryuqq.crawlinghub.application.product.port.in.query.GetCrawledProductDetailUseCase;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImage;
import com.ryuqq.crawlinghub.domain.product.exception.CrawledProductNotFoundException;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * CrawledProduct 상세 조회 Service
 *
 * <p>크롤링 상품 상세 정보 조회 UseCase 구현
 *
 * <ul>
 *   <li>조회 전용
 *   <li>존재하지 않으면 CrawledProductNotFoundException 발생
 *   <li>Domain → DetailResponse 변환 (Assembler)
 *   <li>이미지는 CrawledProductImage 테이블에서 업로드된 것만 조회 (JSON 파싱 제외)
 * </ul>
 *
 * <p><strong>트랜잭션</strong>: QueryService는 @Transactional 사용 금지 (읽기 전용, 불필요)
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class GetCrawledProductDetailService implements GetCrawledProductDetailUseCase {

    private final CrawledProductReadManager productReadManager;
    private final CrawledProductImageReadManager imageReadManager;
    private final CrawledProductAssembler assembler;

    public GetCrawledProductDetailService(
            CrawledProductReadManager productReadManager,
            CrawledProductImageReadManager imageReadManager,
            CrawledProductAssembler assembler) {
        this.productReadManager = productReadManager;
        this.imageReadManager = imageReadManager;
        this.assembler = assembler;
    }

    @Override
    public CrawledProductDetailResponse execute(Long crawledProductId) {
        CrawledProductId id = CrawledProductId.of(crawledProductId);

        CrawledProduct product =
                productReadManager
                        .findById(id)
                        .orElseThrow(() -> new CrawledProductNotFoundException(crawledProductId));

        List<CrawledProductImage> uploadedThumbnails =
                imageReadManager.findUploadedThumbnailsByCrawledProductId(id);

        List<CrawledProductImage> uploadedDescriptionImages =
                imageReadManager.findUploadedDescriptionImagesByCrawledProductId(id);

        return assembler.toDetailResponseWithUploadedImages(
                product, uploadedThumbnails, uploadedDescriptionImages);
    }
}
