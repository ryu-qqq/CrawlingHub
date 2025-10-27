package com.ryuqq.crawlinghub.adapter.out.eventbridge.config;

import com.ryuqq.crawlinghub.adapter.out.eventbridge.executor.EventBridgeExecutor;
import com.ryuqq.orchestrator.adapter.runner.InlineFastPathRunner;
import com.ryuqq.orchestrator.application.orchestrator.Orchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Orchestrator 설정
 * <p>
 * Orchestrator SDK의 핵심 컴포넌트를 Spring Bean으로 등록합니다.
 * </p>
 * <p>
 * InlineFastPathRunner 방식:
 * <ul>
 *   <li>동기 실행: timeBudget 내 완료 시 즉시 반환</li>
 *   <li>비동기 전환: timeBudget 초과 시 OperationHandle 반환</li>
 *   <li>단일 Executor: EventBridgeExecutor가 operation별 분기 처리</li>
 * </ul>
 * </p>
 * <p>
 * Store 통합:
 * <ul>
 *   <li>ApplicationOrchestratorStore: Application Layer의 Store Adapter</li>
 *   <li>Adapter Pattern으로 Store SPI와 OutboxPort 연결</li>
 *   <li>Persistence Adapter는 Store를 전혀 모름 (헥사고날 아키텍처)</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Configuration
public class OrchestratorConfig {

    private static final Logger LOG = LoggerFactory.getLogger(OrchestratorConfig.class);

    private final EventBridgeExecutor eventBridgeExecutor;

    /**
     * 생성자
     * <p>
     * ApplicationOrchestratorStore는 별도의 @Component로 등록되어 있으며,
     * Orchestrator SDK가 Spring Context에서 자동으로 감지하여 사용합니다.
     * </p>
     *
     * @param eventBridgeExecutor EventBridge Executor
     */
    public OrchestratorConfig(EventBridgeExecutor eventBridgeExecutor) {
        this.eventBridgeExecutor = eventBridgeExecutor;
    }

    /**
     * Orchestrator Bean
     * <p>
     * InlineFastPathRunner를 사용하여 Fast Path 실행을 지원합니다.
     * timeBudget (기본 3초) 내 완료 시 동기 반환, 초과 시 비동기 처리됩니다.
     * </p>
     * <p>
     * Store 통합:
     * <ul>
     *   <li>ApplicationOrchestratorStore가 @Component로 등록됨</li>
     *   <li>Orchestrator SDK가 Spring Context에서 Store Bean 자동 감지</li>
     *   <li>Write-Ahead Log, Finalizer/Reaper 자동 활성화</li>
     * </ul>
     * </p>
     *
     * @return Orchestrator (InlineFastPathRunner 구현체)
     */
    @Bean
    public Orchestrator orchestrator() {
        InlineFastPathRunner runner = new InlineFastPathRunner(eventBridgeExecutor);
        LOG.info("Orchestrator Bean created with InlineFastPathRunner and EventBridgeExecutor");
        LOG.info("Store implementation (ApplicationOrchestratorStore) will be auto-discovered from Spring Context");
        return runner;
    }
}
