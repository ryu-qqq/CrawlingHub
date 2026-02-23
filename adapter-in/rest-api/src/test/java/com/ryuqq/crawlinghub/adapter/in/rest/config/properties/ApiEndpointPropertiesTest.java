package com.ryuqq.crawlinghub.adapter.in.rest.config.properties;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * ApiEndpointProperties 단위 테스트
 *
 * <p>API 엔드포인트 경로 설정의 기본값 및 Setter/Getter를 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@DisplayName("ApiEndpointProperties 단위 테스트")
class ApiEndpointPropertiesTest {

    @Nested
    @DisplayName("기본값 검증")
    class DefaultValuesTest {

        @Test
        @DisplayName("기본 baseV1 경로는 /api/v1이다")
        void shouldHaveDefaultBaseV1() {
            // Given
            ApiEndpointProperties properties = new ApiEndpointProperties();

            // Then
            assertThat(properties.getBaseV1()).isEqualTo("/api/v1");
        }

        @Test
        @DisplayName("기본 seller 엔드포인트가 설정된다")
        void shouldHaveDefaultSellerEndpoints() {
            // Given
            ApiEndpointProperties properties = new ApiEndpointProperties();

            // Then
            assertThat(properties.getSeller()).isNotNull();
            assertThat(properties.getSeller().getBase()).isEqualTo("/sellers");
            assertThat(properties.getSeller().getById()).isEqualTo("/{id}");
            assertThat(properties.getSeller().getStatus()).isEqualTo("/{id}/status");
        }

        @Test
        @DisplayName("기본 schedule 엔드포인트가 설정된다")
        void shouldHaveDefaultScheduleEndpoints() {
            // Given
            ApiEndpointProperties properties = new ApiEndpointProperties();

            // Then
            assertThat(properties.getSchedule()).isNotNull();
            assertThat(properties.getSchedule().getBase()).isEqualTo("/schedules");
            assertThat(properties.getSchedule().getById()).isEqualTo("/{id}");
        }

        @Test
        @DisplayName("기본 task 엔드포인트가 설정된다")
        void shouldHaveDefaultTaskEndpoints() {
            // Given
            ApiEndpointProperties properties = new ApiEndpointProperties();

            // Then
            assertThat(properties.getTask()).isNotNull();
            assertThat(properties.getTask().getBase()).isEqualTo("/tasks");
            assertThat(properties.getTask().getById()).isEqualTo("/{id}");
        }

        @Test
        @DisplayName("기본 userAgent 엔드포인트가 설정된다")
        void shouldHaveDefaultUserAgentEndpoints() {
            // Given
            ApiEndpointProperties properties = new ApiEndpointProperties();

            // Then
            assertThat(properties.getUserAgent()).isNotNull();
            assertThat(properties.getUserAgent().getBase()).isEqualTo("/user-agents");
            assertThat(properties.getUserAgent().getPoolStatus()).isEqualTo("/pool-status");
            assertThat(properties.getUserAgent().getRecover()).isEqualTo("/recover");
        }

        @Test
        @DisplayName("기본 execution 엔드포인트가 설정된다")
        void shouldHaveDefaultExecutionEndpoints() {
            // Given
            ApiEndpointProperties properties = new ApiEndpointProperties();

            // Then
            assertThat(properties.getExecution()).isNotNull();
            assertThat(properties.getExecution().getBase()).isEqualTo("/executions");
            assertThat(properties.getExecution().getById()).isEqualTo("/{id}");
        }
    }

    @Nested
    @DisplayName("SellerEndpoints Setter/Getter 검증")
    class SellerEndpointsTest {

        @Test
        @DisplayName("seller base 경로를 변경할 수 있다")
        void shouldSetSellerBase() {
            // Given
            ApiEndpointProperties.SellerEndpoints sellerEndpoints =
                    new ApiEndpointProperties.SellerEndpoints();

            // When
            sellerEndpoints.setBase("/v2/sellers");

            // Then
            assertThat(sellerEndpoints.getBase()).isEqualTo("/v2/sellers");
        }

        @Test
        @DisplayName("seller byId 경로를 변경할 수 있다")
        void shouldSetSellerById() {
            // Given
            ApiEndpointProperties.SellerEndpoints sellerEndpoints =
                    new ApiEndpointProperties.SellerEndpoints();

            // When
            sellerEndpoints.setById("/{sellerId}");

            // Then
            assertThat(sellerEndpoints.getById()).isEqualTo("/{sellerId}");
        }

