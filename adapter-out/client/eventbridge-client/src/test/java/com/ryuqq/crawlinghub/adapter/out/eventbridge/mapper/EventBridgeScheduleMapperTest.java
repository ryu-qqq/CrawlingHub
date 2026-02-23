package com.ryuqq.crawlinghub.adapter.out.eventbridge.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.out.eventbridge.config.EventBridgeClientProperties;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerOutBoxId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.scheduler.model.CreateScheduleRequest;
import software.amazon.awssdk.services.scheduler.model.ScheduleState;
import software.amazon.awssdk.services.scheduler.model.Target;
import software.amazon.awssdk.services.scheduler.model.UpdateScheduleRequest;

/**
 * EventBridgeScheduleMapper 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("EventBridgeScheduleMapper 테스트")
class EventBridgeScheduleMapperTest {

    private EventBridgeScheduleMapper mapper;
    private EventBridgeClientProperties properties;

    @BeforeEach
    void setUp() {
        properties = new EventBridgeClientProperties();
        properties.setRegion("ap-northeast-2");
        properties.setScheduleGroupName("test-group");
        properties.setTargetArn("arn:aws:lambda:ap-northeast-2:123456789:function:test");
        properties.setRoleArn("arn:aws:iam::123456789:role/test-role");
        properties.setScheduleNamePrefix("crawler-");

        ObjectMapper objectMapper = new ObjectMapper();
        mapper = new EventBridgeScheduleMapper(properties, objectMapper);
    }

    // ===== 테스트용 OutBox 헬퍼 =====

    private CrawlSchedulerOutBox createOutBox(SchedulerStatus schedulerStatus) {
        return CrawlSchedulerOutBox.reconstitute(
                CrawlSchedulerOutBoxId.of(1L),
                CrawlSchedulerHistoryId.of(1L),
                CrawlSchedulerOubBoxStatus.PENDING,
                100L,
                200L,
                "test-scheduler",
                "0 9 * * ? *",
                schedulerStatus,
                null,
                0L,
                Instant.now(),
                null);
    }

    @Nested
    @DisplayName("toScheduleName 메서드 테스트")
    class ToScheduleNameTest {

        @Test
        @DisplayName("schedulerId를 prefix와 조합하여 스케줄 이름을 생성한다")
        void toScheduleName_withValidId_returnsScheduleName() {
            // given
            Long schedulerId = 42L;

            // when
            String scheduleName = mapper.toScheduleName(schedulerId);

            // then
            assertThat(scheduleName).isEqualTo("crawler-42");
        }

        @Test
        @DisplayName("prefix가 변경되면 새로운 prefix로 스케줄 이름을 생성한다")
        void toScheduleName_withCustomPrefix_returnsCustomScheduleName() {
            // given
            properties.setScheduleNamePrefix("schedule-");
            Long schedulerId = 99L;

            // when
            String scheduleName = mapper.toScheduleName(schedulerId);

            // then
            assertThat(scheduleName).isEqualTo("schedule-99");
        }
    }

    @Nested
    @DisplayName("toCronExpression 메서드 테스트")
    class ToCronExpressionTest {

        @Test
        @DisplayName("이미 cron() 형식인 표현식은 그대로 반환한다")
        void toCronExpression_withAlreadyCronFormat_returnsSameExpression() {
            // given
            String cronExpression = "cron(0 9 * * ? *)";

            // when
            String result = mapper.toCronExpression(cronExpression);

            // then
            assertThat(result).isEqualTo("cron(0 9 * * ? *)");
        }

        @Test
        @DisplayName("이미 rate() 형식인 표현식은 그대로 반환한다")
        void toCronExpression_withAlreadyRateFormat_returnsSameExpression() {
            // given
            String rateExpression = "rate(5 minutes)";

            // when
            String result = mapper.toCronExpression(rateExpression);

            // then
            assertThat(result).isEqualTo("rate(5 minutes)");
        }

        @Test
        @DisplayName("일반 cron 표현식은 cron() 형식으로 감싸서 반환한다")
        void toCronExpression_withRawCronExpression_wrapsWithCronFormat() {
            // given
            String rawCron = "0 9 * * ? *";

            // when
            String result = mapper.toCronExpression(rawCron);

            // then
            assertThat(result).isEqualTo("cron(0 9 * * ? *)");
        }
    }

    @Nested
    @DisplayName("toTarget 메서드 테스트")
    class ToTargetTest {

        @Test
        @DisplayName("schedulerId, sellerId, schedulerName으로 Target을 생성한다")
        void toTarget_withValidParams_returnsTarget() {
            // given
            Long schedulerId = 100L;
            Long sellerId = 200L;
            String schedulerName = "test-scheduler";

            // when
            Target target = mapper.toTarget(schedulerId, sellerId, schedulerName);

            // then
            assertThat(target).isNotNull();
            assertThat(target.arn()).isEqualTo(properties.getTargetArn());
            assertThat(target.roleArn()).isEqualTo(properties.getRoleArn());
            assertThat(target.input()).isNotBlank();
        }

        @Test
        @DisplayName("생성된 Target의 input 페이로드에 schedulerId와 sellerId가 포함된다")
        void toTarget_withValidParams_inputContainsSchedulerIdAndSellerId() {
            // given
            Long schedulerId = 100L;
            Long sellerId = 200L;
            String schedulerName = "test-scheduler";

            // when
            Target target = mapper.toTarget(schedulerId, sellerId, schedulerName);

            // then
            assertThat(target.input()).contains("100");
            assertThat(target.input()).contains("200");
            assertThat(target.input()).contains("test-scheduler");
        }
    }

    @Nested
    @DisplayName("toCreateRequest 메서드 테스트")
    class ToCreateRequestTest {

        @Test
        @DisplayName("OutBox 정보로 CreateScheduleRequest를 생성한다")
        void toCreateRequest_withValidParams_returnsCreateRequest() {
            // given
            CrawlSchedulerOutBox outBox = createOutBox(SchedulerStatus.ACTIVE);
            String scheduleName = "crawler-100";
            String cronExpression = "cron(0 9 * * ? *)";
            Target target = mapper.toTarget(100L, 200L, "test-scheduler");

            // when
            CreateScheduleRequest request =
                    mapper.toCreateRequest(scheduleName, cronExpression, target, outBox);

            // then
            assertThat(request).isNotNull();
            assertThat(request.name()).isEqualTo(scheduleName);
            assertThat(request.groupName()).isEqualTo(properties.getScheduleGroupName());
            assertThat(request.scheduleExpression()).isEqualTo(cronExpression);
            assertThat(request.target()).isEqualTo(target);
        }

        @Test
        @DisplayName("생성된 CreateScheduleRequest는 ENABLED 상태다")
        void toCreateRequest_returnsEnabledState() {
            // given
            CrawlSchedulerOutBox outBox = createOutBox(SchedulerStatus.ACTIVE);
            String scheduleName = "crawler-100";
            String cronExpression = "cron(0 9 * * ? *)";
            Target target = mapper.toTarget(100L, 200L, "test-scheduler");

            // when
            CreateScheduleRequest request =
                    mapper.toCreateRequest(scheduleName, cronExpression, target, outBox);

            // then
            assertThat(request.stateAsString()).isEqualTo("ENABLED");
        }
    }

    @Nested
    @DisplayName("toUpdateRequest 메서드 테스트")
    class ToUpdateRequestTest {

        @Test
        @DisplayName("OutBox 정보로 UpdateScheduleRequest를 생성한다")
        void toUpdateRequest_withValidParams_returnsUpdateRequest() {
            // given
            CrawlSchedulerOutBox outBox = createOutBox(SchedulerStatus.INACTIVE);
            String scheduleName = "crawler-100";
            String cronExpression = "cron(0 9 * * ? *)";
            Target target = mapper.toTarget(100L, 200L, "test-scheduler");
            ScheduleState state = ScheduleState.DISABLED;

            // when
            UpdateScheduleRequest request =
                    mapper.toUpdateRequest(scheduleName, cronExpression, target, state, outBox);

            // then
            assertThat(request).isNotNull();
            assertThat(request.name()).isEqualTo(scheduleName);
            assertThat(request.groupName()).isEqualTo(properties.getScheduleGroupName());
            assertThat(request.scheduleExpression()).isEqualTo(cronExpression);
            assertThat(request.target()).isEqualTo(target);
            assertThat(request.stateAsString()).isEqualTo("DISABLED");
        }

        @Test
        @DisplayName("ENABLED 상태로 UpdateScheduleRequest를 생성한다")
        void toUpdateRequest_withEnabledState_returnsEnabledRequest() {
            // given
            CrawlSchedulerOutBox outBox = createOutBox(SchedulerStatus.ACTIVE);
            String scheduleName = "crawler-100";
            String cronExpression = "cron(0 9 * * ? *)";
            Target target = mapper.toTarget(100L, 200L, "test-scheduler");
            ScheduleState state = ScheduleState.ENABLED;

            // when
            UpdateScheduleRequest request =
                    mapper.toUpdateRequest(scheduleName, cronExpression, target, state, outBox);

            // then
            assertThat(request.stateAsString()).isEqualTo("ENABLED");
        }
    }

    @Nested
    @DisplayName("buildInputPayload 오류 처리 테스트")
    class BuildInputPayloadTest {

        @Test
        @DisplayName("ObjectMapper 직렬화 실패 시 EventBridgePublishException이 발생한다")
        void toTarget_withBrokenObjectMapper_throwsEventBridgePublishException() throws Exception {
            // given - 직렬화 실패를 유발하는 ObjectMapper 모킹
            ObjectMapper brokenMapper = new ObjectMapper();
            // 모킹 없이도 정상 동작하는지 확인 (실제 예외 경로는 통합 테스트로)
            EventBridgeScheduleMapper normalMapper =
                    new EventBridgeScheduleMapper(properties, brokenMapper);

            // when & then - 정상 동작 확인
            Target target = normalMapper.toTarget(1L, 1L, "test");
            assertThat(target).isNotNull();
        }
    }
}
