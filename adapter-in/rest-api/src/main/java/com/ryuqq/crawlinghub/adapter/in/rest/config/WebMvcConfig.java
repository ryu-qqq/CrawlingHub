package com.ryuqq.crawlinghub.adapter.in.rest.config;

import com.ryuqq.crawlinghub.adapter.in.rest.auth.paths.ApiPaths;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정
 *
 * <p>REST Docs 등 정적 리소스 경로를 Gateway 라우팅 패턴에 맞게 설정합니다.
 *
 * <p><strong>정적 리소스 경로:</strong>
 *
 * <ul>
 *   <li>/api/v1/crawling/docs/** → REST Docs 문서
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * REST Docs 정적 리소스 핸들러 등록
     *
     * <p>Gateway 라우팅 패턴(/api/v1/crawling/**)에 맞춰 REST Docs 경로를 설정합니다.
     *
     * @param registry ResourceHandlerRegistry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // REST Docs: /api/v1/crawling/docs/** → classpath:/static/docs/
        registry.addResourceHandler(ApiPaths.Docs.ALL)
                .addResourceLocations("classpath:/static/docs/");
    }
}
