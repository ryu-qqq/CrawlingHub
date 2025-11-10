package com.ryuqq.crawlinghub.adapter.out.aws.sqs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * AWS SQS Configuration
 *
 * <p>SqsClient Bean 등록 및 AWS 인증 정보 관리
 *
 * <p>필수 환경 변수:
 * <ul>
 *   <li>aws.region: AWS Region (예: ap-northeast-2)</li>
 *   <li>aws.access-key-id: AWS Access Key ID</li>
 *   <li>aws.secret-access-key: AWS Secret Access Key</li>
 * </ul>
 *
 * <p>참고:
 * - 프로덕션 환경에서는 IAM Role 사용 권장 (credentials 노출 방지)
 * - 로컬/개발 환경에서만 Access Key 사용
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Configuration
public class AwsSqsConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.access-key-id:#{null}}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:#{null}}")
    private String secretAccessKey;

    /**
     * SqsClient Bean 생성
     *
     * <p>AWS SDK v2 사용
     *
     * <p>인증 방식:
     * - Access Key가 있으면: StaticCredentialsProvider 사용
     * - Access Key가 없으면: DefaultCredentialsProvider 사용 (IAM Role)
     *
     * @return SqsClient 인스턴스
     */
    @Bean
    public SqsClient sqsClient() {
        if (accessKeyId != null && secretAccessKey != null) {
            // 로컬/개발 환경: Access Key 사용
            AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
            return SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
        } else {
            // 프로덕션 환경: IAM Role 사용 (DefaultCredentialsProvider)
            return SqsClient.builder()
                .region(Region.of(region))
                .build();
        }
    }
}
