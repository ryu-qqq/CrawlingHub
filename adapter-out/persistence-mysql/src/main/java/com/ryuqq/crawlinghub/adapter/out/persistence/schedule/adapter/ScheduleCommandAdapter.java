package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.mapper.ScheduleMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository.ScheduleJpaRepository;
import com.ryuqq.crawlinghub.application.schedule.port.out.SaveSchedulePort;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Schedule Command Adapter (CQRS - Command)
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>✅ CUD (Create, Update, Delete) 작업 전담</li>
 *   <li>✅ SaveSchedulePort 구현</li>
 *   <li>✅ Domain → Entity 변환</li>
 *   <li>✅ JPA Repository 호출</li>
 * </ul>
 *
 * <p><strong>CQRS 패턴:</strong></p>
 * <ul>
 *   <li>✅ Command (쓰기) 전용 Adapter</li>
 *   <li>✅ Query (읽기)는 ScheduleQueryAdapter에 위임</li>
 *   <li>✅ 트랜잭션 관리는 Application Layer에서 수행</li>
 * </ul>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지 - Pure Java Constructor</li>
 *   <li>✅ @Component (Spring Bean 등록)</li>
 *   <li>✅ Objects.requireNonNull() 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class ScheduleCommandAdapter implements SaveSchedulePort {

    private final ScheduleJpaRepository repository;
    private final ScheduleMapper mapper;

    /**
     * 생성자
     *
     * @param repository Schedule JPA Repository
     * @param mapper Domain ↔ Entity Mapper
     */
    public ScheduleCommandAdapter(
        ScheduleJpaRepository repository,
        ScheduleMapper mapper
    ) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    /**
     * 스케줄 저장 (신규 생성 또는 수정)
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Domain → Entity 변환</li>
     *   <li>JPA save() 호출</li>
     *   <li>Entity → Domain 변환</li>
     *   <li>저장된 Domain 반환 (ID 포함)</li>
     * </ol>
     *
     * <p>트랜잭션은 Application Layer에서 관리됩니다.</p>
     *
     * @param schedule 저장할 스케줄
     * @return 저장된 스케줄 (ID 포함)
     */
    @Override
    public CrawlSchedule save(CrawlSchedule schedule) {
        Objects.requireNonNull(schedule, "schedule must not be null");

        // 1. Domain → Entity
        ScheduleEntity entity = mapper.toEntity(schedule);

        // 2. JPA save
        ScheduleEntity savedEntity = repository.save(entity);

        // 3. Entity → Domain
        return mapper.toDomain(savedEntity);
    }
}
