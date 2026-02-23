package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.UserAgentJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.mapper.UserAgentJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.repository.UserAgentJpaRepository;
import com.ryuqq.crawlinghub.application.useragent.port.out.command.UserAgentPersistencePort;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * UserAgentCommandAdapter - UserAgent Command Adapter
 *
 * <p>CQRS의 Command(쓰기) 담당 Adapter입니다.
 *
 * <p><strong>설계 원칙:</strong>
 *
 * <ul>
 *   <li>의도 없이 저장만 담당 (persist만 제공)
 *   <li>모든 비즈니스 로직은 Domain에서 처리 완료된 상태로 전달
 *   <li>JPA 더티체킹 활용: ID null → INSERT, ID 있음 → UPDATE
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>❌ 비즈니스 로직 (Domain에서 처리)
 *   <li>❌ 조회 로직 (QueryAdapter로 분리)
 *   <li>❌ @Transactional 어노테이션 (Application Layer에서 관리)
 *   <li>❌ 의도가 담긴 메서드 (updateStatus, updateHealthScore 등)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentCommandAdapter implements UserAgentPersistencePort {

    private final UserAgentJpaRepository jpaRepository;
    private final UserAgentJpaEntityMapper mapper;

    public UserAgentCommandAdapter(
            UserAgentJpaRepository jpaRepository, UserAgentJpaEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /**
     * UserAgent 저장 (신규 생성 또는 수정)
     *
     * <p>Domain에서 비즈니스 로직이 처리된 상태로 전달받아 그대로 저장합니다.
     *
     * <p><strong>JPA 동작:</strong>
     *
     * <ul>
     *   <li>ID null → INSERT (신규 생성)
     *   <li>ID 있음 → UPDATE (더티체킹으로 변경된 필드만 업데이트)
     * </ul>
     *
     * @param userAgent 저장할 UserAgent (Domain에서 상태 변경 완료)
     * @return 저장된 UserAgent ID
     */
    @Override
    public UserAgentId persist(UserAgent userAgent) {
        UserAgentJpaEntity entity = mapper.toEntity(userAgent);
        UserAgentJpaEntity savedEntity = jpaRepository.save(entity);
        return UserAgentId.of(savedEntity.getId());
    }

    /**
     * 여러 UserAgent 저장 (배치 처리용)
     *
     * <p>Domain에서 비즈니스 로직이 처리된 상태로 전달받아 그대로 저장합니다.
     *
     * @param userAgents 저장할 UserAgent 목록 (Domain에서 상태 변경 완료)
     */
    @Override
    public void persistAll(List<UserAgent> userAgents) {
        List<UserAgentJpaEntity> entities = userAgents.stream().map(mapper::toEntity).toList();
        jpaRepository.saveAll(entities);
    }
}
