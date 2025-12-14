package com.ryuqq.crawlinghub.adapter.in.rest.auth.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("rest-api")
@Tag("security")
@DisplayName("SecurityContextHolder 단위 테스트")
class SecurityContextHolderTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("getContext() 메서드는")
    class GetContextMethod {

        @Test
        @DisplayName("컨텍스트가 설정되지 않으면 anonymous를 반환한다")
        void shouldReturnAnonymousWhenNoContextSet() {
            // When
            SecurityContext context = SecurityContextHolder.getContext();

            // Then
            assertThat(context).isNotNull();
            assertThat(context.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("설정된 컨텍스트를 반환한다")
        void shouldReturnSetContext() {
            // Given
            SecurityContext expected =
                    SecurityContext.builder().userId("user-123").tenantId("tenant-456").build();
            SecurityContextHolder.setContext(expected);

            // When
            SecurityContext actual = SecurityContextHolder.getContext();

            // Then
            assertThat(actual).isSameAs(expected);
        }
    }

    @Nested
    @DisplayName("setContext() 메서드는")
    class SetContextMethod {

        @Test
        @DisplayName("컨텍스트를 설정한다")
        void shouldSetContext() {
            // Given
            SecurityContext context = SecurityContext.builder().userId("user-123").build();

            // When
            SecurityContextHolder.setContext(context);

            // Then
            assertThat(SecurityContextHolder.getContext()).isSameAs(context);
        }

        @Test
        @DisplayName("기존 컨텍스트를 덮어쓴다")
        void shouldOverwriteExistingContext() {
            // Given
            SecurityContext first = SecurityContext.builder().userId("user-1").build();
            SecurityContext second = SecurityContext.builder().userId("user-2").build();
            SecurityContextHolder.setContext(first);

            // When
            SecurityContextHolder.setContext(second);

            // Then
            assertThat(SecurityContextHolder.getContext()).isSameAs(second);
        }
    }

    @Nested
    @DisplayName("clearContext() 메서드는")
    class ClearContextMethod {

        @Test
        @DisplayName("컨텍스트를 제거하면 anonymous를 반환한다")
        void shouldClearContextAndReturnAnonymous() {
            // Given
            SecurityContext context = SecurityContext.builder().userId("user-123").build();
            SecurityContextHolder.setContext(context);

            // When
            SecurityContextHolder.clearContext();

            // Then
            assertThat(SecurityContextHolder.getContext().isAuthenticated()).isFalse();
        }
    }

    @Nested
    @DisplayName("isAuthenticated() 메서드는")
    class IsAuthenticatedMethod {

        @Test
        @DisplayName("인증된 컨텍스트면 true를 반환한다")
        void shouldReturnTrueWhenAuthenticated() {
            // Given
            SecurityContext context = SecurityContext.builder().userId("user-123").build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(SecurityContextHolder.isAuthenticated()).isTrue();
        }

        @Test
        @DisplayName("미인증 상태면 false를 반환한다")
        void shouldReturnFalseWhenNotAuthenticated() {
            // When & Then
            assertThat(SecurityContextHolder.isAuthenticated()).isFalse();
        }
    }

    @Nested
    @DisplayName("getCurrentUserId() 메서드는")
    class GetCurrentUserIdMethod {

        @Test
        @DisplayName("현재 사용자 ID를 반환한다")
        void shouldReturnCurrentUserId() {
            // Given
            SecurityContext context = SecurityContext.builder().userId("user-123").build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(SecurityContextHolder.getCurrentUserId()).isEqualTo("user-123");
        }

        @Test
        @DisplayName("미인증 상태면 null을 반환한다")
        void shouldReturnNullWhenNotAuthenticated() {
            // When & Then
            assertThat(SecurityContextHolder.getCurrentUserId()).isNull();
        }
    }

    @Nested
    @DisplayName("getCurrentTenantId() 메서드는")
    class GetCurrentTenantIdMethod {

        @Test
        @DisplayName("현재 테넌트 ID를 반환한다")
        void shouldReturnCurrentTenantId() {
            // Given
            SecurityContext context =
                    SecurityContext.builder().userId("user-123").tenantId("tenant-456").build();
            SecurityContextHolder.setContext(context);

            // When & Then
            assertThat(SecurityContextHolder.getCurrentTenantId()).isEqualTo("tenant-456");
        }

        @Test
        @DisplayName("미인증 상태면 null을 반환한다")
        void shouldReturnNullWhenNotAuthenticated() {
            // When & Then
            assertThat(SecurityContextHolder.getCurrentTenantId()).isNull();
        }
    }

    @Nested
    @DisplayName("ThreadLocal 격리 테스트")
    class ThreadLocalIsolation {

        @Test
        @DisplayName("다른 스레드의 컨텍스트에 영향을 주지 않는다")
        void shouldIsolateContextBetweenThreads() throws InterruptedException {
            // Given
            String mainThreadUserId = "main-user";
            String childThreadUserId = "child-user";

            SecurityContext mainContext =
                    SecurityContext.builder().userId(mainThreadUserId).build();
            SecurityContextHolder.setContext(mainContext);

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<String> childUserId = new AtomicReference<>();
            AtomicReference<String> childAuthenticatedBeforeSet = new AtomicReference<>();

            // When
            Thread childThread =
                    new Thread(
                            () -> {
                                // 자식 스레드는 처음에 anonymous여야 함
                                childAuthenticatedBeforeSet.set(
                                        SecurityContextHolder.isAuthenticated()
                                                ? "authenticated"
                                                : "anonymous");

                                SecurityContext childContext =
                                        SecurityContext.builder().userId(childThreadUserId).build();
                                SecurityContextHolder.setContext(childContext);
                                childUserId.set(SecurityContextHolder.getCurrentUserId());
                                latch.countDown();
                            });
            childThread.start();
            latch.await();

            // Then
            assertThat(childAuthenticatedBeforeSet.get()).isEqualTo("anonymous");
            assertThat(childUserId.get()).isEqualTo(childThreadUserId);
            assertThat(SecurityContextHolder.getCurrentUserId()).isEqualTo(mainThreadUserId);
        }
    }
}
