package com.ryuqq.crawlinghub.application.useragent.service.command;

import com.ryuqq.crawlinghub.application.useragent.dto.command.UpdateUserAgentMetadataCommand;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentTransactionManager;
import com.ryuqq.crawlinghub.application.useragent.manager.query.UserAgentReadManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.UpdateUserAgentMetadataUseCase;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.exception.UserAgentNotFoundException;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentMetadata;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentString;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserAgent 메타데이터 수정 Service
 *
 * <p>{@link UpdateUserAgentMetadataUseCase} 구현체
 *
 * <p><strong>처리 순서</strong>:
 *
 * <ol>
 *   <li>ID로 기존 UserAgent 조회 (QueryPort)
 *   <li>존재하지 않으면 예외 발생
 *   <li>Domain 메서드로 메타데이터 수정
 *   <li>DB 저장 (PersistencePort)
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class UpdateUserAgentMetadataService implements UpdateUserAgentMetadataUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateUserAgentMetadataService.class);

    private final UserAgentReadManager readManager;
    private final UserAgentTransactionManager transactionManager;
    private final ClockHolder clockHolder;

    public UpdateUserAgentMetadataService(
            UserAgentReadManager readManager,
            UserAgentTransactionManager transactionManager,
            ClockHolder clockHolder) {
        this.readManager = readManager;
        this.transactionManager = transactionManager;
        this.clockHolder = clockHolder;
    }

    /**
     * UserAgent 메타데이터 수정
     *
     * <p>기존 UserAgent의 메타데이터를 수정하고 DB에 저장합니다.
     *
     * @param command 수정 Command (UserAgent ID, 메타데이터 정보 포함)
     * @throws UserAgentNotFoundException UserAgent를 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public void updateMetadata(UpdateUserAgentMetadataCommand command) {
        UserAgentId userAgentId = UserAgentId.of(command.userAgentId());
        log.info("UserAgent 메타데이터 수정 시작: id={}", command.userAgentId());

        UserAgent userAgent =
                readManager
                        .findById(userAgentId)
                        .orElseThrow(() -> new UserAgentNotFoundException(userAgentId));

        Clock clock = clockHolder.getClock();

        UserAgentString newUserAgentString =
                command.userAgentString() != null
                        ? UserAgentString.of(command.userAgentString())
                        : userAgent.getUserAgentString();

        UserAgentMetadata currentMetadata = userAgent.getMetadata();
        UserAgentMetadata newMetadata =
                UserAgentMetadata.of(
                        command.deviceBrand() != null
                                ? command.deviceBrand()
                                : currentMetadata.getDeviceBrand(),
                        command.osType() != null ? command.osType() : currentMetadata.getOsType(),
                        command.osVersion() != null
                                ? command.osVersion()
                                : currentMetadata.getOsVersion(),
                        command.browserType() != null
                                ? command.browserType()
                                : currentMetadata.getBrowserType(),
                        command.browserVersion() != null
                                ? command.browserVersion()
                                : currentMetadata.getBrowserVersion());

        UserAgent updatedUserAgent =
                UserAgent.reconstitute(
                        userAgent.getId(),
                        userAgent.getToken(),
                        newUserAgentString,
                        command.deviceType() != null
                                ? command.deviceType()
                                : userAgent.getDeviceType(),
                        newMetadata,
                        userAgent.getStatus(),
                        userAgent.getHealthScore(),
                        userAgent.getLastUsedAt(),
                        userAgent.getRequestsPerDay(),
                        userAgent.getCreatedAt(),
                        clock.instant());

        transactionManager.persist(updatedUserAgent);

        log.info("UserAgent 메타데이터 수정 완료: id={}", command.userAgentId());
    }
}
