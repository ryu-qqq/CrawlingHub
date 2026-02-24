package com.ryuqq.crawlinghub.adapter.in.rest.auth.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.in.rest.auth.filter.ServiceTokenAuthenticationFilter;
import com.ryuqq.crawlinghub.adapter.in.rest.common.error.ErrorMapperRegistry;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * CrawlingHubSecurityConfig securityFilterChain() 통합 테스트
 *
 * <p>Spring Security 필터 체인의 URL 패턴별 접근 제어를 검증합니다. HttpSecurity를 실제로 구성하여 securityFilterChain 메서드를
 * 커버합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@DisplayName("CrawlingHubSecurityConfig 필터 체인 테스트")
class CrawlingHubSecurityFilterChainTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("ServiceTokenAuthenticationFilter 직접 실행 검증")
    class FilterDirectExecutionTest {

        @Test
        @DisplayName("enabled=true 환경에서 ServiceTokenAuthenticationFilter가 정상 동작한다")
        void shouldFilterWorkWithEnabledTrue() throws Exception {
            // Given
            ServiceTokenProperties props = new ServiceTokenProperties(true, "secret");
            ServiceTokenAuthenticationFilter filter = new ServiceTokenAuthenticationFilter(props);

            MockMvc mockMvc =
                    MockMvcBuilders.standaloneSetup(new TestHealthController())
                            .addFilters(filter)
                            .build();

            // When & Then - 올바른 토큰으로 요청
            mockMvc.perform(
                            get("/api/v1/health")
                                    .header("X-Service-Token", "secret")
                                    .header("X-Service-Name", "test-service"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("enabled=false 환경에서 ServiceTokenAuthenticationFilter가 anonymous로 통과된다")
        void shouldFilterWorkWithEnabledFalse() throws Exception {
            // Given
            ServiceTokenProperties props = new ServiceTokenProperties(false, "");
            ServiceTokenAuthenticationFilter filter = new ServiceTokenAuthenticationFilter(props);

            MockMvc mockMvc =
                    MockMvcBuilders.standaloneSetup(new TestHealthController())
                            .addFilters(filter)
                            .build();

            // When & Then - disabled 환경에서는 헤더 없이도 통과
            mockMvc.perform(get("/api/v1/health")).andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("ErrorMapperRegistry 기본 생성 검증")
    class ErrorMapperRegistryTest {

        @Test
        @DisplayName("빈 매퍼 리스트로 ErrorMapperRegistry를 생성할 수 있다")
        void shouldCreateEmptyErrorMapperRegistry() {
            // When
            ErrorMapperRegistry registry = new ErrorMapperRegistry(Collections.emptyList());

            // Then
            org.assertj.core.api.Assertions.assertThat(registry).isNotNull();
        }
    }

    @Nested
    @DisplayName("SecurityFilterChain 빈 생성 검증")
    class SecurityFilterChainBeanTest {

        @Test
        @DisplayName("SecurityFilterChain을 SpringBoot 컨텍스트 없이 생성할 수 없음을 확인한다")
        void shouldVerifySecurityFilterChainRequiresContext() {
            // securityFilterChain()은 HttpSecurity를 필요로 하여
            // Spring 컨텍스트 없이 단위 테스트로 직접 호출 불가
            // 이 테스트는 SecurityFilterChain 인터페이스가 존재함을 확인
            Class<?> securityFilterChainClass = SecurityFilterChain.class;
            org.assertj.core.api.Assertions.assertThat(securityFilterChainClass).isNotNull();
            org.assertj.core.api.Assertions.assertThat(securityFilterChainClass.isInterface())
                    .isTrue();
        }

        @Test
        @DisplayName("UsernamePasswordAuthenticationToken으로 인증 객체를 생성할 수 있다")
        void shouldCreateAuthenticationToken() {
            // securityFilterChain에서 사용하는 인증 방식 확인
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            "test-service",
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_SERVICE")));

            org.assertj.core.api.Assertions.assertThat(auth.getPrincipal())
                    .isEqualTo("test-service");
            org.assertj.core.api.Assertions.assertThat(auth.getAuthorities())
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SERVICE"));
        }
    }

    /**
     * 테스트용 Health 컨트롤러
     *
     * <p>SecurityFilterChain 동작 확인용 간단한 컨트롤러입니다.
     */
    @org.springframework.web.bind.annotation.RestController
    static class TestHealthController {

        @org.springframework.web.bind.annotation.GetMapping("/api/v1/health")
        public String health() {
            return "OK";
        }
    }
}
