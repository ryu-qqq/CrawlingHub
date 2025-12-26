package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * ImageType Enum 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ImageType 테스트")
class ImageTypeTest {

    @ParameterizedTest
    @EnumSource(ImageType.class)
    @DisplayName("모든 이미지 타입은 유효한 enum 값")
    void shouldHaveValidEnumValues(ImageType imageType) {
        // given & when & then
        assertThat(imageType.name()).isNotBlank();
        assertThat(imageType).isIn(ImageType.THUMBNAIL, ImageType.DESCRIPTION);
    }
}
