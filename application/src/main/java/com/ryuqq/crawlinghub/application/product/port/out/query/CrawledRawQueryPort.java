package com.ryuqq.crawlinghub.application.product.port.out.query;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledRawId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import com.ryuqq.crawlinghub.domain.product.vo.RawDataStatus;
import java.util.List;
import java.util.Optional;

/**
 * CrawledRaw 조회 Port (Port Out - Query)
 *
 * <p>가공 스케줄러에서 PENDING 상태의 Raw 데이터를 타입별로 조회합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawledRawQueryPort {

    /**
     * ID로 조회
     *
     * @param id CrawledRaw ID
     * @return CrawledRaw (Optional)
     */
    Optional<CrawledRaw> findById(CrawledRawId id);

    /**
     * 상태와 타입으로 조회 (가공 스케줄러용)
     *
     * <p>PENDING 상태의 특정 타입 Raw 데이터를 생성순으로 조회합니다.
     *
     * @param status 상태 (보통 PENDING)
     * @param crawlType 크롤링 타입
     * @param limit 최대 조회 건수
     * @return CrawledRaw 목록
     */
    List<CrawledRaw> findByStatusAndType(RawDataStatus status, CrawlType crawlType, int limit);

    /**
     * seller_id와 item_no로 특정 타입 조회
     *
     * <p>DETAIL/OPTION 처리 시 해당 상품의 Raw 데이터가 있는지 확인용
     *
     * @param sellerId 판매자 ID
     * @param itemNo 상품 번호
     * @param crawlType 크롤링 타입
     * @return CrawledRaw (Optional)
     */
    Optional<CrawledRaw> findBySellerIdAndItemNoAndType(
            long sellerId, long itemNo, CrawlType crawlType);
}
