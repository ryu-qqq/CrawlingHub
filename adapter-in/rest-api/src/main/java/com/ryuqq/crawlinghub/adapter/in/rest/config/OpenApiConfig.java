package com.ryuqq.crawlinghub.adapter.in.rest.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) 설정
 *
 * <p>CrawlingHub REST API 문서화를 위한 OpenAPI 3.0 설정
 *
 * <p><strong>제공 기능:</strong>
 *
 * <ul>
 *   <li>API 정보 (제목, 버전, 설명)
 *   <li>Security Scheme (Bearer Token)
 *   <li>서버 정보
 * </ul>
 *
 * <p><strong>Security Scheme:</strong>
 *
 * <ul>
 *   <li>Type: HTTP Bearer
 *   <li>Format: JWT
 *   <li>Header: Authorization
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * OpenAPI 설정 Bean
     *
     * @return OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .components(securityComponents())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    /**
     * API 정보 설정
     *
     * @return API Info 객체
     */
    private Info apiInfo() {
        return new Info()
                .title("CrawlingHub REST API")
                .version("1.0.0")
                .description(
                        """
                        CrawlingHub 크롤링 관리 시스템 REST API

                        ## 인증
                        모든 API는 Bearer Token 인증이 필요합니다.
                        Authorization 헤더에 `Bearer {token}` 형식으로 전달하세요.

                        ## 응답 형식

                        ### 성공 응답
                        모든 성공 응답은 `ApiResponse` 형식을 사용합니다:
                        ```json
                        {
                          "data": { ... },
                          "timestamp": "2025-12-22T10:30:00.000Z",
                          "requestId": "550e8400-e29b-41d4-a716-446655440000"
                        }
                        ```

                        ### 에러 응답
                        에러 응답은 RFC 7807 `ProblemDetail` 형식을 사용합니다:
                        ```json
                        {
                          "type": "about:blank",
                          "title": "Bad Request",
                          "status": 400,
                          "detail": "에러 상세 메시지",
                          "code": "ERROR_CODE",
                          "timestamp": "2025-12-22T10:30:00.000Z",
                          "instance": "/api/v1/crawling/..."
                        }
                        ```

                        ## 권한
                        각 API는 특정 권한이 필요합니다. 권한 형식: `{domain}:{action}`

                        ### 권한 목록
                        - `seller:create` - 셀러 등록
                        - `seller:read` - 셀러 조회
                        - `seller:update` - 셀러 수정
                        - `scheduler:create` - 스케줄러 등록
                        - `scheduler:read` - 스케줄러 조회
                        - `scheduler:update` - 스케줄러 수정
                        - `task:read` - 태스크 조회
                        - `execution:read` - 실행 기록 조회
                        - `useragent:read` - UserAgent 조회
                        - `useragent:manage` - UserAgent 관리

                        ### 슈퍼 관리자
                        `ROLE_SUPER_ADMIN` 역할을 가진 사용자는 모든 권한을 가집니다.
                        """)
                .contact(new Contact().name("Development Team").email("dev@crawlinghub.com"))
                .license(
                        new License()
                                .name("Private License")
                                .url("https://crawlinghub.com/license"));
    }

    /**
     * 서버 정보 설정
     *
     * <p>Gateway 라우팅 패턴에 맞춰 서버 URL을 설정합니다. API 호출 시 /api/v1/crawling prefix가 자동으로 적용됩니다.
     *
     * @return 서버 목록
     */
    private List<Server> servers() {
        return List.of(new Server().url("/").description("CrawlingHub API Server (via Gateway)"));
    }

    /**
     * Security Components 설정
     *
     * @return Components 객체
     */
    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes(
                        SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description(
                                        "JWT 토큰을 입력하세요. Authorization 헤더에 'Bearer {token}' 형식으로"
                                                + " 전달됩니다."))
                .schemas(responseSchemas());
    }

    /**
     * 공통 응답 스키마 정의
     *
     * @return 스키마 맵
     */
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

    /**
     * ApiResponse 스키마 - 성공 응답 형식
     *
     * @return ApiResponse 스키마
     */
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

    /**
     * ProblemDetail 스키마 - RFC 7807 에러 응답 형식
     *
     * @return ProblemDetail 스키마
     */
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

    /**
     * FieldError 스키마 - 필드별 유효성 검증 에러
     *
     * @return FieldError 스키마
     */
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
