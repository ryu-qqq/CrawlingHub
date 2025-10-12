# EventBridge 연동 설정 가이드

## 개요

이 문서는 AWS EventBridge와 CrawlingHub 시스템을 연동하는 방법을 설명합니다.

## 아키텍처

```
[Schedule Domain] → [ScheduleEnabledEvent] → [ScheduleEventBridgeHandler] → [EventBridgeAdapter] → [AWS EventBridge]
```

### 주요 컴포넌트

- **EventBridgeAdapter**: AWS EventBridge SDK를 사용한 Rule/Target 관리
- **EventBridgeProperties**: Type-safe 설정 관리
- **CronExpressionValidator**: Spring Cron (6필드) ↔ AWS Cron (6필드) 변환
- **ScheduleEventBridgeHandler**: Domain Event 기반 EventBridge 연동

## 설정 방법

### 1. application.yml 설정

```yaml
aws:
  eventbridge:
    # EventBridge 통합 활성화 여부 (기본값: true)
    enabled: true

    # AWS Region (기본값: ap-northeast-2)
    region: ap-northeast-2

    # EventBridge Event Bus 이름 (기본값: default)
    event-bus-name: default

    # EventBridge Rule의 Target ARN (Lambda, SQS 등)
    # 필수 설정: 이 값이 없으면 Target 추가 시 에러 발생
    target-arn: arn:aws:lambda:ap-northeast-2:123456789012:function:crawl-trigger

    # AWS 자격증명 (선택사항 - 설정하지 않으면 DefaultCredentialsProvider 사용)
    credentials:
      access-key: ${AWS_ACCESS_KEY_ID:}
      secret-key: ${AWS_SECRET_ACCESS_KEY:}
```

### 2. AWS 자격증명 설정

#### 방법 1: 환경 변수 사용 (권장)

```bash
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
```

#### 방법 2: application.yml에 명시

```yaml
aws:
  eventbridge:
    credentials:
      access-key: AKIAIOSFODNN7EXAMPLE
      secret-key: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
```

#### 방법 3: AWS Credentials File (~/.aws/credentials)

```ini
[default]
aws_access_key_id = AKIAIOSFODNN7EXAMPLE
aws_secret_access_key = wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
```

#### 방법 4: EC2 Instance Profile (운영 환경 권장)

EC2에서 실행하는 경우 Instance Profile을 사용하면 자격증명 관리가 불필요합니다.

### 3. AWS IAM 권한 설정

EventBridge 연동에 필요한 최소 권한:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "EventBridgeRuleManagement",
      "Effect": "Allow",
      "Action": [
        "events:PutRule",
        "events:DeleteRule",
        "events:EnableRule",
        "events:DisableRule",
        "events:DescribeRule"
      ],
      "Resource": "arn:aws:events:ap-northeast-2:*:rule/crawl-schedule-*"
    },
    {
      "Sid": "EventBridgeTargetManagement",
      "Effect": "Allow",
      "Action": [
        "events:PutTargets",
        "events:RemoveTargets"
      ],
      "Resource": "arn:aws:events:ap-northeast-2:*:rule/crawl-schedule-*"
    }
  ]
}
```

**권한 설명**:
- `events:PutRule`: Rule 생성/수정
- `events:DeleteRule`: Rule 삭제
- `events:EnableRule` / `DisableRule`: Rule 활성화/비활성화
- `events:DescribeRule`: Rule 존재 확인
- `events:PutTargets`: Target 추가
- `events:RemoveTargets`: Target 제거

**리소스 제한**:
- `crawl-schedule-*` 패턴으로 시작하는 Rule만 관리 가능 (보안 강화)

## EventBridge Rule 생성 흐름

### 1. Schedule 등록

```java
RegisterScheduleCommand command = new RegisterScheduleCommand(
    workflowId,
    "Daily Product Crawl",
    "0 0 9 * * *",  // Spring Cron: 매일 9시 (초 분 시 일 월 요일)
    "Asia/Seoul",
    inputParams
);

