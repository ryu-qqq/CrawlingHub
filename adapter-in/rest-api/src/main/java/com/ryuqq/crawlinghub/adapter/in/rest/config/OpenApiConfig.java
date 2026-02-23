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
 * <p>Service Token 기반 인증을 사용합니다. X-Service-Token / X-Service-Name 헤더를 SecurityScheme으로 등록하여 Swagger
 * UI에서 인증 헤더를 입력할 수 있게 합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    private static final String HEADER_SERVICE_TOKEN = "X-Service-Token";
    private static final String HEADER_SERVICE_NAME = "X-Service-Name";

    @Bean
    public OpenAPI openAPI(@Value("${api.server.url:}") String serverUrl) {
        OpenAPI openAPI =
                new OpenAPI()
                        .info(
                                new Info()
                                        .title("CrawlingHub API")
                                        .version("v1")
                                        .description("CrawlingHub API 문서"))
                        .components(securityComponents())
                        .security(
                                List.of(
                                        new SecurityRequirement()
                                                .addList(HEADER_SERVICE_TOKEN)
                                                .addList(HEADER_SERVICE_NAME)));

        if (serverUrl != null && !serverUrl.isBlank()) {
            openAPI.servers(List.of(new Server().url(serverUrl).description("API Server")));
        }

        return openAPI;
    }

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes(
                        HEADER_SERVICE_TOKEN,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(HEADER_SERVICE_TOKEN)
                                .description("서비스 인증 토큰 (필수)"))
                .addSecuritySchemes(
                        HEADER_SERVICE_NAME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(HEADER_SERVICE_NAME)
                                .description("호출 서비스 이름"))
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
