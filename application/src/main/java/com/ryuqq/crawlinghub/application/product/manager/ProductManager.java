package com.ryuqq.crawlinghub.application.product.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.product.builder.ProductSnapshotBuilder;
import com.ryuqq.crawlinghub.application.product.port.out.LoadProductPort;
import com.ryuqq.crawlinghub.application.product.port.out.LoadProductSnapshotPort;
import com.ryuqq.crawlinghub.application.product.port.out.SaveProductPort;
import com.ryuqq.crawlinghub.application.product.port.out.SaveProductSnapshotPort;
import com.ryuqq.crawlinghub.application.product.port.out.SaveProductSyncOutboxPort;
import com.ryuqq.crawlinghub.domain.product.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.DataHash;
import com.ryuqq.crawlinghub.domain.product.ProductData;
import com.ryuqq.crawlinghub.domain.product.ProductSnapshot;
import com.ryuqq.crawlinghub.domain.product.ProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ProductSyncOutboxCreatedEvent;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * Product 비즈니스 로직 Manager
 *
 * <p>핵심 책임:
 * <ul>
 *   <li>최초 크롤링 판단 (exists 체크)</li>
 *   <li>해시값 기반 변경 감지</li>
 *   <li>상품 데이터 업데이트 (미니샵/상세/옵션)</li>
 *   <li>ProductSnapshot 업데이트 (CDC 패턴)</li>
 *   <li>ProductSyncOutbox 저장 + Event 발행 (Hybrid Pattern)</li>
 * </ul>
 *
 * <p>Transaction 관리:
 * - 각 메서드는 독립적인 Transaction
 * - Outbox 저장 + Event 발행은 Transaction 내부
 * - 외부 API 호출은 EventListener/Scheduler가 담당 (Transaction 밖)
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class ProductManager {

    private static final Logger log = LoggerFactory.getLogger(ProductManager.class);

    private final LoadProductPort loadProductPort;
    private final SaveProductPort saveProductPort;
    private final LoadProductSnapshotPort loadSnapshotPort;
    private final SaveProductSnapshotPort saveSnapshotPort;
    private final SaveProductSyncOutboxPort saveOutboxPort;
    private final ProductSnapshotBuilder snapshotBuilder;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public ProductManager(
        LoadProductPort loadProductPort,
        SaveProductPort saveProductPort,
        LoadProductSnapshotPort loadSnapshotPort,
        SaveProductSnapshotPort saveSnapshotPort,
        SaveProductSyncOutboxPort saveOutboxPort,
        ProductSnapshotBuilder snapshotBuilder,
        ApplicationEventPublisher eventPublisher,
        ObjectMapper objectMapper
    ) {
        this.loadProductPort = loadProductPort;
        this.saveProductPort = saveProductPort;
        this.loadSnapshotPort = loadSnapshotPort;
        this.saveSnapshotPort = saveSnapshotPort;
        this.saveOutboxPort = saveOutboxPort;
        this.snapshotBuilder = snapshotBuilder;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    /**
     * 미니샵 데이터 처리
     *
     * <p>처리 흐름:
     * <ol>
     *   <li>최초 크롤링 여부 판단 (exists 체크)</li>
     *   <li>최초이면 신규 생성, 기존이면 업데이트</li>
     *   <li>해시값 계산 및 변경 감지</li>
     *   <li>완성 상태 확인 → 외부 발행</li>
     * </ol>
     *
     * @param mustitItemNo 머스트잇 상품 번호
     * @param sellerId 셀러 ID
     * @param rawData 미니샵 원본 데이터 (JSON)
     * @return 저장된 Product
     */
    @Transactional
    public CrawledProduct processMiniShopData(
        Long mustitItemNo,
        MustitSellerId sellerId,
        String rawData
    ) {
        log.info("미니샵 데이터 처리 시작. mustitItemNo={}, sellerId={}", mustitItemNo, sellerId.value());

        // 1. 최초 크롤링 여부 판단
        boolean isFirstCrawl = !loadProductPort.existsByMustitItemNoAndSellerId(mustitItemNo, sellerId);

        CrawledProduct product;
        if (isFirstCrawl) {
            log.info("최초 크롤링. 신규 Product 생성. mustitItemNo={}", mustitItemNo);
            product = CrawledProduct.forNew(mustitItemNo, sellerId);
        } else {
            product = loadProductPort.findByMustitItemNoAndSellerId(mustitItemNo, sellerId)
                .orElseThrow(() -> new IllegalStateException("Product를 찾을 수 없습니다: " + mustitItemNo));
            log.info("기존 Product 조회. productId={}", product.getIdValue());
        }

        // 2. 데이터 업데이트
        ProductData miniShopData = ProductData.of(rawData);
        product.updateMiniShopData(miniShopData);

        // 3. 해시값 계산 및 업데이트
        DataHash newHash = calculateHash(rawData);
        boolean hasChanged = product.hasDataChanged(newHash);

        if (hasChanged) {
            log.info("데이터 변경 감지. mustitItemNo={}", mustitItemNo);
            product.updateDataHash(newHash);
            product.incrementVersion();
        }

        // 4. 저장
        CrawledProduct savedProduct = saveProductPort.save(product);
        log.info("Product 저장 완료. productId={}, version={}", savedProduct.getIdValue(), savedProduct.getVersion());

        // 5. ProductSnapshot 업데이트 및 Outbox 저장 + Event 발행
        updateSnapshotAndCreateOutbox(savedProduct, isFirstCrawl, hasChanged);

        return savedProduct;
    }

    /**
     * 상세 데이터 처리
     *
     * <p>미니샵 데이터 처리와 동일한 흐름
     */
    @Transactional
    public CrawledProduct processDetailData(
        String mustitItemNo,
        MustitSellerId sellerId,
        String rawData
    ) {
        log.info("상세 데이터 처리 시작. mustitItemNo={}, sellerId={}", mustitItemNo, sellerId.value());

        // 1. Product 조회 (반드시 존재해야 함 - 미니샵이 먼저 처리됨)
        CrawledProduct product = loadProductPort.findByMustitItemNoAndSellerId(Long.parseLong(mustitItemNo), sellerId)
            .orElseThrow(() -> new IllegalStateException("Product를 찾을 수 없습니다: " + mustitItemNo));

        // 2. 데이터 업데이트
        ProductData detailData = ProductData.of(rawData);
        product.updateDetailData(detailData);

        // 3. 해시값 계산 및 변경 감지
        DataHash newHash = calculateHash(rawData);
        boolean hasChanged = product.hasDataChanged(newHash);

        if (hasChanged) {
            log.info("데이터 변경 감지. mustitItemNo={}", mustitItemNo);
            product.updateDataHash(newHash);
            product.incrementVersion();
        }

        // 4. 저장
        CrawledProduct savedProduct = saveProductPort.save(product);
        log.info("Product 저장 완료. productId={}, version={}", savedProduct.getIdValue(), savedProduct.getVersion());

        // 5. ProductSnapshot 업데이트 및 Outbox 저장 + Event 발행
        updateSnapshotAndCreateOutbox(savedProduct, false, hasChanged);

        return savedProduct;
    }

    /**
     * 옵션 데이터 처리
     *
     * <p>미니샵 데이터 처리와 동일한 흐름
     */
    @Transactional
    public CrawledProduct processOptionData(
        String mustitItemNo,
        MustitSellerId sellerId,
        String rawData
    ) {
        log.info("옵션 데이터 처리 시작. mustitItemNo={}, sellerId={}", mustitItemNo, sellerId.value());

        // 1. Product 조회 (반드시 존재해야 함 - 미니샵이 먼저 처리됨)
        CrawledProduct product = loadProductPort.findByMustitItemNoAndSellerId(Long.parseLong(mustitItemNo), sellerId)
            .orElseThrow(() -> new IllegalStateException("Product를 찾을 수 없습니다: " + mustitItemNo));

        // 2. 데이터 업데이트
        ProductData optionData = ProductData.of(rawData);
        product.updateOptionData(optionData);

        // 3. 해시값 계산 및 변경 감지
        DataHash newHash = calculateHash(rawData);
        boolean hasChanged = product.hasDataChanged(newHash);

        if (hasChanged) {
            log.info("데이터 변경 감지. mustitItemNo={}", mustitItemNo);
            product.updateDataHash(newHash);
            product.incrementVersion();
        }

        // 4. 저장
        CrawledProduct savedProduct = saveProductPort.save(product);
        log.info("Product 저장 완료. productId={}, version={}", savedProduct.getIdValue(), savedProduct.getVersion());

        // 5. ProductSnapshot 업데이트 및 Outbox 저장 + Event 발행
        updateSnapshotAndCreateOutbox(savedProduct, false, hasChanged);

        return savedProduct;
    }

    /**
     * ProductSnapshot 업데이트 및 Outbox 저장 (Hybrid Pattern)
     *
     * <p>처리 흐름:
     * <ol>
     *   <li>ProductSnapshot 조회 또는 생성</li>
     *   <li>ProductSnapshotBuilder로 변경 감지 및 업데이트</li>
     *   <li>변경이 있으면 ProductSyncOutbox 저장 (PENDING)</li>
     *   <li>ProductSyncOutboxCreatedEvent 발행 (Fast Path 트리거)</li>
     * </ol>
     *
     * <p>⚠️ Transaction 경계:
     * <ul>
     *   <li>이 메서드는 @Transactional 안에서 호출됨</li>
     *   <li>Outbox 저장 + Event 발행은 Transaction 내부</li>
     *   <li>Event는 트랜잭션 커밋 후 발행됨</li>
     * </ul>
     */
    private void updateSnapshotAndCreateOutbox(
        CrawledProduct product,
        boolean isFirstCrawl,
        boolean hasChanged
    ) {
        if (!product.isComplete()) {
            log.debug("Product가 미완성 상태. Snapshot 업데이트 안함. productId={}", product.getIdValue());
            return;
        }

        if (!isFirstCrawl && !hasChanged) {
            log.debug("변경 없음. Outbox 생성 안함. productId={}", product.getIdValue());
            return;
        }

        try {
            // 1. ProductSnapshot 조회 또는 생성
            ProductSnapshot snapshot = loadSnapshotPort.findById(product.getIdValue())
                .orElseGet(() -> ProductSnapshot.forNew(
                    product.getMustitItemNo(),
                    MustitSellerId.of(product.getSellerIdValue())
                ));

            // 2. ProductSnapshotBuilder로 변경 감지 및 업데이트
            // (여기서는 ProductSnapshotBuilder가 내부적으로 Event 발행함)
            // 하지만 우리는 Outbox 생성 시에만 Event 발행하도록 수정 필요

            // 3. Snapshot 저장
            ProductSnapshot savedSnapshot = saveSnapshotPort.save(snapshot);

            // 4. FullProductData JSON 직렬화
            String productJson = objectMapper.writeValueAsString(
                savedSnapshot.toFullProductData()
            );

            // 5. ProductSyncOutbox 저장 (PENDING)
            ProductSyncOutbox outbox = ProductSyncOutbox.create(
                product.getIdValue(),
                productJson
            );
            ProductSyncOutbox savedOutbox = saveOutboxPort.save(outbox);

            log.info("ProductSyncOutbox 저장 완료. outboxId={}, productId={}, status=PENDING",
                savedOutbox.getIdValue(), product.getIdValue());

            // 6. ProductSyncOutboxCreatedEvent 발행 (Fast Path)
            ProductSyncOutboxCreatedEvent event = new ProductSyncOutboxCreatedEvent(
                savedOutbox.getIdValue(),
                product.getIdValue(),
                productJson,
                LocalDateTime.now()
            );
            eventPublisher.publishEvent(event);

            log.info("ProductSyncOutboxCreatedEvent 발행 완료. outboxId={}", savedOutbox.getIdValue());

        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 실패. productId={}", product.getIdValue(), e);
            throw new RuntimeException("ProductSnapshot JSON 변환 실패", e);
        }
    }

    /**
     * SHA-256 해시 계산
     *
     * @param data 원본 데이터
     * @return 64자 해시 문자열
     */
    private DataHash calculateHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            String hashString = HexFormat.of().formatHex(hash);
            return DataHash.of(hashString);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다", e);
        }
    }
}
