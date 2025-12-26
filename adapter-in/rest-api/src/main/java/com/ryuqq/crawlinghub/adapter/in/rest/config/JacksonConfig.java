package com.ryuqq.crawlinghub.adapter.in.rest.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.ProblemDetailJacksonMixin;

/**
 * Jackson ObjectMapper 설정
 *
 * <p>RFC 7807 ProblemDetail의 extension properties가 루트 레벨에 평탄화되도록 ProblemDetailJacksonMixin을 등록합니다.
 *
 * <p><strong>Mixin 적용 전:</strong>
 *
 * <pre>{@code
 * {
 *   "type": "about:blank",
 *   "status": 400,
 *   "properties": {
 *     "code": "VALIDATION_FAILED",
 *     "timestamp": "..."
 *   }
 * }
 * }</pre>
 *
 * <p><strong>Mixin 적용 후:</strong>
 *
 * <pre>{@code
 * {
 *   "type": "about:blank",
 *   "status": 400,
 *   "code": "VALIDATION_FAILED",
 *   "timestamp": "..."
 * }
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
public class JacksonConfig {

    /**
     * ProblemDetail Mixin 등록
     *
     * <p>Spring Framework의 ProblemDetailJacksonMixin을 ObjectMapper에 등록하여 extension
     * properties(@JsonAnyGetter/@JsonAnySetter)가 루트 레벨에 직렬화되도록 합니다.
     *
     * @return Jackson2ObjectMapperBuilderCustomizer
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer problemDetailMixinCustomizer() {
        return builder -> builder.mixIn(ProblemDetail.class, ProblemDetailJacksonMixin.class);
    }
}
