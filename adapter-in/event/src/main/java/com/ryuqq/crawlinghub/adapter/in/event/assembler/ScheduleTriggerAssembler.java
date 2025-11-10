package com.ryuqq.crawlinghub.adapter.in.event.assembler;

import com.ryuqq.crawlinghub.adapter.in.event.dto.request.ScheduleTriggerRequest;
import com.ryuqq.crawlinghub.application.schedule.dto.command.TriggerScheduleCommand;
import org.springframework.stereotype.Component;

/**
 * Schedule Trigger Assembler (Adapter Layer 패턴)
 * <p>
 * EventBridge/SQS Request DTO를 Application Layer Command로 변환합니다.
 * Assembler 패턴을 사용하여 Adapter와 Application Layer 간 결합도를 낮춥니다.
 * </p>
 *
 * <p>변환 흐름:
 * {@code ScheduleTriggerRequest (sellerId)} → {@code TriggerScheduleCommand (sellerId)}
 * </p>
 *
 * @author Sang-won Ryu
 * @since 1.0
 */
@Component
public class ScheduleTriggerAssembler {

    /**
     * Request DTO를 Application Command로 변환.
     * <p>
     * EventBridge에서 전송한 {@code {"sellerId": 1}} 메시지를
     * Application Layer가 이해할 수 있는 Command로 변환합니다.
     * </p>
     *
     * @param request EventBridge/SQS에서 받은 요청
     * @return Application Layer Command
     * @throws IllegalArgumentException request가 null인 경우
     */
    public TriggerScheduleCommand toCommand(ScheduleTriggerRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request는 null일 수 없습니다");
        }

        return new TriggerScheduleCommand(request.sellerId());
    }
}
