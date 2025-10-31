package com.ryuqq.crawlinghub.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DataHash Value Object 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-01-30
 */
@DisplayName("DataHash Value Object 단위 테스트")
class DataHashTest {

    private static final String VALID_SHA256_HASH = "a".repeat(64); // 64자리 SHA-256 해시

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreateTests {

        @Test
        @DisplayName("64자리 해시로 DataHash 생성 성공")
        void shouldCreateWith64CharacterHash() {
            // Given
            String validHash = "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2";

            // When
            DataHash dataHash = DataHash.of(validHash);

            // Then
            assertThat(dataHash).isNotNull();
            assertThat(dataHash.getValue()).isEqualTo(validHash);
            assertThat(dataHash.getValue()).hasSize(64);
        }

        @Test
        @DisplayName("실제 SHA-256 해시 형식으로 DataHash 생성 성공")
        void shouldCreateWithRealSha256Hash() {
            // Given - 실제 "test" 문자열의 SHA-256 해시
            String sha256Hash = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";

            // When
            DataHash dataHash = DataHash.of(sha256Hash);

            // Then
            assertThat(dataHash).isNotNull();
            assertThat(dataHash.getValue()).isEqualTo(sha256Hash);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef", // 숫자+소문자
            "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF", // 숫자+대문자
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", // 동일 문자 반복
            "1234567890123456789012345678901234567890123456789012345678901234"  // 숫자만
        })
        @DisplayName("다양한 형식의 64자리 해시로 DataHash 생성 성공")
        void shouldCreateWithVariousHashFormats(String hash) {
            // When
            DataHash dataHash = DataHash.of(hash);

            // Then
            assertThat(dataHash).isNotNull();
            assertThat(dataHash.getValue()).isEqualTo(hash);
            assertThat(dataHash.getValue()).hasSize(64);
        }
    }

    @Nested
    @DisplayName("예외 케이스 테스트")
    class ExceptionTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("해시가 null 또는 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenHashIsNullOrBlank(String invalidHash) {
            // When & Then
            assertThatThrownBy(() -> DataHash.of(invalidHash))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해시값은 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 10, 32, 63, 65, 100, 128})
        @DisplayName("64자가 아닌 길이의 해시는 예외 발생")
        void shouldThrowExceptionWhenHashLengthIsNot64(int length) {
            // Given
            String invalidHash = "a".repeat(length);

            // When & Then
            assertThatThrownBy(() -> DataHash.of(invalidHash))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SHA-256 해시는 64자여야 합니다");
        }

        @Test
        @DisplayName("63자리 해시는 예외 발생")
        void shouldThrowExceptionWhen63Characters() {
            // Given
            String hash63 = "a".repeat(63);

            // When & Then
            assertThatThrownBy(() -> DataHash.of(hash63))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SHA-256 해시는 64자여야 합니다");
        }

        @Test
        @DisplayName("65자리 해시는 예외 발생")
        void shouldThrowExceptionWhen65Characters() {
            // Given
            String hash65 = "a".repeat(65);

            // When & Then
            assertThatThrownBy(() -> DataHash.of(hash65))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SHA-256 해시는 64자여야 합니다");
        }
    }

    @Nested
    @DisplayName("동등성 비교 테스트")
    class EqualityTests {

        @Test
        @DisplayName("같은 해시를 가진 두 DataHash는 isSameAs() 가 true 반환")
        void shouldReturnTrueForSameHash() {
            // Given
            String hash = VALID_SHA256_HASH;
            DataHash hash1 = DataHash.of(hash);
            DataHash hash2 = DataHash.of(hash);

            // When
            boolean result = hash1.isSameAs(hash2);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("다른 해시를 가진 두 DataHash는 isSameAs() 가 false 반환")
        void shouldReturnFalseForDifferentHash() {
            // Given
            DataHash hash1 = DataHash.of("a".repeat(64));
            DataHash hash2 = DataHash.of("b".repeat(64));

            // When
            boolean result = hash1.isSameAs(hash2);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null과 비교하면 isSameAs() 가 false 반환")
        void shouldReturnFalseWhenComparedWithNull() {
            // Given
            DataHash dataHash = DataHash.of(VALID_SHA256_HASH);

            // When
            boolean result = dataHash.isSameAs(null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("같은 해시를 가진 두 DataHash는 equals() 가 true 반환")
        void shouldReturnTrueForEquals() {
            // Given
            String hash = VALID_SHA256_HASH;
            DataHash hash1 = DataHash.of(hash);
            DataHash hash2 = DataHash.of(hash);

            // When & Then
            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("같은 해시를 가진 두 DataHash는 같은 hashCode 반환")
        void shouldReturnSameHashCode() {
            // Given
            String hash = VALID_SHA256_HASH;
            DataHash hash1 = DataHash.of(hash);
            DataHash hash2 = DataHash.of(hash);

            // When & Then
            assertThat(hash1.hashCode()).isEqualTo(hash2.hashCode());
        }

        @Test
        @DisplayName("다른 해시를 가진 두 DataHash는 다른 hashCode 반환")
        void shouldReturnDifferentHashCode() {
            // Given
            DataHash hash1 = DataHash.of("a".repeat(64));
            DataHash hash2 = DataHash.of("b".repeat(64));

            // When & Then
            assertThat(hash1.hashCode()).isNotEqualTo(hash2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 해시값을 포함한 문자열 반환")
        void shouldReturnStringWithHash() {
            // Given
            String hash = VALID_SHA256_HASH;
            DataHash dataHash = DataHash.of(hash);

            // When
            String result = dataHash.toString();

            // Then
            assertThat(result).contains(hash);
        }
    }

    @Nested
    @DisplayName("Edge Case 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("모두 0으로 구성된 64자리 해시도 정상 생성")
        void shouldCreateWithAllZeros() {
            // Given
            String allZeros = "0".repeat(64);

            // When
            DataHash dataHash = DataHash.of(allZeros);

            // Then
            assertThat(dataHash).isNotNull();
            assertThat(dataHash.getValue()).isEqualTo(allZeros);
        }

        @Test
        @DisplayName("모두 F로 구성된 64자리 해시도 정상 생성")
        void shouldCreateWithAllFs() {
            // Given
            String allFs = "f".repeat(64);

            // When
            DataHash dataHash = DataHash.of(allFs);

            // Then
            assertThat(dataHash).isNotNull();
            assertThat(dataHash.getValue()).isEqualTo(allFs);
        }

        @Test
        @DisplayName("대소문자 혼합 해시도 정상 생성")
        void shouldCreateWithMixedCase() {
            // Given
            String mixedCase = "AaBbCcDd".repeat(8);

            // When
            DataHash dataHash = DataHash.of(mixedCase);

            // Then
            assertThat(dataHash).isNotNull();
            assertThat(dataHash.getValue()).isEqualTo(mixedCase);
            assertThat(dataHash.getValue()).hasSize(64);
        }

        @Test
        @DisplayName("실제 다양한 데이터의 SHA-256 해시로 생성 가능")
        void shouldCreateWithActualSha256Hashes() {
            // Given - 실제 SHA-256 해시 예시들
            String[] actualHashes = {
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", // 빈 문자열
                "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", // "test"
                "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3"  // "123"
            };

            // When & Then
            for (String hash : actualHashes) {
                DataHash dataHash = DataHash.of(hash);
                assertThat(dataHash).isNotNull();
                assertThat(dataHash.getValue()).isEqualTo(hash);
            }
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTests {

        @Test
        @DisplayName("DataHash는 불변 객체이다")
        void shouldBeImmutable() {
            // Given
            String originalHash = VALID_SHA256_HASH;
            DataHash dataHash = DataHash.of(originalHash);

            // When
            String retrievedHash = dataHash.getValue();

            // Then
            assertThat(retrievedHash).isEqualTo(originalHash);
            assertThat(dataHash.getValue()).isEqualTo(originalHash); // 여러 번 호출해도 같은 값
        }
    }
}
