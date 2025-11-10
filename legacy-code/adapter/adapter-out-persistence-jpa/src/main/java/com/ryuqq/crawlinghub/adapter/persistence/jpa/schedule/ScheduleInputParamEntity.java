package com.ryuqq.crawlinghub.adapter.persistence.jpa.schedule;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import com.ryuqq.crawlinghub.domain.common.ParamType;
import jakarta.persistence.*;

@Entity
@Table(name = "schedule_input_param")
public class ScheduleInputParamEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "input_param_id")
    private Long inputParamId;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "param_key", nullable = false, length = 100)
    private String paramKey;

    @Column(name = "param_value", columnDefinition = "TEXT")
    private String paramValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "param_type", nullable = false, length = 50)
    private ParamType paramType;

    protected ScheduleInputParamEntity() {
    }

    private ScheduleInputParamEntity(Long inputParamId, Long scheduleId, String paramKey, String paramValue, ParamType paramType) {
        this.inputParamId = inputParamId;
        this.scheduleId = scheduleId;
        this.paramKey = paramKey;
        this.paramValue = paramValue;
        this.paramType = paramType;
    }

    public Long getInputParamId() {
        return inputParamId;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public String getParamKey() {
        return paramKey;
    }

    public String getParamValue() {
        return paramValue;
    }

    public ParamType getParamType() {
        return paramType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long inputParamId;
        private Long scheduleId;
        private String paramKey;
        private String paramValue;
        private ParamType paramType;

        public Builder inputParamId(Long inputParamId) {
            this.inputParamId = inputParamId;
            return this;
        }

        public Builder scheduleId(Long scheduleId) {
            this.scheduleId = scheduleId;
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

        public Builder paramType(ParamType paramType) {
            this.paramType = paramType;
            return this;
        }

        public ScheduleInputParamEntity build() {
            return new ScheduleInputParamEntity(inputParamId, scheduleId, paramKey, paramValue, paramType);
        }
    }

}
