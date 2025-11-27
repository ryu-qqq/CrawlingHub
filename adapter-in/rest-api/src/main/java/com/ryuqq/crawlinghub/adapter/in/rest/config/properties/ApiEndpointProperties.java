package com.ryuqq.crawlinghub.adapter.in.rest.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * API 엔드포인트 경로 설정 Properties
 *
 * <p>REST API 엔드포인트 경로를 application.yml에서 중앙 관리합니다.
 *
 * <p><strong>주요 기능:</strong>
 *
 * <ul>
 *   <li>Bounded Context별 엔드포인트 구조화
 *   <li>버전 관리 용이 (v1 → v2 마이그레이션)
 *   <li>환경별 엔드포인트 변경 가능
 * </ul>
 *
 * <p><strong>설정 예시 (application.yml):</strong>
 *
 * <pre>{@code
 * api:
 *   endpoints:
 *     base-v1: /api/v1
 *     seller:
 *       base: /sellers
 *       by-id: /{id}
 *       status: /{id}/status
 * }</pre>
 *
 * <p><strong>사용 방법:</strong>
 *
 * <pre>{@code
 * @RestController
 * @RequestMapping("${api.endpoints.base-v1}${api.endpoints.seller.base}")
 * public class SellerController {
 *     // GET /api/v1/sellers/{id}
 *     @GetMapping("${api.endpoints.seller.by-id}")
 *     public ResponseEntity<?> getSeller(@PathVariable Long id) { ... }
 * }
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "api.endpoints")
public class ApiEndpointProperties {

    /** API v1 베이스 경로 (기본값: /api/v1) */
    private String baseV1 = "/api/v1";

    /** Seller 도메인 엔드포인트 설정 */
    private SellerEndpoints seller = new SellerEndpoints();

    /** Schedule 도메인 엔드포인트 설정 */
    private ScheduleEndpoints schedule = new ScheduleEndpoints();

    /** Task 도메인 엔드포인트 설정 */
    private TaskEndpoints task = new TaskEndpoints();

    /** UserAgent 도메인 엔드포인트 설정 */
    private UserAgentEndpoints userAgent = new UserAgentEndpoints();

    /** Execution 도메인 엔드포인트 설정 */
    private ExecutionEndpoints execution = new ExecutionEndpoints();

    /** Seller 도메인 엔드포인트 경로 */
    public static class SellerEndpoints {
        /** Seller 기본 경로 (기본값: /sellers) */
        private String base = "/sellers";

        /** Seller ID 조회 경로 (기본값: /{id}) */
        private String byId = "/{id}";

        /** Seller 상태 변경 경로 (기본값: /{id}/status) */
        private String status = "/{id}/status";

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getById() {
            return byId;
        }

        public void setById(String byId) {
            this.byId = byId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    /** Schedule 도메인 엔드포인트 경로 */
    public static class ScheduleEndpoints {
        /** Schedule 기본 경로 (기본값: /schedules) */
        private String base = "/schedules";

        /** Schedule ID 조회 경로 (기본값: /{id}) */
        private String byId = "/{id}";

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getById() {
            return byId;
        }

        public void setById(String byId) {
            this.byId = byId;
        }
    }

    /** Task 도메인 엔드포인트 경로 */
    public static class TaskEndpoints {
        /** Task 기본 경로 (기본값: /tasks) */
        private String base = "/tasks";

        /** Task ID 조회 경로 (기본값: /{id}) */
        private String byId = "/{id}";

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getById() {
            return byId;
        }

        public void setById(String byId) {
            this.byId = byId;
        }
    }

    /** UserAgent 도메인 엔드포인트 경로 */
    public static class UserAgentEndpoints {
        /** UserAgent 기본 경로 (기본값: /user-agents) */
        private String base = "/user-agents";

        /** Pool 상태 조회 경로 (기본값: /pool-status) */
        private String poolStatus = "/pool-status";

        /** UserAgent 복구 경로 (기본값: /recover) */
        private String recover = "/recover";

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getPoolStatus() {
            return poolStatus;
        }

        public void setPoolStatus(String poolStatus) {
            this.poolStatus = poolStatus;
        }

        public String getRecover() {
            return recover;
        }

        public void setRecover(String recover) {
            this.recover = recover;
        }
    }

    /** Execution 도메인 엔드포인트 경로 */
    public static class ExecutionEndpoints {
        /** Execution 기본 경로 (기본값: /executions) */
        private String base = "/executions";

        /** Execution ID 조회 경로 (기본값: /{id}) */
        private String byId = "/{id}";

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getById() {
            return byId;
        }

        public void setById(String byId) {
            this.byId = byId;
        }
    }

    public String getBaseV1() {
        return baseV1;
    }

    public void setBaseV1(String baseV1) {
        this.baseV1 = baseV1;
    }

    public SellerEndpoints getSeller() {
        return seller;
    }

    public void setSeller(SellerEndpoints seller) {
        this.seller = seller;
    }

    public ScheduleEndpoints getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduleEndpoints schedule) {
        this.schedule = schedule;
    }

    public TaskEndpoints getTask() {
        return task;
    }

    public void setTask(TaskEndpoints task) {
        this.task = task;
    }

    public UserAgentEndpoints getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(UserAgentEndpoints userAgent) {
        this.userAgent = userAgent;
    }

    public ExecutionEndpoints getExecution() {
        return execution;
    }

    public void setExecution(ExecutionEndpoints execution) {
        this.execution = execution;
    }
}
