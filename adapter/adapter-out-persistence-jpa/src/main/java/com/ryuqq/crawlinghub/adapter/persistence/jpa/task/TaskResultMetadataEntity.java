package com.ryuqq.crawlinghub.adapter.persistence.jpa.task;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "task_result_metadata")
public class TaskResultMetadataEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metadata_id")
    private Long metadataId;

    @Column(name = "task_id", nullable = false, unique = true)
    private Long taskId;

    @Column(name = "api_response_time_ms")
    private Long apiResponseTimeMs;

    @Column(name = "api_status_code")
    private Integer apiStatusCode;

    @Column(name = "data_size_bytes")
    private Long dataSizeBytes;

    @Column(name = "items_count")
    private Long itemsCount;

    @Column(name = "s3_bucket", length = 200)
    private String s3Bucket;

    @Column(name = "s3_key", length = 500)
    private String s3Key;

    protected TaskResultMetadataEntity() {
    }

    private TaskResultMetadataEntity(Long metadataId, Long taskId, Long apiResponseTimeMs, Integer apiStatusCode,
                              Long dataSizeBytes, Long itemsCount, String s3Bucket, String s3Key) {
        this.metadataId = metadataId;
        this.taskId = taskId;
        this.apiResponseTimeMs = apiResponseTimeMs;
        this.apiStatusCode = apiStatusCode;
        this.dataSizeBytes = dataSizeBytes;
        this.itemsCount = itemsCount;
        this.s3Bucket = s3Bucket;
        this.s3Key = s3Key;
    }

    public Long getMetadataId() {
        return metadataId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public Long getApiResponseTimeMs() {
        return apiResponseTimeMs;
    }

    public Integer getApiStatusCode() {
        return apiStatusCode;
    }

    public Long getDataSizeBytes() {
        return dataSizeBytes;
    }

    public Long getItemsCount() {
        return itemsCount;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void updateS3Location(String bucket, String key) {
        this.s3Bucket = bucket;
        this.s3Key = key;
    }

    public void updateDataMetrics(Long sizeBytes, Long count) {
        this.dataSizeBytes = sizeBytes;
        this.itemsCount = count;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long metadataId;
        private Long taskId;
        private Long apiResponseTimeMs;
        private Integer apiStatusCode;
        private Long dataSizeBytes;
        private Long itemsCount;
        private String s3Bucket;
        private String s3Key;

        public Builder metadataId(Long metadataId) {
            this.metadataId = metadataId;
            return this;
        }

        public Builder taskId(Long taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder apiResponseTimeMs(Long apiResponseTimeMs) {
            this.apiResponseTimeMs = apiResponseTimeMs;
            return this;
        }

        public Builder apiStatusCode(Integer apiStatusCode) {
            this.apiStatusCode = apiStatusCode;
            return this;
        }

        public Builder dataSizeBytes(Long dataSizeBytes) {
            this.dataSizeBytes = dataSizeBytes;
            return this;
        }

        public Builder itemsCount(Long itemsCount) {
            this.itemsCount = itemsCount;
            return this;
        }

        public Builder s3Bucket(String s3Bucket) {
            this.s3Bucket = s3Bucket;
            return this;
        }

        public Builder s3Key(String s3Key) {
            this.s3Key = s3Key;
            return this;
        }

        public TaskResultMetadataEntity build() {
            return new TaskResultMetadataEntity(metadataId, taskId, apiResponseTimeMs, apiStatusCode,
                                         dataSizeBytes, itemsCount, s3Bucket, s3Key);
        }
    }

}
