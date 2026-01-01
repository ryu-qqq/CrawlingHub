package com.ryuqq.cralwinghub.domain.fixture.useragent;

import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceBrand;

/**
 * DeviceBrand Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class DeviceBrandFixture {

    /**
     * 기본 디바이스 브랜드 (GENERIC)
     *
     * @return DeviceBrand
     */
    public static DeviceBrand aDefaultDeviceBrand() {
        return DeviceBrand.GENERIC;
    }

    /**
     * iPhone 디바이스 브랜드
     *
     * @return DeviceBrand
     */
    public static DeviceBrand anIphoneDeviceBrand() {
        return DeviceBrand.IPHONE;
    }

    /**
     * Samsung 디바이스 브랜드
     *
     * @return DeviceBrand
     */
    public static DeviceBrand aSamsungDeviceBrand() {
        return DeviceBrand.SAMSUNG;
    }

    /**
     * iPad 디바이스 브랜드
     *
     * @return DeviceBrand
     */
    public static DeviceBrand anIpadDeviceBrand() {
        return DeviceBrand.IPAD;
    }

    /**
     * Pixel 디바이스 브랜드
     *
     * @return DeviceBrand
     */
    public static DeviceBrand aPixelDeviceBrand() {
        return DeviceBrand.PIXEL;
    }

    /**
     * Generic 디바이스 브랜드 (Desktop 등)
     *
     * @return DeviceBrand
     */
    public static DeviceBrand aGenericDeviceBrand() {
        return DeviceBrand.GENERIC;
    }

    private DeviceBrandFixture() {
        // Utility class
    }
}
