package com.ryuqq.crawlinghub.adapter.in.rest.schedule.controller;

import com.ryuqq.crawlinghub.application.schedule.dto.command.TriggerScheduleCommand;
import com.ryuqq.crawlinghub.application.schedule.port.in.TriggerScheduleUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TriggerScheduleController - 스케줄 트리거 REST API 컨트롤러
 *
 * <p>EventBridge에서 주기적으로 호출하는 엔드포인트를 제공합니다.
 * 비즈니스 로직은 포함하지 않으며, UseCase 호출만 담당합니다.
 *
 * <p>⚠️ Transaction 경계:
 * <ul>
 *   <li>즉시 202 Accepted 반환 (비동기 처리)</li>
 *   <li>실제 크롤링 태스크 생성은 UseCase 내부에서 처리</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@RestController
@RequestMapping("/api/v1/schedules")
@Tag(name = "Schedule API", description = "스케줄 관리 API")
public class TriggerScheduleController {

    private final TriggerScheduleUseCase triggerScheduleUseCase;

    /**
     * 생성자 주입 (Constructor Injection)
     *
     * @param triggerScheduleUseCase 스케줄 트리거 UseCase
     */
    public TriggerScheduleController(TriggerScheduleUseCase triggerScheduleUseCase) {
        this.triggerScheduleUseCase = triggerScheduleUseCase;
    }

    /**
     * 스케줄 트리거 API
     *
     * <p>EventBridge에서 주기적으로 호출하여 크롤링을 시작합니다.
     * <p>
     * POST /api/v1/schedules/{scheduleId}/trigger
     * </p>
     *
     * <p>실행 흐름:
     * <ol>
     *   <li>스케줄 조회 및 검증</li>
     *   <li>크롤링 태스크 생성 및 Outbox 저장</li>
     *   <li>다음 실행 시간 업데이트</li>
     * </ol>
     *
     * @param scheduleId 트리거할 스케줄 ID (Path Variable)
     * @return 202 Accepted (비동기 처리)
     */
    @PostMapping("/{scheduleId}/trigger")
    @Operation(summary = "스케줄 트리거", description = "EventBridge에서 호출하는 스케줄 트리거 API")
    public ResponseEntity<Void> triggerSchedule(@PathVariable("scheduleId") Long scheduleId) {
        // 1. API Request → Application Command 변환
        TriggerScheduleCommand command = new TriggerScheduleCommand(scheduleId);

        // 2. UseCase 실행 (트랜잭션 내부에서 처리)
        triggerScheduleUseCase.execute(command);

        // 3. 202 Accepted 반환 (비동기 처리)
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}

