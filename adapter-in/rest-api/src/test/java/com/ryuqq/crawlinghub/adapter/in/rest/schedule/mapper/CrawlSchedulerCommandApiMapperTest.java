package com.ryuqq.crawlinghub.adapter.in.rest.schedule.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command.RegisterCrawlSchedulerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command.UpdateCrawlSchedulerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerApiResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RegisterCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.command.UpdateCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawlSchedulerCommandApiMapper 단위 테스트
 *
 * <p>CrawlScheduler Command API ↔ Application Layer 변환 로직을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@Tag("mapper")
@DisplayName("CrawlSchedulerCommandApiMapper 단위 테스트")
class CrawlSchedulerCommandApiMapperTest {

    private CrawlSchedulerCommandApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CrawlSchedulerCommandApiMapper();
    }

    @Nested
    @DisplayName("toCommand() - RegisterCrawlSchedulerApiRequest 테스트")
    class ToRegisterCommandTests {

        @Test
        @DisplayName("등록 요청을 명령으로 변환한다")
        void toCommand_Register_ShouldConvertCorrectly() {
            // given
            RegisterCrawlSchedulerApiRequest request =
                    new RegisterCrawlSchedulerApiRequest(1L, "테스트 스케줄러", "0 0 * * * *");

            // when
            RegisterCrawlSchedulerCommand result = mapper.toCommand(request);

            // then
            assertThat(result.sellerId()).isEqualTo(1L);
            assertThat(result.schedulerName()).isEqualTo("테스트 스케줄러");
            assertThat(result.cronExpression()).isEqualTo("0 0 * * * *");
        }
    }

    @Nested
    @DisplayName("toCommand() - UpdateCrawlSchedulerApiRequest 테스트")
    class ToUpdateCommandTests {

        @Test
        @DisplayName("수정 요청을 명령으로 변환한다")
        void toCommand_Update_ShouldConvertCorrectly() {
            // given
            Long crawlSchedulerId = 1L;
            UpdateCrawlSchedulerApiRequest request =
                    new UpdateCrawlSchedulerApiRequest("수정된 스케줄러", "0 30 * * * *", true);

            // when
            UpdateCrawlSchedulerCommand result = mapper.toCommand(crawlSchedulerId, request);

            // then
            assertThat(result.crawlSchedulerId()).isEqualTo(1L);
            assertThat(result.schedulerName()).isEqualTo("수정된 스케줄러");
            assertThat(result.cronExpression()).isEqualTo("0 30 * * * *");
            assertThat(result.active()).isTrue();
        }

        @Test
        @DisplayName("active가 false인 수정 요청을 명령으로 변환한다")
        void toCommand_UpdateWithInactive_ShouldConvertCorrectly() {
            // given
            Long crawlSchedulerId = 2L;
            UpdateCrawlSchedulerApiRequest request =
                    new UpdateCrawlSchedulerApiRequest("비활성 스케줄러", "0 0 * * * *", false);

            // when
            UpdateCrawlSchedulerCommand result = mapper.toCommand(crawlSchedulerId, request);

            // then
            assertThat(result.active()).isFalse();
        }
    }

    @Nested
    @DisplayName("toApiResponse() 테스트")
    class ToApiResponseTests {

        @Test
        @DisplayName("Application 응답을 API 응답으로 변환한다")
        void toApiResponse_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();
            CrawlSchedulerResponse appResponse =
                    new CrawlSchedulerResponse(
                            1L, 100L, "테스트 스케줄러", "0 0 * * * *", SchedulerStatus.ACTIVE, now, now);

            // when
            CrawlSchedulerApiResponse result = mapper.toApiResponse(appResponse);

            // then
            assertThat(result.crawlSchedulerId()).isEqualTo(1L);
            assertThat(result.sellerId()).isEqualTo(100L);
            assertThat(result.schedulerName()).isEqualTo("테스트 스케줄러");
            assertThat(result.cronExpression()).isEqualTo("0 0 * * * *");
            assertThat(result.status()).isEqualTo("ACTIVE");
            assertThat(result.createdAt()).isNotNull();
            assertThat(result.updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("null 시각 필드를 처리한다")
        void toApiResponse_WithNullInstants_ShouldHandleNullValues() {
            // given
            CrawlSchedulerResponse appResponse =
                    new CrawlSchedulerResponse(
                            1L, 100L, "테스트", "0 0 * * * *", SchedulerStatus.INACTIVE, null, null);

            // when
            CrawlSchedulerApiResponse result = mapper.toApiResponse(appResponse);

            // then
            assertThat(result.createdAt()).isNull();
            assertThat(result.updatedAt()).isNull();
        }
    }
}
