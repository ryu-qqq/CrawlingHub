package com.ryuqq.crawlinghub.application.schedule.dto.command;

/**
 * 크롤 스케줄러 등록 UseCase 입력 DTO.
 *
 * @param sellerId 셀러 ID
 * @param schedulerName 스케줄러 이름 (셀러별 중복 불가)
 * @param cronExpression 크론 표현식 (AWS EventBridge 형식)
 */
public record RegisterCrawlSchedulerCommand(
        Long sellerId, String schedulerName, String cronExpression) {}
