package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * ImageUploadStatus Enum 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ImageUploadStatus 테스트")
class ImageUploadStatusTest {

    @ParameterizedTest
    @EnumSource(ImageUploadStatus.class)
    @DisplayName("모든 업로드 상태는 유효한 enum 값")
    void shouldHaveValidEnumValues(ImageUploadStatus status) {
        // given & when & then
        assertThat(status.name()).isNotBlank();
        assertThat(status)
                .isIn(
                        ImageUploadStatus.PENDING,
                        ImageUploadStatus.UPLOADING,
                        ImageUploadStatus.UPLOADED,
                        ImageUploadStatus.FAILED);
    }
}
