package com.ryuqq.crawlinghub.application.monitoring.service;


import com.ryuqq.crawlinghub.application.monitoring.assembler.MonitoringAssembler;
import com.ryuqq.crawlinghub.application.monitoring.dto.query.TaskProgressQuery;
import com.ryuqq.crawlinghub.application.monitoring.dto.response.TaskProgressResponse;
import com.ryuqq.crawlinghub.application.monitoring.port.in.GetTaskProgressUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.out.TaskProgressPort;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.LoadSellerPort;
import com.ryuqq.crawlinghub.domain.crawl.task.TaskType;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSellerId;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 태스크 진행 상황 조회 UseCase 구현체
 *
 * <p>특정 셀러의 크롤링 태스크 진행 상황을 실시간으로 조회합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@Service
public class GetTaskProgressService implements GetTaskProgressUseCase {

    private final LoadSellerPort loadSellerPort;
    private final TaskProgressPort taskProgressPort;

    public GetTaskProgressService(
        LoadSellerPort loadSellerPort,
        TaskProgressPort taskProgressPort
    ) {
        this.loadSellerPort = loadSellerPort;
        this.taskProgressPort = taskProgressPort;
    }

    /**
     * 태스크 진행 상황 조회
     *
     * <p>실행 순서:
     * 1. 셀러 조회 (이름 확인)
     * 2. 전체 진행 상황 조회
     * 3. 태스크 유형별 세부 통계 조회
     * 4. 응답 생성
     *
     * @param query 진행 상황 조회 Query
     * @return 태스크 진행 상황
     * @throws IllegalArgumentException 셀러를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public TaskProgressResponse execute(TaskProgressQuery query) {
        MustitSellerId sellerId = MustitSellerId.of(query.sellerId());

        // 1. 셀러 조회
        MustitSeller seller = loadSellerPort.findById(sellerId)
            .orElseThrow(() -> new IllegalArgumentException(
                "셀러를 찾을 수 없습니다: " + query.sellerId()
            ));

        // 2. 전체 진행 상황 조회
        TaskProgressPort.ProgressStats progressStats = taskProgressPort.getProgressStats(sellerId);

        // 3. 태스크 유형별 세부 통계 조회
        Map<TaskType, TaskProgressPort.TypeStats> typeStatsMap =
            taskProgressPort.getTaskTypeBreakdown(sellerId);

        // 4. 응답 생성
        return MonitoringAssembler.toTaskProgressResponse(
            seller.getIdValue(),
            seller.getSellerName(),
            progressStats,
            typeStatsMap
        );
    }
}
