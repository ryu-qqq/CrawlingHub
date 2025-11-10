package com.ryuqq.crawlinghub.application.schedule.service;

import com.ryuqq.crawlinghub.application.schedule.dto.command.TriggerScheduleCommand;
import com.ryuqq.crawlinghub.application.schedule.port.in.TriggerScheduleUseCase;
import com.ryuqq.crawlinghub.application.schedule.port.out.LoadSchedulePort;
import com.ryuqq.crawlinghub.application.schedule.port.out.SaveSchedulePort;
import com.ryuqq.crawlinghub.application.schedule.validator.CronExpressionValidator;
import com.ryuqq.crawlinghub.application.task.dto.command.InitiateCrawlingCommand;
import com.ryuqq.crawlinghub.application.task.port.in.InitiateCrawlingUseCase;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final InitiateCrawlingUseCase initiateCrawlingUseCase;

    public TriggerScheduleService(
        LoadSchedulePort loadSchedulePort,
        SaveSchedulePort saveSchedulePort,
        CronExpressionValidator cronValidator,
        InitiateCrawlingUseCase initiateCrawlingUseCase
    ) {
        this.loadSchedulePort = loadSchedulePort;
        this.saveSchedulePort = saveSchedulePort;
        this.cronValidator = cronValidator;
        this.initiateCrawlingUseCase = initiateCrawlingUseCase;
    }

    /**
     * 스케줄 트리거 (크롤링 시작)
     * <p>
     * EventBridge에서 sellerId를 받아 해당 셀러의 활성 스케줄을 실행합니다.
     * </p>
     *
     * <p>실행 순서:
     * 1. sellerId로 활성 스케줄 조회
     * 2. 실행 가능 여부 확인 (실행 시간 도래)
     * 3. CrawlTask 생성 및 Outbox 저장
     * 4. 실행 완료 기록
     * 5. 다음 실행 시간 업데이트
     *
     * @param command 트리거할 셀러 ID
     * @throws IllegalArgumentException 활성 스케줄을 찾을 수 없는 경우
     * @throws IllegalStateException 실행 시간이 도래하지 않은 경우
     */
    @Override
    @Transactional
    public void execute(TriggerScheduleCommand command) {
        // sellerId를 MustitSellerId로 변환 (Domain Value Object)
        MustitSellerId sellerId = MustitSellerId.of(command.sellerId());

        // 1. sellerId로 활성 스케줄 조회
        CrawlSchedule schedule = loadSchedulePort.findActiveBySellerId(sellerId)
            .orElseThrow(() -> new IllegalArgumentException(
                "활성 스케줄을 찾을 수 없습니다. sellerId: " + command.sellerId()
            ));

        // 2. 실행 시간 도래 여부 확인 (활성 상태는 이미 확인됨)
        if (!schedule.isTimeToExecute()) {
            throw new IllegalStateException(
                "실행 시간이 아직 도래하지 않았습니다. 다음 실행 시간: " + schedule.getNextExecutionTime()
            );
        }

        // 3. CrawlTask 생성 및 Outbox 저장
        InitiateCrawlingCommand crawlingCommand =
            new InitiateCrawlingCommand(schedule.getSellerIdValue());
        initiateCrawlingUseCase.execute(crawlingCommand);

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
