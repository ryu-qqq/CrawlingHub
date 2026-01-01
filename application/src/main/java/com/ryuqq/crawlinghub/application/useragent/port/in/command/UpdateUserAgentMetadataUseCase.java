package com.ryuqq.crawlinghub.application.useragent.port.in.command;

import com.ryuqq.crawlinghub.application.useragent.dto.command.UpdateUserAgentMetadataCommand;

/**
 * UserAgent 메타데이터 수정 UseCase
 *
 * <p>기존 UserAgent의 메타데이터를 수정하는 UseCase입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface UpdateUserAgentMetadataUseCase {

    /**
     * UserAgent 메타데이터 수정
     *
     * @param command 수정 Command
     */
    void updateMetadata(UpdateUserAgentMetadataCommand command);
}
