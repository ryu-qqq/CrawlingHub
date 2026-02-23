package com.ryuqq.crawlinghub.adapter.in.rest.schedule.mapper;

import static com.ryuqq.crawlinghub.adapter.in.rest.common.util.DateTimeFormatUtils.format;

import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command.RegisterCrawlSchedulerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command.UpdateCrawlSchedulerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerApiResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RegisterCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.command.UpdateCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import org.springframework.stereotype.Component;

/**
 * CrawlSchedulerCommandApiMapper - CrawlScheduler Command REST API ↔ Application Layer 변환
 *
 * <p>CrawlScheduler Command 요청/응답에 대한 DTO 변환을 담당합니다.
 *
 * <p><strong>변환 방향:</strong>
 *
 * <ul>
 *   <li>API Command Request → Application Command (Controller → Application)
 *   <li>Application Response → API Response (Application → Controller)
 * </ul>
 *
 * <p><strong>CQRS 패턴 적용:</strong>
 *
 * <ul>
 *   <li>Command: RegisterCrawlScheduler, UpdateCrawlScheduler 요청 변환
 * </ul>
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>필드 매핑만 수행 (비즈니스 로직 포함 금지)
 *   <li>API DTO ↔ Application DTO 단순 변환
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerCommandApiMapper {

    /**
     * RegisterCrawlSchedulerApiRequest → RegisterCrawlSchedulerCommand 변환
     *
     * @param request REST API 크롤 스케줄러 등록 요청
     * @return Application Layer 크롤 스케줄러 등록 명령
     */
    public RegisterCrawlSchedulerCommand toCommand(RegisterCrawlSchedulerApiRequest request) {
        return new RegisterCrawlSchedulerCommand(
                request.sellerId(), request.schedulerName(), request.cronExpression());
    }

    /**
     * UpdateCrawlSchedulerApiRequest → UpdateCrawlSchedulerCommand 변환
     *
     * @param crawlSchedulerId 크롤 스케줄러 ID (PathVariable)
     * @param request REST API 크롤 스케줄러 수정 요청
     * @return Application Layer 크롤 스케줄러 수정 명령
     */
    public UpdateCrawlSchedulerCommand toCommand(
            Long crawlSchedulerId, UpdateCrawlSchedulerApiRequest request) {
        return new UpdateCrawlSchedulerCommand(
                crawlSchedulerId,
                request.schedulerName(),
                request.cronExpression(),
                request.active());
    }

    /**
     * CrawlSchedulerResponse → CrawlSchedulerApiResponse 변환
     *
     * @param appResponse Application Layer 크롤 스케줄러 응답
     * @return REST API 크롤 스케줄러 응답
     */
    public CrawlSchedulerApiResponse toApiResponse(CrawlSchedulerResponse appResponse) {
        return new CrawlSchedulerApiResponse(
                appResponse.crawlSchedulerId(),
                appResponse.sellerId(),
                appResponse.schedulerName(),
                appResponse.cronExpression(),
                appResponse.status().name(),
                format(appResponse.createdAt()),
                format(appResponse.updatedAt()));
    }
}
