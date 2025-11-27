package com.ryuqq.crawlinghub.adapter.in.rest.config;

import com.ryuqq.crawlinghub.adapter.in.rest.common.error.ErrorMapperRegistry;
import java.util.Collections;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 테스트용 Spring Boot 설정 클래스
 *
 * <p>@WebMvcTest에서 사용하기 위한 최소한의 Spring Boot 설정입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.ryuqq.crawlinghub.adapter.in.rest")
public class TestConfiguration {

    /**
     * 테스트용 ErrorMapperRegistry 빈
     *
     * <p>GlobalExceptionHandler에서 필요로 하는 빈입니다. 테스트 환경에서는 빈 매퍼 리스트를 사용합니다.
     *
     * @return 빈 매퍼 리스트를 가진 ErrorMapperRegistry
     */
    @Bean
    public ErrorMapperRegistry errorMapperRegistry() {
        return new ErrorMapperRegistry(Collections.emptyList());
    }
}
