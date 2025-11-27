package com.ryuqq.crawlinghub.application.task.assembler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.task.dto.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.command.TriggerCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.query.ListCrawlTasksQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskDetailResponse;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlEndpoint;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * CrawlTask Aggregate ↔ Response DTO 변환기
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskAssembler {

    private final ObjectMapper objectMapper;

    public CrawlTaskAssembler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * TriggerCrawlTaskCommand + CrawlScheduler → CrawlTaskBundle 변환
     *
     * @param command 트리거 명령
     * @param scheduler 검증된 스케줄러
     * @return CrawlTask 번들
     */
    public CrawlTaskBundle toBundle(TriggerCrawlTaskCommand command, CrawlScheduler scheduler) {
        // 1. CrawlTask 생성
        CrawlEndpoint endpoint = CrawlEndpoint.forMeta(scheduler.getSellerIdValue());
        CrawlTask crawlTask =
                CrawlTask.forNew(
                        scheduler.getCrawlSchedulerId(),
                        scheduler.getSellerId(),
                        CrawlTaskType.META,
                        endpoint);

        // 2. Outbox 페이로드 생성
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
    public CrawlTaskBundle toBundle(CreateCrawlTaskCommand command) {
        // 1. CrawlEndpoint 생성 (TaskType에 따라 분기)
        CrawlEndpoint endpoint = createEndpoint(command);

        // 2. CrawlTask 생성
        CrawlTask crawlTask =
                CrawlTask.forNew(
                        CrawlSchedulerId.of(command.crawlSchedulerId()),
                        SellerId.of(command.sellerId()),
                        command.taskType(),
                        endpoint);

        // 3. Outbox 페이로드 생성
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
            case META -> CrawlEndpoint.forMeta(command.sellerId());
            case MINI_SHOP -> CrawlEndpoint.forMiniShopList(command.sellerId(), 1, 100);
            case DETAIL -> CrawlEndpoint.forProductDetail(command.targetId());
            case OPTION -> CrawlEndpoint.forProductOption(command.targetId());
        };
    }

    /**
     * CrawlTask → Outbox 페이로드 (JSON) 변환 (스케줄러 없이)
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

    /**
     * ListCrawlTasksQuery → CrawlTaskCriteria 변환
     *
     * @param query 목록 조회 쿼리
     * @return Domain 조회 조건 객체
     */
    public CrawlTaskCriteria toCriteria(ListCrawlTasksQuery query) {
        return new CrawlTaskCriteria(
                CrawlSchedulerId.of(query.crawlSchedulerId()),
                query.status(),
                query.page(),
                query.size());
    }

    /**
     * CrawlTask Aggregate → CrawlTaskResponse 변환
     *
     * @param crawlTask CrawlTask Aggregate
     * @return CrawlTask 응답 DTO
     */
    public CrawlTaskResponse toResponse(CrawlTask crawlTask) {
        return new CrawlTaskResponse(
                crawlTask.getId().value(),
                crawlTask.getCrawlSchedulerId().value(),
                crawlTask.getSellerId().value(),
                crawlTask.getEndpoint().toFullUrl(),
                crawlTask.getStatus(),
                crawlTask.getTaskType(),
                crawlTask.getRetryCount().value(),
                crawlTask.getCreatedAt());
    }

    /**
     * CrawlTask Aggregate → CrawlTaskDetailResponse 변환
     *
     * @param crawlTask CrawlTask Aggregate
     * @return CrawlTask 상세 응답 DTO
     */
    public CrawlTaskDetailResponse toDetailResponse(CrawlTask crawlTask) {
        CrawlEndpoint endpoint = crawlTask.getEndpoint();
        return new CrawlTaskDetailResponse(
                crawlTask.getId().value(),
                crawlTask.getCrawlSchedulerId().value(),
                crawlTask.getSellerId().value(),
                crawlTask.getStatus(),
                crawlTask.getTaskType(),
                crawlTask.getRetryCount().value(),
                endpoint.baseUrl(),
                endpoint.path(),
                endpoint.queryParams(),
                endpoint.toFullUrl(),
                crawlTask.getCreatedAt(),
                crawlTask.getUpdatedAt());
    }

    /**
     * CrawlTask 목록 → CrawlTaskResponse 목록
     *
     * @param crawlTasks CrawlTask Aggregate 목록
     * @return 응답 목록
     */
    public List<CrawlTaskResponse> toResponses(List<CrawlTask> crawlTasks) {
        return crawlTasks.stream().map(this::toResponse).toList();
    }

    /**
     * CrawlTask 목록 + 총 개수 → PageResponse 변환
     *
     * @param crawlTasks CrawlTask 목록
     * @param page 현재 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param totalElements 전체 데이터 개수
     * @return 페이징된 응답
     */
    public PageResponse<CrawlTaskResponse> toPageResponse(
            List<CrawlTask> crawlTasks, int page, int size, long totalElements) {
        List<CrawlTaskResponse> content = toResponses(crawlTasks);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean first = (page == 0);
        boolean last = (page >= totalPages - 1) || totalElements == 0;

        return PageResponse.of(content, page, size, totalElements, totalPages, first, last);
    }
}
