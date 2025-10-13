#!/usr/bin/env python3
"""
User-Agent 문자열 생성 스크립트
100개의 다양한 브라우저 User-Agent를 생성합니다.
"""

import random
from typing import List


class UserAgentGenerator:
    """User-Agent 문자열 생성기"""

    # 브라우저 버전 범위
    CHROME_VERSIONS = list(range(110, 131))
    FIREFOX_VERSIONS = list(range(110, 126))
    SAFARI_VERSIONS = ["16.0", "16.1", "16.2", "16.3", "16.4", "16.5", "16.6", "17.0", "17.1", "17.2"]
    EDGE_VERSIONS = list(range(110, 126))

    # OS 버전
    WINDOWS_VERSIONS = ["10.0", "11.0"]
    MACOS_VERSIONS = ["10_15_7", "11_0_0", "11_1_0", "12_0_0", "12_1_0", "13_0_0", "13_1_0", "14_0_0"]
    LINUX_DISTROS = ["X11; Linux x86_64", "X11; Ubuntu; Linux x86_64"]

    # 모바일 OS
    IOS_VERSIONS = ["15_0", "15_1", "16_0", "16_1", "16_2", "17_0", "17_1"]
    ANDROID_VERSIONS = ["11", "12", "13", "14"]

    def generate_chrome_desktop(self) -> str:
        """Chrome 데스크톱 User-Agent 생성"""
        version = random.choice(self.CHROME_VERSIONS)
        webkit_version = version + random.randint(5000, 6000)

        os_choices = [
            f"Windows NT {random.choice(self.WINDOWS_VERSIONS)}; Win64; x64",
            f"Macintosh; Intel Mac OS X {random.choice(self.MACOS_VERSIONS)}",
            random.choice(self.LINUX_DISTROS),
        ]
        os_string = random.choice(os_choices)

        return (
            f"Mozilla/5.0 ({os_string}) "
            f"AppleWebKit/{webkit_version}.0 (KHTML, like Gecko) "
            f"Chrome/{version}.0.0.0 Safari/{webkit_version}.0"
        )

    def generate_firefox_desktop(self) -> str:
        """Firefox 데스크톱 User-Agent 생성"""
        version = random.choice(self.FIREFOX_VERSIONS)

        os_choices = [
            f"Windows NT {random.choice(self.WINDOWS_VERSIONS)}; Win64; x64",
            f"Macintosh; Intel Mac OS X {random.choice(self.MACOS_VERSIONS).replace('_', '.')}",
            random.choice(self.LINUX_DISTROS),
        ]
        os_string = random.choice(os_choices)

        return f"Mozilla/5.0 ({os_string}; rv:{version}.0) Gecko/20100101 Firefox/{version}.0"

    def generate_safari_desktop(self) -> str:
        """Safari 데스크톱 User-Agent 생성"""
        version = random.choice(self.SAFARI_VERSIONS)
        macos_version = random.choice(self.MACOS_VERSIONS)
        webkit_version = random.randint(605, 620)

        return (
            f"Mozilla/5.0 (Macintosh; Intel Mac OS X {macos_version}) "
            f"AppleWebKit/{webkit_version}.1.15 (KHTML, like Gecko) "
            f"Version/{version} Safari/{webkit_version}.1.15"
        )

    def generate_edge_desktop(self) -> str:
        """Edge 데스크톱 User-Agent 생성"""
        version = random.choice(self.EDGE_VERSIONS)
        webkit_version = version + random.randint(5000, 6000)

        os_choices = [
            f"Windows NT {random.choice(self.WINDOWS_VERSIONS)}; Win64; x64",
            f"Macintosh; Intel Mac OS X {random.choice(self.MACOS_VERSIONS)}",
        ]
        os_string = random.choice(os_choices)

        return (
            f"Mozilla/5.0 ({os_string}) "
            f"AppleWebKit/{webkit_version}.0 (KHTML, like Gecko) "
            f"Chrome/{version}.0.0.0 Safari/{webkit_version}.0 Edg/{version}.0.0.0"
        )

    def generate_chrome_mobile(self) -> str:
        """Chrome 모바일 User-Agent 생성"""
        version = random.choice(self.CHROME_VERSIONS)
        webkit_version = version + random.randint(5000, 6000)

        if random.choice([True, False]):
            # Android
            android_version = random.choice(self.ANDROID_VERSIONS)
            return (
                f"Mozilla/5.0 (Linux; Android {android_version}; Pixel 6) "
                f"AppleWebKit/{webkit_version}.0 (KHTML, like Gecko) "
                f"Chrome/{version}.0.0.0 Mobile Safari/{webkit_version}.0"
            )
        else:
            # iOS
            ios_version = random.choice(self.IOS_VERSIONS)
            return (
                f"Mozilla/5.0 (iPhone; CPU iPhone OS {ios_version} like Mac OS X) "
                f"AppleWebKit/{webkit_version}.0 (KHTML, like Gecko) "
                f"CriOS/{version}.0.0.0 Mobile/15E148 Safari/{webkit_version}.0"
            )

    def generate_safari_mobile(self) -> str:
        """Safari 모바일 (iOS) User-Agent 생성"""
        ios_version = random.choice(self.IOS_VERSIONS)
        safari_version = random.choice(self.SAFARI_VERSIONS)
        webkit_version = random.randint(605, 620)

        return (
            f"Mozilla/5.0 (iPhone; CPU iPhone OS {ios_version} like Mac OS X) "
            f"AppleWebKit/{webkit_version}.1.15 (KHTML, like Gecko) "
            f"Version/{safari_version} Mobile/15E148 Safari/{webkit_version}.1"
        )

    def generate_user_agents(self, count: int = 100) -> List[str]:
        """
        지정된 개수의 User-Agent 문자열을 생성합니다.

        Args:
            count: 생성할 User-Agent 개수

        Returns:
            User-Agent 문자열 리스트
        """
        generators = [
            self.generate_chrome_desktop,
            self.generate_firefox_desktop,
            self.generate_safari_desktop,
            self.generate_edge_desktop,
            self.generate_chrome_mobile,
            self.generate_safari_mobile,
        ]

        user_agents = set()
        while len(user_agents) < count:
            generator = random.choice(generators)
            user_agent = generator()
            user_agents.add(user_agent)

        return sorted(list(user_agents))


