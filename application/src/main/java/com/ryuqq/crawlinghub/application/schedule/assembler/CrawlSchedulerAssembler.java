package com.ryuqq.crawlinghub.application.schedule.assembler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.CrawlSchedulerBundle;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RegisterCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.query.SearchCrawlSchedulersQuery;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerQueryCriteria;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** CrawlScheduler Aggregate ↔ Response DTO 변환기. */
@Component
public class CrawlSchedulerAssembler {

    private final ClockHolder clockHolder;
    private final ObjectMapper objectMapper;

    public CrawlSchedulerAssembler(ClockHolder clockHolder, ObjectMapper objectMapper) {
        this.clockHolder = clockHolder;
        this.objectMapper = objectMapper;
    }

    /**
     * RegisterCrawlSchedulerCommand → CrawlSchedulerBundle 변환.
     *
     * <p>스케줄러, 히스토리, 아웃박스를 하나의 번들로 묶어 반환
     *
     * @param command 등록 명령
     * @return CrawlSchedulerBundle
     */
    public CrawlSchedulerBundle toBundle(RegisterCrawlSchedulerCommand command) {
        CrawlScheduler scheduler = toCrawlScheduler(command);
        String eventPayload = buildEventPayload(command);
        return CrawlSchedulerBundle.of(scheduler, eventPayload, clockHolder.clock());
    }

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
     * RegisterCrawlSchedulerCommand → CrawlScheduler Aggregate 변환.
     *
     * @param command 등록 명령
     * @return 신규 CrawlScheduler Aggregate
     */
    public CrawlScheduler toCrawlScheduler(RegisterCrawlSchedulerCommand command) {
        return CrawlScheduler.forNew(
                SellerId.of(command.sellerId()),
                SchedulerName.of(command.schedulerName()),
                CronExpression.of(command.cronExpression()),
                clockHolder.clock());
    }

    /**
     * CrawlScheduler Aggregate → CrawlSchedulerResponse 변환.
     *
     * @param crawlScheduler 크롤 스케줄러 Aggregate
     * @return 크롤 스케줄러 응답 DTO
     */
    public CrawlSchedulerResponse toResponse(CrawlScheduler crawlScheduler) {
        return new CrawlSchedulerResponse(
                crawlScheduler.getCrawlSchedulerIdValue(),
                crawlScheduler.getSellerIdValue(),
                crawlScheduler.getSchedulerNameValue(),
                crawlScheduler.getCronExpressionValue(),
                crawlScheduler.getStatus(),
                crawlScheduler.getCreatedAt(),
                crawlScheduler.getUpdatedAt());
    }

    /**
     * CrawlScheduler Aggregate → 이벤트 페이로드 JSON 변환.
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

    /**
     * SearchCrawlSchedulersQuery → CrawlSchedulerQueryCriteria (조회 조건 변환)
     *
     * <p>Application Layer Query DTO를 Domain VO로 변환
     *
     * @param query 스케줄러 검색 Query
     * @return Domain 조회 조건 객체
     */
    public CrawlSchedulerQueryCriteria toCriteria(SearchCrawlSchedulersQuery query) {
        SellerId sellerId = query.sellerId() != null ? SellerId.of(query.sellerId()) : null;

        return new CrawlSchedulerQueryCriteria(
                sellerId, query.status(), query.page(), query.size());
    }

    /**
     * CrawlScheduler 목록 → CrawlSchedulerResponse 목록
     *
     * @param schedulers CrawlScheduler Aggregate 목록
     * @return 응답 목록
     */
    public List<CrawlSchedulerResponse> toResponses(List<CrawlScheduler> schedulers) {
        return schedulers.stream().map(this::toResponse).toList();
    }

    /**
     * CrawlScheduler 목록 + 총 개수 → PageResponse<CrawlSchedulerResponse>
     *
     * <p>페이징 메타데이터를 계산하여 PageResponse로 변환
     *
     * @param schedulers CrawlScheduler 목록
     * @param page 현재 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param totalElements 전체 데이터 개수
     * @return 페이징된 응답
     */
    public PageResponse<CrawlSchedulerResponse> toPageResponse(
            List<CrawlScheduler> schedulers, int page, int size, long totalElements) {
        List<CrawlSchedulerResponse> content = toResponses(schedulers);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean first = (page == 0);
        boolean last = (page >= totalPages - 1);

        return PageResponse.of(content, page, size, totalElements, totalPages, first, last);
    }
}
