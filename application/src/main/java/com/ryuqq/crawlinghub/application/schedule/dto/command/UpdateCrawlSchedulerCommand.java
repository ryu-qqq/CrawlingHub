package com.ryuqq.crawlinghub.application.schedule.dto.command;

/**
 * 크롤 스케줄러 수정 UseCase 입력 DTO.
 *
 * @param crawlSchedulerId 크롤 스케줄러 ID
 * @param schedulerName 스케줄러 이름 (선택적)
 * @param cronExpression 크론 표현식 (선택적, AWS EventBridge 동기화)
 * @param active 활성화 여부 (선택적, AWS EventBridge 동기화)
 */
public record UpdateCrawlSchedulerCommand(
        Long crawlSchedulerId, String schedulerName, String cronExpression, Boolean active) {}
