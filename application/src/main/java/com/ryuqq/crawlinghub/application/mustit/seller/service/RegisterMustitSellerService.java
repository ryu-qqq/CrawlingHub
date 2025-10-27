package com.ryuqq.crawlinghub.application.mustit.seller.service;

import com.ryuqq.crawlinghub.application.mustit.seller.dto.command.RegisterMustitSellerCommand;
import com.ryuqq.crawlinghub.application.mustit.seller.port.in.RegisterMustitSellerUseCase;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.LoadMustitSellerPort;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.SaveMustitSellerPort;
import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlInterval;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.mustit.seller.exception.DuplicateSellerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 머스트잇 셀러 등록 UseCase
 * <p>
 * 새로운 셀러를 등록하는 비즈니스 로직을 처리합니다.
 * </p>
 * <p>
 * 주요 책임:
 * <ul>
 *   <li>셀러 ID 중복 검증</li>
 *   <li>크롤링 주기 Value Object 생성</li>
 *   <li>셀러 Aggregate 생성 및 저장</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Service
public class RegisterMustitSellerService implements RegisterMustitSellerUseCase {

    private final LoadMustitSellerPort loadMustitSellerPort;
    private final SaveMustitSellerPort saveMustitSellerPort;

    /**
     * UseCase 생성자
     *
     * @param loadMustitSellerPort 셀러 조회 Port
     * @param saveMustitSellerPort 셀러 저장 Port
     */
    public RegisterMustitSellerService(
            LoadMustitSellerPort loadMustitSellerPort,
            SaveMustitSellerPort saveMustitSellerPort
    ) {
        this.loadMustitSellerPort = Objects.requireNonNull(loadMustitSellerPort);
        this.saveMustitSellerPort = Objects.requireNonNull(saveMustitSellerPort);
    }

    /**
     * 새로운 셀러를 등록합니다.
     * <p>
     * 트랜잭션 내에서 다음 작업을 수행합니다:
     * <ol>
     *   <li>sellerId 중복 검증</li>
     *   <li>CrawlInterval Value Object 생성</li>
     *   <li>MustitSeller Aggregate 생성 (활성 상태 기본값: true)</li>
     *   <li>셀러 저장</li>
     * </ol>
     * </p>
     *
     * @param command 셀러 등록 Command
     * @return 등록된 셀러 Aggregate
     * @throws DuplicateSellerException 이미 동일한 sellerId가 존재하는 경우
     * @throws IllegalArgumentException command가 null이거나 유효하지 않은 경우
     */
    @Transactional
    public MustitSeller execute(RegisterMustitSellerCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        // 1. 중복 검증
        validateDuplicateSeller(command.sellerId());

        // 2. CrawlInterval Value Object 생성
        CrawlInterval crawlInterval = new CrawlInterval(
                command.intervalType(),
                command.intervalValue()
        );

        // 3. MustitSeller Aggregate 생성 (활성 상태 기본값: true)
        MustitSeller seller = new MustitSeller(
                command.sellerId(),
                command.name(),
                crawlInterval
        );

        // 4. 셀러 저장
        return saveMustitSellerPort.save(seller);
    }

    /**
     * sellerId 중복 검증
     *
     * @param sellerId 검증할 셀러 ID
     * @throws DuplicateSellerException 이미 동일한 sellerId가 존재하는 경우
     */
    private void validateDuplicateSeller(String sellerId) {
        if (loadMustitSellerPort.existsBySellerId(sellerId)) {
            throw new DuplicateSellerException(sellerId);
        }
    }
}
