package com.ryuqq.crawlinghub.application.product.manager;

import com.ryuqq.crawlinghub.application.product.port.out.command.ImageOutboxPersistencePort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 이미지 업로드 Outbox 트랜잭션 관리자
 *
 * <p><strong>책임</strong>:
 * <ul>
 *   <li>ImageOutbox 영속성 관리
 *   <li>Outbox 상태 전환 관리
 *   <li>트랜잭션 경계 관리
 * </ul>
 *
 * <p><strong>상태 전환</strong>:
 * <pre>
 * PENDING → PROCESSING (파일서버 API 호출 시작)
 * PROCESSING → COMPLETED (웹훅 수신 - 업로드 성공)
 * PROCESSING → FAILED (타임아웃 또는 오류)
 * FAILED → PENDING (재시도 가능 시)
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */

public class ImageOutboxManager {

    // private final ImageOutboxPersistencePort imageOutboxPersistencePort;
    //
    // public ImageOutboxManager(ImageOutboxPersistencePort imageOutboxPersistencePort) {
    //     this.imageOutboxPersistencePort = imageOutboxPersistencePort;
    // }
    //
    // // === 생성 ===
    //
    // /**
    //  * 이미지 업로드 Outbox 생성 및 저장
    //  *
    //  * @param crawledProductId CrawledProduct ID
    //  * @param originalUrl 원본 이미지 URL
    //  * @param imageType 이미지 타입
    //  * @return 저장된 ImageOutbox
    //  */
    // @Transactional
    // public CrawledProductImageOutbox create(
    //         CrawledProductId crawledProductId,
    //         String originalUrl,
    //         ImageType imageType) {
    //     CrawledProductImageOutbox outbox = CrawledProductImageOutbox.forNew(
    //             crawledProductId, originalUrl, imageType);
    //     imageOutboxPersistencePort.persist(outbox);
    //     return outbox;
    // }
    //
    // /**
    //  * 이미지 업로드 Outbox 일괄 생성 및 저장
    //  *
    //  * @param crawledProductId CrawledProduct ID
    //  * @param imageUrls 이미지 URL 목록
    //  * @param imageType 이미지 타입
    //  * @return 저장된 ImageOutbox 목록
    //  */
    // @Transactional
    // public List<CrawledProductImageOutbox> createAll(
    //         CrawledProductId crawledProductId,
    //         List<String> imageUrls,
    //         ImageType imageType) {
    //     List<CrawledProductImageOutbox> outboxes = imageUrls.stream()
    //             .map(url -> CrawledProductImageOutbox.forNew(crawledProductId, url, imageType))
    //             .toList();
    //     imageOutboxPersistencePort.persistAll(outboxes);
    //     return outboxes;
    // }
    //
    // // === 상태 전환 ===
    //
    // /**
    //  * 처리 시작 (파일서버 API 호출 시작)
    //  *
    //  * @param outbox 처리 시작할 Outbox
    //  */
    // @Transactional
    // public void markAsProcessing(CrawledProductImageOutbox outbox) {
    //     outbox.markAsProcessing();
    //     imageOutboxPersistencePort.update(outbox);
    // }
    //
    // /**
    //  * 업로드 완료 (웹훅 수신)
    //  *
    //  * @param outbox 완료할 Outbox
    //  * @param s3Url 업로드된 S3 URL
    //  */
    // @Transactional
    // public void markAsCompleted(CrawledProductImageOutbox outbox, String s3Url) {
    //     outbox.markAsCompleted(s3Url);
    //     imageOutboxPersistencePort.update(outbox);
    // }
    //
    // /**
    //  * 처리 실패
    //  *
    //  * @param outbox 실패한 Outbox
    //  * @param errorMessage 오류 메시지
    //  */
    // @Transactional
    // public void markAsFailed(CrawledProductImageOutbox outbox, String errorMessage) {
    //     outbox.markAsFailed(errorMessage);
    //     imageOutboxPersistencePort.update(outbox);
    // }
    //
    // /**
    //  * 재시도를 위해 PENDING으로 복귀
    //  *
    //  * @param outbox 재시도할 Outbox
    //  */
    // @Transactional
    // public void resetToPending(CrawledProductImageOutbox outbox) {
    //     if (outbox.canRetry()) {
    //         outbox.resetToPending();
    //         imageOutboxPersistencePort.update(outbox);
    //     }
    // }
}
