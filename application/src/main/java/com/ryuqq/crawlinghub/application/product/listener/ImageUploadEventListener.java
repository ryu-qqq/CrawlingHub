package com.ryuqq.crawlinghub.application.product.listener;

// TODO: Phase 2 - 이미지 업로드 기능 구현 시 활성화
// 현재는 2-stage pipeline (Raw → CrawledProduct) 구현이 우선

/**
 * 이미지 업로드 요청 이벤트 리스너
 *
 * <p><strong>용도</strong>: 트랜잭션 커밋 후 파일서버 API 호출 및 Outbox 상태 업데이트
 *
 * <p><strong>현재 상태</strong>: Phase 2에서 구현 예정 (임시 비활성화)
 *
 * <p><strong>처리 흐름</strong>:
 * <ol>
 *   <li>이벤트 수신 (트랜잭션 커밋 후)
 *   <li>Outbox 조회 및 상태 PROCESSING으로 변경
 *   <li>파일서버 API 호출 (비동기 업로드 요청)
 *   <li>실패 시: Outbox 상태 → FAILED (재시도 스케줄러에서 처리)
 * </ol>
 *
 * <p><strong>완료 처리</strong>: 파일서버 웹훅에서 완료 콜백 수신
 *
 * @author development-team
 * @since 1.0.0
 */
// @Component  // TODO: Phase 2에서 활성화
public class ImageUploadEventListener {

    // 임시 비활성화 - 컴파일 오류 방지용 스텁
    public ImageUploadEventListener() {
    }

    // Phase 2에서 아래 코드 활성화 예정
    // 필요한 의존성:
    // - ImageOutboxQueryPort
    // - ImageOutboxManager
    // - FileServerClient
    // - ImageUploadRequestedEvent (도메인 이벤트 완성 필요)
}
