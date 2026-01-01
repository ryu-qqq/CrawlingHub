package com.ryuqq.crawlinghub.domain.useragent.vo;

import java.util.Objects;

/**
 * UserAgentMetadata - User-Agent 메타데이터 Value Object
 *
 * <p>User-Agent 문자열에서 파싱된 메타데이터를 담는 불변 객체입니다. 디바이스 브랜드, OS, 브라우저 정보를 포함합니다.
 *
 * <p><strong>불변성 보장:</strong>
 *
 * <ul>
 *   <li>모든 필드는 final
 *   <li>Setter 없음
 *   <li>방어적 복사 불필요 (모든 필드가 불변 타입)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public final class UserAgentMetadata {

    private final DeviceBrand deviceBrand;
    private final OsType osType;
    private final String osVersion;
    private final BrowserType browserType;
    private final String browserVersion;

    private UserAgentMetadata(
            DeviceBrand deviceBrand,
            OsType osType,
            String osVersion,
            BrowserType browserType,
            String browserVersion) {
        this.deviceBrand = Objects.requireNonNull(deviceBrand, "deviceBrand must not be null");
        this.osType = Objects.requireNonNull(osType, "osType must not be null");
        this.osVersion = osVersion;
        this.browserType = Objects.requireNonNull(browserType, "browserType must not be null");
        this.browserVersion = browserVersion;
    }

    /**
     * User-Agent 문자열에서 메타데이터를 파싱하여 생성합니다.
     *
     * @param userAgentString User-Agent 문자열
     * @return 파싱된 UserAgentMetadata
     */
    public static UserAgentMetadata parseFrom(String userAgentString) {
        DeviceBrand deviceBrand = DeviceBrand.parseFrom(userAgentString);
        OsType osType = OsType.parseFrom(userAgentString);
        String osVersion = osType.extractVersion(userAgentString);
        BrowserType browserType = BrowserType.parseFrom(userAgentString);
        String browserVersion = browserType.extractVersion(userAgentString);

        return new UserAgentMetadata(deviceBrand, osType, osVersion, browserType, browserVersion);
    }

    /**
     * 명시적 값으로 메타데이터를 생성합니다.
     *
     * @param deviceBrand 디바이스 브랜드
     * @param osType OS 타입
     * @param osVersion OS 버전 (nullable)
     * @param browserType 브라우저 타입
     * @param browserVersion 브라우저 버전 (nullable)
     * @return 생성된 UserAgentMetadata
     */
    public static UserAgentMetadata of(
            DeviceBrand deviceBrand,
            OsType osType,
            String osVersion,
            BrowserType browserType,
            String browserVersion) {
        return new UserAgentMetadata(deviceBrand, osType, osVersion, browserType, browserVersion);
    }

    /**
     * 기본값으로 메타데이터를 생성합니다.
     *
     * @return 기본 메타데이터 (GENERIC, LINUX, CHROME)
     */
    public static UserAgentMetadata defaultMetadata() {
        return new UserAgentMetadata(
                DeviceBrand.GENERIC, OsType.LINUX, null, BrowserType.CHROME, null);
    }

    public DeviceBrand getDeviceBrand() {
        return deviceBrand;
    }

    public OsType getOsType() {
        return osType;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public BrowserType getBrowserType() {
        return browserType;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserAgentMetadata that = (UserAgentMetadata) o;
        return deviceBrand == that.deviceBrand
                && osType == that.osType
                && Objects.equals(osVersion, that.osVersion)
                && browserType == that.browserType
                && Objects.equals(browserVersion, that.browserVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceBrand, osType, osVersion, browserType, browserVersion);
    }

    @Override
    public String toString() {
        return "UserAgentMetadata{"
                + "deviceBrand="
                + deviceBrand
                + ", osType="
                + osType
                + ", osVersion='"
                + osVersion
                + '\''
                + ", browserType="
                + browserType
                + ", browserVersion='"
                + browserVersion
                + '\''
                + '}';
    }
}
