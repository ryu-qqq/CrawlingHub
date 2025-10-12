package com.ryuqq.crawlinghub.adapter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for AWS EventBridge.
 * Type-safe configuration management instead of @Value annotations
 *
 * @author crawlinghub (noreply@crawlinghub.com)
 */
@ConfigurationProperties(prefix = "aws.eventbridge")
public class EventBridgeProperties {

    /**
     * Whether EventBridge integration is enabled
     * Default: true
     */
    private boolean enabled = true;

    /**
     * AWS EventBridge event bus name
     * Default: "default"
     */
    private String eventBusName = "default";

    /**
     * Target ARN for EventBridge rules (Lambda, SQS, etc.)
     */
    private String targetArn;

    /**
     * AWS region for EventBridge client
     * Default: "ap-northeast-2" (Seoul)
     */
    private String region = "ap-northeast-2";

    /**
     * AWS credentials configuration
     */
    private Credentials credentials = new Credentials();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEventBusName() {
        return eventBusName;
    }

    public void setEventBusName(String eventBusName) {
        this.eventBusName = eventBusName;
    }

    public String getTargetArn() {
        return targetArn;
    }

    public void setTargetArn(String targetArn) {
        this.targetArn = targetArn;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    /**
     * AWS credentials configuration
     */
    public static class Credentials {
        /**
         * AWS access key (optional - uses default credential provider chain if not set)
         */
        private String accessKey;

        /**
         * AWS secret key (optional - uses default credential provider chain if not set)
         */
        private String secretKey;

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        /**
         * Check if explicit credentials are configured.
         *
         * @return true if both access key and secret key are configured
         */
        public boolean hasExplicitCredentials() {
            return accessKey != null && !accessKey.isBlank()
                    && secretKey != null && !secretKey.isBlank();
        }
    }
}
