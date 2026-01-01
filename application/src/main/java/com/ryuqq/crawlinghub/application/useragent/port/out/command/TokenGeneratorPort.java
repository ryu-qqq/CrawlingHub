package com.ryuqq.crawlinghub.application.useragent.port.out.command;

import com.ryuqq.crawlinghub.domain.useragent.vo.Token;

/**
 * Token 생성 Port
 *
 * <p>UserAgent 식별을 위한 암호화된 토큰을 생성하는 Port입니다.
 *
 * <p><strong>구현 요구사항</strong>:
 *
 * <ul>
 *   <li>AES-256 암호화 토큰 생성
 *   <li>Base64 인코딩
 *   <li>최소 44자 이상의 토큰 길이 보장
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface TokenGeneratorPort {

    /**
     * 새로운 암호화 토큰 생성
     *
     * @return 생성된 Token
     */
    Token generate();
}
