package com.ryuqq.crawlinghub.domain.useragent.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("DeviceBrand 단위 테스트")
class DeviceBrandTest {

    @Nested
    @DisplayName("parseFrom() 테스트")
    class ParseFromTest {

        @Test
        @DisplayName("iPhone UA에서 IPHONE을 파싱한다")
        void parsesIphoneAsIphone() {
            DeviceBrand result =
                    DeviceBrand.parseFrom("Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)");
            assertThat(result).isEqualTo(DeviceBrand.IPHONE);
        }

        @Test
        @DisplayName("iPad UA에서 IPAD를 파싱한다")
        void parsesIpadAsIpad() {
            DeviceBrand result =
                    DeviceBrand.parseFrom("Mozilla/5.0 (iPad; CPU OS 14_0 like Mac OS X)");
            assertThat(result).isEqualTo(DeviceBrand.IPAD);
        }

        @Test
        @DisplayName("Pixel UA에서 PIXEL을 파싱한다")
        void parsesPixelAsPixel() {
            DeviceBrand result = DeviceBrand.parseFrom("Mozilla/5.0 (Linux; Android 11; Pixel 5)");
            assertThat(result).isEqualTo(DeviceBrand.PIXEL);
        }

        @Test
        @DisplayName("Galaxy Tab UA에서 GALAXY_TAB을 파싱한다")
        void parsesGalaxyTabAsGalaxyTab() {
            DeviceBrand result = DeviceBrand.parseFrom("Mozilla/5.0 (Linux; Android 11; SM-T870) ");
            assertThat(result).isEqualTo(DeviceBrand.GALAXY_TAB);
        }

        @Test
        @DisplayName("Samsung UA에서 SAMSUNG을 파싱한다")
        void parsesSamsungAsSamsung() {
            DeviceBrand result = DeviceBrand.parseFrom("Mozilla/5.0 (Linux; Android 11; SM-G973F)");
            assertThat(result).isEqualTo(DeviceBrand.SAMSUNG);
        }

        @Test
        @DisplayName("Xiaomi/Redmi UA에서 XIAOMI를 파싱한다")
        void parsesXiaomiAsXiaomi() {
            DeviceBrand result =
                    DeviceBrand.parseFrom("Mozilla/5.0 (Linux; Android 11; Redmi Note 10)");
            assertThat(result).isEqualTo(DeviceBrand.XIAOMI);
        }

        @Test
        @DisplayName("Huawei UA에서 HUAWEI를 파싱한다")
        void parsesHuaweiAsHuawei() {
            DeviceBrand result =
                    DeviceBrand.parseFrom("Mozilla/5.0 (Linux; Android 10; Huawei P30)");
            assertThat(result).isEqualTo(DeviceBrand.HUAWEI);
        }

        @Test
        @DisplayName("OPPO/Realme UA에서 OPPO를 파싱한다")
        void parsesOppoAsOppo() {
            DeviceBrand result =
                    DeviceBrand.parseFrom("Mozilla/5.0 (Linux; Android 11; realme X50)");
            assertThat(result).isEqualTo(DeviceBrand.OPPO);
        }

        @Test
        @DisplayName("OnePlus UA에서 ONEPLUS를 파싱한다")
        void parsesOnePlusAsOnePlus() {
            DeviceBrand result =
                    DeviceBrand.parseFrom("Mozilla/5.0 (Linux; Android 11; OnePlus 9)");
            assertThat(result).isEqualTo(DeviceBrand.ONEPLUS);
        }

        @Test
        @DisplayName("Windows UA에서 GENERIC을 파싱한다")
        void parsesWindowsAsGeneric() {
            DeviceBrand result =
                    DeviceBrand.parseFrom("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/91.0");
            assertThat(result).isEqualTo(DeviceBrand.GENERIC);
        }

        @Test
        @DisplayName("null UA는 GENERIC을 반환한다")
        void parsesNullAsGeneric() {
            DeviceBrand result = DeviceBrand.parseFrom(null);
            assertThat(result).isEqualTo(DeviceBrand.GENERIC);
        }

        @Test
        @DisplayName("빈 UA는 GENERIC을 반환한다")
        void parsesBlankAsGeneric() {
            DeviceBrand result = DeviceBrand.parseFrom("  ");
            assertThat(result).isEqualTo(DeviceBrand.GENERIC);
        }
    }

    @Nested
    @DisplayName("getDisplayName() 테스트")
    class GetDisplayNameTest {

        @Test
        @DisplayName("각 브랜드의 표시 이름을 반환한다")
        void returnsDisplayNames() {
            assertThat(DeviceBrand.IPHONE.getDisplayName()).isEqualTo("iPhone");
            assertThat(DeviceBrand.IPAD.getDisplayName()).isEqualTo("iPad");
            assertThat(DeviceBrand.SAMSUNG.getDisplayName()).isEqualTo("Samsung");
            assertThat(DeviceBrand.PIXEL.getDisplayName()).isEqualTo("Pixel");
            assertThat(DeviceBrand.XIAOMI.getDisplayName()).isEqualTo("Xiaomi");
            assertThat(DeviceBrand.HUAWEI.getDisplayName()).isEqualTo("Huawei");
            assertThat(DeviceBrand.OPPO.getDisplayName()).isEqualTo("Oppo");
            assertThat(DeviceBrand.ONEPLUS.getDisplayName()).isEqualTo("OnePlus");
            assertThat(DeviceBrand.GALAXY_TAB.getDisplayName()).isEqualTo("Galaxy Tab");
            assertThat(DeviceBrand.GENERIC.getDisplayName()).isEqualTo("Generic");
        }
    }
}
