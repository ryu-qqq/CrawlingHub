package com.ryuqq.crawlinghub.application.schedule.manager;

import com.ryuqq.crawlinghub.application.schedule.port.out.LoadSchedulePort;
import com.ryuqq.crawlinghub.application.schedule.port.out.SaveSchedulePort;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.CrawlScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.CronExpression;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Schedule State Manager
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>✅ Schedule 상태 변경 (생성, 수정, 활성화, 일시정지)</li>
 *   <li>✅ Schedule CRUD (Port 호출)</li>
 *   <li>✅ 트랜잭션 경계 관리</li>
 * </ul>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Pure Java Constructor (Lombok 금지)</li>
 *   <li>✅ @Component (Spring Bean 등록)</li>
 *   <li>✅ @Transactional (트랜잭션 경계)</li>
 *   <li>✅ Single Responsibility (Schedule 상태 관리만)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class ScheduleStateManager {

    private final SaveSchedulePort saveSchedulePort;
    private final LoadSchedulePort loadSchedulePort;

    public ScheduleStateManager(
        SaveSchedulePort saveSchedulePort,
        LoadSchedulePort loadSchedulePort
    ) {
        this.saveSchedulePort = saveSchedulePort;
        this.loadSchedulePort = loadSchedulePort;
    }

    /**
     * Schedule 생성 및 저장 (이벤트 발행 포함)
     *
     * <p>처리 흐름:
     * <ol>
     *   <li>CrawlSchedule Domain 생성</li>
     *   <li>다음 실행 시간 계산</li>
     *   <li>DB 저장 (ID 생성)</li>
     *   <li>Domain Event 발행 (ID 생성 후)</li>
     *   <li>재저장 (이벤트 포함)</li>
     * </ol>
     *
     * @param sellerId 셀러 ID
     * @param cronExpression Cron 표현식
     * @param nextExecutionTime 다음 실행 시간
     * @param idemKey Outbox Idempotency Key
     * @return 저장된 CrawlSchedule (Domain Event 포함)
     */
    @Transactional
    public CrawlSchedule createSchedule(
        MustitSellerId sellerId,
        CronExpression cronExpression,
        LocalDateTime nextExecutionTime,
        String idemKey
    ) {
        // 1. Domain 생성
        CrawlSchedule schedule = CrawlSchedule.forNew(sellerId, cronExpression);

        // 2. 다음 실행 시간 계산
        schedule.calculateNextExecution(nextExecutionTime);

        // 3. DB 저장 (ID 생성)
        CrawlSchedule savedSchedule = saveSchedulePort.save(schedule);

        // 4. Domain Event 발행 (ID 생성 후)
        savedSchedule.publishCreatedEvent(idemKey);

        // 5. 재저장 (이벤트 포함 - 트랜잭션 커밋 시 자동 발행)
        return saveSchedulePort.save(savedSchedule);
    }

    /**
     * Schedule 수정 및 저장 (이벤트 발행 포함)
     *
     * <p>처리 흐름:
     * <ol>
     *   <li>기존 Schedule 조회</li>
     *   <li>Schedule 수정 (updateSchedule - 이벤트 자동 등록)</li>
     *   <li>다음 실행 시간 재계산</li>
     *   <li>DB 저장 (Domain Event 포함 - 트랜잭션 커밋 시 자동 발행)</li>
     * </ol>
     *
     * @param scheduleId 스케줄 ID
     * @param cronExpression 새로운 Cron 표현식
     * @param nextExecutionTime 다음 실행 시간
     * @param idemKey Outbox Idempotency Key
     * @return 수정된 CrawlSchedule
     */
    @Transactional
    public CrawlSchedule updateSchedule(
        CrawlScheduleId scheduleId,
        CronExpression cronExpression,
        LocalDateTime nextExecutionTime,
        String idemKey
    ) {
        // 1. 기존 Schedule 조회
        CrawlSchedule schedule = loadSchedulePort.findById(scheduleId)
            .orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다: " + scheduleId.value()));

        // 2. Schedule 수정 (Domain에서 이벤트 자동 등록)
        schedule.updateSchedule(cronExpression, idemKey);

        // 3. 다음 실행 시간 재계산
        schedule.calculateNextExecution(nextExecutionTime);

        // 4. DB 저장 (트랜잭션 커밋 시 Domain Event 자동 발행)
        return saveSchedulePort.save(schedule);
    }

    /**
     * Schedule 활성화
     *
     * @param scheduleId 스케줄 ID
     * @return 활성화된 CrawlSchedule
     */
    @Transactional
    public CrawlSchedule activateSchedule(CrawlScheduleId scheduleId) {
        CrawlSchedule schedule = loadSchedulePort.findById(scheduleId)
            .orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다: " + scheduleId.value()));

        schedule.activate();
        return saveSchedulePort.save(schedule);
    }

    /**
     * Schedule 일시정지
     *
     * @param scheduleId 스케줄 ID
     * @return 일시정지된 CrawlSchedule
     */
    @Transactional
    public CrawlSchedule suspendSchedule(CrawlScheduleId scheduleId) {
        CrawlSchedule schedule = loadSchedulePort.findById(scheduleId)
            .orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다: " + scheduleId.value()));

        schedule.suspend();
        return saveSchedulePort.save(schedule);
    }

    /**
     * Schedule 조회
     *
     * @param scheduleId 스케줄 ID
     * @return CrawlSchedule
     */
    @Transactional(readOnly = true)
    public CrawlSchedule getSchedule(CrawlScheduleId scheduleId) {
        return loadSchedulePort.findById(scheduleId)
            .orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다: " + scheduleId.value()));
    }
}