Long scheduleId = registerScheduleUseCase.execute(command);
```

### 2. Schedule 활성화

```java
enableScheduleUseCase.execute(scheduleId);
```

**내부 처리 흐름**:
1. Schedule 상태를 `enabled`로 변경
2. Spring Cron → AWS Cron 변환 (`CronExpressionValidator`)
3. `ScheduleEnabledEvent` 발행
4. **트랜잭션 커밋 후** `ScheduleEventBridgeHandler`가 EventBridge Rule 생성
5. EventBridge Rule에 Target (Lambda/SQS) 추가
6. EventBridge Rule 활성화

### 3. EventBridge Rule 구조

**Rule 이름**: `crawl-schedule-{sanitized-schedule-name}`

**Cron 표현식**: `cron(0 9 * * ? *)` (AWS 형식)

**Target Input (JSON)**:
```json
{
  "scheduleId": 1,
  "workflowId": 10,
  "scheduleName": "Daily Product Crawl",
  "inputParams": {
    "siteId": "100",
    "category": "electronics"
  }
}
```

## Cron 표현식 형식

### Spring Cron (6필드)

```
초  분  시  일  월  요일
*  *   *   *   *   *
```

**예시**:
- `0 0 9 * * *`: 매일 9시
- `0 */5 * * * *`: 5분마다
- `0 0 0 1 * *`: 매월 1일 자정
- `0 0 9 * * MON`: 매주 월요일 9시
- `0 30 14 1 1 *`: 1월 1일 14:30

### AWS EventBridge Cron (6필드)

```
분  시  일  월  요일  연도
*   *   *   *   *     *
```

**예시**:
- `0 9 * * ? *`: 매일 9시
- `*/5 * * * ? *`: 5분마다
- `0 0 1 * ? *`: 매월 1일 자정
- `0 9 ? * MON *`: 매주 월요일 9시

**주의사항**:
- AWS Cron은 `일`과 `요일` 중 하나만 지정 가능 (나머지는 `?` 사용)
- 프로젝트는 Spring Cron 6필드를 사용하며, EventBridge 연동 시에는 `CronExpressionValidator`가 다음과 같이 변환합니다:
  - 첫 번째 필드(초)는 반드시 `0`이어야 함 (AWS는 초 단위 미지원)
  - 초 필드를 제외한 나머지 5개 필드를 AWS Cron 형식으로 변환
  - `일`과 `요일` 필드 중 하나를 `?`로 변환하여 AWS Cron 규칙 준수

## Timezone 처리

### 설계 원칙

- **Schedule 도메인**에 `timezone` 필드 저장
- **CronExecutionCalculator**가 timezone을 사용하여 다음 실행 시간 계산
- **EventBridge는 UTC 기준으로 동작**하지만, Cron 표현식 자체는 timezone 정보를 포함하지 않음
- 따라서 **별도 UTC 변환 불필요**

### 예시

사용자가 "Asia/Seoul" 기준 매일 9시 실행을 원하는 경우:

1. Schedule에 `cronExpression: "0 0 9 * * *"`, `timezone: "Asia/Seoul"` 저장
2. `CronExecutionCalculator`가 다음 실행 시간을 서울 시간 기준으로 계산
3. EventBridge에는 `cron(0 9 * * ? *)` 그대로 전달 (UTC 변환 없음)
4. EventBridge는 UTC 기준으로 Rule을 실행하지만, 애플리케이션이 `nextExecutionTime`을 관리

**결론**: Timezone은 애플리케이션 레벨에서 처리하며, EventBridge는 단순히 Cron 트리거 역할만 수행

## 로컬 개발 환경 설정

### EventBridge Disabled 모드

로컬 개발 시 AWS EventBridge를 사용하지 않으려면:

```yaml
aws:
  eventbridge:
    enabled: false
```

이 경우 EventBridge 관련 작업이 모두 스킵되며, 로그만 출력됩니다.

### LocalStack 사용 (선택사항)

LocalStack으로 EventBridge를 로컬에서 테스트할 수 있습니다:

```yaml
aws:
  eventbridge:
    region: us-east-1
    endpoint: http://localhost:4566  # LocalStack endpoint (커스텀 설정 필요)
```

## 보상 트랜잭션 (Compensation Transaction)

EventBridge 작업 실패 시 자동 보상 처리:

### Enable 실패 시

1. EventBridge Rule 생성/활성화 실패
2. **자동 보상**: Schedule 상태를 `disabled`로 되돌림
3. 로그 출력: `Compensation completed: Schedule reverted to disabled state`

### Disable 실패 시

1. EventBridge Rule 비활성화 실패
2. **자동 보상**: Schedule 상태를 `enabled`로 되돌림

### Delete 실패 시

⚠️ **보상 불가능**: DB에서 이미 삭제된 상태이므로 복구 불가
- 수동 개입 필요
- 로그: `COMPENSATION NOT POSSIBLE: Manual cleanup of EventBridge rule may be required`

## 트러블슈팅

### 1. "Target ARN is not configured" 에러

**원인**: `aws.eventbridge.target-arn` 설정 누락

**해결**: application.yml에 target-arn 추가

### 2. "Failed to create EventBridge rule" 에러

**원인**: AWS 자격증명 문제 또는 IAM 권한 부족

**확인 사항**:
1. AWS 자격증명 설정 확인
2. IAM 권한 확인 (`events:PutRule` 권한 필요)
3. Region 설정 확인

### 3. Rule이 생성되지 않음

**확인 사항**:
1. `aws.eventbridge.enabled: true` 확인
2. 로그에서 `EventBridge is disabled` 메시지 확인
3. 트랜잭션이 정상 커밋되었는지 확인 (@TransactionalEventListener는 커밋 후 실행)

### 4. Cron 표현식 검증 실패

**원인**: Spring Cron 형식(6필드)이 아닌 다른 형식 사용

**해결**: Spring Cron 6필드 형식 사용 (초 분 시 일 월 요일)

## 모니터링

### 로그 확인

```bash
# EventBridge 작업 로그
grep "EventBridge" logs/application.log

# 보상 트랜잭션 로그
grep "Compensation" logs/application.log

# 에러 로그
grep "CRITICAL" logs/application.log
```

### AWS Console에서 확인

1. AWS Console → EventBridge → Rules
2. `crawl-schedule-*` 패턴으로 검색
3. Rule 상태 (Enabled/Disabled) 확인
4. Target 설정 확인

## 참고 자료

- [AWS EventBridge 공식 문서](https://docs.aws.amazon.com/eventbridge/)
- [Spring Framework Cron Expression](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronExpression.html)
- [AWS IAM Policy Generator](https://awspolicygen.s3.amazonaws.com/policygen.html)
