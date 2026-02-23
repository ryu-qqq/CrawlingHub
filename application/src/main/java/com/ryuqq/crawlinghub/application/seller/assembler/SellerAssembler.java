package com.ryuqq.crawlinghub.application.seller.assembler;

import com.ryuqq.crawlinghub.application.seller.dto.response.SellerPageResult;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResult;
import com.ryuqq.crawlinghub.domain.common.vo.PageMeta;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Seller Assembler
 *
 * <p>Domain → Response DTO 변환 전용 컴포넌트
 *
 * <ul>
 *   <li>Domain → DTO: Domain을 Response DTO로 변환
 *   <li>비즈니스 로직 없음 (단순 변환만)
 * </ul>
 *
 * <p><strong>주의</strong>: Command → Domain 변환은 {@code SellerCommandFactory}에서 담당
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerAssembler {

    /**
     * Seller → SellerResult 변환
     *
     * @param seller Seller Aggregate
     * @return 셀러 결과 DTO
     */
    public SellerResult toResult(Seller seller) {
        return SellerResult.from(seller);
    }

    /**
     * Seller 목록 → SellerResult 목록 변환
     *
     * @param sellers Seller Aggregate 목록
     * @return 셀러 결과 DTO 목록
     */
    public List<SellerResult> toResults(List<Seller> sellers) {
        return sellers.stream().map(this::toResult).toList();
    }

    /**
     * Seller 목록 + 페이징 정보 → SellerPageResult 변환
     *
     * @param sellers Seller 목록
     * @param page 현재 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param totalElements 전체 데이터 개수
     * @return 페이지 결과
     */
    public SellerPageResult toPageResult(
            List<Seller> sellers, int page, int size, long totalElements) {
        List<SellerResult> results = toResults(sellers);
        PageMeta pageMeta = PageMeta.of(page, size, totalElements);
        return SellerPageResult.of(results, pageMeta);
    }
}
