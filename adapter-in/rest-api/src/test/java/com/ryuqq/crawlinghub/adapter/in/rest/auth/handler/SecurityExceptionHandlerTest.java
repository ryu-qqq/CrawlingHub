package com.ryuqq.crawlinghub.adapter.in.rest.auth.handler;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

@Tag("unit")
@Tag("rest-api")
@Tag("security")
@DisplayName("SecurityExceptionHandler 단위 테스트")
class SecurityExceptionHandlerTest {

    private SecurityExceptionHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new SecurityExceptionHandler(objectMapper);
    }

    @Nested
    @DisplayName("commence() 메서드는")
    class CommenceMethod {

        @Test
        @DisplayName("401 Unauthorized 응답을 반환한다")
        void shouldReturn401Unauthorized() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            AuthenticationException authException =
                    new BadCredentialsException("Invalid credentials");

            // When
            handler.commence(request, response, authException);

            // Then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        @DisplayName("application/problem+json 형식으로 응답한다")
        void shouldReturnProblemJsonResponse() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            AuthenticationException authException =
                    new BadCredentialsException("Invalid credentials");

            // When
            handler.commence(request, response, authException);

            // Then
            assertThat(response.getContentType())
                    .startsWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
        }

        @Test
        @DisplayName("UNAUTHORIZED 코드와 메시지를 포함한다")
        void shouldContainUnauthorizedCodeAndMessage() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            AuthenticationException authException =
                    new BadCredentialsException("Invalid credentials");

            // When
            handler.commence(request, response, authException);

            // Then
            JsonNode responseBody = objectMapper.readTree(response.getContentAsString());
            assertThat(responseBody.get("code").asText()).isEqualTo("UNAUTHORIZED");
            assertThat(responseBody.get("detail").asText()).isEqualTo("인증이 필요합니다.");
        }
    }

    @Nested
    @DisplayName("handle() 메서드는")
    class HandleMethod {

        @Test
        @DisplayName("403 Forbidden 응답을 반환한다")
        void shouldReturn403Forbidden() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            AccessDeniedException accessDeniedException =
                    new AccessDeniedException("Access denied");

            // When
            handler.handle(request, response, accessDeniedException);

            // Then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        }

        @Test
        @DisplayName("application/problem+json 형식으로 응답한다")
        void shouldReturnProblemJsonResponse() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            AccessDeniedException accessDeniedException =
                    new AccessDeniedException("Access denied");

            // When
            handler.handle(request, response, accessDeniedException);

            // Then
            assertThat(response.getContentType())
                    .startsWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
        }

        @Test
        @DisplayName("FORBIDDEN 코드와 메시지를 포함한다")
        void shouldContainForbiddenCodeAndMessage() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            AccessDeniedException accessDeniedException =
                    new AccessDeniedException("Access denied");

            // When
            handler.handle(request, response, accessDeniedException);

            // Then
            JsonNode responseBody = objectMapper.readTree(response.getContentAsString());
            assertThat(responseBody.get("code").asText()).isEqualTo("FORBIDDEN");
            assertThat(responseBody.get("detail").asText()).isEqualTo("접근 권한이 없습니다.");
        }
    }

    @Nested
    @DisplayName("RFC 7807 ProblemDetail 구조 검증")
    class ProblemDetailStructure {

        @Test
        @DisplayName("401 응답이 올바른 ProblemDetail 구조를 가진다")
        void shouldHaveCorrectStructureFor401() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/test");
            MockHttpServletResponse response = new MockHttpServletResponse();
            AuthenticationException authException = new BadCredentialsException("Test");

            // When
            handler.commence(request, response, authException);

            // Then
            JsonNode responseBody = objectMapper.readTree(response.getContentAsString());
            assertThat(responseBody.has("type")).isTrue();
            assertThat(responseBody.has("title")).isTrue();
            assertThat(responseBody.has("status")).isTrue();
            assertThat(responseBody.has("detail")).isTrue();
            assertThat(responseBody.has("code")).isTrue();
            assertThat(responseBody.has("timestamp")).isTrue();
            assertThat(responseBody.has("instance")).isTrue();

            assertThat(responseBody.get("type").asText()).isEqualTo("about:blank");
            assertThat(responseBody.get("title").asText()).isEqualTo("Unauthorized");
            assertThat(responseBody.get("status").asInt()).isEqualTo(401);
            assertThat(responseBody.get("instance").asText()).isEqualTo("/api/test");
        }

        @Test
        @DisplayName("403 응답이 올바른 ProblemDetail 구조를 가진다")
        void shouldHaveCorrectStructureFor403() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/admin");
            MockHttpServletResponse response = new MockHttpServletResponse();
            AccessDeniedException accessDeniedException = new AccessDeniedException("Test");

            // When
            handler.handle(request, response, accessDeniedException);

            // Then
            JsonNode responseBody = objectMapper.readTree(response.getContentAsString());
            assertThat(responseBody.has("type")).isTrue();
            assertThat(responseBody.has("title")).isTrue();
            assertThat(responseBody.has("status")).isTrue();
            assertThat(responseBody.has("detail")).isTrue();
            assertThat(responseBody.has("code")).isTrue();
            assertThat(responseBody.has("timestamp")).isTrue();
            assertThat(responseBody.has("instance")).isTrue();

            assertThat(responseBody.get("type").asText()).isEqualTo("about:blank");
            assertThat(responseBody.get("title").asText()).isEqualTo("Forbidden");
            assertThat(responseBody.get("status").asInt()).isEqualTo(403);
            assertThat(responseBody.get("instance").asText()).isEqualTo("/api/admin");
        }

        @Test
        @DisplayName("쿼리스트링이 있는 경우 instance에 포함된다")
        void shouldIncludeQueryStringInInstance() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/test");
            request.setQueryString("page=1&size=10");
            MockHttpServletResponse response = new MockHttpServletResponse();
            AuthenticationException authException = new BadCredentialsException("Test");

            // When
            handler.commence(request, response, authException);

            // Then
            JsonNode responseBody = objectMapper.readTree(response.getContentAsString());
            assertThat(responseBody.get("instance").asText()).isEqualTo("/api/test?page=1&size=10");
        }
    }
}
