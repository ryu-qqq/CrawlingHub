package com.ryuqq.crawlinghub.adapter.persistence.jpa.task;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import com.ryuqq.crawlinghub.domain.common.ParamType;
import jakarta.persistence.*;

@Entity
@Table(name = "task_input_param")
public class TaskInputParamEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "input_param_id")
    private Long inputParamId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Enumerated(EnumType.STRING)
    @Column(name = "param_type", nullable = false, length = 50)
    private ParamType paramType;

    @Column(name = "param_key", nullable = false, length = 100)
    private String paramKey;

    @Column(name = "param_value", columnDefinition = "TEXT")
    private String paramValue;

    protected TaskInputParamEntity() {
    }

    private TaskInputParamEntity(Long inputParamId, Long taskId, ParamType paramType, String paramKey, String paramValue) {
        this.inputParamId = inputParamId;
        this.taskId = taskId;
        this.paramType = paramType;
        this.paramKey = paramKey;
        this.paramValue = paramValue;
    }

    public Long getInputParamId() {
        return inputParamId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public ParamType getParamType() {
        return paramType;
    }

    public String getParamKey() {
        return paramKey;
    }

    public String getParamValue() {
        return paramValue;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long inputParamId;
        private Long taskId;
        private ParamType paramType;
        private String paramKey;
        private String paramValue;

        public Builder inputParamId(Long inputParamId) {
            this.inputParamId = inputParamId;
            return this;
        }

        public Builder taskId(Long taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder paramType(ParamType paramType) {
            this.paramType = paramType;
            return this;
        }

        public Builder paramKey(String paramKey) {
            this.paramKey = paramKey;
            return this;
        }

        public Builder paramValue(String paramValue) {
            this.paramValue = paramValue;
            return this;
        }

        public TaskInputParamEntity build() {
            return new TaskInputParamEntity(inputParamId, taskId, paramType, paramKey, paramValue);
        }
    }

}
