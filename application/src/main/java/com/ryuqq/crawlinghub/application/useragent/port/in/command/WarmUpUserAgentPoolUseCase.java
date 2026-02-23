package com.ryuqq.crawlinghub.application.useragent.port.in.command;

/**
 * UserAgent Pool WarmUp UseCase
 *
 * <p>서버 시작 시 DB의 AVAILABLE UserAgent를 Redis Pool에 로딩합니다.
 *
 * <p>분산 환경(ECS 다중 인스턴스)에서 최초 1회만 실행되도록 보장합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface WarmUpUserAgentPoolUseCase {

    /**
     * Pool WarmUp 실행
     *
     * <p>이미 WarmUp이 완료되었거나 Pool에 데이터가 있으면 skip합니다.
     *
     * @return WarmUp된 UserAgent 수 (skip된 경우 0)
     */
    int execute();
}
