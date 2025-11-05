package com.ryuqq.crawlinghub.application.task.port.in;

import com.ryuqq.crawlinghub.application.task.assembler.command.InitiateCrawlingCommand;

/**
 * 크롤링 시작 UseCase
 *
 * <p>초기 미니샵 태스크를 생성하고 Outbox에 저장합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface InitiateCrawlingUseCase {

    /**
     * 크롤링 시작
     *
     * @param command 시작할 셀러 정보
     */
    void execute(InitiateCrawlingCommand command);
}
