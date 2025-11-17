package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.crawler.vo.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenTest {

    @Test
    void shouldCreateTokenWithValidValue() {
        // Given
        String validToken = "mustit_token_abc123";

        // When
        Token token = new Token(validToken);

        // Then
        assertThat(token.value()).isEqualTo(validToken);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void shouldThrowExceptionWhenTokenIsBlank(String invalidToken) {
        // When & Then
        assertThatThrownBy(() -> new Token(invalidToken))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Token은 비어있을 수 없습니다");
    }
}
