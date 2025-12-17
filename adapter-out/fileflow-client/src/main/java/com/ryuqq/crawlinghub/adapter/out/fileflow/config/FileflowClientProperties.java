package com.ryuqq.crawlinghub.adapter.out.fileflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Fileflow Client 설정 Properties
 *
 * <p>Fileflow 서버 연동을 위한 설정 값들을 관리합니다.
 *
 * <p>application.yml 설정 예시:
 *
 * <pre>
 * fileflow:
 *   client:
 *     base-url: https://fileflow.example.com
 *     callback-url: https://myservice.com/api/v1/webhook/image-upload
 *     connect-timeout: 5000
 *     read-timeout: 30000
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "fileflow.client")
public class FileflowClientProperties {

    private String baseUrl;
    private String callbackUrl;
    private int connectTimeout = 5000;
    private int readTimeout = 30000;
    private String serviceToken;
    private String serviceName = "crawlinghub";

    public FileflowClientProperties() {}

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getServiceToken() {
        return serviceToken;
    }

    public void setServiceToken(String serviceToken) {
        this.serviceToken = serviceToken;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Service Token이 설정되어 있는지 확인합니다.
     *
     * @return Service Token이 설정되어 있으면 true
     */
    public boolean hasServiceToken() {
        return serviceToken != null && !serviceToken.isBlank();
    }

    /**
     * 외부 다운로드 API 엔드포인트 URL 반환
     *
     * @return 외부 다운로드 API URL
     */
    public String getExternalDownloadUrl() {
        return baseUrl + "/api/v1/file/external-downloads";
    }

    /**
     * 외부 다운로드 상태 조회 API 엔드포인트 URL 반환
     *
     * @param downloadId 다운로드 ID
     * @return 상태 조회 API URL
     */
    public String getExternalDownloadStatusUrl(String downloadId) {
        return baseUrl + "/api/v1/file/external-downloads/" + downloadId;
    }
}
