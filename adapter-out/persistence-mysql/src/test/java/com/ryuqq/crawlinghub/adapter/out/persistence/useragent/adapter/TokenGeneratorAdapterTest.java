package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.useragent.vo.Token;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * TokenGeneratorAdapter 단위 테스트
 *
 * <p>AES-256-GCM 암호화 토큰 생성 로직 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("TokenGeneratorAdapter 단위 테스트")
class TokenGeneratorAdapterTest {

    private TokenGeneratorAdapter tokenGeneratorAdapter;

    @BeforeEach
    void setUp() {
        tokenGeneratorAdapter = new TokenGeneratorAdapter();
    }

    @Test
    @DisplayName("성공 - 토큰 생성 시 Token 객체 반환")
    void shouldGenerateToken() {
        // When
        Token token = tokenGeneratorAdapter.generate();

        // Then
        assertThat(token).isNotNull();
        assertThat(token.encryptedValue()).isNotNull();
    }

    @Test
    @DisplayName("성공 - 생성된 토큰은 Base64 형식")
    void shouldGenerateBase64EncodedToken() {
        // When
        Token token = tokenGeneratorAdapter.generate();

        // Then
        String encodedValue = token.encryptedValue();
        assertThat(encodedValue).matches("^[A-Za-z0-9+/=]+$");
    }

    @Test
    @DisplayName("성공 - 토큰 길이는 최소 44자 이상")
    void shouldGenerateTokenWithMinimumLength() {
        // When
        Token token = tokenGeneratorAdapter.generate();

        // Then
        assertThat(token.encryptedValue().length()).isGreaterThanOrEqualTo(44);
    }

    @Test
    @DisplayName("성공 - 각 호출마다 고유한 토큰 생성")
    void shouldGenerateUniqueTokensOnEachCall() {
        // Given
        int tokenCount = 100;
        Set<String> generatedTokens = new HashSet<>();

        // When
        for (int i = 0; i < tokenCount; i++) {
            Token token = tokenGeneratorAdapter.generate();
            generatedTokens.add(token.encryptedValue());
        }

        // Then - 모든 토큰이 고유해야 함
        assertThat(generatedTokens).hasSize(tokenCount);
    }

    @Test
    @DisplayName("성공 - 여러 번 호출해도 항상 유효한 토큰 생성")
    void shouldGenerateValidTokensConsistently() {
        // When & Then
        for (int i = 0; i < 10; i++) {
            Token token = tokenGeneratorAdapter.generate();

            assertThat(token).isNotNull();
            assertThat(token.encryptedValue())
                    .isNotNull()
                    .isNotBlank()
                    .hasSizeGreaterThanOrEqualTo(44);
        }
    }

    @Test
    @DisplayName("성공 - 토큰에 유효한 Base64 문자만 포함")
    void shouldContainOnlyValidBase64Characters() {
        // When
        Token token = tokenGeneratorAdapter.generate();

        // Then
        String value = token.encryptedValue();
        for (char c : value.toCharArray()) {
            assertThat(isValidBase64Character(c)).as("Invalid character: %c", c).isTrue();
        }
    }

    @Test
    @DisplayName("성공 - TokenGenerationException 생성 및 메시지 전달")
    void shouldCreateTokenGenerationExceptionWithMessage() {
        // Given
        String message = "토큰 생성 실패";
        Throwable cause = new RuntimeException("원인");

        // When
        TokenGeneratorAdapter.TokenGenerationException exception =
                new TokenGeneratorAdapter.TokenGenerationException(message, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("성공 - TokenGenerationException은 RuntimeException 상속")
    void shouldTokenGenerationExceptionBeRuntimeException() {
        // Given
        TokenGeneratorAdapter.TokenGenerationException exception =
                new TokenGeneratorAdapter.TokenGenerationException("test", new RuntimeException());

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    /**
     * Base64 문자 유효성 검사
     *
     * @param c 검사할 문자
     * @return 유효한 Base64 문자이면 true
     */
    private boolean isValidBase64Character(char c) {
        return (c >= 'A' && c <= 'Z')
                || (c >= 'a' && c <= 'z')
                || (c >= '0' && c <= '9')
                || c == '+'
                || c == '/'
                || c == '=';
    }
}
