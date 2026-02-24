package com.ryuqq.crawlinghub.adapter.in.rest.common.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ApiDocsController 단위 테스트
 *
 * <p>API 문서 접근 및 Swagger UI 서빙 컨트롤러의 리다이렉트 및 스크립트 응답을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@DisplayName("ApiDocsController 단위 테스트")
class ApiDocsControllerTest {

    private MockMvc mockMvc;
    private ApiDocsController controller;

    @BeforeEach
    void setUp() {
        controller = new ApiDocsController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Nested
    @DisplayName("GET /api/v1/crawling/docs - REST Docs 메인 페이지 리다이렉트")
    class RedirectToRestDocsTest {

        @Test
        @DisplayName("docs 경로로 접근하면 index.html로 리다이렉트한다")
        void shouldRedirectToDocsIndexHtml() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/crawling/docs"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(header().string("Location", "/api/v1/crawling/docs/index.html"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/crawling/swagger - Swagger UI 리다이렉트")
    class RedirectToSwaggerTest {

        @Test
        @DisplayName("swagger 경로로 접근하면 swagger-ui/index.html로 리다이렉트한다")
        void shouldRedirectToSwaggerUiIndexHtml() throws Exception {
            // When & Then
            MvcResult result =
                    mockMvc.perform(get("/api/v1/crawling/swagger"))
                            .andExpect(status().is3xxRedirection())
                            .andReturn();

            String location = result.getResponse().getHeader("Location");
            assertThat(location).isNotNull();
            assertThat(location).contains("/api/v1/crawling/swagger-ui/index.html");
            assertThat(location).contains("url=");
            assertThat(location).contains("/api/v1/crawling/api-docs");
        }
    }

    @Nested
    @DisplayName("GET /api/v1/crawling/swagger-ui/swagger-initializer.js - Swagger 초기화 스크립트")
    class SwaggerInitializerTest {

        @Test
        @DisplayName("swagger-initializer.js 내용을 반환한다")
        void shouldReturnSwaggerInitializerScript() throws Exception {
            // When
            MvcResult result =
                    mockMvc.perform(get("/api/v1/crawling/swagger-ui/swagger-initializer.js"))
                            .andExpect(status().isOk())
                            .andReturn();

            // Then
            String content = result.getResponse().getContentAsString();
            assertThat(content).isNotNull();
            assertThat(content).isNotBlank();
        }

        @Test
        @DisplayName("swagger-initializer.js에 SwaggerUIBundle이 포함된다")
        void shouldContainSwaggerUIBundle() throws Exception {
            // When
            MvcResult result =
                    mockMvc.perform(get("/api/v1/crawling/swagger-ui/swagger-initializer.js"))
                            .andExpect(status().isOk())
                            .andReturn();

            // Then
            String content = result.getResponse().getContentAsString();
            assertThat(content).contains("SwaggerUIBundle");
        }

        @Test
        @DisplayName("swagger-initializer.js에 API docs URL이 포함된다")
        void shouldContainApiDocsUrl() throws Exception {
            // When
            MvcResult result =
                    mockMvc.perform(get("/api/v1/crawling/swagger-ui/swagger-initializer.js"))
                            .andExpect(status().isOk())
                            .andReturn();

            // Then
            String content = result.getResponse().getContentAsString();
            assertThat(content).contains("/api/v1/crawling/api-docs");
        }

        @Test
        @DisplayName("swagger-initializer.js에 deepLinking 설정이 포함된다")
        void shouldContainDeepLinkingConfig() throws Exception {
            // When
            MvcResult result =
                    mockMvc.perform(get("/api/v1/crawling/swagger-ui/swagger-initializer.js"))
                            .andExpect(status().isOk())
                            .andReturn();

            // Then
            String content = result.getResponse().getContentAsString();
            assertThat(content).contains("deepLinking");
        }
    }

    @Nested
    @DisplayName("WebMvcConfigurer 구현 검증")
    class WebMvcConfigurerTest {

        @Test
        @DisplayName("ApiDocsController는 WebMvcConfigurer를 구현한다")
        void shouldImplementWebMvcConfigurer() {
            // When & Then
            // ApiDocsController가 WebMvcConfigurer를 구현하는지 확인
            assertThat(controller).isInstanceOf(WebMvcConfigurer.class);
        }

        @Test
        @DisplayName("컨트롤러 인스턴스가 정상적으로 생성된다")
        void shouldCreateControllerInstance() {
            // When
            ApiDocsController newController = new ApiDocsController();

            // Then
            assertThat(newController).isNotNull();
        }

        @Test
        @DisplayName("swaggerInitializer 메서드가 tryItOutEnabled 설정을 포함한다")
        void shouldContainTryItOutEnabled() throws Exception {
            // When
            MvcResult result =
                    mockMvc.perform(get("/api/v1/crawling/swagger-ui/swagger-initializer.js"))
                            .andExpect(status().isOk())
                            .andReturn();

            // Then
            String content = result.getResponse().getContentAsString();
            assertThat(content).contains("tryItOutEnabled");
        }

        @Test
        @DisplayName("swaggerInitializer 메서드가 displayRequestDuration 설정을 포함한다")
        void shouldContainDisplayRequestDuration() throws Exception {
            // When
            MvcResult result =
                    mockMvc.perform(get("/api/v1/crawling/swagger-ui/swagger-initializer.js"))
                            .andExpect(status().isOk())
                            .andReturn();

            // Then
            String content = result.getResponse().getContentAsString();
            assertThat(content).contains("displayRequestDuration");
        }
    }
}
