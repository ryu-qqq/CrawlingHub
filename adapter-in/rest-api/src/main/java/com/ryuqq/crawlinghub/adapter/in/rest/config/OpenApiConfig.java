package com.ryuqq.crawlinghub.adapter.in.rest.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
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
     * @return 서버 목록
     */
    private List<Server> servers() {
        return List.of(new Server().url("/").description("Current Server"));
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
                                                + " 전달됩니다."));
    }
}
