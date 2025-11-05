package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.UserAgentEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.mapper.UserAgentMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.repository.UserAgentJpaRepository;
import com.ryuqq.crawlinghub.application.useragent.port.out.SaveUserAgentPort;
import com.ryuqq.crawlinghub.domain.useragent.UserAgent;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserAgent Command Adapter - CQRS Command Adapter (쓰기 전용)
 *
 * <p><strong>CQRS 패턴 적용 - Command 작업만 수행 ⭐</strong></p>
 * <ul>
 *   <li>✅ Write 작업 전용 (save)</li>
 *   <li>✅ Domain Aggregate를 Entity로 변환하여 저장</li>
 *   <li>✅ 저장된 Entity를 Domain Aggregate로 변환하여 반환</li>
 * </ul>
 *
 * <p><strong>주의사항:</strong></p>
 * <ul>
 *   <li>❌ Read 작업은 UserAgentQueryAdapter에서 처리</li>
 *   <li>❌ Query 작업은 이 Adapter에서 금지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class UserAgentCommandAdapter implements SaveUserAgentPort {

    private final UserAgentJpaRepository jpaRepository;
    private final UserAgentMapper mapper;

    /**
     * Adapter 생성자
     *
     * @param jpaRepository JPA Repository
     * @param mapper Domain ↔ Entity 변환 Mapper
     */
    public UserAgentCommandAdapter(
        UserAgentJpaRepository jpaRepository,
        UserAgentMapper mapper
    ) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    /**
     * UserAgent 저장 (신규 생성 또는 수정)
     *
     * <p>Domain Aggregate를 Entity로 변환하여 저장한 후,
     * 저장된 Entity를 다시 Domain Aggregate로 변환하여 반환합니다.</p>
     *
     * @param userAgent 저장할 UserAgent Aggregate (null 불가)
     * @return 저장된 UserAgent Aggregate (ID 포함)
     * @throws IllegalArgumentException userAgent가 null인 경우
     */
    @Override
    @Transactional
    public UserAgent save(UserAgent userAgent) {
        Objects.requireNonNull(userAgent, "userAgent must not be null");

        UserAgentEntity entity = mapper.toEntity(userAgent);
        UserAgentEntity savedEntity = jpaRepository.save(entity);

        return mapper.toDomain(savedEntity);
    }
}

