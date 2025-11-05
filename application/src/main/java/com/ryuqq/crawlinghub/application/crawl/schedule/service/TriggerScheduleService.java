package com.ryuqq.crawlinghub.application.crawl.schedule.service;

import com.ryuqq.crawlinghub.application.crawl.schedule.dto.command.TriggerScheduleCommand;
import com.ryuqq.crawlinghub.application.crawl.schedule.port.in.TriggerScheduleUseCase;
import com.ryuqq.crawlinghub.application.crawl.schedule.port.out.LoadSchedulePort;
import com.ryuqq.crawlinghub.application.crawl.schedule.port.out.SaveSchedulePort;
import com.ryuqq.crawlinghub.application.crawl.schedule.validator.CronExpressionValidator;
import com.ryuqq.crawlinghub.domain.crawl.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.crawl.schedule.CrawlScheduleId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 스케줄 트리거 UseCase 구현체 (EventBridge에서 호출)
 *
 * <p>실제 크롤링 태스크를 생성하고 Outbox에 저장합니다.
 * 이 UseCase는 EventBridge에서 주기적으로 호출됩니다.
 *
 * <p>⚠️ Transaction 경계:
 * <ul>
 *   <li>순수 DB 작업만 수행하므로 트랜잭션 안전</li>
 *   <li>외부 API 호출 없음</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Service
public class TriggerScheduleService implements TriggerScheduleUseCase {

    private final LoadSchedulePort loadSchedulePort;
    private final SaveSchedulePort saveSchedulePort;
    private final CronExpressionValidator cronValidator;

    public TriggerScheduleService(
        LoadSchedulePort loadSchedulePort,
        SaveSchedulePort saveSchedulePort,
        CronExpressionValidator cronValidator
    ) {
        this.loadSchedulePort = loadSchedulePort;
        this.saveSchedulePort = saveSchedulePort;
        this.cronValidator = cronValidator;
    }

    /**
     * 스케줄 트리거 (크롤링 시작)
     *
     * <p>실행 순서:
     * 1. 스케줄 조회
     * 2. 실행 가능 여부 확인 (활성 상태 + 실행 시간 도래)
     * 3. CrawlTask 생성 및 Outbox 저장 (TODO: TASK-03에서 구현)
     * 4. 실행 완료 기록
     * 5. 다음 실행 시간 업데이트
     *
     * @param command 트리거할 스케줄 ID
     * @throws IllegalArgumentException 스케줄을 찾을 수 없거나 실행 불가능한 경우
     */
    @Override
    @Transactional
    public void execute(TriggerScheduleCommand command) {
        CrawlScheduleId scheduleId = CrawlScheduleId.of(command.scheduleId());

        // 1. 스케줄 조회
        CrawlSchedule schedule = loadSchedulePort.findById(scheduleId)
            .orElseThrow(() -> new IllegalArgumentException(
                "스케줄을 찾을 수 없습니다: " + command.scheduleId()
            ));

        // 2. 실행 가능 여부 확인
        if (!schedule.isActive()) {
            throw new IllegalStateException("비활성 스케줄입니다: " + command.scheduleId());
        }

        if (!schedule.isTimeToExecute()) {
            throw new IllegalStateException(
                "실행 시간이 아직 도래하지 않았습니다. 다음 실행 시간: " + schedule.getNextExecutionTime()
            );
        }

        // 3. CrawlTask 생성 및 Outbox 저장
        // TODO: TASK-03 InitiateCrawlingUseCase에서 구현 예정
        // createInitialCrawlTask(schedule.getSellerIdValue());

        // 4. 실행 완료 기록
        schedule.markExecuted();

        // 5. 다음 실행 시간 업데이트 (Application Layer Validator)
        LocalDateTime nextExecution = cronValidator.calculateNextExecution(
            schedule.getCronExpressionValue(),
            LocalDateTime.now()
        );
        schedule.calculateNextExecution(nextExecution);

        // 6. 저장
        saveSchedulePort.save(schedule);
    }
}