        @Test
        @DisplayName("seller status 경로를 변경할 수 있다")
        void shouldSetSellerStatus() {
            // Given
            ApiEndpointProperties.SellerEndpoints sellerEndpoints =
                    new ApiEndpointProperties.SellerEndpoints();

            // When
            sellerEndpoints.setStatus("/{sellerId}/activate");

            // Then
            assertThat(sellerEndpoints.getStatus()).isEqualTo("/{sellerId}/activate");
        }
    }

    @Nested
    @DisplayName("ScheduleEndpoints Setter/Getter 검증")
    class ScheduleEndpointsTest {

        @Test
        @DisplayName("schedule base 경로를 변경할 수 있다")
        void shouldSetScheduleBase() {
            // Given
            ApiEndpointProperties.ScheduleEndpoints scheduleEndpoints =
                    new ApiEndpointProperties.ScheduleEndpoints();

            // When
            scheduleEndpoints.setBase("/v2/schedules");

            // Then
            assertThat(scheduleEndpoints.getBase()).isEqualTo("/v2/schedules");
        }

        @Test
        @DisplayName("schedule byId 경로를 변경할 수 있다")
        void shouldSetScheduleById() {
            // Given
            ApiEndpointProperties.ScheduleEndpoints scheduleEndpoints =
                    new ApiEndpointProperties.ScheduleEndpoints();

            // When
            scheduleEndpoints.setById("/{scheduleId}");

            // Then
            assertThat(scheduleEndpoints.getById()).isEqualTo("/{scheduleId}");
        }
    }

    @Nested
    @DisplayName("TaskEndpoints Setter/Getter 검증")
    class TaskEndpointsTest {

        @Test
        @DisplayName("task base 경로를 변경할 수 있다")
        void shouldSetTaskBase() {
            // Given
            ApiEndpointProperties.TaskEndpoints taskEndpoints =
                    new ApiEndpointProperties.TaskEndpoints();

            // When
            taskEndpoints.setBase("/v2/tasks");

            // Then
            assertThat(taskEndpoints.getBase()).isEqualTo("/v2/tasks");
        }

        @Test
        @DisplayName("task byId 경로를 변경할 수 있다")
        void shouldSetTaskById() {
            // Given
            ApiEndpointProperties.TaskEndpoints taskEndpoints =
                    new ApiEndpointProperties.TaskEndpoints();

            // When
            taskEndpoints.setById("/{taskId}");

            // Then
            assertThat(taskEndpoints.getById()).isEqualTo("/{taskId}");
        }
    }

    @Nested
    @DisplayName("UserAgentEndpoints Setter/Getter 검증")
    class UserAgentEndpointsTest {

        @Test
        @DisplayName("userAgent base 경로를 변경할 수 있다")
        void shouldSetUserAgentBase() {
            // Given
            ApiEndpointProperties.UserAgentEndpoints userAgentEndpoints =
                    new ApiEndpointProperties.UserAgentEndpoints();

            // When
            userAgentEndpoints.setBase("/v2/user-agents");

            // Then
            assertThat(userAgentEndpoints.getBase()).isEqualTo("/v2/user-agents");
        }

        @Test
        @DisplayName("userAgent poolStatus 경로를 변경할 수 있다")
        void shouldSetUserAgentPoolStatus() {
            // Given
            ApiEndpointProperties.UserAgentEndpoints userAgentEndpoints =
                    new ApiEndpointProperties.UserAgentEndpoints();

            // When
            userAgentEndpoints.setPoolStatus("/status");

            // Then
            assertThat(userAgentEndpoints.getPoolStatus()).isEqualTo("/status");
        }

        @Test
        @DisplayName("userAgent recover 경로를 변경할 수 있다")
        void shouldSetUserAgentRecover() {
            // Given
            ApiEndpointProperties.UserAgentEndpoints userAgentEndpoints =
                    new ApiEndpointProperties.UserAgentEndpoints();

            // When
            userAgentEndpoints.setRecover("/restore");

            // Then
            assertThat(userAgentEndpoints.getRecover()).isEqualTo("/restore");
        }
    }

    @Nested
    @DisplayName("ExecutionEndpoints Setter/Getter 검증")
    class ExecutionEndpointsTest {

