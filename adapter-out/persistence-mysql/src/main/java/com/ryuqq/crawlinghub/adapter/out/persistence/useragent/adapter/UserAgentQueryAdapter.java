package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.UserAgentJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.mapper.UserAgentJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.repository.UserAgentQueryDslRepository;
import com.ryuqq.crawlinghub.application.useragent.port.out.query.UserAgentQueryPort;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * UserAgentQueryAdapter - UserAgent Query Adapter
 *
 * <p>CQRS의 Query(읽기) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>단건 조회 (findById)
 *   <li>상태별 조회 (findByStatus, findAllAvailable)
 *   <li>카운트 조회 (countByStatus, countAll)
 *   <li>QueryDslRepository 호출
 *   <li>Mapper를 통한 Entity → Domain 변환
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>❌ 비즈니스 로직
 *   <li>❌ 저장/수정/삭제 (CommandAdapter로 분리)
 *   <li>❌ JPAQueryFactory 직접 사용 (QueryDslRepository에서 처리)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentQueryAdapter implements UserAgentQueryPort {

    private final UserAgentQueryDslRepository queryDslRepository;
    private final UserAgentJpaEntityMapper mapper;

    public UserAgentQueryAdapter(
            UserAgentQueryDslRepository queryDslRepository, UserAgentJpaEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    /**
     * 활성화된 UserAgent 전체 조회 (Pool 초기화용)
     *
     * @return AVAILABLE 상태인 UserAgent 목록
     */
    @Override
    public List<UserAgent> findAllAvailable() {
        List<UserAgentJpaEntity> entities =
                queryDslRepository.findByStatus(UserAgentStatus.AVAILABLE);
        return entities.stream().map(mapper::toDomain).toList();
    }

    /**
     * ID로 UserAgent 단건 조회
     *
     * @param userAgentId UserAgent ID
     * @return UserAgent Domain (Optional)
     */
    @Override
    public Optional<UserAgent> findById(UserAgentId userAgentId) {
        return queryDslRepository.findById(userAgentId.value()).map(mapper::toDomain);
    }

    /**
     * 상태별 UserAgent 개수 조회
     *
     * @param status UserAgent 상태
     * @return 해당 상태의 UserAgent 개수
     */
    @Override
    public long countByStatus(UserAgentStatus status) {
        return queryDslRepository.countByStatus(status);
    }

    /**
     * 전체 UserAgent 개수 조회
     *
     * @return 전체 UserAgent 개수
     */
    @Override
    public long countAll() {
        return queryDslRepository.countAll();
    }

    /**
     * 상태별 UserAgent 목록 조회
     *
     * @param status UserAgent 상태
     * @return 해당 상태의 UserAgent 목록
     */
    @Override
    public List<UserAgent> findByStatus(UserAgentStatus status) {
        List<UserAgentJpaEntity> entities = queryDslRepository.findByStatus(status);
        return entities.stream().map(mapper::toDomain).toList();
    }
}
