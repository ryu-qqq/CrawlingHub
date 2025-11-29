package com.ryuqq.cralwinghub.domain.fixture.useragent;

import com.ryuqq.crawlinghub.domain.useragent.vo.DeviceType;

/**
 * DeviceType Test Fixture
 *
 * <p>Object Mother 패턴을 사용한 테스트 데이터 생성
 *
 * @author development-team
 * @since 1.0.0
 */
public final class DeviceTypeFixture {

    /**
     * 기본 디바이스 타입 (MOBILE)
     *
     * @return DeviceType
     */
    public static DeviceType aDefaultDeviceType() {
        return DeviceType.of(DeviceType.Type.MOBILE);
    }

    /**
     * MOBILE 디바이스 타입
     *
     * @return DeviceType
     */
    public static DeviceType aMobileDeviceType() {
        return DeviceType.of(DeviceType.Type.MOBILE);
    }

    /**
     * TABLET 디바이스 타입
     *
     * @return DeviceType
     */
    public static DeviceType aTabletDeviceType() {
        return DeviceType.of(DeviceType.Type.TABLET);
    }

    /**
     * DESKTOP 디바이스 타입
     *
     * @return DeviceType
     */
    public static DeviceType aDesktopDeviceType() {
        return DeviceType.of(DeviceType.Type.DESKTOP);
    }

    /**
     * 타입 이름으로 디바이스 타입 생성
     *
     * @param typeName 타입 이름 (MOBILE, TABLET, DESKTOP)
     * @return DeviceType
     */
    public static DeviceType aDeviceType(String typeName) {
        return DeviceType.of(typeName);
    }

    private DeviceTypeFixture() {
        // Utility class
    }
}
