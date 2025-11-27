package com.ryuqq.crawlinghub.application.product.manager;

import com.ryuqq.crawlinghub.application.product.port.out.command.CrawledRawPersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledRawId;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawledRaw 트랜잭션 관리자
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>CrawledRaw 영속성 관리 (저장)
 *   <li>도메인 메서드 호출을 통한 상태 변경
 *   <li>트랜잭션 경계 관리
 * </ul>
 *
 * <p><strong>주의</strong>:
 *
 * <ul>
 *   <li>QueryPort는 사용하지 않음 (Facade/Scheduler에서 사용)
 *   <li>외부 API 호출 금지 (트랜잭션 내)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledRawManager {

    private final CrawledRawPersistencePort crawledRawPersistencePort;

    public CrawledRawManager(CrawledRawPersistencePort crawledRawPersistencePort) {
        this.crawledRawPersistencePort = crawledRawPersistencePort;
    }

    // === 저장 ===

    /**
     * CrawledRaw 단건 저장
     *
     * @param crawledRaw 저장할 CrawledRaw
     * @return 저장된 CrawledRaw ID
     */
    @Transactional
    public CrawledRawId save(CrawledRaw crawledRaw) {
        return crawledRawPersistencePort.persist(crawledRaw);
    }

    /**
     * CrawledRaw 벌크 저장
     *
     * @param crawledRaws 저장할 CrawledRaw 목록
     * @return 저장된 CrawledRaw ID 목록
     */
    @Transactional
    public List<CrawledRawId> saveAll(List<CrawledRaw> crawledRaws) {
        if (crawledRaws == null || crawledRaws.isEmpty()) {
            return List.of();
        }
        return crawledRawPersistencePort.persistAll(crawledRaws);
    }

    // === 상태 변경 ===

    /**
     * CrawledRaw 처리 완료 상태로 변경 및 저장
     *
     * @param crawledRaw 처리 완료할 CrawledRaw
     * @return 저장된 CrawledRaw ID
     */
    @Transactional
    public CrawledRawId markAsProcessed(CrawledRaw crawledRaw) {
        CrawledRaw processed = crawledRaw.markAsProcessed();
        return crawledRawPersistencePort.persist(processed);
    }

    /**
     * CrawledRaw 처리 실패 상태로 변경 및 저장
     *
     * @param crawledRaw 처리 실패한 CrawledRaw
     * @param errorMessage 에러 메시지
     * @return 저장된 CrawledRaw ID
     */
    @Transactional
    public CrawledRawId markAsFailed(CrawledRaw crawledRaw, String errorMessage) {
        CrawledRaw failed = crawledRaw.markAsFailed(errorMessage);
        return crawledRawPersistencePort.persist(failed);
    }
}
