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
        @DisplayName("JSON 형식으로 응답한다")
        void shouldReturnJsonResponse() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            AuthenticationException authException =
                    new BadCredentialsException("Invalid credentials");

            // When
            handler.commence(request, response, authException);

            // Then
            assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
            assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
        }

        @Test
        @DisplayName("AUTH_001 에러 코드와 메시지를 포함한다")
        void shouldContainAuthErrorCodeAndMessage() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            AuthenticationException authException =
                    new BadCredentialsException("Invalid credentials");

            // When
            handler.commence(request, response, authException);

            // Then
            JsonNode responseBody = objectMapper.readTree(response.getContentAsString());
            JsonNode error = responseBody.get("error");
            assertThat(error.get("errorCode").asText()).isEqualTo("AUTH_001");
            assertThat(error.get("message").asText()).isEqualTo("인증이 필요합니다.");
        }

        @Test
        @DisplayName("success가 false인 응답을 반환한다")
        void shouldReturnSuccessFalse() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            AuthenticationException authException =
                    new BadCredentialsException("Invalid credentials");

            // When
            handler.commence(request, response, authException);

            // Then
            JsonNode responseBody = objectMapper.readTree(response.getContentAsString());
            assertThat(responseBody.get("success").asBoolean()).isFalse();
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
        @DisplayName("JSON 형식으로 응답한다")
        void shouldReturnJsonResponse() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            AccessDeniedException accessDeniedException =
                    new AccessDeniedException("Access denied");

            // When
            handler.handle(request, response, accessDeniedException);

            // Then
            assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
            assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
        }

        @Test
        @DisplayName("AUTH_002 에러 코드와 메시지를 포함한다")
        void shouldContainAccessDeniedErrorCodeAndMessage() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            AccessDeniedException accessDeniedException =
                    new AccessDeniedException("Access denied");

            // When
            handler.handle(request, response, accessDeniedException);

            // Then
            JsonNode responseBody = objectMapper.readTree(response.getContentAsString());
            JsonNode error = responseBody.get("error");
            assertThat(error.get("errorCode").asText()).isEqualTo("AUTH_002");
            assertThat(error.get("message").asText()).isEqualTo("접근 권한이 없습니다.");
        }

        @Test
        @DisplayName("success가 false인 응답을 반환한다")
        void shouldReturnSuccessFalse() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            AccessDeniedException accessDeniedException =
                    new AccessDeniedException("Access denied");

            // When
            handler.handle(request, response, accessDeniedException);

            // Then
            JsonNode responseBody = objectMapper.readTree(response.getContentAsString());
            assertThat(responseBody.get("success").asBoolean()).isFalse();
        }
    }

    @Nested
    @DisplayName("ApiResponse 구조 검증")
    class ApiResponseStructure {

        @Test
        @DisplayName("401 응답이 올바른 ApiResponse 구조를 가진다")
        void shouldHaveCorrectStructureFor401() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            AuthenticationException authException = new BadCredentialsException("Test");

            // When
            handler.commence(request, response, authException);

            // Then
            JsonNode responseBody = objectMapper.readTree(response.getContentAsString());
            assertThat(responseBody.has("success")).isTrue();
            assertThat(responseBody.has("data")).isTrue();
            assertThat(responseBody.has("error")).isTrue();
            assertThat(responseBody.has("timestamp")).isTrue();
            assertThat(responseBody.has("requestId")).isTrue();
            assertThat(responseBody.get("data").isNull()).isTrue();
            assertThat(responseBody.get("error").isNull()).isFalse();
        }

        @Test
        @DisplayName("403 응답이 올바른 ApiResponse 구조를 가진다")
        void shouldHaveCorrectStructureFor403() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            AccessDeniedException accessDeniedException = new AccessDeniedException("Test");

            // When
            handler.handle(request, response, accessDeniedException);

            // Then
            JsonNode responseBody = objectMapper.readTree(response.getContentAsString());
            assertThat(responseBody.has("success")).isTrue();
            assertThat(responseBody.has("data")).isTrue();
            assertThat(responseBody.has("error")).isTrue();
            assertThat(responseBody.has("timestamp")).isTrue();
            assertThat(responseBody.has("requestId")).isTrue();
            assertThat(responseBody.get("data").isNull()).isTrue();
            assertThat(responseBody.get("error").isNull()).isFalse();
        }

        @Test
        @DisplayName("error 필드가 올바른 ErrorInfo 구조를 가진다")
        void shouldHaveCorrectErrorInfoStructure() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            AuthenticationException authException = new BadCredentialsException("Test");

            // When
            handler.commence(request, response, authException);

            // Then
            JsonNode responseBody = objectMapper.readTree(response.getContentAsString());
            JsonNode error = responseBody.get("error");
            assertThat(error.has("errorCode")).isTrue();
            assertThat(error.has("message")).isTrue();
        }
    }
}
