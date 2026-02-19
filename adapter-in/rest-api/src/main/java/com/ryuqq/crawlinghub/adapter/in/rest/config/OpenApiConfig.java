package com.ryuqq.crawlinghub.adapter.in.rest.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 스펙 설정.
 *
 * <p>Gateway 뒤에서 동작하므로, Swagger UI의 "Try it out" 기능이 올바른 URL로 요청하도록 서버 URL을 명시합니다. Gateway-first
 * 아키텍처에서 전달되는 X-User-* 헤더를 SecurityScheme으로 등록하여 Swagger UI에서 인증 헤더를 입력할 수 있게 합니다.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String HEADER_USER_PERMISSIONS = "X-User-Permissions";
    private static final String HEADER_ORGANIZATION_ID = "X-Organization-Id";

    @Bean
    public OpenAPI openAPI(@Value("${api.server.url:}") String serverUrl) {
        OpenAPI openAPI =
                new OpenAPI()
                        .info(
                                new Info()
                                        .title("CrawlingHub Admin API")
                                        .version("v1")
                                        .description("CrawlingHub Admin API 문서"))
                        .components(securityComponents())
                        .security(
                                List.of(
                                        new SecurityRequirement()
                                                .addList(HEADER_USER_ID)
                                                .addList(HEADER_USER_ROLES)
                                                .addList(HEADER_USER_PERMISSIONS)
                                                .addList(HEADER_ORGANIZATION_ID)));

        if (serverUrl != null && !serverUrl.isBlank()) {
            openAPI.servers(List.of(new Server().url(serverUrl).description("API Server")));
        }

        return openAPI;
    }

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes(
                        HEADER_USER_ID,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(HEADER_USER_ID)
                                .description("사용자 ID (필수)"))
                .addSecuritySchemes(
                        HEADER_USER_ROLES,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(HEADER_USER_ROLES)
                                .description("사용자 역할 (예: ROLE_SUPER_ADMIN)"))
                .addSecuritySchemes(
                        HEADER_USER_PERMISSIONS,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(HEADER_USER_PERMISSIONS)
                                .description("사용자 권한 (예: *:*, seller:read)"))
                .addSecuritySchemes(
                        HEADER_ORGANIZATION_ID,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(HEADER_ORGANIZATION_ID)
                                .description("조직 ID"))
                .schemas(responseSchemas());
    }

    @SuppressWarnings("rawtypes")
    private Map<String, Schema> responseSchemas() {
        return Map.of(
                "ApiResponse",
                apiResponseSchema(),
                "ProblemDetail",
                problemDetailSchema(),
                "FieldError",
                fieldErrorSchema());
    }

    @SuppressWarnings("rawtypes")
    private Schema apiResponseSchema() {
        return new ObjectSchema()
                .description("표준 API 성공 응답")
                .addProperty("data", new ObjectSchema().description("응답 데이터 (null 가능)"))
                .addProperty(
                        "timestamp",
                        new StringSchema()
                                .description("ISO 8601 형식 응답 시각")
                                .example("2025-12-22T10:30:00.000Z"))
                .addProperty(
                        "requestId",
                        new StringSchema()
                                .description("UUID 형식 요청 ID")
                                .example("550e8400-e29b-41d4-a716-446655440000"));
    }

    @SuppressWarnings("rawtypes")
    private Schema problemDetailSchema() {
        return new ObjectSchema()
                .description("RFC 7807 ProblemDetail 에러 응답")
                .addProperty(
                        "type", new StringSchema().description("에러 타입 URI").example("about:blank"))
                .addProperty(
                        "title",
                        new StringSchema().description("HTTP 상태 설명").example("Bad Request"))
                .addProperty("status", new IntegerSchema().description("HTTP 상태 코드").example(400))
                .addProperty(
                        "detail",
                        new StringSchema().description("에러 상세 메시지").example("유효성 검증에 실패했습니다."))
                .addProperty(
                        "code",
                        new StringSchema().description("애플리케이션 에러 코드").example("VALIDATION_ERROR"))
                .addProperty(
                        "timestamp",
                        new StringSchema()
                                .description("ISO 8601 형식 에러 발생 시각")
                                .example("2025-12-22T10:30:00.000Z"))
                .addProperty(
                        "instance",
                        new StringSchema()
                                .description("에러가 발생한 요청 경로")
                                .example("/api/v1/crawling/sellers"))
                .addProperty(
                        "errors",
                        new ArraySchema()
                                .items(new Schema<>().$ref("#/components/schemas/FieldError"))
                                .description("필드별 유효성 검증 에러 목록 (선택)"));
    }

    @SuppressWarnings("rawtypes")
    private Schema fieldErrorSchema() {
        return new ObjectSchema()
                .description("필드별 유효성 검증 에러")
                .addProperty(
                        "field", new StringSchema().description("에러 발생 필드명").example("siteUrl"))
                .addProperty(
                        "message",
                        new StringSchema().description("에러 메시지").example("사이트 URL은 필수입니다."));
    }
}
