package com.ryuqq.crawlinghub.adapter.persistence.jpa.task;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "task_output_data")
public class TaskOutputDataEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "output_data_id")
    private Long outputDataId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "output_key", nullable = false, length = 100)
    private String outputKey;

    @Column(name = "output_value", columnDefinition = "TEXT")
    private String outputValue;

    @Column(name = "output_type", nullable = false, length = 50)
    private String outputType;

    protected TaskOutputDataEntity() {
    }

    private TaskOutputDataEntity(Long outputDataId, Long taskId, String outputKey, String outputValue, String outputType) {
        this.outputDataId = outputDataId;
        this.taskId = taskId;
        this.outputKey = outputKey;
        this.outputValue = outputValue;
        this.outputType = outputType;
    }

    public Long getOutputDataId() {
        return outputDataId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public String getOutputValue() {
        return outputValue;
    }

    public String getOutputType() {
        return outputType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long outputDataId;
        private Long taskId;
        private String outputKey;
        private String outputValue;
        private String outputType;

        public Builder outputDataId(Long outputDataId) {
            this.outputDataId = outputDataId;
            return this;
        }

        public Builder taskId(Long taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder outputKey(String outputKey) {
            this.outputKey = outputKey;
            return this;
        }

        public Builder outputValue(String outputValue) {
            this.outputValue = outputValue;
            return this;
        }

        public Builder outputType(String outputType) {
            this.outputType = outputType;
            return this;
        }

        public TaskOutputDataEntity build() {
            return new TaskOutputDataEntity(outputDataId, taskId, outputKey, outputValue, outputType);
        }
    }

}
