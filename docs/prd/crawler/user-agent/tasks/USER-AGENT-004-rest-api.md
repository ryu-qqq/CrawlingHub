# USER-AGENT-004: UserAgent REST API Layer 구현

**Bounded Context**: Crawler
**Sub-Context**: UserAgent
**Layer**: REST API Layer
**브랜치**: feature/USER-AGENT-004-rest-api

---

## 📝 목적

UserAgent Pool 상태 조회 API 구현.

---

## 🎯 요구사항

### 1. API 엔드포인트

#### GET /api/v1/user-agents/status - UserAgent Pool 상태 조회
- Request: 없음
- Response: `UserAgentPoolStatusResponse`
- Status Code: 200 OK
- Authentication: JWT (관리자 전용)

### 2. Response DTO

```java
public record UserAgentPoolStatusResponse(
    Long totalAgents,
    Long availableAgents,
    Long suspendedAgents,
    Long blockedAgents,
    Double availableRate,
    HealthScoreStats healthScoreStats
) {
    public record HealthScoreStats(
        Double avg,
        Integer min,
        Integer max
    ) {}

    public static UserAgentPoolStatusResponse of(UserAgentStats stats) {
        return new UserAgentPoolStatusResponse(
            stats.totalAgents(),
            stats.availableAgents(),
            stats.suspendedAgents(),
            stats.blockedAgents(),
            stats.getAvailableRate(),
            new HealthScoreStats(
                stats.avgHealthScore(),
                stats.minHealthScore(),
                stats.maxHealthScore()
            )
        );
    }
}
```

---

## ✅ 완료 조건

- [ ] GET /api/v1/user-agents/status 구현 완료
- [ ] Integration Test 완료

---

## 🔗 관련 문서

- **Plan**: docs/prd/crawler/user-agent/plans/USER-AGENT-004-rest-api-plan.md

---

## 📚 참고사항

### UserAgentPoolController 구현 예시

```java
@RestController
@RequestMapping("/api/v1/user-agents")
@RequiredArgsConstructor
public class UserAgentPoolController {
    private final GetUserAgentPoolStatusUseCase getUserAgentPoolStatusUseCase;

    @GetMapping("/status")
    public ResponseEntity<UserAgentPoolStatusResponse> getPoolStatus() {
        UserAgentPoolStatusResponse response = getUserAgentPoolStatusUseCase.execute();
        return ResponseEntity.ok(response);
    }
}
```

### Integration Test 예시

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class UserAgentPoolControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserAgentCommandPort userAgentCommandPort;

    @BeforeEach
    void setUp() {
        // Given: 3개의 UserAgent 등록 (AVAILABLE 2개, SUSPENDED 1개)
        UserAgent available1 = UserAgentFixture.createAvailable(100);
        UserAgent available2 = UserAgentFixture.createAvailable(80);
        UserAgent suspended = UserAgentFixture.createSuspended(20);

        userAgentCommandPort.save(available1);
        userAgentCommandPort.save(available2);
        userAgentCommandPort.save(suspended);
    }

    @Test
    void 유저에이전트_풀_상태_조회_성공() {
        // When: Pool 상태 조회
        ResponseEntity<UserAgentPoolStatusResponse> response =
            restTemplate.getForEntity("/api/v1/user-agents/status", UserAgentPoolStatusResponse.class);

        // Then: 200 OK
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        UserAgentPoolStatusResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.totalAgents()).isEqualTo(3L);
        assertThat(body.availableAgents()).isEqualTo(2L);
        assertThat(body.suspendedAgents()).isEqualTo(1L);
        assertThat(body.blockedAgents()).isEqualTo(0L);
        assertThat(body.availableRate()).isEqualTo(66.67, within(0.01));

        HealthScoreStats healthStats = body.healthScoreStats();
        assertThat(healthStats.avg()).isEqualTo(66.67, within(0.01));
        assertThat(healthStats.min()).isEqualTo(20);
        assertThat(healthStats.max()).isEqualTo(100);
    }
}
```

### Response 예시

```json
{
  "totalAgents": 10,
  "availableAgents": 7,
  "suspendedAgents": 2,
  "blockedAgents": 1,
  "availableRate": 70.0,
  "healthScoreStats": {
    "avg": 75.5,
    "min": 20,
    "max": 100
  }
}
```
