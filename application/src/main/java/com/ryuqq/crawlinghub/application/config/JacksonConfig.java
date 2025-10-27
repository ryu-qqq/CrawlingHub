package com.ryuqq.crawlinghub.application.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson ObjectMapper 설정
 * <p>
 * Application Layer에서 사용하는 JSON 직렬화/역직렬화 설정입니다.
 * </p>
 * <p>
 * 주요 설정:
 * <ul>
 *   <li>JavaTimeModule: Java 8 날짜/시간 API 지원</li>
 *   <li>WRITE_DATES_AS_TIMESTAMPS: 날짜를 타임스탬프가 아닌 ISO-8601 형식으로</li>
 *   <li>FAIL_ON_UNKNOWN_PROPERTIES: 알 수 없는 필드 무시 (유연성)</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Configuration
public class JacksonConfig {

    /**
     * ObjectMapper 빈 생성
     * <p>
     * Orchestrator Payload 직렬화 및 기타 JSON 변환에 사용됩니다.
     * </p>
     *
     * @return 설정된 ObjectMapper 인스턴스
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Java 8 날짜/시간 API 지원
        mapper.registerModule(new JavaTimeModule());

        // 날짜를 ISO-8601 형식으로 직렬화 (타임스탬프 X)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 알 수 없는 필드 무시 (역직렬화 시 유연성)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }
}