def generate_sql_insert_statements(user_agents: List[str]) -> str:
    """
    User-Agent 리스트를 SQL INSERT 문으로 변환합니다.

    Args:
        user_agents: User-Agent 문자열 리스트

    Returns:
        SQL INSERT 문
    """
    sql_lines = [
        "-- ========================================",
        "-- User-Agent Pool Seed Data (100개)",
        "-- ========================================",
        "",
        "INSERT INTO user_agent_pool (user_agent, is_active, is_blocked, usage_count, success_count, failure_count)",
        "VALUES",
    ]

    values = []
    for user_agent in user_agents:
        # SQL Injection 방지: 작은따옴표 이스케이프
        escaped_ua = user_agent.replace("'", "''")
        values.append(f"    ('{escaped_ua}', TRUE, FALSE, 0, 0, 0)")

    sql_lines.append(",\n".join(values) + ";")
    sql_lines.append("")
    sql_lines.append("-- User-Agent Pool 초기화 완료")

    return "\n".join(sql_lines)


def main():
    """메인 함수"""
    generator = UserAgentGenerator()
    user_agents = generator.generate_user_agents(100)

    print("=" * 80)
    print("🚀 User-Agent 생성 완료!")
    print(f"📊 총 {len(user_agents)}개 생성")
    print("=" * 80)
    print()

    # SQL 파일 생성
    sql_content = generate_sql_insert_statements(user_agents)
    output_file = "seed_user_agents.sql"

    with open(output_file, "w", encoding="utf-8") as f:
        f.write(sql_content)

    print(f"✅ SQL 파일 생성: {output_file}")
    print()
    print("📝 생성된 User-Agent 샘플 (처음 5개):")
    print("-" * 80)
    for i, ua in enumerate(user_agents[:5], 1):
        print(f"{i}. {ua}")
    print()


if __name__ == "__main__":
    main()
