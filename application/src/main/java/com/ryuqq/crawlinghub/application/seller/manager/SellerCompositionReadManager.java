package com.ryuqq.crawlinghub.application.seller.manager;

import com.ryuqq.crawlinghub.application.seller.dto.composite.SellerDetailResult;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerCompositionQueryPort;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seller Composite 조회 ReadManager
 *
 * <p><strong>책임</strong>: Composite 패턴을 통한 셀러 상세 조회
 *
 * <p><strong>규칙</strong>: SellerCompositionQueryPort 단일 의존, 읽기 전용 트랜잭션
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@Transactional(readOnly = true)
public class SellerCompositionReadManager {

    private final SellerCompositionQueryPort compositionQueryPort;

    public SellerCompositionReadManager(SellerCompositionQueryPort compositionQueryPort) {
        this.compositionQueryPort = compositionQueryPort;
    }

    /**
     * 셀러 상세 정보 조회
     *
     * @param sellerId 셀러 ID
     * @return 셀러 상세 결과
     * @throws SellerNotFoundException 셀러가 존재하지 않는 경우
     */
    public SellerDetailResult getSellerDetail(Long sellerId) {
        return compositionQueryPort
                .findSellerDetailById(sellerId)
                .orElseThrow(() -> new SellerNotFoundException(sellerId));
    }
}
