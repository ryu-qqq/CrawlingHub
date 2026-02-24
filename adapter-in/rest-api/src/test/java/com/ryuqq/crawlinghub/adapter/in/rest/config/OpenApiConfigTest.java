package com.ryuqq.crawlinghub.adapter.in.rest.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * OpenApiConfig 단위 테스트
 *
 * <p>OpenAPI 스펙 설정의 보안 스키마, 서버 URL, 응답 스키마를 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@DisplayName("OpenApiConfig 단위 테스트")
class OpenApiConfigTest {

    private OpenApiConfig openApiConfig;

    @BeforeEach
    void setUp() {
        openApiConfig = new OpenApiConfig();
    }

    @Nested
    @DisplayName("openAPI() 빈 검증")
    class OpenAPIBeanTest {

        @Test
        @DisplayName("OpenAPI 빈이 null이 아니다")
        void shouldCreateNonNullOpenAPI() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            assertThat(openAPI).isNotNull();
        }

        @Test
        @DisplayName("API 제목이 CrawlingHub API이다")
        void shouldHaveCrawlingHubApiTitle() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            assertThat(openAPI.getInfo()).isNotNull();
            assertThat(openAPI.getInfo().getTitle()).isEqualTo("CrawlingHub API");
        }

        @Test
        @DisplayName("API 버전이 v1이다")
        void shouldHaveV1Version() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            assertThat(openAPI.getInfo().getVersion()).isEqualTo("v1");
        }

        @Test
        @DisplayName("API 설명이 포함된다")
        void shouldHaveDescription() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            assertThat(openAPI.getInfo().getDescription()).isEqualTo("CrawlingHub API 문서");
        }
    }

    @Nested
    @DisplayName("서버 URL 설정 검증")
    class ServerUrlTest {

        @Test
        @DisplayName("serverUrl이 비어있으면 서버 목록이 null이거나 비어있다")
        void shouldNotSetServerWhenUrlIsEmpty() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            // 서버가 설정되지 않으면 null이거나 빈 목록
            if (openAPI.getServers() != null) {
                assertThat(openAPI.getServers()).isEmpty();
            } else {
                assertThat(openAPI.getServers()).isNull();
            }
        }

        @Test
        @DisplayName("serverUrl이 설정되면 서버 목록에 포함된다")
        void shouldSetServerWhenUrlIsProvided() {
            // Given
            String serverUrl = "https://api.example.com";

            // When
            OpenAPI openAPI = openApiConfig.openAPI(serverUrl);

            // Then
            assertThat(openAPI.getServers()).isNotNull();
            assertThat(openAPI.getServers()).hasSize(1);
            assertThat(openAPI.getServers().get(0).getUrl()).isEqualTo(serverUrl);
            assertThat(openAPI.getServers().get(0).getDescription()).isEqualTo("API Server");
        }

        @Test
        @DisplayName("serverUrl이 공백으로만 이루어지면 서버가 설정되지 않는다")
        void shouldNotSetServerWhenUrlIsBlank() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("   ");

            // Then
            if (openAPI.getServers() != null) {
                assertThat(openAPI.getServers()).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("보안 설정 검증")
    class SecurityConfigTest {

        @Test
        @DisplayName("Components가 null이 아니다")
        void shouldHaveNonNullComponents() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            assertThat(openAPI.getComponents()).isNotNull();
        }

        @Test
        @DisplayName("X-Service-Token 보안 스키마가 등록된다")
        void shouldRegisterXServiceTokenSecurityScheme() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("X-Service-Token");
            SecurityScheme tokenScheme =
                    openAPI.getComponents().getSecuritySchemes().get("X-Service-Token");
            assertThat(tokenScheme.getType()).isEqualTo(SecurityScheme.Type.APIKEY);
            assertThat(tokenScheme.getIn()).isEqualTo(SecurityScheme.In.HEADER);
            assertThat(tokenScheme.getName()).isEqualTo("X-Service-Token");
        }

        @Test
        @DisplayName("X-Service-Name 보안 스키마가 등록된다")
        void shouldRegisterXServiceNameSecurityScheme() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("X-Service-Name");
            SecurityScheme nameScheme =
                    openAPI.getComponents().getSecuritySchemes().get("X-Service-Name");
            assertThat(nameScheme.getType()).isEqualTo(SecurityScheme.Type.APIKEY);
            assertThat(nameScheme.getIn()).isEqualTo(SecurityScheme.In.HEADER);
            assertThat(nameScheme.getName()).isEqualTo("X-Service-Name");
        }

        @Test
        @DisplayName("보안 요구사항에 X-Service-Token이 포함된다")
        void shouldIncludeXServiceTokenInSecurityRequirements() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            assertThat(openAPI.getSecurity()).isNotNull();
            assertThat(openAPI.getSecurity()).isNotEmpty();

            // 첫 번째 SecurityRequirement에 X-Service-Token이 있어야 함
            SecurityRequirement requirement = openAPI.getSecurity().get(0);
            assertThat(requirement).containsKey("X-Service-Token");
        }

        @Test
        @DisplayName("보안 요구사항에 X-Service-Name이 포함된다")
        void shouldIncludeXServiceNameInSecurityRequirements() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            assertThat(openAPI.getSecurity()).isNotNull();
            assertThat(openAPI.getSecurity()).isNotEmpty();

            SecurityRequirement requirement = openAPI.getSecurity().get(0);
            assertThat(requirement).containsKey("X-Service-Name");
        }
    }

    @Nested
    @DisplayName("응답 스키마 검증")
    class ResponseSchemasTest {

        @Test
        @DisplayName("ApiResponse 스키마가 등록된다")
        void shouldRegisterApiResponseSchema() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            assertThat(openAPI.getComponents().getSchemas()).containsKey("ApiResponse");
        }

        @Test
        @DisplayName("ProblemDetail 스키마가 등록된다")
        void shouldRegisterProblemDetailSchema() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            assertThat(openAPI.getComponents().getSchemas()).containsKey("ProblemDetail");
        }

        @Test
        @DisplayName("FieldError 스키마가 등록된다")
        void shouldRegisterFieldErrorSchema() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            assertThat(openAPI.getComponents().getSchemas()).containsKey("FieldError");
        }

        @Test
        @DisplayName("ApiResponse 스키마에 data 프로퍼티가 있다")
        void shouldHaveDataPropertyInApiResponseSchema() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            var apiResponseSchema = openAPI.getComponents().getSchemas().get("ApiResponse");
            assertThat(apiResponseSchema).isNotNull();
            assertThat(apiResponseSchema.getProperties()).containsKey("data");
        }

        @Test
        @DisplayName("ApiResponse 스키마에 timestamp 프로퍼티가 있다")
        void shouldHaveTimestampPropertyInApiResponseSchema() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            var apiResponseSchema = openAPI.getComponents().getSchemas().get("ApiResponse");
            assertThat(apiResponseSchema.getProperties()).containsKey("timestamp");
        }

        @Test
        @DisplayName("ApiResponse 스키마에 requestId 프로퍼티가 있다")
        void shouldHaveRequestIdPropertyInApiResponseSchema() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            var apiResponseSchema = openAPI.getComponents().getSchemas().get("ApiResponse");
            assertThat(apiResponseSchema.getProperties()).containsKey("requestId");
        }

        @Test
        @DisplayName("ProblemDetail 스키마에 status 프로퍼티가 있다")
        void shouldHaveStatusPropertyInProblemDetailSchema() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            var problemDetailSchema = openAPI.getComponents().getSchemas().get("ProblemDetail");
            assertThat(problemDetailSchema.getProperties()).containsKey("status");
        }

        @Test
        @DisplayName("ProblemDetail 스키마에 errors 프로퍼티가 있다")
        void shouldHaveErrorsPropertyInProblemDetailSchema() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            var problemDetailSchema = openAPI.getComponents().getSchemas().get("ProblemDetail");
            assertThat(problemDetailSchema.getProperties()).containsKey("errors");
        }

        @Test
        @DisplayName("FieldError 스키마에 field 프로퍼티가 있다")
        void shouldHaveFieldPropertyInFieldErrorSchema() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            var fieldErrorSchema = openAPI.getComponents().getSchemas().get("FieldError");
            assertThat(fieldErrorSchema.getProperties()).containsKey("field");
        }

        @Test
        @DisplayName("FieldError 스키마에 message 프로퍼티가 있다")
        void shouldHaveMessagePropertyInFieldErrorSchema() {
            // When
            OpenAPI openAPI = openApiConfig.openAPI("");

            // Then
            var fieldErrorSchema = openAPI.getComponents().getSchemas().get("FieldError");
            assertThat(fieldErrorSchema.getProperties()).containsKey("message");
        }
    }
}
