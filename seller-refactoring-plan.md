# 셀러 바운디드 컨텍스트 리팩토링 작업 계획서 v2

> **🎯 Cursor AI 작업 가이드**
> 이 문서는 Cursor AI에게 작업을 위임하기 위한 상세 명세서입니다.
> **작업 시작**: `/queue-add seller` → `/queue-start seller`

**작성일**: 2025-11-05
**작성자**: Claude Code
**프로젝트**: CrawlingHub - Seller Bounded Context Refactoring
**목적**: CQRS 패턴, Manager/Facade 패턴, PageResponse 통합 적용

---

## 📋 목차

1. [요구사항 분석](#1-요구사항-분석)
2. [아키텍처 패턴 적용](#2-아키텍처-패턴-적용)
3. [Domain Layer 작업 계획](#3-domain-layer-작업-계획)
4. [Application Layer 작업 계획](#4-application-layer-작업-계획)
5. [Persistence Layer 작업 계획 (CQRS)](#5-persistence-layer-작업-계획-cqrs)
6. [REST API Layer 작업 계획](#6-rest-api-layer-작업-계획)
7. [테스트 전략](#7-테스트-전략)
8. [작업 단계 (Work Phases)](#8-작업-단계-work-phases)
9. [Cursor AI 작업 큐 가이드](#9-cursor-ai-작업-큐-가이드)
10. [Zero-Tolerance 체크리스트](#10-zero-tolerance-체크리스트)

---

## 1. 요구사항 분석

### 1.1 기능 요구사항

#### REST API Layer
1. **셀러 등록 (POST /api/v1/sellers)** ✅ 이미 존재
2. **셀러 상태 변경 (PUT /api/v1/sellers/{sellerId})** ✅ 이미 존재
3. **셀러 목록 조회 (GET /api/v1/sellers)** ✅ 이미 존재
   - 응답: 셀러명, 상태
4. **셀러 상세 조회 (GET /api/v1/sellers/{sellerId})** ✅ 이미 존재
   - 응답: 기본 정보 + 아래 추가 정보
   - 총 상품 수 ✅
   - **🆕 상품 수 변경 이력** (신규)
   - **🆕 크롤링 스케줄 정보** (신규)
   - **🆕 크롤링 실행 이력** (신규)

#### 신규 요구사항 (수정됨) ⭐

1. **상품 수 변경 이력** (ProductCountHistory) - **구조 단순화**
   - ❌ ~~변경 전 수량 (previousCount)~~ (제거)
   - ✅ **실행 날짜** (executedDate)
   - ✅ **카운트 된 수** (productCount)
   - **이유**: 이력 추적 시 변경 전 수량은 불필요, 해당 날짜의 실제 카운트만 저장

2. **크롤링 스케줄 정보** (CrawlSchedule)
   - **위치**: `application/src/main/java/com/ryuqq/crawlinghub/application/crawl/schedule/`
   - 셀러별 크롤링 스케줄 조회
   - Cron 표현식, 상태, 다음 실행 시간

3. **크롤링 실행 이력** (CrawlScheduleHistory)
   - 스케줄 실행 이력 조회
   - 실행 시작/종료 시간, 성공/실패 상태

---

## 2. 아키텍처 패턴 적용

### 2.1 CQRS 패턴 (Command/Query 분리) ⭐

#### Persistence Layer 구조
```
adapter-out/persistence-mysql/
└── com/ryuqq/crawlinghub/adapter/out/persistence/mustit/seller/
    ├── entity/
    │   ├── MustitSellerEntity.java
    │   └── ProductCountHistoryEntity.java
    ├── repository/
    │   ├── MustitSellerJpaRepository.java (Command - JPA)
    │   ├── ProductCountHistoryJpaRepository.java (Command - JPA)
    │   └── ProductCountHistoryQueryRepository.java (Query - QueryDSL) ⭐ 신규
    ├── mapper/
    │   ├── MustitSellerMapper.java (MapStruct)
    │   └── ProductCountHistoryMapper.java (MapStruct)
    └── adapter/
        ├── MustitSellerCommandAdapter.java (Command Adapter)
        ├── MustitSellerQueryAdapter.java (Query Adapter)
        ├── ProductCountHistoryCommandAdapter.java (Command Adapter) ⭐ 신규
        └── ProductCountHistoryQueryAdapter.java (Query Adapter) ⭐ 신규
```

#### CQRS 원칙
1. **Command (쓰기)**: JPA Repository 사용
   - `save()`, `delete()`, `update()` 등
   - `@Transactional` 적용
   - Command Adapter에서 처리

2. **Query (읽기)**: QueryDSL 사용 ⭐
   - `SELECT` 쿼리만 수행
   - N+1 문제 방지
   - DTO Projection 최적화
   - Query Adapter에서 처리

### 2.2 Manager 패턴 적용 ⭐

**위치**: `application/.../component/`

#### SellerManager 역할
```java
@Component
public class SellerManager {

    private final SaveSellerPort saveSellerPort;
    private final LoadSellerPort loadSellerPort;
    private final SaveProductCountHistoryPort saveHistoryPort; // 추가

    /**
     * 상품 수 업데이트 + 자동 이력 저장
     *
     * Manager가 횡단 관심사 처리:
     * - Seller Domain 업데이트
     * - ProductCountHistory 자동 생성
     * - 트랜잭션 조율
     */
    @Transactional
    public void updateProductCountWithHistory(MustitSeller seller, Integer newCount) {
        // 1. Seller 업데이트
        seller.updateProductCount(newCount);
        saveSellerPort.save(seller);

        // 2. 이력 자동 저장
        ProductCountHistory history = ProductCountHistory.record(
            MustitSellerId.of(seller.getIdValue()),
            newCount,
            LocalDateTime.now()
        );
        saveHistoryPort.saveHistory(history);
    }
}
```

### 2.3 Facade 패턴 적용 ⭐

**위치**: `application/.../facade/`

#### SellerCommandFacade 역할
```java
@Service
public class SellerCommandFacade {

    private final RegisterSellerUseCase registerSellerUseCase;
    private final UpdateSellerStatusUseCase updateSellerStatusUseCase;
    private final SellerManager sellerManager; // Manager 주입

    /**
     * 셀러 등록 Facade
     *
     * 여러 UseCase 조율:
     * - 셀러 등록
     * - 초기 이력 생성
     */
    @Transactional
    public SellerResponse registerSellerWithInitialHistory(RegisterSellerCommand command) {
        // 1. UseCase 호출
        SellerResponse response = registerSellerUseCase.register(command);

        // 2. Manager를 통한 초기 이력 생성
        MustitSeller seller = loadSellerPort.load(response.sellerId());
        sellerManager.updateProductCountWithHistory(seller, 0);

        return response;
    }
}
```

### 2.4 PageResponse/SliceResponse 통합 ⭐

#### Application Layer 사용
```java
// Application Layer DTO
public record GetProductCountHistoriesQuery(
    Long sellerId,
    int page,
    int size
) {}

// Application Layer UseCase
@Service
public class GetProductCountHistoriesService implements GetProductCountHistoriesUseCase {

    @Override
    public PageResponse<ProductCountHistoryResponse> getHistories(GetProductCountHistoriesQuery query) {
        // PageResponse 반환 (Application Layer 공통 DTO)
        return PageResponse.of(
            historyList,
            query.page(),
            query.size(),
            totalElements,
            totalPages,
            isFirst,
            isLast
        );
    }
}
```

#### REST API Layer 변환
```java
// REST API Mapper
@Component
public class SellerApiMapper {

    /**
     * Application PageResponse → REST API PageApiResponse 변환
     */
    public PageApiResponse<ProductCountHistoryApiResponse> toPageApiResponse(
        PageResponse<ProductCountHistoryResponse> pageResponse
    ) {
        return PageApiResponse.of(
            pageResponse.content().stream()
                .map(this::toProductCountHistoryApiResponse)
                .toList(),
            pageResponse.page(),
            pageResponse.size(),
            pageResponse.totalElements(),
            pageResponse.totalPages(),
            pageResponse.first(),
            pageResponse.last()
        );
    }
}
```

---

## 3. Domain Layer 작업 계획

### 3.1 ProductCountHistory 도메인 객체 (수정됨) ⭐

**위치**: `domain/src/main/java/com/ryuqq/crawlinghub/domain/mustit/seller/history/ProductCountHistory.java`

```java
package com.ryuqq.crawlinghub.domain.mustit.seller.history;

import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;

import java.time.LocalDateTime;

/**
 * ProductCountHistory - 상품 수 변경 이력
 *
 * <p>Pure Java 도메인 객체 (Lombok 금지)</p>
 *
 * <p><strong>🆕 변경사항 (v2):</strong></p>
 * <ul>
 *   <li>❌ previousCount 필드 제거</li>
 *   <li>✅ executedDate + productCount만 저장</li>
 *   <li>✅ 이유: 변경 전 수량은 이력 추적에 불필요</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public class ProductCountHistory {

    private final ProductCountHistoryId id;
    private final MustitSellerId sellerId;
    private final Integer productCount;         // 카운트 된 수 ⭐
    private final LocalDateTime executedDate;   // 실행 날짜 ⭐

    // Private Constructor (Factory Method 강제)
    private ProductCountHistory(
        ProductCountHistoryId id,
        MustitSellerId sellerId,
        Integer productCount,
        LocalDateTime executedDate
    ) {
        this.id = id;
        this.sellerId = sellerId;
        this.productCount = productCount;
        this.executedDate = executedDate;
    }

    /**
     * Factory Method - 새로운 이력 기록
     *
     * @param sellerId 셀러 ID
     * @param productCount 실행 시점 상품 수
     * @param executedDate 실행 날짜
     * @return ProductCountHistory
     */
    public static ProductCountHistory record(
        MustitSellerId sellerId,
        Integer productCount,
        LocalDateTime executedDate
    ) {
        validateProductCount(productCount);
        return new ProductCountHistory(
            null, // ID는 Persistence Layer에서 할당
            sellerId,
            productCount,
            executedDate
        );
    }

    /**
     * Factory Method - 기존 이력 복원 (Persistence → Domain)
     */
    public static ProductCountHistory reconstitute(
        ProductCountHistoryId id,
        MustitSellerId sellerId,
        Integer productCount,
        LocalDateTime executedDate
    ) {
        return new ProductCountHistory(id, sellerId, productCount, executedDate);
    }

    /**
     * 상품 수 검증
     */
    private static void validateProductCount(Integer productCount) {
        if (productCount == null || productCount < 0) {
            throw new IllegalArgumentException("상품 수는 0 이상이어야 합니다");
        }
    }

    // Getters (Pure Java)
    public ProductCountHistoryId getId() {
        return id;
    }

    public MustitSellerId getSellerId() {
        return sellerId;
    }

    public Long getSellerIdValue() {
        return sellerId.value();
    }

    public Integer getProductCount() {
        return productCount;
    }

    public LocalDateTime getExecutedDate() {
        return executedDate;
    }

    /**
     * 날짜 변경 체크 (동일 날짜에 중복 저장 방지)
     */
    public boolean isSameDate(LocalDateTime other) {
        return this.executedDate.toLocalDate().equals(other.toLocalDate());
    }
}
```

### 3.2 ProductCountHistoryId Value Object

**위치**: `domain/src/main/java/com/ryuqq/crawlinghub/domain/mustit/seller/history/ProductCountHistoryId.java`

```java
package com.ryuqq.crawlinghub.domain.mustit.seller.history;

/**
 * ProductCountHistoryId - 상품 수 이력 식별자
 *
 * <p>Record 패턴 사용 (Java 21)</p>
 *
 * @param value Long FK (ID)
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record ProductCountHistoryId(Long value) {

    /**
     * Compact Constructor - 검증 로직
     */
    public ProductCountHistoryId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ProductCountHistoryId는 양수여야 합니다");
        }
    }

    /**
     * Factory Method
     */
    public static ProductCountHistoryId of(Long value) {
        return new ProductCountHistoryId(value);
    }
}
```

---

## 4. Application Layer 작업 계획

### 4.1 신규 Port 인터페이스

#### 4.1.1 SaveProductCountHistoryPort (Command Port)

**위치**: `application/.../port/out/SaveProductCountHistoryPort.java`

```java
package com.ryuqq.crawlinghub.application.mustit.seller.port.out;

import history.com.ryuqq.crawlinghub.domain.seller.ProductCountHistory;

/**
 * SaveProductCountHistoryPort - 상품 수 이력 저장 포트 (Command)
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public interface SaveProductCountHistoryPort {

    /**
     * 상품 수 이력 저장
     *
     * @param history 이력 Domain 객체
     * @return 저장된 ProductCountHistory
     */
    ProductCountHistory saveHistory(ProductCountHistory history);
}
```

#### 4.1.2 LoadProductCountHistoryPort (Query Port) ⭐

**위치**: `application/.../port/out/LoadProductCountHistoryPort.java`

```java
package com.ryuqq.crawlinghub.application.mustit.seller.port.out;

import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import history.com.ryuqq.crawlinghub.domain.seller.ProductCountHistory;

import java.util.List;

/**
 * LoadProductCountHistoryPort - 상품 수 이력 조회 포트 (Query)
 *
 * <p>QueryDSL로 최적화된 조회 ⭐</p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public interface LoadProductCountHistoryPort {

    /**
     * 셀러별 상품 수 이력 조회 (페이징)
     *
     * @param sellerId 셀러 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return ProductCountHistory 리스트
     */
    List<ProductCountHistory> loadHistories(MustitSellerId sellerId, int page, int size);

    /**
     * 전체 이력 개수 조회
     *
     * @param sellerId 셀러 ID
     * @return 전체 개수
     */
    long countHistories(MustitSellerId sellerId);
}
```

#### 4.1.3 LoadSchedulePort / LoadScheduleHistoryPort

**위치**: `application/crawl/schedule/port/out/`

```java
/**
 * LoadSchedulePort - 스케줄 조회 포트
 *
 * <p>이미 존재하는 Port, 신규 메서드 추가</p>
 */
public interface LoadSchedulePort {

    /**
     * 셀러별 스케줄 조회
     */
    Optional<CrawlSchedule> findBySellerId(MustitSellerId sellerId);
}

/**
 * LoadScheduleHistoryPort - 스케줄 실행 이력 조회 포트
 *
 * <p>이미 존재하는 Port, 신규 메서드 추가</p>
 */
public interface LoadScheduleHistoryPort {

    /**
     * 스케줄별 실행 이력 조회 (페이징)
     */
    List<CrawlScheduleHistory> loadHistories(CrawlScheduleId scheduleId, int page, int size);

    /**
     * 전체 이력 개수 조회
     */
    long countHistories(CrawlScheduleId scheduleId);
}
```

### 4.2 SellerManager (Manager Pattern) ⭐

**위치**: `application/.../component/SellerManager.java`

```java
package com.ryuqq.crawlinghub.application.mustit.seller.component;

import out.port.com.ryuqq.crawlinghub.application.seller.LoadSellerPort;
import out.port.com.ryuqq.crawlinghub.application.seller.SaveProductCountHistoryPort;
import out.port.com.ryuqq.crawlinghub.application.seller.SaveSellerPort;
import com.ryuqq.crawlinghub.domain.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import history.com.ryuqq.crawlinghub.domain.seller.ProductCountHistory;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * SellerManager - Seller Bounded Context 상태 관리
 *
 * <p><strong>Manager 패턴 적용 ⭐</strong></p>
 * <ul>
 *   <li>횡단 관심사 처리 (상품 수 업데이트 + 이력 자동 저장)</li>
 *   <li>트랜잭션 조율</li>
 *   <li>Bounded Context 내 상태 변경 관리</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class SellerManager {

    private final SaveSellerPort saveSellerPort;
    private final LoadSellerPort loadSellerPort;
    private final SaveProductCountHistoryPort saveHistoryPort;

    public SellerManager(
        SaveSellerPort saveSellerPort,
        LoadSellerPort loadSellerPort,
        SaveProductCountHistoryPort saveHistoryPort
    ) {
        this.saveSellerPort = saveSellerPort;
        this.loadSellerPort = loadSellerPort;
        this.saveHistoryPort = saveHistoryPort;
    }

    /**
     * 상품 수 업데이트 + 자동 이력 저장
     *
     * <p>Manager가 횡단 관심사 처리:
     * <ol>
     *   <li>Seller Domain 업데이트</li>
     *   <li>ProductCountHistory 자동 생성</li>
     *   <li>하나의 트랜잭션으로 조율</li>
     * </ol>
     *
     * @param seller Seller Domain 객체
     * @param newCount 새로운 상품 수
     */
    @Transactional
    public void updateProductCountWithHistory(MustitSeller seller, Integer newCount) {
        // 1. Seller 업데이트
        seller.updateProductCount(newCount);
        saveSellerPort.save(seller);

        // 2. 이력 자동 저장
        ProductCountHistory history = ProductCountHistory.record(
            MustitSellerId.of(seller.getIdValue()),
            newCount,
            LocalDateTime.now()
        );
        saveHistoryPort.saveHistory(history);
    }

    /**
     * 셀러 등록 (Manager가 일관된 저장 방식 제공)
     *
     * @param seller Seller Domain 객체
     * @return 저장된 MustitSeller
     */
    @Transactional
    public MustitSeller registerSeller(MustitSeller seller) {
        return saveSellerPort.save(seller);
    }

    /**
     * 셀러 상태 업데이트
     *
     * @param sellerId 셀러 ID
     * @return 업데이트된 MustitSeller
     */
    @Transactional
    public MustitSeller updateSellerStatus(Long sellerId) {
        return loadSellerPort.load(sellerId);
    }
}
```

### 4.3 SellerCommandFacade (Facade Pattern) ⭐

**위치**: `application/.../facade/SellerCommandFacade.java`

```java
package com.ryuqq.crawlinghub.application.mustit.seller.facade;

import component.com.ryuqq.crawlinghub.application.seller.SellerManager;
import command.dto.com.ryuqq.crawlinghub.application.seller.RegisterSellerCommand;
import response.dto.com.ryuqq.crawlinghub.application.seller.SellerResponse;
import in.port.com.ryuqq.crawlinghub.application.seller.RegisterSellerUseCase;
import in.port.com.ryuqq.crawlinghub.application.seller.UpdateSellerStatusUseCase;
import com.ryuqq.crawlinghub.domain.seller.MustitSeller;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SellerCommandFacade - Seller Command 작업 조율
 *
 * <p><strong>Facade 패턴 적용 ⭐</strong></p>
 * <ul>
 *   <li>여러 UseCase 조율</li>
 *   <li>트랜잭션 경계 관리</li>
 *   <li>Controller 의존성 감소</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Service
public class SellerCommandFacade {

    private final RegisterSellerUseCase registerSellerUseCase;
    private final UpdateSellerStatusUseCase updateSellerStatusUseCase;
    private final SellerManager sellerManager;

    public SellerCommandFacade(
        RegisterSellerUseCase registerSellerUseCase,
        UpdateSellerStatusUseCase updateSellerStatusUseCase,
        SellerManager sellerManager
    ) {
        this.registerSellerUseCase = registerSellerUseCase;
        this.updateSellerStatusUseCase = updateSellerStatusUseCase;
        this.sellerManager = sellerManager;
    }

    /**
     * 셀러 등록 + 초기 이력 생성
     *
     * <p>Facade가 여러 작업 조율:
     * <ol>
     *   <li>RegisterSellerUseCase 호출 (셀러 등록)</li>
     *   <li>SellerManager를 통한 초기 이력 생성 (상품 수 0)</li>
     * </ol>
     *
     * @param command 등록 Command
     * @return SellerResponse
     */
    @Transactional
    public SellerResponse registerSellerWithInitialHistory(RegisterSellerCommand command) {
        // 1. UseCase 호출
        SellerResponse response = registerSellerUseCase.register(command);

        // 2. Manager를 통한 초기 이력 생성
        MustitSeller seller = sellerManager.updateSellerStatus(response.sellerId());
        sellerManager.updateProductCountWithHistory(seller, 0);

        return response;
    }
}
```

### 4.4 GetSellerDetailService 확장

**위치**: `application/.../service/GetSellerDetailService.java`

```java
package com.ryuqq.crawlinghub.application.mustit.seller.service;

import com.ryuqq.crawlinghub.application.common.dto.PageResponse;
import com.ryuqq.crawlinghub.application.crawl.schedule.port.out.LoadScheduleHistoryPort;
import com.ryuqq.crawlinghub.application.crawl.schedule.port.out.LoadSchedulePort;
import assembler.com.ryuqq.crawlinghub.application.seller.SellerAssembler;
import response.dto.com.ryuqq.crawlinghub.application.seller.ProductCountHistoryResponse;
import response.dto.com.ryuqq.crawlinghub.application.seller.ScheduleHistoryResponse;
import response.dto.com.ryuqq.crawlinghub.application.seller.ScheduleInfoResponse;
import response.dto.com.ryuqq.crawlinghub.application.seller.SellerDetailResponse;
import in.port.com.ryuqq.crawlinghub.application.seller.GetSellerDetailUseCase;
import out.port.com.ryuqq.crawlinghub.application.seller.LoadProductCountHistoryPort;
import out.port.com.ryuqq.crawlinghub.application.seller.LoadSellerPort;
import out.port.com.ryuqq.crawlinghub.application.seller.LoadSellerStatsPort;
import com.ryuqq.crawlinghub.domain.crawl.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.crawl.schedule.CrawlScheduleHistory;
import com.ryuqq.crawlinghub.domain.crawl.schedule.CrawlScheduleId;
import com.ryuqq.crawlinghub.domain.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import history.com.ryuqq.crawlinghub.domain.seller.ProductCountHistory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * GetSellerDetailService - 셀러 상세 조회 UseCase 구현
 *
 * <p><strong>확장된 기능 (v2) ⭐</strong></p>
 * <ul>
 *   <li>기본 셀러 정보</li>
 *   <li>총 상품 수</li>
 *   <li>🆕 상품 수 변경 이력 (PageResponse)</li>
 *   <li>🆕 크롤링 스케줄 정보</li>
 *   <li>🆕 크롤링 실행 이력 (PageResponse)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Service
public class GetSellerDetailService implements GetSellerDetailUseCase {

    private final LoadSellerPort loadSellerPort;
    private final LoadSellerStatsPort loadSellerStatsPort;
    private final LoadProductCountHistoryPort loadHistoryPort; // 추가 ⭐
    private final LoadSchedulePort loadSchedulePort; // 추가 ⭐
    private final LoadScheduleHistoryPort loadScheduleHistoryPort; // 추가 ⭐
    private final SellerAssembler sellerAssembler;

    public GetSellerDetailService(
        LoadSellerPort loadSellerPort,
        LoadSellerStatsPort loadSellerStatsPort,
        LoadProductCountHistoryPort loadHistoryPort,
        LoadSchedulePort loadSchedulePort,
        LoadScheduleHistoryPort loadScheduleHistoryPort,
        SellerAssembler sellerAssembler
    ) {
        this.loadSellerPort = loadSellerPort;
        this.loadSellerStatsPort = loadSellerStatsPort;
        this.loadHistoryPort = loadHistoryPort;
        this.loadSchedulePort = loadSchedulePort;
        this.loadScheduleHistoryPort = loadScheduleHistoryPort;
        this.sellerAssembler = sellerAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public SellerDetailResponse getDetail(Long sellerId) {
        // 1. 셀러 기본 정보 조회
        MustitSeller seller = loadSellerPort.load(sellerId);
        MustitSellerId mustitSellerId = MustitSellerId.of(sellerId);

        // 2. 총 상품 수 조회
        Integer totalProductCount = seller.getTotalProductCount();

        // 3. 상품 수 변경 이력 조회 (PageResponse) ⭐
        PageResponse<ProductCountHistoryResponse> historyPage = getProductCountHistories(
            mustitSellerId,
            0,  // 기본 페이지 0
            10  // 기본 10개
        );

        // 4. 크롤링 스케줄 정보 조회 ⭐
        Optional<ScheduleInfoResponse> scheduleInfo = getScheduleInfo(mustitSellerId);

        // 5. 크롤링 실행 이력 조회 (PageResponse) ⭐
        PageResponse<ScheduleHistoryResponse> scheduleHistoryPage = scheduleInfo
            .map(info -> getScheduleHistories(
                CrawlScheduleId.of(info.scheduleId()),
                0,
                10
            ))
            .orElse(PageResponse.empty(0, 10));

        // 6. Assembler를 통한 DTO 변환
        return sellerAssembler.toSellerDetailResponse(
            seller,
            totalProductCount,
            historyPage,
            scheduleInfo.orElse(null),
            scheduleHistoryPage
        );
    }

    /**
     * 상품 수 변경 이력 조회 (PageResponse) ⭐
     */
    private PageResponse<ProductCountHistoryResponse> getProductCountHistories(
        MustitSellerId sellerId,
        int page,
        int size
    ) {
        List<ProductCountHistory> histories = loadHistoryPort.loadHistories(sellerId, page, size);
        long totalElements = loadHistoryPort.countHistories(sellerId);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PageResponse.of(
            histories.stream()
                .map(sellerAssembler::toProductCountHistoryResponse)
                .toList(),
            page,
            size,
            totalElements,
            totalPages,
            page == 0,
            page == totalPages - 1
        );
    }

    /**
     * 크롤링 스케줄 정보 조회 ⭐
     */
    private Optional<ScheduleInfoResponse> getScheduleInfo(MustitSellerId sellerId) {
        return loadSchedulePort.findBySellerId(sellerId)
            .map(sellerAssembler::toScheduleInfoResponse);
    }

    /**
     * 크롤링 실행 이력 조회 (PageResponse) ⭐
     */
    private PageResponse<ScheduleHistoryResponse> getScheduleHistories(
        CrawlScheduleId scheduleId,
        int page,
        int size
    ) {
        List<CrawlScheduleHistory> histories = loadScheduleHistoryPort.loadHistories(scheduleId, page, size);
        long totalElements = loadScheduleHistoryPort.countHistories(scheduleId);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PageResponse.of(
            histories.stream()
                .map(sellerAssembler::toScheduleHistoryResponse)
                .toList(),
            page,
            size,
            totalElements,
            totalPages,
            page == 0,
            page == totalPages - 1
        );
    }
}
```

### 4.5 신규 DTO

#### 4.5.1 ProductCountHistoryResponse

```java
package com.ryuqq.crawlinghub.application.mustit.seller.dto.response;

import java.time.LocalDateTime;

/**
 * ProductCountHistoryResponse - 상품 수 이력 응답 DTO
 *
 * <p><strong>🆕 변경사항 (v2):</strong></p>
 * <ul>
 *   <li>❌ previousCount 제거</li>
 *   <li>✅ executedDate + productCount만 반환</li>
 * </ul>
 *
 * @param historyId 이력 ID
 * @param executedDate 실행 날짜
 * @param productCount 카운트 된 수
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record ProductCountHistoryResponse(
    Long historyId,
    LocalDateTime executedDate,
    Integer productCount
) {}
```

#### 4.5.2 ScheduleInfoResponse

```java
package com.ryuqq.crawlinghub.application.mustit.seller.dto.response;

import java.time.LocalDateTime;

/**
 * ScheduleInfoResponse - 크롤링 스케줄 정보 응답 DTO
 *
 * @param scheduleId 스케줄 ID
 * @param cronExpression Cron 표현식
 * @param status 스케줄 상태
 * @param nextExecutionTime 다음 실행 시간
 * @param createdAt 생성 일시
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record ScheduleInfoResponse(
    Long scheduleId,
    String cronExpression,
    String status,
    LocalDateTime nextExecutionTime,
    LocalDateTime createdAt
) {}
```

#### 4.5.3 ScheduleHistoryResponse

```java
package com.ryuqq.crawlinghub.application.mustit.seller.dto.response;

import java.time.LocalDateTime;

/**
 * ScheduleHistoryResponse - 스케줄 실행 이력 응답 DTO
 *
 * @param historyId 이력 ID
 * @param startedAt 시작 시간
 * @param completedAt 완료 시간
 * @param status 실행 상태 (SUCCESS, FAILURE)
 * @param message 실행 메시지
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record ScheduleHistoryResponse(
    Long historyId,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    String status,
    String message
) {}
```

#### 4.5.4 SellerDetailResponse (확장)

```java
package com.ryuqq.crawlinghub.application.mustit.seller.dto.response;

import com.ryuqq.crawlinghub.application.common.dto.PageResponse;

/**
 * SellerDetailResponse - 셀러 상세 조회 응답 DTO (확장)
 *
 * <p><strong>확장된 필드 (v2) ⭐</strong></p>
 * <ul>
 *   <li>🆕 productCountHistories (PageResponse)</li>
 *   <li>🆕 scheduleInfo (크롤링 스케줄)</li>
 *   <li>🆕 scheduleHistories (PageResponse)</li>
 * </ul>
 *
 * @param sellerId 셀러 ID
 * @param sellerCode 셀러 코드
 * @param sellerName 셀러명
 * @param status 상태
 * @param totalProductCount 총 상품 수
 * @param productCountHistories 상품 수 변경 이력 (PageResponse) ⭐
 * @param scheduleInfo 크롤링 스케줄 정보 ⭐
 * @param scheduleHistories 크롤링 실행 이력 (PageResponse) ⭐
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record SellerDetailResponse(
    Long sellerId,
    String sellerCode,
    String sellerName,
    String status,
    Integer totalProductCount,
    PageResponse<ProductCountHistoryResponse> productCountHistories, // ⭐
    ScheduleInfoResponse scheduleInfo, // ⭐
    PageResponse<ScheduleHistoryResponse> scheduleHistories // ⭐
) {}
```

---

## 5. Persistence Layer 작업 계획 (CQRS)

### 5.1 Entity 설계

#### 5.1.1 ProductCountHistoryEntity

**위치**: `adapter-out/persistence-mysql/.../entity/ProductCountHistoryEntity.java`

```java
package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * ProductCountHistoryEntity - 상품 수 이력 JPA Entity
 *
 * <p><strong>🆕 변경사항 (v2):</strong></p>
 * <ul>
 *   <li>❌ previousCount 컬럼 제거</li>
 *   <li>✅ executedDate + productCount만 저장</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 금지</li>
 *   <li>✅ Pure Java getter/setter</li>
 *   <li>❌ JPA 관계 어노테이션 금지 (@ManyToOne 등)</li>
 *   <li>✅ Long FK 전략 (sellerId)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Entity
@Table(
    name = "product_count_history",
    indexes = {
        @Index(name = "idx_seller_id_executed_date", columnList = "seller_id, executed_date")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class ProductCountHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId; // Long FK ⭐

    @Column(name = "product_count", nullable = false)
    private Integer productCount; // 카운트 된 수 ⭐

    @Column(name = "executed_date", nullable = false)
    private LocalDateTime executedDate; // 실행 날짜 ⭐

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Protected No-Args Constructor (JPA 요구사항)
    protected ProductCountHistoryEntity() {}

    // Private Constructor (Factory Method 강제)
    private ProductCountHistoryEntity(
        Long id,
        Long sellerId,
        Integer productCount,
        LocalDateTime executedDate
    ) {
        this.id = id;
        this.sellerId = sellerId;
        this.productCount = productCount;
        this.executedDate = executedDate;
    }

    /**
     * Static Factory Method - 새로운 Entity 생성
     */
    public static ProductCountHistoryEntity create(
        Long sellerId,
        Integer productCount,
        LocalDateTime executedDate
    ) {
        return new ProductCountHistoryEntity(null, sellerId, productCount, executedDate);
    }

    /**
     * Static Factory Method - 기존 Entity 복원
     */
    public static ProductCountHistoryEntity reconstitute(
        Long id,
        Long sellerId,
        Integer productCount,
        LocalDateTime executedDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        ProductCountHistoryEntity entity = new ProductCountHistoryEntity(
            id,
            sellerId,
            productCount,
            executedDate
        );
        entity.createdAt = createdAt;
        entity.updatedAt = updatedAt;
        return entity;
    }

    // Getters (Pure Java)
    public Long getId() {
        return id;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public Integer getProductCount() {
        return productCount;
    }

    public LocalDateTime getExecutedDate() {
        return executedDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
```

### 5.2 Repository 설계 (CQRS) ⭐

#### 5.2.1 ProductCountHistoryJpaRepository (Command)

**위치**: `adapter-out/persistence-mysql/.../repository/ProductCountHistoryJpaRepository.java`

```java
package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.repository;

import entity.com.ryuqq.crawlinghub.adapter.out.persistence.seller.ProductCountHistoryEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ProductCountHistoryJpaRepository - Command Repository (JPA)
 *
 * <p>Command 작업만 수행 (쓰기 전용) ⭐</p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Repository
public interface ProductCountHistoryJpaRepository extends JpaRepository<ProductCountHistoryEntity, Long> {
    // Command 작업만 수행 (save, delete 등)
    // 조회 메서드는 없음 ⭐
}
```

#### 5.2.2 ProductCountHistoryQueryRepository (Query - QueryDSL) ⭐

**위치**: `adapter-out/persistence-mysql/.../repository/ProductCountHistoryQueryRepository.java`

```java
package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import entity.com.ryuqq.crawlinghub.adapter.out.persistence.seller.ProductCountHistoryEntity;
import entity.com.ryuqq.crawlinghub.adapter.out.persistence.seller.QProductCountHistoryEntity;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ProductCountHistoryQueryRepository - Query Repository (QueryDSL)
 *
 * <p><strong>QueryDSL 기반 읽기 전용 Repository ⭐</strong></p>
 * <ul>
 *   <li>N+1 문제 방지</li>
 *   <li>DTO Projection 최적화</li>
 *   <li>타입 안전한 쿼리</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Repository
public class ProductCountHistoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    public ProductCountHistoryQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 셀러별 상품 수 이력 조회 (페이징)
     *
     * <p>QueryDSL 기반 최적화 쿼리 ⭐</p>
     *
     * @param sellerId 셀러 ID
     * @param offset 시작 위치
     * @param limit 조회 개수
     * @return ProductCountHistoryEntity 리스트
     */
    public List<ProductCountHistoryEntity> findHistoriesBySellerId(Long sellerId, int offset, int limit) {
        QProductCountHistoryEntity history = QProductCountHistoryEntity.productCountHistoryEntity;

        return queryFactory
            .selectFrom(history)
            .where(history.sellerId.eq(sellerId))
            .orderBy(history.executedDate.desc())
            .offset(offset)
            .limit(limit)
            .fetch();
    }

    /**
     * 전체 이력 개수 조회
     *
     * @param sellerId 셀러 ID
     * @return 전체 개수
     */
    public long countHistoriesBySellerId(Long sellerId) {
        QProductCountHistoryEntity history = QProductCountHistoryEntity.productCountHistoryEntity;

        return queryFactory
            .selectFrom(history)
            .where(history.sellerId.eq(sellerId))
            .fetchCount();
    }
}
```

### 5.3 Mapper 설계 (MapStruct)

**위치**: `adapter-out/persistence-mysql/.../mapper/ProductCountHistoryMapper.java`

```java
package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.mapper;

import entity.com.ryuqq.crawlinghub.adapter.out.persistence.seller.ProductCountHistoryEntity;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import history.com.ryuqq.crawlinghub.domain.seller.ProductCountHistory;
import history.com.ryuqq.crawlinghub.domain.seller.ProductCountHistoryId;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * ProductCountHistoryMapper - Entity ↔ Domain 변환 (MapStruct)
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductCountHistoryMapper {

    /**
     * Domain → Entity 변환
     */
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "sellerId", source = "sellerId.value")
    @Mapping(target = "productCount", source = "productCount")
    @Mapping(target = "executedDate", source = "executedDate")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductCountHistoryEntity toEntity(ProductCountHistory domain);

    /**
     * Entity → Domain 변환
     */
    default ProductCountHistory toDomain(ProductCountHistoryEntity entity) {
        return ProductCountHistory.reconstitute(
            entity.getId() != null ? ProductCountHistoryId.of(entity.getId()) : null,
            MustitSellerId.of(entity.getSellerId()),
            entity.getProductCount(),
            entity.getExecutedDate()
        );
    }
}
```

### 5.4 Adapter 설계 (CQRS) ⭐

#### 5.4.1 ProductCountHistoryCommandAdapter (Command)

**위치**: `adapter-out/persistence-mysql/.../adapter/ProductCountHistoryCommandAdapter.java`

```java
package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.adapter;

import entity.com.ryuqq.crawlinghub.adapter.out.persistence.seller.ProductCountHistoryEntity;
import mapper.com.ryuqq.crawlinghub.adapter.out.persistence.seller.ProductCountHistoryMapper;
import repository.com.ryuqq.crawlinghub.adapter.out.persistence.seller.ProductCountHistoryJpaRepository;
import out.port.com.ryuqq.crawlinghub.application.seller.SaveProductCountHistoryPort;
import history.com.ryuqq.crawlinghub.domain.seller.ProductCountHistory;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProductCountHistoryCommandAdapter - Command Adapter (쓰기 전용)
 *
 * <p>CQRS 패턴 적용 - Command 작업만 수행 ⭐</p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class ProductCountHistoryCommandAdapter implements SaveProductCountHistoryPort {

    private final ProductCountHistoryJpaRepository jpaRepository;
    private final ProductCountHistoryMapper mapper;

    public ProductCountHistoryCommandAdapter(
        ProductCountHistoryJpaRepository jpaRepository,
        ProductCountHistoryMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ProductCountHistory saveHistory(ProductCountHistory history) {
        ProductCountHistoryEntity entity = mapper.toEntity(history);
        ProductCountHistoryEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
```

#### 5.4.2 ProductCountHistoryQueryAdapter (Query - QueryDSL) ⭐

**위치**: `adapter-out/persistence-mysql/.../adapter/ProductCountHistoryQueryAdapter.java`

```java
package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.adapter;

import entity.com.ryuqq.crawlinghub.adapter.out.persistence.seller.ProductCountHistoryEntity;
import mapper.com.ryuqq.crawlinghub.adapter.out.persistence.seller.ProductCountHistoryMapper;
import repository.com.ryuqq.crawlinghub.adapter.out.persistence.seller.ProductCountHistoryQueryRepository;
import out.port.com.ryuqq.crawlinghub.application.seller.LoadProductCountHistoryPort;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import history.com.ryuqq.crawlinghub.domain.seller.ProductCountHistory;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ProductCountHistoryQueryAdapter - Query Adapter (읽기 전용)
 *
 * <p><strong>CQRS 패턴 적용 - Query 작업만 수행 ⭐</strong></p>
 * <ul>
 *   <li>QueryDSL 기반 최적화 조회</li>
 *   <li>N+1 문제 방지</li>
 *   <li>DTO Projection</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class ProductCountHistoryQueryAdapter implements LoadProductCountHistoryPort {

    private final ProductCountHistoryQueryRepository queryRepository;
    private final ProductCountHistoryMapper mapper;

    public ProductCountHistoryQueryAdapter(
        ProductCountHistoryQueryRepository queryRepository,
        ProductCountHistoryMapper mapper
    ) {
        this.queryRepository = queryRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductCountHistory> loadHistories(MustitSellerId sellerId, int page, int size) {
        int offset = page * size;
        List<ProductCountHistoryEntity> entities = queryRepository.findHistoriesBySellerId(
            sellerId.value(),
            offset,
            size
        );
        return entities.stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countHistories(MustitSellerId sellerId) {
        return queryRepository.countHistoriesBySellerId(sellerId.value());
    }
}
```

---

## 6. REST API Layer 작업 계획

### 6.1 API DTO 설계 (PageApiResponse 통합) ⭐

#### 6.1.1 ProductCountHistoryApiResponse

**위치**: `adapter-in/rest-api/.../dto/response/ProductCountHistoryApiResponse.java`

```java
package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * ProductCountHistoryApiResponse - 상품 수 이력 API 응답 DTO
 *
 * <p><strong>🆕 변경사항 (v2):</strong></p>
 * <ul>
 *   <li>❌ previousCount 제거</li>
 *   <li>✅ executedDate + productCount만 반환</li>
 * </ul>
 *
 * @param historyId 이력 ID
 * @param executedDate 실행 날짜
 * @param productCount 카운트 된 수
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record ProductCountHistoryApiResponse(
    Long historyId,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime executedDate,
    Integer productCount
) {}
```

#### 6.1.2 ScheduleInfoApiResponse

```java
package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * ScheduleInfoApiResponse - 스케줄 정보 API 응답 DTO
 *
 * @param scheduleId 스케줄 ID
 * @param cronExpression Cron 표현식
 * @param status 상태
 * @param nextExecutionTime 다음 실행 시간
 * @param createdAt 생성 일시
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record ScheduleInfoApiResponse(
    Long scheduleId,
    String cronExpression,
    String status,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime nextExecutionTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt
) {}
```

#### 6.1.3 ScheduleHistoryApiResponse

```java
package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * ScheduleHistoryApiResponse - 스케줄 실행 이력 API 응답 DTO
 *
 * @param historyId 이력 ID
 * @param startedAt 시작 시간
 * @param completedAt 완료 시간
 * @param status 상태
 * @param message 메시지
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record ScheduleHistoryApiResponse(
    Long historyId,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime startedAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime completedAt,
    String status,
    String message
) {}
```

#### 6.1.4 SellerDetailApiResponse (확장)

```java
package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.PageApiResponse;

/**
 * SellerDetailApiResponse - 셀러 상세 API 응답 DTO (확장)
 *
 * <p><strong>🆕 확장된 필드 (v2) ⭐</strong></p>
 * <ul>
 *   <li>productCountHistories (PageApiResponse)</li>
 *   <li>scheduleInfo (스케줄 정보)</li>
 *   <li>scheduleHistories (PageApiResponse)</li>
 * </ul>
 *
 * @param sellerId 셀러 ID
 * @param sellerCode 셀러 코드
 * @param sellerName 셀러명
 * @param status 상태
 * @param totalProductCount 총 상품 수
 * @param productCountHistories 상품 수 변경 이력 (PageApiResponse) ⭐
 * @param scheduleInfo 스케줄 정보 ⭐
 * @param scheduleHistories 스케줄 실행 이력 (PageApiResponse) ⭐
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record SellerDetailApiResponse(
    Long sellerId,
    String sellerCode,
    String sellerName,
    String status,
    Integer totalProductCount,
    PageApiResponse<ProductCountHistoryApiResponse> productCountHistories, // ⭐
    ScheduleInfoApiResponse scheduleInfo, // ⭐
    PageApiResponse<ScheduleHistoryApiResponse> scheduleHistories // ⭐
) {}
```

### 6.2 PageApiResponse (REST API 공통 DTO) ⭐

**위치**: `adapter-in/rest-api/.../common/dto/PageApiResponse.java`

```java
package com.ryuqq.crawlinghub.adapter.in.rest.common.dto;

import java.util.List;

/**
 * PageApiResponse - REST API 페이징 응답 공통 DTO
 *
 * <p><strong>Application PageResponse → REST API 변환 ⭐</strong></p>
 * <ul>
 *   <li>Application Layer PageResponse를 REST API로 변환</li>
 *   <li>JSON 응답 포맷 제공</li>
 * </ul>
 *
 * @param <T> 콘텐츠 타입
 * @param content 데이터 리스트
 * @param page 페이지 번호
 * @param size 페이지 크기
 * @param totalElements 전체 개수
 * @param totalPages 전체 페이지 수
 * @param first 첫 페이지 여부
 * @param last 마지막 페이지 여부
 * @author ryu-qqq
 * @since 2025-11-05
 */
public record PageApiResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {

    /**
     * Static Factory Method
     */
    public static <T> PageApiResponse<T> of(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
    ) {
        return new PageApiResponse<>(content, page, size, totalElements, totalPages, first, last);
    }
}
```

### 6.3 SellerApiMapper 확장 (PageResponse 변환) ⭐

**위치**: `adapter-in/rest-api/.../mapper/SellerApiMapper.java`

```java
package com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.ProductCountHistoryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.ScheduleHistoryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.ScheduleInfoApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerDetailApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.PageResponse;
import response.dto.com.ryuqq.crawlinghub.application.seller.ProductCountHistoryResponse;
import response.dto.com.ryuqq.crawlinghub.application.seller.ScheduleHistoryResponse;
import response.dto.com.ryuqq.crawlinghub.application.seller.ScheduleInfoResponse;
import response.dto.com.ryuqq.crawlinghub.application.seller.SellerDetailResponse;

import org.springframework.stereotype.Component;

/**
 * SellerApiMapper - Application DTO ↔ REST API DTO 변환
 *
 * <p><strong>PageResponse 변환 로직 추가 ⭐</strong></p>
 * <ul>
 *   <li>Application PageResponse → REST API PageApiResponse</li>
 *   <li>Domain 객체 → API DTO</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class SellerApiMapper {

    /**
     * SellerDetailResponse → SellerDetailApiResponse 변환
     *
     * <p>PageResponse도 함께 변환 ⭐</p>
     */
    public SellerDetailApiResponse toSellerDetailApiResponse(SellerDetailResponse response) {
        return new SellerDetailApiResponse(
            response.sellerId(),
            response.sellerCode(),
            response.sellerName(),
            response.status(),
            response.totalProductCount(),
            toPageApiResponse(response.productCountHistories()), // ⭐
            toScheduleInfoApiResponse(response.scheduleInfo()), // ⭐
            toPageApiResponse(response.scheduleHistories()) // ⭐
        );
    }

    /**
     * PageResponse<ProductCountHistoryResponse> → PageApiResponse<ProductCountHistoryApiResponse> 변환 ⭐
     */
    public PageApiResponse<ProductCountHistoryApiResponse> toPageApiResponse(
        PageResponse<ProductCountHistoryResponse> pageResponse
    ) {
        return PageApiResponse.of(
            pageResponse.content().stream()
                .map(this::toProductCountHistoryApiResponse)
                .toList(),
            pageResponse.page(),
            pageResponse.size(),
            pageResponse.totalElements(),
            pageResponse.totalPages(),
            pageResponse.first(),
            pageResponse.last()
        );
    }

    /**
     * ProductCountHistoryResponse → ProductCountHistoryApiResponse 변환
     */
    public ProductCountHistoryApiResponse toProductCountHistoryApiResponse(
        ProductCountHistoryResponse response
    ) {
        return new ProductCountHistoryApiResponse(
            response.historyId(),
            response.executedDate(),
            response.productCount()
        );
    }

    /**
     * ScheduleInfoResponse → ScheduleInfoApiResponse 변환
     */
    public ScheduleInfoApiResponse toScheduleInfoApiResponse(ScheduleInfoResponse response) {
        if (response == null) {
            return null;
        }
        return new ScheduleInfoApiResponse(
            response.scheduleId(),
            response.cronExpression(),
            response.status(),
            response.nextExecutionTime(),
            response.createdAt()
        );
    }

    /**
     * PageResponse<ScheduleHistoryResponse> → PageApiResponse<ScheduleHistoryApiResponse> 변환
     */
    public PageApiResponse<ScheduleHistoryApiResponse> toPageApiResponse(
        PageResponse<ScheduleHistoryResponse> pageResponse
    ) {
        return PageApiResponse.of(
            pageResponse.content().stream()
                .map(this::toScheduleHistoryApiResponse)
                .toList(),
            pageResponse.page(),
            pageResponse.size(),
            pageResponse.totalElements(),
            pageResponse.totalPages(),
            pageResponse.first(),
            pageResponse.last()
        );
    }

    /**
     * ScheduleHistoryResponse → ScheduleHistoryApiResponse 변환
     */
    public ScheduleHistoryApiResponse toScheduleHistoryApiResponse(ScheduleHistoryResponse response) {
        return new ScheduleHistoryApiResponse(
            response.historyId(),
            response.startedAt(),
            response.completedAt(),
            response.status(),
            response.message()
        );
    }
}
```

### 6.4 SellerController 수정

**위치**: `adapter-in/rest-api/.../controller/SellerController.java`

```java
package com.ryuqq.crawlinghub.adapter.in.rest.seller.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper.SellerApiMapper;
import response.dto.com.ryuqq.crawlinghub.application.seller.SellerDetailResponse;
import in.port.com.ryuqq.crawlinghub.application.seller.GetSellerDetailUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SellerController - 셀러 REST API 컨트롤러
 *
 * <p>상세 조회 API 확장 (v2) ⭐</p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@RestController
@RequestMapping("/api/v1/sellers")
@Tag(name = "Seller API", description = "셀러 관리 API")
public class SellerController {

    private final GetSellerDetailUseCase getSellerDetailUseCase;
    private final SellerApiMapper sellerApiMapper;

    public SellerController(
        GetSellerDetailUseCase getSellerDetailUseCase,
        SellerApiMapper sellerApiMapper
    ) {
        this.getSellerDetailUseCase = getSellerDetailUseCase;
        this.sellerApiMapper = sellerApiMapper;
    }

    /**
     * 셀러 상세 조회 (확장됨) ⭐
     *
     * <p>반환 정보:
     * <ul>
     *   <li>기본 셀러 정보</li>
     *   <li>총 상품 수</li>
     *   <li>🆕 상품 수 변경 이력 (PageApiResponse)</li>
     *   <li>🆕 크롤링 스케줄 정보</li>
     *   <li>🆕 크롤링 실행 이력 (PageApiResponse)</li>
     * </ul>
     */
    @GetMapping("/{sellerId}")
    @Operation(summary = "셀러 상세 조회", description = "셀러 상세 정보 + 이력 조회")
    public ResponseEntity<ApiResponse<SellerDetailApiResponse>> getSellerDetail(@PathVariable Long sellerId) {
        SellerDetailResponse response = getSellerDetailUseCase.getDetail(sellerId);
        SellerDetailApiResponse apiResponse = sellerApiMapper.toSellerDetailApiResponse(response);
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
```

---

## 7. 테스트 전략

### 7.1 테스트 타입별 분류

| Layer | 테스트 타입 | 태그 | 도구 | 커버리지 목표 |
|-------|------------|------|------|--------------|
| Domain | Unit Test | `@Tag("unit")` `@Tag("domain")` | JUnit 5 | 90% |
| Application | Unit Test | `@Tag("unit")` `@Tag("application")` | JUnit 5 + Mockito | 80% |
| Persistence | Unit Test (Command) | `@Tag("unit")` `@Tag("command")` | JUnit 5 + Mockito | 70% |
| Persistence | Unit Test (Query) | `@Tag("unit")` `@Tag("query")` | JUnit 5 + QueryDSL | 70% |
| Persistence | Integration Test | `@Tag("integration")` `@Tag("persistence")` | Testcontainers | 70% |
| REST API | Integration Test | `@Tag("integration")` `@Tag("rest-api")` | MockMvc | 70% |

### 7.2 Domain Layer 테스트

#### 7.2.1 ProductCountHistoryTest.java

**위치**: `domain/src/test/java/.../ProductCountHistoryTest.java`

```java
package com.ryuqq.crawlinghub.domain.mustit.seller.history;

import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ProductCountHistoryTest - ProductCountHistory 도메인 객체 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Tag("unit")
@Tag("domain")
@DisplayName("ProductCountHistory 도메인 객체 테스트")
class ProductCountHistoryTest {

    @Test
    @DisplayName("정상 케이스: 상품 수 이력 생성 성공")
    void record_success() {
        // Given
        MustitSellerId sellerId = MustitSellerId.of(1L);
        Integer productCount = 100;
        LocalDateTime executedDate = LocalDateTime.now();

        // When
        ProductCountHistory history = ProductCountHistory.record(sellerId, productCount, executedDate);

        // Then
        assertThat(history).isNotNull();
        assertThat(history.getSellerIdValue()).isEqualTo(1L);
        assertThat(history.getProductCount()).isEqualTo(100);
        assertThat(history.getExecutedDate()).isEqualTo(executedDate);
    }

    @Test
    @DisplayName("예외 케이스: 상품 수 null 시 예외 발생")
    void record_fail_whenProductCountIsNull() {
        // Given
        MustitSellerId sellerId = MustitSellerId.of(1L);
        Integer productCount = null;
        LocalDateTime executedDate = LocalDateTime.now();

        // When & Then
        assertThatThrownBy(() -> ProductCountHistory.record(sellerId, productCount, executedDate))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("상품 수는 0 이상이어야 합니다");
    }

    @Test
    @DisplayName("예외 케이스: 상품 수 음수 시 예외 발생")
    void record_fail_whenProductCountIsNegative() {
        // Given
        MustitSellerId sellerId = MustitSellerId.of(1L);
        Integer productCount = -1;
        LocalDateTime executedDate = LocalDateTime.now();

        // When & Then
        assertThatThrownBy(() -> ProductCountHistory.record(sellerId, productCount, executedDate))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("상품 수는 0 이상이어야 합니다");
    }

    @Test
    @DisplayName("정상 케이스: 동일 날짜 체크 성공")
    void isSameDate_success() {
        // Given
        MustitSellerId sellerId = MustitSellerId.of(1L);
        LocalDateTime date1 = LocalDateTime.of(2025, 11, 5, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2025, 11, 5, 15, 0);
        ProductCountHistory history = ProductCountHistory.record(sellerId, 100, date1);

        // When
        boolean isSameDate = history.isSameDate(date2);

        // Then
        assertThat(isSameDate).isTrue();
    }

    @Test
    @DisplayName("정상 케이스: 다른 날짜 체크 성공")
    void isSameDate_fail_whenDifferentDate() {
        // Given
        MustitSellerId sellerId = MustitSellerId.of(1L);
        LocalDateTime date1 = LocalDateTime.of(2025, 11, 5, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2025, 11, 6, 10, 0);
        ProductCountHistory history = ProductCountHistory.record(sellerId, 100, date1);

        // When
        boolean isSameDate = history.isSameDate(date2);

        // Then
        assertThat(isSameDate).isFalse();
    }
}
```

### 7.3 Application Layer 테스트

#### 7.3.1 SellerManagerTest.java

```java
package com.ryuqq.crawlinghub.application.mustit.seller.component;

import out.port.com.ryuqq.crawlinghub.application.seller.LoadSellerPort;
import out.port.com.ryuqq.crawlinghub.application.seller.SaveProductCountHistoryPort;
import out.port.com.ryuqq.crawlinghub.application.seller.SaveSellerPort;
import com.ryuqq.crawlinghub.domain.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.seller.SellerName;
import com.ryuqq.crawlinghub.domain.seller.SellerStatus;
import history.com.ryuqq.crawlinghub.domain.seller.ProductCountHistory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SellerManagerTest - SellerManager 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
@DisplayName("SellerManager 단위 테스트")
class SellerManagerTest {

    @InjectMocks
    private SellerManager sellerManager;

    @Mock
    private SaveSellerPort saveSellerPort;

    @Mock
    private LoadSellerPort loadSellerPort;

    @Mock
    private SaveProductCountHistoryPort saveHistoryPort;

    @Test
    @DisplayName("정상 케이스: 상품 수 업데이트 + 이력 자동 저장 성공")
    void updateProductCountWithHistory_success() {
        // Given
        MustitSeller seller = createTestSeller();
        Integer newCount = 150;

        when(saveSellerPort.save(any(MustitSeller.class))).thenReturn(seller);
        when(saveHistoryPort.saveHistory(any(ProductCountHistory.class))).thenReturn(null);

        // When
        sellerManager.updateProductCountWithHistory(seller, newCount);

        // Then
        ArgumentCaptor<MustitSeller> sellerCaptor = ArgumentCaptor.forClass(MustitSeller.class);
        ArgumentCaptor<ProductCountHistory> historyCaptor = ArgumentCaptor.forClass(ProductCountHistory.class);

        verify(saveSellerPort).save(sellerCaptor.capture());
        verify(saveHistoryPort).saveHistory(historyCaptor.capture());

        MustitSeller savedSeller = sellerCaptor.getValue();
        assertThat(savedSeller.getTotalProductCount()).isEqualTo(150);

        ProductCountHistory savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getSellerIdValue()).isEqualTo(1L);
        assertThat(savedHistory.getProductCount()).isEqualTo(150);
    }

    private MustitSeller createTestSeller() {
        return MustitSeller.create(
            MustitSellerId.of(1L),
            "SELLER001",
            SellerName.of("테스트 셀러"),
            SellerStatus.ACTIVE
        );
    }
}
```

### 7.4 Persistence Layer 테스트 (CQRS) ⭐

#### 7.4.1 ProductCountHistoryCommandAdapterTest.java (Command)

```java
package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.adapter;

import entity.com.ryuqq.crawlinghub.adapter.out.persistence.seller.ProductCountHistoryEntity;
import mapper.com.ryuqq.crawlinghub.adapter.out.persistence.seller.ProductCountHistoryMapper;
import repository.com.ryuqq.crawlinghub.adapter.out.persistence.seller.ProductCountHistoryJpaRepository;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import history.com.ryuqq.crawlinghub.domain.seller.ProductCountHistory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ProductCountHistoryCommandAdapterTest - Command Adapter 단위 테스트 ⭐
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Tag("unit")
@Tag("command")
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCountHistoryCommandAdapter 단위 테스트")
class ProductCountHistoryCommandAdapterTest {

    @InjectMocks
    private ProductCountHistoryCommandAdapter commandAdapter;

    @Mock
    private ProductCountHistoryJpaRepository jpaRepository;

    @Mock
    private ProductCountHistoryMapper mapper;

    @Test
    @DisplayName("정상 케이스: 상품 수 이력 저장 성공")
    void saveHistory_success() {
        // Given
        ProductCountHistory history = createTestHistory();
        ProductCountHistoryEntity entity = createTestEntity();

        when(mapper.toEntity(any(ProductCountHistory.class))).thenReturn(entity);
        when(jpaRepository.save(any(ProductCountHistoryEntity.class))).thenReturn(entity);
        when(mapper.toDomain(any(ProductCountHistoryEntity.class))).thenReturn(history);

        // When
        ProductCountHistory savedHistory = commandAdapter.saveHistory(history);

        // Then
        verify(jpaRepository).save(any(ProductCountHistoryEntity.class));
        assertThat(savedHistory).isNotNull();
    }

    private ProductCountHistory createTestHistory() {
        return ProductCountHistory.record(
            MustitSellerId.of(1L),
            100,
            LocalDateTime.now()
        );
    }

    private ProductCountHistoryEntity createTestEntity() {
        return ProductCountHistoryEntity.create(1L, 100, LocalDateTime.now());
    }
}
```

#### 7.4.2 ProductCountHistoryQueryAdapterTest.java (Query) ⭐

```java
package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.adapter;

import entity.com.ryuqq.crawlinghub.adapter.out.persistence.seller.ProductCountHistoryEntity;
import mapper.com.ryuqq.crawlinghub.adapter.out.persistence.seller.ProductCountHistoryMapper;
import repository.com.ryuqq.crawlinghub.adapter.out.persistence.seller.ProductCountHistoryQueryRepository;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import history.com.ryuqq.crawlinghub.domain.seller.ProductCountHistory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * ProductCountHistoryQueryAdapterTest - Query Adapter 단위 테스트 (QueryDSL) ⭐
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Tag("unit")
@Tag("query")
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCountHistoryQueryAdapter 단위 테스트")
class ProductCountHistoryQueryAdapterTest {

    @InjectMocks
    private ProductCountHistoryQueryAdapter queryAdapter;

    @Mock
    private ProductCountHistoryQueryRepository queryRepository;

    @Mock
    private ProductCountHistoryMapper mapper;

    @Test
    @DisplayName("정상 케이스: 상품 수 이력 조회 성공")
    void loadHistories_success() {
        // Given
        MustitSellerId sellerId = MustitSellerId.of(1L);
        int page = 0;
        int size = 10;

        List<ProductCountHistoryEntity> entities = List.of(createTestEntity());
        ProductCountHistory history = createTestHistory();

        when(queryRepository.findHistoriesBySellerId(anyLong(), anyInt(), anyInt())).thenReturn(entities);
        when(mapper.toDomain(any(ProductCountHistoryEntity.class))).thenReturn(history);

        // When
        List<ProductCountHistory> histories = queryAdapter.loadHistories(sellerId, page, size);

        // Then
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getSellerIdValue()).isEqualTo(1L);
    }

    @Test
    @DisplayName("정상 케이스: 전체 이력 개수 조회 성공")
    void countHistories_success() {
        // Given
        MustitSellerId sellerId = MustitSellerId.of(1L);
        when(queryRepository.countHistoriesBySellerId(anyLong())).thenReturn(5L);

        // When
        long count = queryAdapter.countHistories(sellerId);

        // Then
        assertThat(count).isEqualTo(5L);
    }

    private ProductCountHistory createTestHistory() {
        return ProductCountHistory.record(
            MustitSellerId.of(1L),
            100,
            LocalDateTime.now()
        );
    }

    private ProductCountHistoryEntity createTestEntity() {
        return ProductCountHistoryEntity.create(1L, 100, LocalDateTime.now());
    }
}
```

#### 7.4.3 ProductCountHistoryQueryRepositoryIntegrationTest.java (Integration - QueryDSL) ⭐

```java
package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.repository;

import entity.com.ryuqq.crawlinghub.adapter.out.persistence.seller.ProductCountHistoryEntity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProductCountHistoryQueryRepositoryIntegrationTest - QueryDSL 통합 테스트 ⭐
 *
 * <p>Testcontainers 기반 실제 DB 통합 테스트</p>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@DataJpaTest
@Testcontainers
@Tag("integration")
@Tag("persistence")
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ProductCountHistoryQueryRepository.class) // QueryDSL Repository 주입
@DisplayName("ProductCountHistoryQueryRepository 통합 테스트 (QueryDSL)")
class ProductCountHistoryQueryRepositoryIntegrationTest {

    @Autowired
    private ProductCountHistoryQueryRepository queryRepository;

    @Autowired
    private ProductCountHistoryJpaRepository jpaRepository;

    @Test
    @DisplayName("정상 케이스: 셀러별 이력 조회 성공 (페이징)")
    void findHistoriesBySellerId_success() {
        // Given
        Long sellerId = 1L;
        jpaRepository.save(ProductCountHistoryEntity.create(sellerId, 100, LocalDateTime.now()));
        jpaRepository.save(ProductCountHistoryEntity.create(sellerId, 150, LocalDateTime.now()));

        // When
        List<ProductCountHistoryEntity> histories = queryRepository.findHistoriesBySellerId(sellerId, 0, 10);

        // Then
        assertThat(histories).hasSize(2);
        assertThat(histories.get(0).getSellerId()).isEqualTo(sellerId);
    }

    @Test
    @DisplayName("정상 케이스: 전체 이력 개수 조회 성공")
    void countHistoriesBySellerId_success() {
        // Given
        Long sellerId = 1L;
        jpaRepository.save(ProductCountHistoryEntity.create(sellerId, 100, LocalDateTime.now()));
        jpaRepository.save(ProductCountHistoryEntity.create(sellerId, 150, LocalDateTime.now()));

        // When
        long count = queryRepository.countHistoriesBySellerId(sellerId);

        // Then
        assertThat(count).isEqualTo(2L);
    }
}
```

### 7.5 REST API Layer 테스트

#### 7.5.1 SellerControllerIntegrationTest.java

```java
package com.ryuqq.crawlinghub.adapter.in.rest.seller.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.ProductCountHistoryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerDetailApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.PageResponse;
import response.dto.com.ryuqq.crawlinghub.application.seller.ProductCountHistoryResponse;
import response.dto.com.ryuqq.crawlinghub.application.seller.SellerDetailResponse;
import in.port.com.ryuqq.crawlinghub.application.seller.GetSellerDetailUseCase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SellerControllerIntegrationTest - SellerController 통합 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@WebMvcTest(SellerController.class)
@Tag("integration")
@Tag("rest-api")
@DisplayName("SellerController 통합 테스트")
class SellerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetSellerDetailUseCase getSellerDetailUseCase;

    @Test
    @DisplayName("정상 케이스: 셀러 상세 조회 성공 (확장된 응답)")
    void getSellerDetail_success() throws Exception {
        // Given
        Long sellerId = 1L;
        SellerDetailResponse response = createTestSellerDetailResponse();
        when(getSellerDetailUseCase.getDetail(anyLong())).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/sellers/{sellerId}", sellerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.sellerId").value(1))
            .andExpect(jsonPath("$.data.sellerName").value("테스트 셀러"))
            .andExpect(jsonPath("$.data.totalProductCount").value(100))
            .andExpect(jsonPath("$.data.productCountHistories.content").isArray())
            .andExpect(jsonPath("$.data.productCountHistories.totalElements").value(1))
            .andExpect(jsonPath("$.data.scheduleInfo.scheduleId").value(10));
    }

    private SellerDetailResponse createTestSellerDetailResponse() {
        PageResponse<ProductCountHistoryResponse> historyPage = PageResponse.of(
            List.of(new ProductCountHistoryResponse(1L, LocalDateTime.now(), 100)),
            0,
            10,
            1L,
            1,
            true,
            true
        );

        return new SellerDetailResponse(
            1L,
            "SELLER001",
            "테스트 셀러",
            "ACTIVE",
            100,
            historyPage,
            null,
            PageResponse.empty(0, 10)
        );
    }
}
```

---

## 8. 작업 단계 (Work Phases)

### Phase 1: Domain Layer (1일)
- [ ] `ProductCountHistory` Domain 객체 생성 (실행날짜 + 카운트만) ⭐
- [ ] `ProductCountHistoryId` Value Object 생성
- [ ] 단위 테스트 작성 (2개)
- [ ] **검증**: `./gradlew :domain:test`

### Phase 2: Application Layer (2일)
- [ ] `SaveProductCountHistoryPort` / `LoadProductCountHistoryPort` Port 생성 ⭐
- [ ] `SellerManager` 생성 (Manager 패턴) ⭐
- [ ] `SellerCommandFacade` 생성 (Facade 패턴) ⭐
- [ ] `GetSellerDetailService` 확장 (PageResponse 통합) ⭐
- [ ] 신규 DTO 3개 생성 (`ProductCountHistoryResponse`, `ScheduleInfoResponse`, `ScheduleHistoryResponse`)
- [ ] `SellerDetailResponse` 확장 (PageResponse 필드 추가)
- [ ] 단위 테스트 작성 (3개)
- [ ] **검증**: `./gradlew :application:test`

### Phase 3: Persistence Layer (CQRS) (2-3일) ⭐
- [ ] Flyway 마이그레이션 스크립트 작성 (실행날짜 + 카운트 컬럼) ⭐
- [ ] `ProductCountHistoryEntity` JPA Entity 생성
- [ ] `ProductCountHistoryJpaRepository` (Command - JPA) ⭐
- [ ] `ProductCountHistoryQueryRepository` (Query - QueryDSL) ⭐
- [ ] `ProductCountHistoryMapper` (MapStruct)
- [ ] `ProductCountHistoryCommandAdapter` (Command Adapter) ⭐
- [ ] `ProductCountHistoryQueryAdapter` (Query Adapter) ⭐
- [ ] 단위 테스트 3개 (Command, Query, Integration - QueryDSL) ⭐
- [ ] **검증**: `./gradlew :adapter-out:persistence-mysql:test`

### Phase 4: REST API Layer (1일)
- [ ] `PageApiResponse` 공통 DTO 생성 ⭐
- [ ] 신규 API DTO 3개 생성
- [ ] `SellerDetailApiResponse` 확장 (PageApiResponse 필드 추가) ⭐
- [ ] `SellerApiMapper` 확장 (PageResponse → PageApiResponse 변환) ⭐
- [ ] `SellerController` 수정
- [ ] 통합 테스트 작성 (1개)
- [ ] **검증**: `./gradlew :adapter-in:rest-api:test`

### Phase 5: 최종 검증 (1일)
- [ ] `./gradlew build` 실행 (전체 빌드)
- [ ] ArchUnit 테스트 통과 확인
- [ ] Checkstyle, SpotBugs 통과 확인 (0 violations)
- [ ] JaCoCo 커버리지 확인 (Domain 90%, Application 80%, Adapter 70%)
- [ ] API 엔드포인트 수동 테스트 (Postman/curl)
- [ ] OpenAPI/Swagger 문서 확인

**예상 총 기간**: 7-8일

---

## 9. Cursor AI 작업 큐 가이드

### 9.1 큐 시스템 사용법

```bash
# 1. 작업 큐에 추가
/queue-add seller seller-refactoring-plan.md

# 2. 작업 시작 (Worktree 자동 생성)
/queue-start seller

# 3. Worktree로 이동
cd ../wt-seller

# 4. Cursor AI로 Boilerplate 생성
# - Domain Layer: ProductCountHistory, ProductCountHistoryId
# - Application Layer: Ports, Manager, Facade, DTOs
# - Persistence Layer: Entity, Repository (CQRS), Mapper, Adapter
# - REST API Layer: API DTOs, Mapper, Controller

# 5. Claude Code로 비즈니스 로직 구현
# - Manager 패턴 로직
# - Facade 패턴 로직
# - QueryDSL 쿼리 최적화

# 6. Git Commit
git add .
git commit -m "feat(seller): CQRS 패턴 및 Manager/Facade 패턴 적용

- ProductCountHistory 도메인 객체 생성 (실행날짜 + 카운트만)
- CQRS 패턴 적용 (Command/Query 분리)
- Manager 패턴 적용 (SellerManager)
- Facade 패턴 적용 (SellerCommandFacade)
- PageResponse/PageApiResponse 통합

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>"

# 7. 작업 완료 표시
/queue-complete seller

# 8. PR 생성
gh pr create --title "feat(seller): CQRS 패턴 및 Manager/Facade 패턴 적용" \
  --body "$(cat seller-refactoring-plan.md)"
```

### 9.2 작업 단계별 큐 작업

#### Phase 1: Domain Layer
```bash
/queue-add seller-domain "Phase 1: Domain Layer 작업"
/queue-start seller-domain
# Cursor AI: ProductCountHistory, ProductCountHistoryId 생성
# Claude Code: 비즈니스 로직 검증
git commit -m "feat(domain): ProductCountHistory 도메인 객체 생성"
/queue-complete seller-domain
```

#### Phase 2: Application Layer
```bash
/queue-add seller-application "Phase 2: Application Layer 작업"
/queue-start seller-application
# Cursor AI: Ports, Manager, Facade, DTOs 생성
# Claude Code: Manager/Facade 로직 구현
git commit -m "feat(application): Manager/Facade 패턴 적용"
/queue-complete seller-application
```

#### Phase 3: Persistence Layer (CQRS)
```bash
/queue-add seller-persistence "Phase 3: Persistence Layer 작업 (CQRS)"
/queue-start seller-persistence
# Cursor AI: Entity, Repository, Mapper, Adapter 생성
# Claude Code: QueryDSL 쿼리 최적화
git commit -m "feat(persistence): CQRS 패턴 적용 (QueryDSL)"
/queue-complete seller-persistence
```

#### Phase 4: REST API Layer
```bash
/queue-add seller-rest-api "Phase 4: REST API Layer 작업"
/queue-start seller-rest-api
# Cursor AI: API DTOs, Mapper, Controller 생성
# Claude Code: PageResponse 변환 로직 구현
git commit -m "feat(rest-api): PageApiResponse 통합"
/queue-complete seller-rest-api
```

---

## 10. Zero-Tolerance 체크리스트

### 10.1 코딩 규칙 준수

- [ ] **Lombok 금지**: 모든 레이어에서 Pure Java 사용
- [ ] **Law of Demeter**: Getter 체이닝 없음
- [ ] **Long FK 전략**: JPA 관계 어노테이션 없음
- [ ] **Transaction 경계**: `@Transactional` 내 외부 API 호출 없음
- [ ] **Javadoc 필수**: 모든 public 클래스/메서드에 Javadoc
- [ ] **Scope 준수**: 요청된 코드만 작성

### 10.2 CQRS 패턴 준수 ⭐

- [ ] **Command/Query 분리**: Command Adapter + Query Adapter 분리
- [ ] **QueryDSL 사용**: Query Adapter에서 QueryDSL 사용
- [ ] **JPA Repository**: Command 작업만 수행
- [ ] **N+1 방지**: QueryDSL로 최적화된 쿼리

### 10.3 Manager/Facade 패턴 준수 ⭐

- [ ] **Manager 역할**: 횡단 관심사 처리 (상품 수 업데이트 + 이력 자동 저장)
- [ ] **Facade 역할**: 여러 UseCase 조율
- [ ] **트랜잭션 관리**: Manager/Facade에서 `@Transactional` 적용

### 10.4 PageResponse 통합 준수 ⭐

- [ ] **Application Layer**: `PageResponse` 사용
- [ ] **REST API Layer**: `PageApiResponse`로 변환
- [ ] **Mapper 변환**: `SellerApiMapper`에서 변환 로직 구현

---

## 11. 리스크 분석

### 11.1 기술적 리스크

| 리스크 | 확률 | 영향 | 완화 전략 |
|--------|------|------|----------|
| QueryDSL 설정 누락 | 중 | 높음 | `build.gradle.kts`에 QueryDSL 의존성 추가 확인 |
| N+1 문제 발생 | 중 | 중간 | QueryDSL로 최적화된 쿼리 작성, 통합 테스트로 검증 |
| Manager/Facade 오용 | 낮음 | 중간 | 명확한 역할 정의, 코드 리뷰로 검증 |
| PageResponse 변환 누락 | 낮음 | 낮음 | SellerApiMapper 단위 테스트로 검증 |

### 11.2 일정 리스크

| 리스크 | 확률 | 영향 | 완화 전략 |
|--------|------|------|----------|
| Persistence Layer 작업 지연 (QueryDSL) | 중 | 높음 | Phase 3를 2-3일로 충분한 시간 확보 |
| 테스트 작성 지연 | 중 | 중간 | 각 Phase에서 즉시 테스트 작성 |
| 통합 테스트 실패 | 낮음 | 중간 | Testcontainers 환경 사전 검증 |

---

**✅ 이 문서는 Cursor AI에게 작업을 위임하기 위한 완전한 명세서입니다.**

**💡 핵심 변경사항 (v2)**:
1. **ProductCountHistory 단순화**: 실행날짜 + 카운트만 저장 ⭐
2. **CQRS 패턴 적용**: Command/Query 분리, QueryDSL 사용 ⭐
3. **Manager 패턴**: SellerManager 추가 ⭐
4. **Facade 패턴**: SellerCommandFacade 추가 ⭐
5. **PageResponse 통합**: Application Layer → REST API Layer 변환 ⭐
6. **Cursor AI 작업 큐**: `/queue-add` → `/queue-start` 워크플로우 ⭐
