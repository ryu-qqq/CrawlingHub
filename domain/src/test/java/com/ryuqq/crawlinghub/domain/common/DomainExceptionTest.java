package com.ryuqq.crawlinghub.domain.common;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("DomainException 기본 동작 테스트")
class DomainExceptionTest {

    @Test
    @DisplayName("메시지, 코드, Args를 노출한다")
    void shouldExposeMessageCodeAndArgs() {
        DomainException exception = new StubDomainError("test-message");

        assertEquals("TEST-001", exception.code());
        assertEquals("test-message", exception.message());
        assertEquals(Map.of("key", "value"), exception.args());
    }

    @Test
    @DisplayName("Cause를 포함한 생성자를 지원한다")
    void shouldSupportCauseConstructor() {
        IllegalStateException cause = new IllegalStateException("boom");
        DomainException exception = new StubDomainErrorWithCause("with-cause", cause);

        assertSame(cause, exception.getCause());
    }

    private static final class StubDomainError extends DomainException {

        private StubDomainError(String message) {
            super(message);
        }

        @Override
        public String code() {
            return "TEST-001";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("key", "value");
        }
    }

    private static final class StubDomainErrorWithCause extends DomainException {

        private StubDomainErrorWithCause(String message, Throwable cause) {
            super(message, cause);
        }

        @Override
        public String code() {
            return "TEST-CAUSE-001";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }
}

