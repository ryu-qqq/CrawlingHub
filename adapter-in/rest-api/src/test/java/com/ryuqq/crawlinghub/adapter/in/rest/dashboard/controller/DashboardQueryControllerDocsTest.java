package com.ryuqq.crawlinghub.adapter.in.rest.dashboard.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsSecuritySnippets;
import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse;
import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse.DailySuccessRate;
import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse.FailedTaskSummary;
import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse.OutboxStats;
import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse.ScheduleStats;
import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse.TodayTaskStats;
import com.ryuqq.crawlinghub.application.dashboard.port.in.query.GetDashboardStatsUseCase;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * DashboardQueryController REST Docs 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(DashboardQueryController.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DisplayName("DashboardQueryController REST Docs")
class DashboardQueryControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private GetDashboardStatsUseCase getDashboardStatsUseCase;

    @Test
    @DisplayName("GET /api/v1/crawling/dashboard/stats - 대시보드 통계 조회 API 문서")
    void getDashboardStats() throws Exception {
        // given
        TodayTaskStats todayTaskStats = TodayTaskStats.of(150L, 120L, 15L, 10L, 5L);

        List<DailySuccessRate> weeklySuccessRates = createWeeklySuccessRates();

        ScheduleStats scheduleStats = new ScheduleStats(50L, 45L, 5L);

        OutboxStats outboxStats = new OutboxStats(10L, 500L, 3L);

        List<FailedTaskSummary> recentFailedTasks =
                List.of(
                        new FailedTaskSummary(
                                1001L,
                                101L,
                                "PRODUCT_CRAWL",
                                "FAILED",
                                Instant.parse("2025-12-28T09:30:00Z")),
                        new FailedTaskSummary(
                                1002L,
                                102L,
                                "CATEGORY_CRAWL",
                                "TIMEOUT",
                                Instant.parse("2025-12-28T09:15:00Z")));

        DashboardStatsResponse response =
                new DashboardStatsResponse(
                        todayTaskStats,
                        weeklySuccessRates,
                        scheduleStats,
                        outboxStats,
                        recentFailedTasks);

        given(getDashboardStatsUseCase.execute()).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/crawling/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.todayTaskStats.total").value(150))
                .andExpect(jsonPath("$.data.todayTaskStats.success").value(120))
                .andExpect(jsonPath("$.data.todayTaskStats.failed").value(15))
                .andExpect(jsonPath("$.data.todayTaskStats.inProgress").value(10))
                .andExpect(jsonPath("$.data.todayTaskStats.waiting").value(5))
                .andExpect(jsonPath("$.data.weeklySuccessRates").isArray())
                .andExpect(jsonPath("$.data.weeklySuccessRates.length()").value(7))
                .andExpect(jsonPath("$.data.scheduleStats.total").value(50))
                .andExpect(jsonPath("$.data.scheduleStats.active").value(45))
                .andExpect(jsonPath("$.data.scheduleStats.inactive").value(5))
                .andExpect(jsonPath("$.data.outboxStats.pending").value(10))
                .andExpect(jsonPath("$.data.outboxStats.sent").value(500))
                .andExpect(jsonPath("$.data.outboxStats.failed").value(3))
                .andExpect(jsonPath("$.data.recentFailedTasks").isArray())
                .andExpect(jsonPath("$.data.recentFailedTasks.length()").value(2))
                .andDo(
                        document(
                                "dashboard-query/stats",
                                RestDocsSecuritySnippets.authorization("dashboard:read"),
                                responseFields(
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        // TodayTaskStats
                                        fieldWithPath("data.todayTaskStats")
                                                .type(JsonFieldType.OBJECT)
                                                .description("오늘 태스크 통계"),
                                        fieldWithPath("data.todayTaskStats.total")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 태스크 수"),
                                        fieldWithPath("data.todayTaskStats.success")
                                                .type(JsonFieldType.NUMBER)
                                                .description("성공한 태스크 수"),
                                        fieldWithPath("data.todayTaskStats.failed")
                                                .type(JsonFieldType.NUMBER)
                                                .description("실패한 태스크 수"),
                                        fieldWithPath("data.todayTaskStats.inProgress")
                                                .type(JsonFieldType.NUMBER)
                                                .description("진행 중인 태스크 수"),
                                        fieldWithPath("data.todayTaskStats.waiting")
                                                .type(JsonFieldType.NUMBER)
                                                .description("대기 중인 태스크 수"),
                                        fieldWithPath("data.todayTaskStats.successRate")
                                                .type(JsonFieldType.NUMBER)
                                                .description("성공률 (0.0 ~ 1.0)"),
                                        // WeeklySuccessRates
                                        fieldWithPath("data.weeklySuccessRates")
                                                .type(JsonFieldType.ARRAY)
                                                .description("최근 7일 성공률"),
                                        fieldWithPath("data.weeklySuccessRates[].date")
                                                .type(JsonFieldType.STRING)
                                                .description("날짜 (YYYY-MM-DD)"),
                                        fieldWithPath("data.weeklySuccessRates[].total")
                                                .type(JsonFieldType.NUMBER)
                                                .description("해당일 전체 태스크 수"),
                                        fieldWithPath("data.weeklySuccessRates[].success")
                                                .type(JsonFieldType.NUMBER)
                                                .description("해당일 성공 태스크 수"),
                                        fieldWithPath("data.weeklySuccessRates[].successRate")
                                                .type(JsonFieldType.NUMBER)
                                                .description("해당일 성공률 (0.0 ~ 1.0)"),
                                        // ScheduleStats
                                        fieldWithPath("data.scheduleStats")
                                                .type(JsonFieldType.OBJECT)
                                                .description("스케줄 통계"),
                                        fieldWithPath("data.scheduleStats.total")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 스케줄 수"),
                                        fieldWithPath("data.scheduleStats.active")
                                                .type(JsonFieldType.NUMBER)
                                                .description("활성 스케줄 수"),
                                        fieldWithPath("data.scheduleStats.inactive")
                                                .type(JsonFieldType.NUMBER)
                                                .description("비활성 스케줄 수"),
                                        // OutboxStats
                                        fieldWithPath("data.outboxStats")
                                                .type(JsonFieldType.OBJECT)
                                                .description("Outbox 통계"),
                                        fieldWithPath("data.outboxStats.pending")
                                                .type(JsonFieldType.NUMBER)
                                                .description("대기 중인 Outbox 수"),
                                        fieldWithPath("data.outboxStats.sent")
                                                .type(JsonFieldType.NUMBER)
                                                .description("발행 완료 Outbox 수"),
                                        fieldWithPath("data.outboxStats.failed")
                                                .type(JsonFieldType.NUMBER)
                                                .description("실패한 Outbox 수"),
                                        // RecentFailedTasks
                                        fieldWithPath("data.recentFailedTasks")
                                                .type(JsonFieldType.ARRAY)
                                                .description("최근 실패 태스크 목록"),
                                        fieldWithPath("data.recentFailedTasks[].taskId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("태스크 ID"),
                                        fieldWithPath("data.recentFailedTasks[].schedulerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("스케줄러 ID"),
                                        fieldWithPath("data.recentFailedTasks[].taskType")
                                                .type(JsonFieldType.STRING)
                                                .description("태스크 유형"),
                                        fieldWithPath("data.recentFailedTasks[].status")
                                                .type(JsonFieldType.STRING)
                                                .description("태스크 상태"),
                                        fieldWithPath("data.recentFailedTasks[].failedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("실패 시각 (ISO-8601)"),
                                        // Common
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }

    @Test
    @DisplayName("GET /api/v1/crawling/dashboard/stats - 데이터가 없는 경우 API 문서")
    void getDashboardStats_noData() throws Exception {
        // given
        TodayTaskStats todayTaskStats = TodayTaskStats.of(0L, 0L, 0L, 0L, 0L);

        List<DailySuccessRate> weeklySuccessRates = createEmptyWeeklySuccessRates();

        ScheduleStats scheduleStats = new ScheduleStats(0L, 0L, 0L);

        OutboxStats outboxStats = new OutboxStats(0L, 0L, 0L);

        DashboardStatsResponse response =
                new DashboardStatsResponse(
                        todayTaskStats,
                        weeklySuccessRates,
                        scheduleStats,
                        outboxStats,
                        Collections.emptyList());

        given(getDashboardStatsUseCase.execute()).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/crawling/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.todayTaskStats.total").value(0))
                .andExpect(jsonPath("$.data.todayTaskStats.successRate").value(0.0))
                .andExpect(jsonPath("$.data.weeklySuccessRates").isArray())
                .andExpect(jsonPath("$.data.weeklySuccessRates.length()").value(7))
                .andExpect(jsonPath("$.data.scheduleStats.total").value(0))
                .andExpect(jsonPath("$.data.outboxStats.pending").value(0))
                .andExpect(jsonPath("$.data.recentFailedTasks").isEmpty())
                .andDo(
                        document(
                                "dashboard-query/stats-no-data",
                                RestDocsSecuritySnippets.authorization("dashboard:read"),
                                responseFields(
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.todayTaskStats")
                                                .type(JsonFieldType.OBJECT)
                                                .description("오늘 태스크 통계 (모두 0)"),
                                        fieldWithPath("data.todayTaskStats.total")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 태스크 수"),
                                        fieldWithPath("data.todayTaskStats.success")
                                                .type(JsonFieldType.NUMBER)
                                                .description("성공한 태스크 수"),
                                        fieldWithPath("data.todayTaskStats.failed")
                                                .type(JsonFieldType.NUMBER)
                                                .description("실패한 태스크 수"),
                                        fieldWithPath("data.todayTaskStats.inProgress")
                                                .type(JsonFieldType.NUMBER)
                                                .description("진행 중인 태스크 수"),
                                        fieldWithPath("data.todayTaskStats.waiting")
                                                .type(JsonFieldType.NUMBER)
                                                .description("대기 중인 태스크 수"),
                                        fieldWithPath("data.todayTaskStats.successRate")
                                                .type(JsonFieldType.NUMBER)
                                                .description("성공률 (데이터 없을 시 0.0)"),
                                        fieldWithPath("data.weeklySuccessRates")
                                                .type(JsonFieldType.ARRAY)
                                                .description("최근 7일 성공률 (데이터 없어도 7일치 반환)"),
                                        fieldWithPath("data.weeklySuccessRates[].date")
                                                .type(JsonFieldType.STRING)
                                                .description("날짜 (YYYY-MM-DD)"),
                                        fieldWithPath("data.weeklySuccessRates[].total")
                                                .type(JsonFieldType.NUMBER)
                                                .description("해당일 전체 태스크 수"),
                                        fieldWithPath("data.weeklySuccessRates[].success")
                                                .type(JsonFieldType.NUMBER)
                                                .description("해당일 성공 태스크 수"),
                                        fieldWithPath("data.weeklySuccessRates[].successRate")
                                                .type(JsonFieldType.NUMBER)
                                                .description("해당일 성공률"),
                                        fieldWithPath("data.scheduleStats")
                                                .type(JsonFieldType.OBJECT)
                                                .description("스케줄 통계 (모두 0)"),
                                        fieldWithPath("data.scheduleStats.total")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 스케줄 수"),
                                        fieldWithPath("data.scheduleStats.active")
                                                .type(JsonFieldType.NUMBER)
                                                .description("활성 스케줄 수"),
                                        fieldWithPath("data.scheduleStats.inactive")
                                                .type(JsonFieldType.NUMBER)
                                                .description("비활성 스케줄 수"),
                                        fieldWithPath("data.outboxStats")
                                                .type(JsonFieldType.OBJECT)
                                                .description("Outbox 통계 (모두 0)"),
                                        fieldWithPath("data.outboxStats.pending")
                                                .type(JsonFieldType.NUMBER)
                                                .description("대기 중인 Outbox 수"),
                                        fieldWithPath("data.outboxStats.sent")
                                                .type(JsonFieldType.NUMBER)
                                                .description("발행 완료 Outbox 수"),
                                        fieldWithPath("data.outboxStats.failed")
                                                .type(JsonFieldType.NUMBER)
                                                .description("실패한 Outbox 수"),
                                        fieldWithPath("data.recentFailedTasks")
                                                .type(JsonFieldType.ARRAY)
                                                .description("최근 실패 태스크 목록 (빈 배열)"),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }

    private List<DailySuccessRate> createWeeklySuccessRates() {
        List<DailySuccessRate> rates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long total = 100 + (6 - i) * 10;
            long success = (long) (total * (0.75 + (6 - i) * 0.03));
            rates.add(DailySuccessRate.of(date.format(formatter), total, success));
        }
        return rates;
    }

    private List<DailySuccessRate> createEmptyWeeklySuccessRates() {
        List<DailySuccessRate> rates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            rates.add(DailySuccessRate.of(date.format(formatter), 0L, 0L));
        }
        return rates;
    }
}
