package com.ryuqq.crawlinghub.domain.execution;

public class ExecutionS3Path {

    private final Long s3PathId;
    private final Long executionId;
    private final String pathType;
    private final String s3Bucket;
    private final String s3Key;
    private Long fileSizeBytes;

    private ExecutionS3Path(Long s3PathId, Long executionId, String pathType, String s3Bucket,
                           String s3Key, Long fileSizeBytes) {
        this.s3PathId = s3PathId;
        this.executionId = executionId;
        this.pathType = pathType;
        this.s3Bucket = s3Bucket;
        this.s3Key = s3Key;
        this.fileSizeBytes = fileSizeBytes;
    }

    public void updateFileSize(Long bytes) {
        this.fileSizeBytes = bytes;
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

    public static ExecutionS3Path create(Long executionId, String pathType, String s3Bucket, String s3Key) {
        validateCreate(executionId, pathType, s3Bucket, s3Key);
        return new ExecutionS3Path(null, executionId, pathType, s3Bucket, s3Key, null);
    }

    public static ExecutionS3Path reconstitute(Long s3PathId, Long executionId, String pathType,
                                              String s3Bucket, String s3Key, Long fileSizeBytes) {
        return new ExecutionS3Path(s3PathId, executionId, pathType, s3Bucket, s3Key, fileSizeBytes);
    }

    private static void validateCreate(Long executionId, String pathType, String s3Bucket, String s3Key) {
        if (executionId == null) {
            throw new IllegalArgumentException("Execution ID cannot be null");
        }
        if (pathType == null || pathType.isBlank()) {
            throw new IllegalArgumentException("Path type cannot be null or blank");
        }
        if (s3Bucket == null || s3Bucket.isBlank()) {
            throw new IllegalArgumentException("S3 bucket cannot be null or blank");
        }
        if (s3Key == null || s3Key.isBlank()) {
            throw new IllegalArgumentException("S3 key cannot be null or blank");
        }
    }

}
