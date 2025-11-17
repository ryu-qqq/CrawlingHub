package com.ryuqq.crawlinghub.application.seller.port.out.query;

import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;

import java.util.List;
import java.util.Optional;

/**
 * Seller Query Port (Outbound Port)
 *
 * <p>Application Layer에서 Persistence Layer로의 의존성 역전을 위한 인터페이스</p>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Query Port 명명 규칙: Find*, Exists*, Count*</li>
 *   <li>✅ 필수 메서드: findById, existsById, findByCriteria, countByCriteria</li>
 *   <li>✅ Domain Aggregate만 파라미터/반환 타입으로 사용</li>
 *   <li>✅ Infrastructure Layer 의존 금지</li>
 *   <li>✅ CQRS 원칙: 조회 전용 메서드만 포함</li>
 * </ul>
 *
 * <p><strong>구현체 위치:</strong> adapter-out/persistence-*</p>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public interface SellerQueryPort {

    /**
     * Seller ID로 조회
     *
     * @param id Seller ID (Value Object)
     * @return Optional로 감싼 Seller Aggregate (존재하지 않으면 empty)
     */
    Optional<Seller> findById(SellerId id);

    /**
     * Seller 존재 여부 확인
     *
     * @param id Seller ID (Value Object)
     * @return 존재하면 true, 아니면 false
     */
    boolean existsById(SellerId id);

    /**
     * 조회 조건으로 Seller 목록 검색
     *
     * <p>다양한 조건 조합 조회 지원:</p>
     * <ul>
     *   <li>상태별 조회 (status)</li>
     *   <li>이름 검색 (name like)</li>
     *   <li>생성일 범위 (createdAt between)</li>
     *   <li>복합 조건 조합</li>
     * </ul>
     *
     * @param criteria 조회 조건 (향후 SellerSearchCriteria VO로 대체 예정)
     * @return 조건에 맞는 Seller 목록
     */
    List<Seller> findByCriteria(Object criteria);

    /**
     * 조회 조건으로 Seller 개수 카운트
     *
     * @param criteria 조회 조건 (향후 SellerSearchCriteria VO로 대체 예정)
     * @return 조건에 맞는 Seller 개수
     */
    long countByCriteria(Object criteria);
}
