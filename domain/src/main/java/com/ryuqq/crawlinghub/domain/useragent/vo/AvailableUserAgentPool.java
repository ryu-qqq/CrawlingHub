package com.ryuqq.crawlinghub.domain.useragent.vo;

import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.exception.NoAvailableUserAgentException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 사용 가능한 UserAgent Pool (Value Object)
 *
 * <p>DB 폴백 시 사용 가능한 UserAgent 목록에서 최적의 에이전트를 선택하는 도메인 VO
 *
 * <p><strong>선택 전략</strong>:
 *
 * <ul>
 *   <li>selectBest(): Health Score가 가장 높은 에이전트 선택
 *   <li>selectRoundRobin(): 순환 방식으로 부하 분산
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public final class AvailableUserAgentPool {

    private final List<UserAgent> agents;
    private final AtomicInteger roundRobinIndex;

    public AvailableUserAgentPool(List<UserAgent> agents) {
        if (agents == null || agents.isEmpty()) {
            throw new NoAvailableUserAgentException();
        }
        this.agents = List.copyOf(agents);
        this.roundRobinIndex = new AtomicInteger(0);
    }

    /**
     * Health Score가 가장 높은 UserAgent 선택
     *
     * @return 최적의 UserAgent
     */
    public UserAgent selectBest() {
        return agents.stream()
                .max(Comparator.comparingInt(UserAgent::getHealthScoreValue))
                .orElseThrow(NoAvailableUserAgentException::new);
    }

    /**
     * Round-Robin 방식으로 UserAgent 선택
     *
     * <p>부하를 균등하게 분산하여 특정 UserAgent에 집중되는 것을 방지
     *
     * @return 순환 선택된 UserAgent
     */
    public UserAgent selectRoundRobin() {
        int index = roundRobinIndex.getAndUpdate(i -> (i + 1) % agents.size());
        return agents.get(index);
    }

    /**
     * Pool 내 UserAgent 수
     *
     * @return UserAgent 수
     */
    public int size() {
        return agents.size();
    }
}
