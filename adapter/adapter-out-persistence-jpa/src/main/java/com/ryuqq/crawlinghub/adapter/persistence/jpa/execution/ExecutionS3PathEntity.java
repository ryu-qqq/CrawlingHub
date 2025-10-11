package com.ryuqq.crawlinghub.adapter.persistence.jpa.execution;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "execution_s3_path")
public class ExecutionS3PathEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "s3_path_id")
    private Long s3PathId;

    @Column(name = "execution_id", nullable = false)
    private Long executionId;

    @Column(name = "path_type", nullable = false, length = 50)
    private String pathType;

    @Column(name = "s3_bucket", nullable = false, length = 200)
    private String s3Bucket;

    @Column(name = "s3_key", nullable = false, length = 500)
    private String s3Key;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    protected ExecutionS3PathEntity() {
    }

    private ExecutionS3PathEntity(Long s3PathId, Long executionId, String pathType, String s3Bucket,
                           String s3Key, Long fileSizeBytes) {
        this.s3PathId = s3PathId;
        this.executionId = executionId;
        this.pathType = pathType;
        this.s3Bucket = s3Bucket;
        this.s3Key = s3Key;
        this.fileSizeBytes = fileSizeBytes;
    }

    public Long getS3PathId() {
        return s3PathId;
    }

    public Long getExecutionId() {
        return executionId;
    }

    public String getPathType() {
        return pathType;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public String getS3Key() {
        return s3Key;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long s3PathId;
        private Long executionId;
        private String pathType;
        private String s3Bucket;
        private String s3Key;
        private Long fileSizeBytes;

        public Builder s3PathId(Long s3PathId) {
            this.s3PathId = s3PathId;
            return this;
        }

        public Builder executionId(Long executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder pathType(String pathType) {
            this.pathType = pathType;
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

        public Builder fileSizeBytes(Long fileSizeBytes) {
            this.fileSizeBytes = fileSizeBytes;
            return this;
        }

        public ExecutionS3PathEntity build() {
            return new ExecutionS3PathEntity(s3PathId, executionId, pathType, s3Bucket, s3Key, fileSizeBytes);
        }
    }

}
