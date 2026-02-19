package com.ryuqq.crawlinghub.bootstrap.config;

import com.ryuqq.authhub.sdk.sync.EndpointSyncClient;
import com.ryuqq.authhub.sdk.sync.EndpointSyncRequest;
import com.ryuqq.authhub.sdk.sync.EndpointSyncRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * EndpointSync 설정.
 *
 * <p>애플리케이션 시작 시 {@code @RequirePermission} 어노테이션이 붙은 엔드포인트를 스캔하여 AuthHub에 동기화합니다.
 *
 * <p>{@code authhub.endpoint-sync.enabled=true}일 때만 활성화됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(AuthHubSyncProperties.class)
@ConditionalOnProperty(prefix = "authhub.endpoint-sync", name = "enabled", havingValue = "true")
public class EndpointSyncConfig {

    private static final Logger log = LoggerFactory.getLogger(EndpointSyncConfig.class);

    private final String baseUrl;
    private final String serviceCode;
    private final String serviceToken;

    public EndpointSyncConfig(AuthHubSyncProperties properties) {
        this.baseUrl = properties.getBaseUrl();
        this.serviceCode = properties.getServiceCode();
        this.serviceToken = properties.getServiceToken();
    }

    @Bean
    public EndpointSyncClient endpointSyncClient() {
        RestTemplate restTemplate = new RestTemplate();
        String syncUrl = baseUrl + "/api/v1/internal/endpoints/sync";

        return (EndpointSyncRequest request) -> {
            log.info(
                    "Endpoint sync started: {} endpoints to sync to AuthHub (url={})",
                    request.endpoints().size(),
                    syncUrl);

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-Service-Name", serviceCode);
                headers.set("X-Service-Token", serviceToken);

                HttpEntity<EndpointSyncRequest> entity = new HttpEntity<>(request, headers);
                restTemplate.postForEntity(syncUrl, entity, Void.class);

                log.info(
                        "Endpoint sync completed: {} endpoints synced to AuthHub",
                        request.endpoints().size());
            } catch (HttpStatusCodeException e) {
                log.error(
                        "Endpoint sync failed: AuthHub returned HTTP {} - {}",
                        e.getStatusCode().value(),
                        e.getResponseBodyAsString(),
                        e);
            } catch (ResourceAccessException e) {
                log.error("Endpoint sync failed: cannot connect to AuthHub (url={})", syncUrl, e);
            } catch (Exception e) {
                log.error("Endpoint sync failed: unexpected error", e);
            }
        };
    }

    @Bean
    public EndpointSyncRunner endpointSyncRunner(
            @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping,
            EndpointSyncClient syncClient) {

        return new EndpointSyncRunner(handlerMapping, syncClient, "crawlinghub", serviceCode, true);
    }
}
