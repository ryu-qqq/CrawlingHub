package com.ryuqq.crawlinghub.application.useragent.port.out.command;

import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import java.util.List;

/**
 * UserAgent Persistence Port (DB 저장)
 *
 * <p>UserAgent 영속화를 위한 Port입니다.
 *
 * <p><strong>설계 원칙:</strong>
 *
 * <ul>
 *   <li>Persistence Layer는 의도 없이 저장만 담당
 *   <li>모든 비즈니스 로직은 Domain에서 처리 완료된 상태로 전달
 *   <li>JPA 더티체킹 활용: ID null → INSERT, ID 있음 → UPDATE
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface UserAgentPersistencePort {

    /**
     * UserAgent 저장 (신규 생성 또는 수정)
     *
     * <p>Domain에서 비즈니스 로직이 처리된 상태로 전달받아 그대로 저장합니다.
     *
     * @param userAgent 저장할 UserAgent (Domain에서 상태 변경 완료)
     * @return 저장된 UserAgent ID
     */
    UserAgentId persist(UserAgent userAgent);

    /**
     * 여러 UserAgent 저장 (배치 처리용)
     *
     * <p>Domain에서 비즈니스 로직이 처리된 상태로 전달받아 그대로 저장합니다.
     *
     * @param userAgents 저장할 UserAgent 목록 (Domain에서 상태 변경 완료)
     */
    void persistAll(List<UserAgent> userAgents);
}
