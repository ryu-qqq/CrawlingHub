package com.ryuqq.crawlinghub.application.mustit.seller.usecase;

import com.ryuqq.crawlinghub.application.mustit.seller.dto.command.UpdateMustitSellerCommand;
import com.ryuqq.crawlinghub.application.mustit.seller.port.in.UpdateMustitSellerUseCase;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.LoadMustitSellerPort;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.SaveMustitSellerPort;
import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlInterval;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.mustit.seller.exception.SellerNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 머스트잇 셀러 수정 UseCase
 * <p>
 * 기존 셀러의 활성 상태 및 크롤링 주기를 수정하는 비즈니스 로직을 처리합니다.
 * </p>
 * <p>
 * 주요 책임:
 * <ul>
 *   <li>sellerId 존재 여부 검증</li>
 *   <li>활성 상태 변경 (activate/deactivate)</li>
 *   <li>크롤링 주기 변경 (Domain Event 발행)</li>
 *   <li>변경된 Aggregate 저장</li>
 * </ul>
 * </p>
 * <p>
 * 비즈니스 규칙:
 * <ul>
 *   <li>sellerId는 수정 불가 (PK)</li>
 *   <li>크롤링 주기 변경 시 SellerCrawlIntervalChangedEvent 발행</li>
 *   <li>변경된 Aggregate 저장</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Service
public class UpdateMustitSellerService implements UpdateMustitSellerUseCase {

    private final LoadMustitSellerPort loadMustitSellerPort;
    private final SaveMustitSellerPort saveMustitSellerPort;

    /**
     * UseCase 생성자
     *
     * @param loadMustitSellerPort 셀러 조회 Port
     * @param saveMustitSellerPort 셀러 저장 Port
     */
    public UpdateMustitSellerService(
            LoadMustitSellerPort loadMustitSellerPort,
            SaveMustitSellerPort saveMustitSellerPort
    ) {
        this.loadMustitSellerPort = Objects.requireNonNull(loadMustitSellerPort);
        this.saveMustitSellerPort = Objects.requireNonNull(saveMustitSellerPort);
    }

    /**
     * 기존 셀러의 정보를 수정합니다.
     * <p>
     * 트랜잭션 내에서 다음 작업을 수행합니다:
     * <ol>
     *   <li>sellerId로 기존 셀러 조회 (존재하지 않으면 예외)</li>
     *   <li>활성 상태 변경 (요청된 경우)</li>
     *   <li>크롤링 주기 변경 (요청된 경우, Domain Event 발행)</li>
     *   <li>변경된 셀러 저장 (Domain Event는 트랜잭션 커밋 시 자동 발행)</li>
     * </ol>
     * </p>
     *
     * @param command 셀러 수정 Command
     * @return 수정된 셀러 Aggregate
     * @throws IllegalArgumentException command가 null인 경우
     * @throws SellerNotFoundException sellerId가 존재하지 않는 경우
     */
    @Transactional
    @Override
    public MustitSeller execute(UpdateMustitSellerCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        // 1. 기존 셀러 조회
        MustitSeller seller = loadMustitSellerPort.findBySellerId(command.sellerId())
                .orElseThrow(() -> new SellerNotFoundException(command.sellerId()));

        // 2. 활성 상태 변경 (요청된 경우)
        if (command.hasActiveUpdate()) {
            updateActiveStatus(seller, command.isActive());
        }

        // 3. 크롤링 주기 변경 (요청된 경우, Domain Event 발행)
        if (command.hasCrawlIntervalUpdate()) {
            CrawlInterval newInterval = new CrawlInterval(
                    command.intervalType(),
                    command.intervalValue()
            );
            seller.updateCrawlInterval(newInterval);
        }

        // 4. 변경된 셀러 저장
        // Domain Event는 Persistence Adapter에서 Repository save() 호출 시
        // 트랜잭션 커밋 시점에 자동으로 발행됩니다.
        return saveMustitSellerPort.save(seller);
    }

    /**
     * 활성 상태를 변경합니다.
     *
     * @param seller 대상 셀러 Aggregate
     * @param isActive 새로운 활성 상태
     */
    private void updateActiveStatus(MustitSeller seller, boolean isActive) {
        if (isActive) {
            seller.activate();
        } else {
            seller.deactivate();
        }
    }
}
