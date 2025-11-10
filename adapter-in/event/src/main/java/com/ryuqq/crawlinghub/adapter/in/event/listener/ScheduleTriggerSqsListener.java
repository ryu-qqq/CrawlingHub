package com.ryuqq.crawlinghub.adapter.in.event.listener;

import io.awspring.cloud.sqs.annotation.SqsListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.in.event.assembler.ScheduleTriggerAssembler;
import com.ryuqq.crawlinghub.adapter.in.event.dto.request.ScheduleTriggerRequest;
import com.ryuqq.crawlinghub.application.schedule.dto.command.TriggerScheduleCommand;
import com.ryuqq.crawlinghub.application.schedule.port.in.TriggerScheduleUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * EventBridge/SQS Schedule Trigger Listener (Inbound Adapter)
 * <p>
 * EventBridge에서 SQS FIFO Queue로 전송된 메시지를 수신하여 스케줄을 트리거합니다.
 * </p>
 *
 * <p>처리 흐름:
 * <ol>
 *   <li>SQS FIFO Queue에서 메시지 수신 ({@code {"sellerId": 1}})</li>
 *   <li>JSON 역직렬화 → {@code ScheduleTriggerRequest}</li>
 *   <li>Assembler를 통해 {@code TriggerScheduleCommand}로 변환</li>
 *   <li>UseCase 실행 (Application Layer 위임)</li>
 * </ol>
 * </p>
 *
 * <p>FIFO 보장:
 * <ul>
 *   <li>Message Group ID: sellerId 기반 (셀러별 순서 보장)</li>
 *   <li>Message Deduplication: 5분 내 중복 제거</li>
 *   <li>Visibility Timeout: 30초 (처리 시간 고려)</li>
 * </ul>
 * </p>
 *
 * @author Sang-won Ryu
 * @since 1.0
 */
@Component
public class ScheduleTriggerSqsListener {

    private static final Logger log = LoggerFactory.getLogger(ScheduleTriggerSqsListener.class);

    private final TriggerScheduleUseCase triggerScheduleUseCase;
    private final ScheduleTriggerAssembler assembler;
    private final ObjectMapper objectMapper;

    /**
     * Constructor Injection (Pure Java, No Lombok).
     *
     * @param triggerScheduleUseCase Schedule 트리거 UseCase
     * @param assembler              Request → Command 변환 Assembler
     * @param objectMapper           JSON 역직렬화
     */
    public ScheduleTriggerSqsListener(
        TriggerScheduleUseCase triggerScheduleUseCase,
        ScheduleTriggerAssembler assembler,
        ObjectMapper objectMapper
    ) {
        this.triggerScheduleUseCase = triggerScheduleUseCase;
        this.assembler = assembler;
        this.objectMapper = objectMapper;
    }

    /**
     * SQS FIFO Queue에서 메시지 수신 및 처리.
     * <p>
     * EventBridge가 전송한 {@code {"sellerId": 1}} 메시지를 수신하여
     * 해당 셀러의 스케줄을 트리거합니다.
     * </p>
     *
     * <p>에러 처리:
     * <ul>
     *   <li>JSON 파싱 실패: IllegalArgumentException → DLQ로 이동</li>
     *   <li>UseCase 실행 실패: 예외 로깅 후 재시도 (Max 3회)</li>
     * </ul>
     * </p>
     *
     * @param message SQS 메시지 (JSON String)
     * @throws IllegalArgumentException JSON 파싱 실패 시
     */
    @SqsListener("${aws.sqs.schedule-trigger-queue-name}")
    public void handleScheduleTrigger(String message) {
        log.info("SQS 메시지 수신: {}", message);

        try {
            // 1. JSON → ScheduleTriggerRequest
            ScheduleTriggerRequest request = objectMapper.readValue(
                message,
                ScheduleTriggerRequest.class
            );

            log.info("Schedule 트리거 요청 파싱 완료. sellerId: {}", request.sellerId());

            // 2. Request → Command (Assembler 패턴)
            TriggerScheduleCommand command = assembler.toCommand(request);

            // 3. UseCase 실행 (Application Layer 위임)
            triggerScheduleUseCase.execute(command);

            log.info("Schedule 트리거 성공. sellerId: {}", request.sellerId());

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("JSON 파싱 실패. message: {}", message, e);
            throw new IllegalArgumentException("유효하지 않은 JSON 형식입니다: " + message, e);

        } catch (IllegalArgumentException | IllegalStateException e) {
            // Business 예외 (스케줄 없음, 실행 시간 미도래 등)
            log.warn("Schedule 트리거 실패 (Business 규칙): {}", e.getMessage());
            // DLQ로 보내지 않고 무시 (정상적인 비즈니스 흐름)

        } catch (Exception e) {
            // 예상치 못한 시스템 예외
            log.error("Schedule 트리거 실패 (시스템 오류). message: {}", message, e);
            throw e; // 재시도 또는 DLQ로 이동
        }
    }
}
