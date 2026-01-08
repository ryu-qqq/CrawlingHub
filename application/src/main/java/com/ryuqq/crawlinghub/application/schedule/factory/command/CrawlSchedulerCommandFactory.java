package com.ryuqq.crawlinghub.application.schedule.factory.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.schedule.dto.CrawlSchedulerBundle;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RegisterCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * CrawlScheduler CommandFactory
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>Command → Domain 변환
 *   <li>Bundle 생성
 *   <li>이벤트 페이로드 생성
 * </ul>
 *
 * <p><strong>금지</strong>:
 *
 * <ul>
 *   <li>@Transactional 금지 (변환만, 트랜잭션 불필요)
 *   <li>Port 의존 금지
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerCommandFactory {

    private final ClockHolder clockHolder;
    private final ObjectMapper objectMapper;

    public CrawlSchedulerCommandFactory(ClockHolder clockHolder, ObjectMapper objectMapper) {
        this.clockHolder = clockHolder;
        this.objectMapper = objectMapper;
    }

    /**
     * RegisterCrawlSchedulerCommand → CrawlSchedulerBundle 변환
     *
     * @param command 등록 명령
     * @return CrawlSchedulerBundle
     */
    public CrawlSchedulerBundle createBundle(RegisterCrawlSchedulerCommand command) {
        CrawlScheduler scheduler = createScheduler(command);
        String eventPayload = buildEventPayload(command);
        return CrawlSchedulerBundle.of(scheduler, eventPayload);
    }

    /**
     * RegisterCrawlSchedulerCommand → CrawlScheduler Aggregate 변환
     *
     * @param command 등록 명령
     * @return 신규 CrawlScheduler Aggregate
     */
    public CrawlScheduler createScheduler(RegisterCrawlSchedulerCommand command) {
        return CrawlScheduler.forNew(
                SellerId.of(command.sellerId()),
                SchedulerName.of(command.schedulerName()),
                CronExpression.of(command.cronExpression()),
                clockHolder.getClock());
    }

    /**
     * RegisterCrawlSchedulerCommand → 이벤트 페이로드 생성
     *
     * @param command 등록 명령
     * @return JSON 문자열
     */
    private String buildEventPayload(RegisterCrawlSchedulerCommand command) {
        try {
            Map<String, Object> payload =
                    Map.of(
                            "sellerId", command.sellerId(),
                            "schedulerName", command.schedulerName(),
                            "cronExpression", command.cronExpression(),
                            "status", "ACTIVE");
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("이벤트 페이로드 생성 실패", e);
        }
    }

    /**
     * CrawlScheduler Aggregate → 이벤트 페이로드 JSON 변환
     *
     * @param crawlScheduler 크롤 스케줄러 Aggregate
     * @return 이벤트 페이로드 JSON 문자열
     */
    public String toEventPayload(CrawlScheduler crawlScheduler) {
        try {
            Map<String, Object> payload =
                    Map.of(
                            "schedulerId", crawlScheduler.getCrawlSchedulerIdValue(),
                            "sellerId", crawlScheduler.getSellerIdValue(),
                            "schedulerName", crawlScheduler.getSchedulerNameValue(),
                            "cronExpression", crawlScheduler.getCronExpressionValue(),
                            "status", crawlScheduler.getStatus().name());
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("이벤트 페이로드 생성 실패", e);
        }
    }
}

