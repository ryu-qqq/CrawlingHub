package com.ryuqq.crawlinghub.application.image.port.in.command;

import com.ryuqq.crawlinghub.application.image.dto.command.ImageUploadWebhookCommand;

/**
 * 이미지 업로드 웹훅 처리 UseCase (Port In - Command)
 *
 * <p>Fileflow에서 이미지 업로드 완료/실패 시 웹훅을 수신하여 Outbox 상태를 업데이트합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface HandleImageUploadWebhookUseCase {

    /**
     * 이미지 업로드 웹훅 처리
     *
     * @param command 웹훅 Command
     */
    void execute(ImageUploadWebhookCommand command);
}