        @Test
        @DisplayName("execution base 경로를 변경할 수 있다")
        void shouldSetExecutionBase() {
            // Given
            ApiEndpointProperties.ExecutionEndpoints executionEndpoints =
                    new ApiEndpointProperties.ExecutionEndpoints();

            // When
            executionEndpoints.setBase("/v2/executions");

            // Then
            assertThat(executionEndpoints.getBase()).isEqualTo("/v2/executions");
        }

        @Test
        @DisplayName("execution byId 경로를 변경할 수 있다")
        void shouldSetExecutionById() {
            // Given
            ApiEndpointProperties.ExecutionEndpoints executionEndpoints =
                    new ApiEndpointProperties.ExecutionEndpoints();

            // When
            executionEndpoints.setById("/{executionId}");

            // Then
            assertThat(executionEndpoints.getById()).isEqualTo("/{executionId}");
        }
    }

    @Nested
    @DisplayName("ApiEndpointProperties 전체 Setter 검증")
    class PropertiesSetterTest {

        @Test
        @DisplayName("baseV1을 변경할 수 있다")
        void shouldSetBaseV1() {
            // Given
            ApiEndpointProperties properties = new ApiEndpointProperties();

            // When
            properties.setBaseV1("/api/v2");

            // Then
            assertThat(properties.getBaseV1()).isEqualTo("/api/v2");
        }

        @Test
        @DisplayName("seller 엔드포인트 객체를 교체할 수 있다")
        void shouldSetSellerEndpoints() {
            // Given
            ApiEndpointProperties properties = new ApiEndpointProperties();
            ApiEndpointProperties.SellerEndpoints newSellerEndpoints =
                    new ApiEndpointProperties.SellerEndpoints();
            newSellerEndpoints.setBase("/v2/sellers");

            // When
            properties.setSeller(newSellerEndpoints);

            // Then
            assertThat(properties.getSeller().getBase()).isEqualTo("/v2/sellers");
        }

        @Test
        @DisplayName("schedule 엔드포인트 객체를 교체할 수 있다")
        void shouldSetScheduleEndpoints() {
            // Given
            ApiEndpointProperties properties = new ApiEndpointProperties();
            ApiEndpointProperties.ScheduleEndpoints newScheduleEndpoints =
                    new ApiEndpointProperties.ScheduleEndpoints();
            newScheduleEndpoints.setBase("/v2/schedules");

            // When
            properties.setSchedule(newScheduleEndpoints);

            // Then
            assertThat(properties.getSchedule().getBase()).isEqualTo("/v2/schedules");
        }

        @Test
        @DisplayName("task 엔드포인트 객체를 교체할 수 있다")
        void shouldSetTaskEndpoints() {
            // Given
            ApiEndpointProperties properties = new ApiEndpointProperties();
            ApiEndpointProperties.TaskEndpoints newTaskEndpoints =
                    new ApiEndpointProperties.TaskEndpoints();
            newTaskEndpoints.setBase("/v2/tasks");

            // When
            properties.setTask(newTaskEndpoints);

            // Then
            assertThat(properties.getTask().getBase()).isEqualTo("/v2/tasks");
        }

        @Test
        @DisplayName("userAgent 엔드포인트 객체를 교체할 수 있다")
        void shouldSetUserAgentEndpoints() {
            // Given
            ApiEndpointProperties properties = new ApiEndpointProperties();
            ApiEndpointProperties.UserAgentEndpoints newUserAgentEndpoints =
                    new ApiEndpointProperties.UserAgentEndpoints();
            newUserAgentEndpoints.setBase("/v2/user-agents");

            // When
            properties.setUserAgent(newUserAgentEndpoints);

            // Then
            assertThat(properties.getUserAgent().getBase()).isEqualTo("/v2/user-agents");
        }

        @Test
        @DisplayName("execution 엔드포인트 객체를 교체할 수 있다")
        void shouldSetExecutionEndpoints() {
            // Given
            ApiEndpointProperties properties = new ApiEndpointProperties();
            ApiEndpointProperties.ExecutionEndpoints newExecutionEndpoints =
                    new ApiEndpointProperties.ExecutionEndpoints();
            newExecutionEndpoints.setBase("/v2/executions");

            // When
            properties.setExecution(newExecutionEndpoints);

            // Then
            assertThat(properties.getExecution().getBase()).isEqualTo("/v2/executions");
        }
    }
}
