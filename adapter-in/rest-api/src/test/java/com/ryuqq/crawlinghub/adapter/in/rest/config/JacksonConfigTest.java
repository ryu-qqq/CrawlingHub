package com.ryuqq.crawlinghub.adapter.in.rest.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.ProblemDetailJacksonMixin;

/**
 * JacksonConfig 단위 테스트
 *
 * <p>Jackson ObjectMapper 설정의 ProblemDetail Mixin 등록을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@DisplayName("JacksonConfig 단위 테스트")
class JacksonConfigTest {

    @Nested
    @DisplayName("problemDetailMixinCustomizer() 빈 검증")
    class ProblemDetailMixinCustomizerTest {

        @Test
        @DisplayName("Jackson2ObjectMapperBuilderCustomizer 빈을 반환한다")
        void shouldReturnJackson2ObjectMapperBuilderCustomizer() {
            // Given
            JacksonConfig jacksonConfig = new JacksonConfig();

            // When
            Jackson2ObjectMapperBuilderCustomizer customizer =
                    jacksonConfig.problemDetailMixinCustomizer();

            // Then
            assertThat(customizer).isNotNull();
        }

        @Test
        @DisplayName("customizer 적용 시 ProblemDetail에 MixIn이 등록된다")
        void shouldRegisterProblemDetailMixinWhenCustomizerApplied() {
            // Given
            JacksonConfig jacksonConfig = new JacksonConfig();
            Jackson2ObjectMapperBuilderCustomizer customizer =
                    jacksonConfig.problemDetailMixinCustomizer();

            Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

            // When
            customizer.customize(builder);
            ObjectMapper objectMapper = builder.build();

            // Then
            // ProblemDetail에 ProblemDetailJacksonMixin이 등록되었는지 확인
            Class<?> mixInClass = objectMapper.findMixInClassFor(ProblemDetail.class);
            assertThat(mixInClass).isEqualTo(ProblemDetailJacksonMixin.class);
        }
    }
}
