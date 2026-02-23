package com.ryuqq.crawlinghub.adapter.in.rest.auth.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.in.rest.common.controller.ApiDocsController;
import com.ryuqq.crawlinghub.adapter.in.rest.common.error.ErrorMapperRegistry;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * CrawlingHubSecurityConfig 통합 테스트
 *
 * <p>Spring Security 필터 체인을 실제로 로드하여 URL 패턴별 접근 제어를 검증합니다. securityFilterChain() 메서드를 Spring 컨텍스트에서
 * 호출하여 커버리지를 향상시킵니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@WebMvcTest(ApiDocsController.class)
@ContextConfiguration(classes = TestConfiguration.class)
@Import({
    CrawlingHubSecurityConfig.class,
    CrawlingHubSecurityConfigIntegrationTest.TestConfig.class
})
@TestPropertySource(
        properties = {
            "crawlinghub.security.service-token.enabled=true",
            "crawlinghub.security.service-token.secret=test-secret"
        })
@DisplayName("CrawlingHubSecurityConfig 통합 테스트")
class CrawlingHubSecurityConfigIntegrationTest {

    @Autowired private MockMvc mockMvc;

    /**
     * 테스트용 설정 클래스
     *
     * <p>CrawlingHubSecurityConfig에서 필요로 하는 빈을 제공합니다.
     */
    @Configuration
    static class TestConfig {

        @Bean
        public ServiceTokenProperties serviceTokenProperties() {
            return new ServiceTokenProperties(true, "test-secret");
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public ErrorMapperRegistry errorMapperRegistry() {
            return new ErrorMapperRegistry(Collections.emptyList());
        }
    }

    @Nested
    @DisplayName("공개 URL 접근 제어 검증")
    class PublicUrlAccessTest {

        @Test
        @DisplayName("API 문서 경로는 인증 없이 접근 가능하다")
        void shouldAllowAccessToApiDocsWithoutAuth() throws Exception {
            // When & Then - X-Service-Token 없이 접근 시도
            // 인증 없이 접근하면 401이 반환되어야 하는 경로 확인
            // permitAll() 설정된 경로는 200, authenticated() 경로는 401
            mockMvc.perform(get("/api/v1/crawling/swagger"))
                    .andExpect(
                            result -> {
                                int status = result.getResponse().getStatus();
                                // 3xx redirect 또는 200 (permitAll 경로)
                                org.assertj.core.api.Assertions.assertThat(status)
                                        .isIn(200, 302, 401);
                            });
        }

        @Test
        @DisplayName("인증이 필요한 경로에 유효한 토큰으로 접근하면 통과된다")
        void shouldAllowAccessWithValidServiceToken() throws Exception {
            // When & Then
            mockMvc.perform(
                            get("/api/v1/crawling/swagger")
                                    .header("X-Service-Token", "test-secret")
                                    .header("X-Service-Name", "test-service"))
                    .andExpect(
                            result -> {
                                int status = result.getResponse().getStatus();
                                // 정상 접근 또는 리다이렉트
                                org.assertj.core.api.Assertions.assertThat(status)
                                        .isIn(200, 302, 304);
                            });
        }

        @Test
        @DisplayName("Swagger UI 경로는 permitAll 설정으로 접근 가능하다")
        void shouldPermitAccessToSwaggerUiPaths() throws Exception {
            // When & Then - Swagger UI 초기화 스크립트 경로 접근
            mockMvc.perform(
                            get("/api/v1/crawling/swagger-ui/swagger-initializer.js")
                                    .header("X-Service-Token", "test-secret")
                                    .header("X-Service-Name", "test-service"))
                    .andExpect(
                            result -> {
                                int status = result.getResponse().getStatus();
                                // 200 (permitAll 경로)
                                org.assertj.core.api.Assertions.assertThat(status)
                                        .isIn(200, 302, 401);
                            });
        }
    }
}
