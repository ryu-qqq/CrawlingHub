package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.adapter;

import com.ryuqq.crawlinghub.application.useragent.port.out.command.TokenGeneratorPort;
import com.ryuqq.crawlinghub.domain.useragent.vo.Token;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import org.springframework.stereotype.Component;

/**
 * TokenGeneratorAdapter - Token 생성 Adapter
 *
 * <p>{@link TokenGeneratorPort} 구현체로, AES-256-GCM 암호화 토큰을 생성합니다.
 *
 * <p><strong>암호화 스펙:</strong>
 *
 * <ul>
 *   <li>알고리즘: AES-256-GCM (Authenticated Encryption)
 *   <li>키 길이: 256 bits
 *   <li>IV 길이: 96 bits (GCM 권장)
 *   <li>인증 태그 길이: 128 bits
 *   <li>출력 형식: Base64 인코딩
 * </ul>
 *
 * <p><strong>토큰 구조:</strong> IV (12 bytes) + 암호화된 데이터 + Auth Tag (16 bytes)
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class TokenGeneratorAdapter implements TokenGeneratorPort {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int TIMESTAMP_BYTES = 8;
    private static final int RANDOM_BYTES = 16;

    private final SecureRandom secureRandom;

    public TokenGeneratorAdapter() {
        this.secureRandom = new SecureRandom();
    }

    /**
     * 새로운 암호화 토큰 생성
     *
     * <p>각 호출마다 새로운 랜덤 키와 IV를 사용하여 고유한 토큰을 생성합니다.
     *
     * <p><strong>토큰 생성 과정:</strong>
     *
     * <ol>
     *   <li>현재 타임스탬프 (8 bytes) + 랜덤 데이터 (16 bytes)로 평문 생성
     *   <li>AES-256-GCM으로 암호화
     *   <li>IV + 암호문 + Auth Tag를 Base64 인코딩
     * </ol>
     *
     * @return 생성된 Token (최소 44자 Base64 문자열)
     */
    @Override
    public Token generate() {
        try {
            SecretKey secretKey = generateSecretKey();
            byte[] iv = generateIv();
            byte[] plaintext = generatePlaintext();

            byte[] encrypted = encrypt(plaintext, secretKey, iv);

            byte[] tokenBytes = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, tokenBytes, 0, iv.length);
            System.arraycopy(encrypted, 0, tokenBytes, iv.length, encrypted.length);

            String encodedToken = Base64.getEncoder().encodeToString(tokenBytes);

            return Token.of(encodedToken);

        } catch (Exception e) {
            throw new TokenGenerationException("토큰 생성 중 오류가 발생했습니다", e);
        }
    }

    /**
     * AES-256 비밀 키 생성
     *
     * @return SecretKey 256-bit AES 키
     * @throws Exception 키 생성 실패 시
     */
    private SecretKey generateSecretKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_SIZE, secureRandom);
        return keyGenerator.generateKey();
    }

    /**
     * GCM 초기화 벡터 (IV) 생성
     *
     * @return 12 bytes IV
     */
    private byte[] generateIv() {
        byte[] iv = new byte[IV_SIZE];
        secureRandom.nextBytes(iv);
        return iv;
    }

    /**
     * 암호화할 평문 생성
     *
     * <p>타임스탬프 + 랜덤 데이터로 구성하여 고유성을 보장합니다.
     *
     * @return 평문 바이트 배열
     */
    private byte[] generatePlaintext() {
        byte[] timestamp = longToBytes(System.currentTimeMillis());
        byte[] randomData = new byte[RANDOM_BYTES];
        secureRandom.nextBytes(randomData);

        byte[] plaintext = new byte[TIMESTAMP_BYTES + RANDOM_BYTES];
        System.arraycopy(timestamp, 0, plaintext, 0, TIMESTAMP_BYTES);
        System.arraycopy(randomData, 0, plaintext, TIMESTAMP_BYTES, RANDOM_BYTES);

        return plaintext;
    }

    /**
     * AES-GCM 암호화 수행
     *
     * @param plaintext 평문
     * @param secretKey 비밀 키
     * @param iv 초기화 벡터
     * @return 암호문 (Auth Tag 포함)
     * @throws Exception 암호화 실패 시
     */
    private byte[] encrypt(byte[] plaintext, SecretKey secretKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
        return cipher.doFinal(plaintext);
    }

    /**
     * long 값을 8 bytes 배열로 변환
     *
     * @param value long 값
     * @return 8 bytes 배열
     */
    private byte[] longToBytes(long value) {
        byte[] bytes = new byte[TIMESTAMP_BYTES];
        for (int i = TIMESTAMP_BYTES - 1; i >= 0; i--) {
            bytes[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return bytes;
    }

    /**
     * 토큰 생성 예외
     *
     * <p>암호화 과정에서 발생하는 예외를 래핑합니다.
     */
    public static class TokenGenerationException extends RuntimeException {
        public TokenGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
