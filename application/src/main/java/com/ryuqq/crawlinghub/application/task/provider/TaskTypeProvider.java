package com.ryuqq.crawlinghub.application.task.provider;

import com.ryuqq.crawlinghub.application.task.strategy.TaskStrategy;
import com.ryuqq.crawlinghub.domain.task.TaskType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * TaskType별 TaskStrategy 제공자
 *
 * <p>Strategy Pattern의 Provider 역할
 * - TaskType → TaskStrategy 매핑 관리
 * - Spring DI를 통한 Strategy 주입
 * - 런타임 Strategy 조회
 *
 * <p>사용 예시:
 * <pre>{@code
 * TaskStrategy strategy = taskTypeProvider.getStrategy(TaskType.META);
 * strategy.execute(task);
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class TaskTypeProvider {

    private final Map<TaskType, TaskStrategy> strategyMap;

    /**
     * Spring DI를 통해 모든 TaskStrategy 구현체 주입
     *
     * <p>자동 매핑 로직:
     * - MetaTaskStrategy → TaskType.META
     * - MiniShopTaskStrategy → TaskType.MINI_SHOP
     * - ProductDetailTaskStrategy → TaskType.PRODUCT_DETAIL
     * - ProductOptionTaskStrategy → TaskType.PRODUCT_OPTION
     *
     * @param strategies Spring이 주입하는 모든 TaskStrategy 구현체 List
     */
    public TaskTypeProvider(List<TaskStrategy> strategies) {
        this.strategyMap = new EnumMap<>(TaskType.class);

        for (TaskStrategy strategy : strategies) {
            TaskType taskType = resolveTaskType(strategy);
            strategyMap.put(taskType, strategy);
        }

        validateAllTaskTypesHaveStrategy();
    }

    /**
     * TaskType에 해당하는 TaskStrategy 조회
     *
     * @param taskType Task 타입
     * @return 해당 TaskType의 처리 전략
     * @throws IllegalArgumentException 지원하지 않는 TaskType인 경우
     */
    public TaskStrategy getStrategy(TaskType taskType) {
        TaskStrategy strategy = strategyMap.get(taskType);

        if (strategy == null) {
            throw new IllegalArgumentException(
                "지원하지 않는 TaskType입니다: " + taskType
            );
        }

        return strategy;
    }

    /**
     * Strategy 클래스명으로 TaskType 추론
     *
     * <p>Convention:
     * - MetaTaskStrategy → META
     * - MiniShopTaskStrategy → MINI_SHOP
     * - ProductDetailTaskStrategy → PRODUCT_DETAIL
     * - ProductOptionTaskStrategy → PRODUCT_OPTION
     *
     * @param strategy TaskStrategy 구현체
     * @return 추론된 TaskType
     */
    private TaskType resolveTaskType(TaskStrategy strategy) {
        String className = strategy.getClass().getSimpleName();

        if (className.startsWith("Meta")) {
            return TaskType.META;
        } else if (className.startsWith("MiniShop")) {
            return TaskType.MINI_SHOP;
        } else if (className.startsWith("ProductDetail")) {
            return TaskType.PRODUCT_DETAIL;
        } else if (className.startsWith("ProductOption")) {
            return TaskType.PRODUCT_OPTION;
        }

        throw new IllegalStateException(
            "TaskType을 추론할 수 없는 Strategy입니다: " + className
        );
    }

    /**
     * 모든 TaskType에 대한 Strategy가 등록되었는지 검증
     *
     * @throws IllegalStateException Strategy가 누락된 TaskType이 있는 경우
     */
    private void validateAllTaskTypesHaveStrategy() {
        for (TaskType taskType : TaskType.values()) {
            if (!strategyMap.containsKey(taskType)) {
                throw new IllegalStateException(
                    "TaskType에 대한 Strategy가 등록되지 않았습니다: " + taskType
                );
            }
        }
    }
}
