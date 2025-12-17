package com.ryuqq.crawlinghub.application.product.service.query;

import com.ryuqq.crawlinghub.application.product.assembler.CrawledProductAssembler;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductDetailResponse;
import com.ryuqq.crawlinghub.application.product.port.in.query.GetCrawledProductDetailUseCase;
import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.exception.CrawledProductNotFoundException;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
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
 * </ul>
 *
 * <p><strong>트랜잭션</strong>: QueryService는 @Transactional 사용 금지 (읽기 전용, 불필요)
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class GetCrawledProductDetailService implements GetCrawledProductDetailUseCase {

    private final CrawledProductQueryPort queryPort;
    private final CrawledProductAssembler assembler;

    public GetCrawledProductDetailService(
            CrawledProductQueryPort queryPort, CrawledProductAssembler assembler) {
        this.queryPort = queryPort;
        this.assembler = assembler;
    }

    @Override
    public CrawledProductDetailResponse execute(Long crawledProductId) {
        CrawledProductId id = CrawledProductId.of(crawledProductId);

        CrawledProduct product =
                queryPort
                        .findById(id)
                        .orElseThrow(() -> new CrawledProductNotFoundException(crawledProductId));

        return assembler.toDetailResponse(product);
    }
}
