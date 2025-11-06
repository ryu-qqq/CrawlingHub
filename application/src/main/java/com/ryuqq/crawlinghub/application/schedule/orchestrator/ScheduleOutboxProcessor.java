package com.ryuqq.crawlinghub.application.schedule.orchestrator;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ryuqq.crawlinghub.application.schedule.dto.response.ScheduleOutcome;
import com.ryuqq.crawlinghub.application.schedule.manager.ScheduleOutboxStateManager;
import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutbox;

/**
 * Schedule Outbox Processor (S2 Phase - Execute)
 *
 * <p>Orchestration Pattern 3-Phase Lifecycle의 S2 Phase를 담당합니다:
 * <ul>
 *   <li>S1 (Accept): Facade가 DB + Outbox 저장 완료</li>
 *   <li>S2 (Execute): **이 Processor가 Outbox를 읽고 EventBridge 호출** ✅</li>
 *   <li>S3 (Finalize): Finalizer가 재시도 및 정리</li>
 * </ul>
 *
 * <p><strong>리팩토링 완료 (2025-11-06):</strong></p>
 * <ul>
 *   <li>✅ ScheduleOutboxStateManager 도입: Outbox 상태 관리 위임</li>
 *   <li>✅ AOP 이슈 해결: manager.processOne() 호출로 @Transactional 정상 작동</li>
 *   <li>✅ Outcome 반환: processOne()이 ScheduleOutcome 반환</li>
 *   <li>✅ SRP 준수: Processor는 스케줄링, Manager는 상태 관리</li>
 * </ul>
 *
 * <p><strong>핵심 원칙:</strong>
 * <ul>
 *   <li>✅ @Scheduled로 주기적 실행 (1초마다)</li>
 *   <li>✅ Outbox에서 PENDING 상태 조회</li>
 *   <li>✅ ScheduleOutboxStateManager.processOne() 호출 (Spring Proxy 통과)</li>
 *   <li>✅ 성공 시 COMPLETED, 실패 시 retryCount++</li>
 *   <li>✅ 각 Outbox 처리는 별도 트랜잭션</li>
 * </ul>
 *
 * <p><strong>왜 @Async가 아니라 @Scheduled인가? ⭐</strong></p>
 *
 * <p>이 질문은 Orchestration Pattern의 핵심 설계 결정입니다. 두 어노테이션의 차이점을 명확히 이해해야 합니다.</p>
 *
 * <h3>1. 실행 방식의 차이</h3>
 * <ul>
 *   <li><strong>@Scheduled</strong>: Spring이 자동으로 주기적으로 메서드를 호출합니다 (Polling 방식)
 *     <ul>
 *       <li>✅ 별도 스레드 풀에서 실행 (TaskScheduler 사용)</li>
 *       <li>✅ 애플리케이션 시작 시 자동으로 스케줄링 시작</li>
 *       <li>✅ 외부 트리거 없이 자체적으로 동작</li>
 *     </ul>
 *   </li>
 *   <li><strong>@Async</strong>: 메서드 호출 시점에 비동기로 실행합니다 (Event-Driven 방식)
 *     <ul>
 *       <li>❌ 명시적인 메서드 호출이 필요</li>
 *       <li>❌ Outbox 저장 후 Processor를 호출해야 함 (강결합)</li>
 *       <li>❌ Outbox 패턴의 장점(느슨한 결합)을 상실</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h3>2. Outbox Pattern과의 관계</h3>
 * <ul>
 *   <li><strong>Outbox Pattern의 핵심</strong>: DB 저장과 외부 시스템 호출의 분리
 *     <ul>
 *       <li>S1: Facade가 DB + Outbox 저장 (트랜잭션 내)</li>
 *       <li>S2: Processor가 Outbox를 읽고 외부 시스템 호출 (별도 프로세스)</li>
 *       <li>S3: Finalizer가 재시도 및 정리</li>
 *     </ul>
 *   </li>
 *   <li><strong>@Scheduled 사용 시</strong>:
 *     <ul>
 *       <li>✅ Facade와 Processor가 완전히 분리 (느슨한 결합)</li>
 *       <li>✅ Facade는 Outbox 저장만 하고 끝 (Processor는 독립적으로 동작)</li>
 *       <li>✅ Processor 장애 시에도 Facade는 정상 동작</li>
 *       <li>✅ 여러 인스턴스에서 동시 실행 가능 (Idempotency 보장 필요)</li>
 *     </ul>
 *   </li>
 *   <li><strong>@Async 사용 시</strong>:
 *     <ul>
 *       <li>❌ Facade가 Outbox 저장 후 Processor를 직접 호출해야 함</li>
 *       <li>❌ Facade와 Processor가 강결합됨</li>
 *       <li>❌ Processor 장애 시 Facade에도 영향 (예외 전파 가능)</li>
 *       <li>❌ Outbox 패턴의 장점(느슨한 결합)을 상실</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h3>3. 실제 사용 시나리오 비교</h3>
 *
 * <p><strong>@Scheduled 방식 (현재 구현):</strong></p>
 * <pre>{@code
 * // S1: Facade
 * @Transactional
 * public void registerSchedule(...) {
 *     schedule = scheduleRepository.save(schedule);
 *     outbox = outboxRepository.save(outbox); // PENDING 상태
 *     // ✅ Processor는 자동으로 주기적으로 Outbox를 읽음
 * }
 *
 * // S2: Processor (별도 프로세스)
 * @Scheduled(fixedDelay = 1000)
 * public void processOutbox() {
 *     List<Outbox> pending = outboxRepository.findByWalStatePending();
 *     // ✅ 주기적으로 Outbox를 읽고 처리
 * }
 * }</pre>
 *
 * <p><strong>@Async 방식 (권장하지 않음):</strong></p>
 * <pre>{@code
 * // S1: Facade
 * @Transactional
 * public void registerSchedule(...) {
 *     schedule = scheduleRepository.save(schedule);
 *     outbox = outboxRepository.save(outbox);
 *     // ❌ Processor를 직접 호출해야 함
 *     outboxProcessor.processOne(outbox); // 강결합!
 * }
 *
 * // S2: Processor
 * @Async
 * public void processOne(Outbox outbox) {
 *     // ❌ Facade가 호출해야 실행됨
 * }
 * }</pre>
 *
 * <h3>4. @Async를 사용해야 하는 경우</h3>
 * <p>@Async는 다음과 같은 경우에 사용합니다:</p>
 * <ul>
 *   <li>✅ <strong>즉시 실행이 필요한 경우</strong>: 메서드 호출 시점에 바로 비동기로 실행</li>
 *   <li>✅ <strong>이벤트 기반 처리</strong>: 특정 이벤트 발생 시 즉시 처리</li>
 *   <li>✅ <strong>Facade와 Processor가 같은 프로세스</strong>: 분리된 프로세스가 아닌 경우</li>
 * </ul>
 *
 * <p><strong>하지만 Outbox Pattern에서는 @Scheduled가 더 적합합니다!</strong></p>
 *
 * <h3>5. 결론</h3>
 * <ul>
 *   <li>✅ <strong>Outbox Pattern은 Polling 방식</strong>이므로 @Scheduled 사용</li>
 *   <li>✅ <strong>느슨한 결합</strong>을 위해 Facade와 Processor 분리</li>
 *   <li>✅ <strong>장애 격리</strong>: Processor 장애가 Facade에 영향 주지 않음</li>
 *   <li>✅ <strong>확장성</strong>: 여러 인스턴스에서 동시 실행 가능</li>
 * </ul>
 *
 * <p><strong>참고 문서:</strong></p>
 * <ul>
 *   <li>Orchestration Pattern 3-Phase Lifecycle: {@code docs/coding_convention/09-orchestration-patterns/}</li>
 *   <li>Outbox Pattern: Transactional Outbox Pattern 문서</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class ScheduleOutboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(ScheduleOutboxProcessor.class);

    private final ScheduleOutboxStateManager outboxStateManager;

    public ScheduleOutboxProcessor(ScheduleOutboxStateManager outboxStateManager) {
        this.outboxStateManager = outboxStateManager;
    }

    /**
     * Outbox 처리 (S2 Phase - Execute)
     *
     * <p><strong>리팩토링 완료:</strong> ScheduleOutboxStateManager.processOne() 호출</p>
     *
     * <p>처리 흐름:
     * <ol>
     *   <li>ScheduleOutboxStateManager.findPendingOutboxes() 조회</li>
     *   <li>각 Outbox에 대해 outboxStateManager.processOne() 호출 (Spring Proxy 통과)</li>
     *   <li>ScheduleOutcome 반환값 처리 (Ok/Fail)</li>
     * </ol>
     *
     * <p>실행 주기: 1초마다 (fixedDelay = 1000ms)
     */
    @Scheduled(fixedDelay = 1000)
    public void processOutbox() {
        List<ScheduleOutbox> pendingOutboxes = outboxStateManager.findPendingOutboxes();

        if (pendingOutboxes.isEmpty()) {
            return; // PENDING 없으면 조용히 종료
        }

        log.info("📋 Outbox 처리 시작: {} 건", pendingOutboxes.size());

        int successCount = 0;
        int failCount = 0;

        for (ScheduleOutbox outbox : pendingOutboxes) {
            try {
                // ✅ Spring Proxy를 통한 호출 → @Transactional 정상 작동!
                ScheduleOutcome outcome = outboxStateManager.processOne(outbox);

                // Outcome 처리
                if (outcome.isSuccess()) {
                    successCount++;
                    log.debug("✅ Outbox 처리 성공: ID={}, Message={}", outbox.getId(), outcome.message());
                } else {
                    failCount++;
                    log.warn("❌ Outbox 처리 실패: ID={}, Error={}", outbox.getId(), outcome.errorCode());
                }
            } catch (Exception e) {
                failCount++;
                log.error("❌ Outbox 처리 예외 (ID: {}): {}", outbox.getId(), e.getMessage(), e);
                // 개별 실패는 기록하고 계속 진행 (다른 Outbox 처리)
            }
        }

        log.info("✅ Outbox 처리 완료: 성공={}, 실패={}", successCount, failCount);
    }

}
