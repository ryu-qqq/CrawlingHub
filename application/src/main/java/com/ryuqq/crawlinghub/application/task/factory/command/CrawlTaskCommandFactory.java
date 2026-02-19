package com.ryuqq.crawlinghub.application.task.factory.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.task.dto.bundle.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.command.TriggerCrawlTaskCommand;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlEndpoint;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * CrawlTask CommandFactory
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>Command → Domain 변환
 *   <li>Bundle 생성
 *   <li>Outbox 페이로드 생성
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
public class CrawlTaskCommandFactory {

    private final ObjectMapper objectMapper;
    private final TimeProvider timeProvider;

    public CrawlTaskCommandFactory(ObjectMapper objectMapper, TimeProvider timeProvider) {
        this.objectMapper = objectMapper;
        this.timeProvider = timeProvider;
    }

    /**
     * TriggerCrawlTaskCommand + CrawlScheduler + Seller → CrawlTaskBundle 변환
     *
     * <p><strong>타입-엔드포인트 조합 설명</strong>:
     *
     * <ul>
     *   <li>SEARCH 타입 + forSearchItems: 스케줄러에서 최초 크롤링 트리거 시 사용
     *   <li>셀러명을 keyword로 검색하여 상품 목록을 조회
     *   <li>nid, uid 쿠키는 워커에서 CrawlContext.buildSearchEndpoint()로 추가됨
     * </ul>
     *
     * @param command 트리거 명령
     * @param scheduler 검증된 스케줄러
     * @param seller 셀러 (mustItSellerName 조회용)
     * @return CrawlTask 번들
     */
    public CrawlTaskBundle createBundle(
            TriggerCrawlTaskCommand command, CrawlScheduler scheduler, Seller seller) {
        CrawlEndpoint endpoint = CrawlEndpoint.forSearchItems(seller.getMustItSellerNameValue(), 1);
        CrawlTask crawlTask =
                CrawlTask.forNew(
                        scheduler.getCrawlSchedulerId(),
                        scheduler.getSellerId(),
                        CrawlTaskType.SEARCH,
                        endpoint,
                        timeProvider.now());

        String outboxPayload = toOutboxPayload(crawlTask, scheduler);
        return CrawlTaskBundle.of(crawlTask, outboxPayload);
    }

    /**
     * CreateCrawlTaskCommand → CrawlTaskBundle 변환
     *
     * <p>크롤러가 동적으로 후속 태스크를 생성할 때 사용
     *
     * @param command 동적 생성 명령
     * @return CrawlTask 번들
     */
    public CrawlTaskBundle createBundle(CreateCrawlTaskCommand command) {
        CrawlEndpoint endpoint = createEndpoint(command);
        CrawlTask crawlTask =
                CrawlTask.forNew(
                        CrawlSchedulerId.of(command.crawlSchedulerId()),
                        SellerId.of(command.sellerId()),
                        command.taskType(),
                        endpoint,
                        timeProvider.now());

        String outboxPayload = toOutboxPayload(crawlTask);
        return CrawlTaskBundle.of(crawlTask, outboxPayload);
    }

    /**
     * CreateCrawlTaskCommand에서 CrawlEndpoint 생성
     *
     * @param command 생성 명령
     * @return CrawlEndpoint
     */
    private CrawlEndpoint createEndpoint(CreateCrawlTaskCommand command) {
        return switch (command.taskType()) {
            case META -> CrawlEndpoint.forMeta(command.mustItSellerName());
            case MINI_SHOP -> CrawlEndpoint.forMiniShopList(command.mustItSellerName(), 1, 100);
            case DETAIL -> CrawlEndpoint.forProductDetail(command.targetId());
            case OPTION -> CrawlEndpoint.forProductOption(command.targetId());
            case SEARCH -> CrawlEndpoint.forSearchApi(command.endpoint());
        };
    }

    /**
     * CrawlTask → Outbox 페이로드 (JSON) 변환
     *
     * @param crawlTask CrawlTask
     * @return JSON 문자열
     */
    public String toOutboxPayload(CrawlTask crawlTask) {
        try {
            Map<String, Object> payload =
                    Map.of(
                            "schedulerId", crawlTask.getCrawlSchedulerId().value(),
                            "sellerId", crawlTask.getSellerId().value(),
                            "taskType", crawlTask.getTaskType().name(),
                            "endpoint", crawlTask.getEndpoint().toFullUrl());
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Outbox 페이로드 생성 실패", e);
        }
    }

    /**
     * CrawlTask + CrawlScheduler → Outbox 페이로드 (JSON) 변환
     *
     * @param crawlTask CrawlTask
     * @param scheduler CrawlScheduler
     * @return JSON 문자열
     */
    public String toOutboxPayload(CrawlTask crawlTask, CrawlScheduler scheduler) {
        try {
            Map<String, Object> payload =
                    Map.of(
                            "schedulerId", scheduler.getCrawlSchedulerId().value(),
                            "sellerId", scheduler.getSellerIdValue(),
                            "taskType", crawlTask.getTaskType().name(),
                            "endpoint", crawlTask.getEndpoint().toFullUrl());
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Outbox 페이로드 생성 실패", e);
        }
    }
}
