# 🛠️ 구현 가이드

## 📌 개요

이 문서는 범용 크롤링 시스템의 스키마를 실제 애플리케이션에서 구현하는 방법을 안내합니다.
EAV 패턴 기반의 유연한 스키마를 효과적으로 활용하는 방법을 제시합니다.

## 🏗️ 아키텍처 레이어

### 1. 데이터베이스 레이어
```
MySQL Database
├── Core Tables (EAV Pattern)
├── Workflow Tables
├── Security Tables
└── Monitoring Tables
```

### 2. 데이터 액세스 레이어
```
Repository Layer
├── FlexibleRepository (EAV CRUD)
├── TypeRepository (메타데이터)
├── WorkflowRepository
└── SecurityRepository
```

### 3. 비즈니스 로직 레이어
```
Service Layer
├── CrawlingService
├── DataMappingService
├── ValidationService
└── MonitoringService
```

## 📦 JPA 엔티티 구현

### 1. Core 엔티티

#### CrawlingSource.java
```java
@Entity
@Table(name = "crawling_sources")
public class CrawlingSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "source_code", unique = true, nullable = false)
    private String sourceCode;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "base_url")
    private String baseUrl;
    
    @Column(name = "api_base_url")
    private String apiBaseUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;
    
    @Enumerated(EnumType.STRING)
    private SourceStatus status = SourceStatus.ACTIVE;
    
    @OneToMany(mappedBy = "source")
    private List<CrawlingTarget> targets = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    // Getters and Setters
}
```

#### TargetType.java
```java
@Entity
@Table(name = "target_types")
public class TargetType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "type_code", unique = true, nullable = false)
    private String typeCode;
    
    @Column(name = "type_name", nullable = false)
    private String typeName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "parent_type_id")
    private TargetType parentType;
    
    @OneToMany(mappedBy = "parentType")
    private List<TargetType> childTypes = new ArrayList<>();
    
    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL)
    private List<TargetAttribute> attributes = new ArrayList<>();
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // Getters and Setters
}
```

#### CrawlingData.java (EAV Entity)
```java
@Entity
@Table(name = "crawling_data")
public class CrawlingData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "target_id", nullable = false)
    private CrawlingTarget target;
    
    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private CrawlingJob job;
    
    @Column(name = "crawled_at")
    private LocalDateTime crawledAt = LocalDateTime.now();
    
    @Column(name = "version_hash", nullable = false)
    private String versionHash;
    
    @Column(name = "s3_path")
    private String s3Path;
    
    @Column(name = "is_current")
    private Boolean isCurrent = true;
    
    @OneToMany(mappedBy = "data", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrawlingDataValue> values = new ArrayList<>();
    
    // Helper methods
    public void addValue(TargetAttribute attribute, Object value) {
        CrawlingDataValue dataValue = new CrawlingDataValue();
        dataValue.setData(this);
        dataValue.setAttribute(attribute);
        dataValue.setValue(value);
        values.add(dataValue);
    }
    
    public Map<String, Object> toMap() {
        return values.stream()
            .collect(Collectors.toMap(
                v -> v.getAttribute().getAttributeCode(),
                CrawlingDataValue::getValue
            ));
    }
}
```

### 2. Repository 구현

#### FlexibleRepository.java
```java
@Repository
public class FlexibleRepository {
    
    @PersistenceContext
    private EntityManager em;
    
    /**
     * EAV 데이터를 Map으로 조회
     */
    public Map<String, Object> findDataAsMap(Long targetId) {
        String jpql = """
            SELECT ta.attributeCode, cdv
            FROM CrawlingData cd
            JOIN cd.values cdv
            JOIN cdv.attribute ta
            WHERE cd.target.id = :targetId
              AND cd.isCurrent = true
            """;
        
        List<Object[]> results = em.createQuery(jpql, Object[].class)
            .setParameter("targetId", targetId)
            .getResultList();
        
        Map<String, Object> dataMap = new HashMap<>();
        for (Object[] row : results) {
            String code = (String) row[0];
            CrawlingDataValue value = (CrawlingDataValue) row[1];
            dataMap.put(code, value.getValue());
        }
        
        return dataMap;
    }
    
    /**
     * EAV 데이터를 특정 클래스로 매핑
     */
    public <T> T findDataAs(Long targetId, Class<T> targetClass) {
        Map<String, Object> dataMap = findDataAsMap(targetId);
        return mapToObject(dataMap, targetClass);
    }
    
    /**
     * 동적 타입 추가
     */
    @Transactional
    public TargetType createType(String typeCode, String typeName, 
                                 Map<String, DataType> attributes) {
        TargetType type = new TargetType();
        type.setTypeCode(typeCode);
        type.setTypeName(typeName);
        em.persist(type);
        
        for (Map.Entry<String, DataType> entry : attributes.entrySet()) {
            TargetAttribute attr = new TargetAttribute();
            attr.setType(type);
            attr.setAttributeCode(entry.getKey());
            attr.setAttributeName(entry.getKey());
            attr.setDataType(entry.getValue());
            em.persist(attr);
        }
        
        return type;
    }
    
    /**
     * 데이터 저장
     */
    @Transactional
    public CrawlingData saveData(CrawlingTarget target, Map<String, Object> data) {
        // 이전 버전을 비활성화
        em.createQuery("""
            UPDATE CrawlingData cd
            SET cd.isCurrent = false
            WHERE cd.target = :target AND cd.isCurrent = true
            """)
            .setParameter("target", target)
            .executeUpdate();
        
        // 새 버전 생성
        CrawlingData newData = new CrawlingData();
        newData.setTarget(target);
        newData.setVersionHash(calculateHash(data));
        newData.setIsCurrent(true);
        
        // 속성별 값 저장
        for (TargetAttribute attr : target.getType().getAttributes()) {
            Object value = data.get(attr.getAttributeCode());
            if (value != null) {
                newData.addValue(attr, value);
            }
        }
        
        em.persist(newData);
        return newData;
    }
    
    private <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        // ObjectMapper 또는 리플렉션을 사용한 매핑
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(map, clazz);
    }
}
```

### 3. Service 구현

#### CrawlingService.java
```java
@Service
@Transactional
public class CrawlingService {
    
    @Autowired
    private FlexibleRepository repository;
    
    @Autowired
    private WorkflowService workflowService;
    
    @Autowired
    private ValidationService validationService;
    
    /**
     * 새로운 타입의 데이터 크롤링
     */
    public void crawlNewType(String sourceCode, String typeCode, String url) {
        // 1. 타입이 없으면 동적 생성
        TargetType type = repository.findTypeByCode(typeCode)
            .orElseGet(() -> createTypeFromUrl(url));
        
        // 2. 타겟 생성/조회
        CrawlingTarget target = repository.findOrCreateTarget(
            sourceCode, typeCode, extractTargetCode(url), url
        );
        
        // 3. 크롤링 실행
        Map<String, Object> crawledData = executeCrawling(url);
        
        // 4. 검증
        ValidationResult validation = validationService.validate(crawledData, type);
        if (!validation.isValid()) {
            throw new ValidationException(validation.getErrors());
        }
        
        // 5. 저장
        repository.saveData(target, crawledData);
    }
    
    /**
     * MUSTIT 상품 크롤링 (구체적 예시)
     */
    public void crawlMustitProduct(String productCode) {
        // EAV로 저장되지만 타입 안정성 유지
        ProductData product = repository.findDataAs(
            targetId, 
            ProductData.class
        );
        
        // 비즈니스 로직 처리
        if (product.getPrice() < product.getOriginalPrice()) {
            product.setDiscountRate(
                calculateDiscountRate(product.getPrice(), product.getOriginalPrice())
            );
        }
        
        // 다시 Map으로 변환하여 저장
        Map<String, Object> dataMap = objectToMap(product);
        repository.saveData(target, dataMap);
    }
}
```

### 4. 동적 쿼리 지원

#### DynamicQueryBuilder.java
```java
@Component
public class DynamicQueryBuilder {
    
    @PersistenceContext
    private EntityManager em;
    
    /**
     * 동적 조건으로 EAV 데이터 조회
     */
    public List<Map<String, Object>> findByDynamicCriteria(
            String typeCode, 
            Map<String, Object> criteria) {
        
        StringBuilder jpql = new StringBuilder("""
            SELECT DISTINCT cd
            FROM CrawlingData cd
            JOIN cd.target t
            JOIN t.type tt
            WHERE tt.typeCode = :typeCode
              AND cd.isCurrent = true
            """);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("typeCode", typeCode);
        
        // 동적 조건 추가
        for (Map.Entry<String, Object> criterion : criteria.entrySet()) {
            String attrCode = criterion.getKey();
            Object value = criterion.getValue();
            
            jpql.append("""
                AND EXISTS (
                    SELECT 1 FROM CrawlingDataValue cdv
                    JOIN cdv.attribute ta
                    WHERE cdv.data = cd
                      AND ta.attributeCode = :attr_%s
                      AND cdv.%s = :value_%s
                )
                """.formatted(attrCode, getValueColumn(value), attrCode));
            
            parameters.put("attr_" + attrCode, attrCode);
            parameters.put("value_" + attrCode, value);
        }
        
        TypedQuery<CrawlingData> query = em.createQuery(jpql.toString(), CrawlingData.class);
        parameters.forEach(query::setParameter);
        
        return query.getResultList().stream()
            .map(CrawlingData::toMap)
            .collect(Collectors.toList());
    }
    
    private String getValueColumn(Object value) {
        if (value instanceof Number) return "valueNumber";
        if (value instanceof Boolean) return "valueBoolean";
        if (value instanceof LocalDateTime) return "valueDate";
        return "valueText";
    }
}
```

## 🚀 성능 최적화

### 1. 캐싱 전략

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}

@Service
public class CachedDataService {
    
    @Cacheable(value = "targetTypes", key = "#typeCode")
    public TargetType getType(String typeCode) {
        // 타입 메타데이터는 자주 변경되지 않으므로 캐싱
        return repository.findTypeByCode(typeCode);
    }
    
    @Cacheable(value = "currentData", key = "#targetId")
    public Map<String, Object> getCurrentData(Long targetId) {
        // 현재 데이터 캐싱
        return repository.findDataAsMap(targetId);
    }
    
    @CacheEvict(value = "currentData", key = "#target.id")
    public void updateData(CrawlingTarget target, Map<String, Object> data) {
        repository.saveData(target, data);
    }
}
```

### 2. Batch 처리

```java
@Configuration
@EnableBatchProcessing
public class BatchConfig {
    
    @Bean
    public Job crawlingJob(JobBuilderFactory jobBuilderFactory,
                           StepBuilderFactory stepBuilderFactory) {
        return jobBuilderFactory.get("crawlingJob")
            .start(crawlingStep(stepBuilderFactory))
            .build();
    }
    
    @Bean
    public Step crawlingStep(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory.get("crawlingStep")
            .<CrawlingTarget, CrawlingData>chunk(100)
            .reader(targetReader())
            .processor(crawlingProcessor())
            .writer(dataWriter())
            .build();
    }
    
    @Bean
    public ItemReader<CrawlingTarget> targetReader() {
        return new JpaPagingItemReaderBuilder<CrawlingTarget>()
            .name("targetReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString("""
                SELECT t FROM CrawlingTarget t
                WHERE t.status = 'ACTIVE'
                  AND t.lastCrawledAt < :threshold
                ORDER BY t.crawlPriority DESC
                """)
            .parameterValues(Map.of("threshold", LocalDateTime.now().minusHours(1)))
            .pageSize(100)
            .build();
    }
}
```

### 3. View Materialization

```java
@Component
public class MaterializedViewService {
    
    @Scheduled(fixedDelay = 300000) // 5분마다
    public void refreshProductView() {
        jdbcTemplate.execute("""
            CREATE OR REPLACE VIEW v_current_products AS
            SELECT 
                t.id as target_id,
                t.target_code as product_code,
                MAX(CASE WHEN ta.attribute_code = 'name' THEN cdv.value_text END) as name,
                MAX(CASE WHEN ta.attribute_code = 'price' THEN cdv.value_number END) as price,
                MAX(CASE WHEN ta.attribute_code = 'brand' THEN cdv.value_text END) as brand
            FROM crawling_targets t
            JOIN crawling_data cd ON t.id = cd.target_id AND cd.is_current = TRUE
            JOIN crawling_data_values cdv ON cd.id = cdv.data_id
            JOIN target_attributes ta ON cdv.attribute_id = ta.id
            WHERE t.type_id = (SELECT id FROM target_types WHERE type_code = 'PRODUCT')
            GROUP BY t.id, t.target_code
            """);
    }
}
```

## 🔍 모니터링

### Metrics 수집
```java
@Component
public class CrawlingMetrics {
    
    private final MeterRegistry registry;
    
    public CrawlingMetrics(MeterRegistry registry) {
        this.registry = registry;
    }
    
    public void recordCrawling(String sourceCode, String typeCode, boolean success) {
        registry.counter("crawling.attempts",
            "source", sourceCode,
            "type", typeCode,
            "result", success ? "success" : "failure"
        ).increment();
    }
    
    public void recordResponseTime(String sourceCode, long milliseconds) {
        registry.timer("crawling.response.time",
            "source", sourceCode
        ).record(Duration.ofMillis(milliseconds));
    }
}
```

## ✅ 체크리스트

### 초기 구현
- [ ] JPA 엔티티 생성
- [ ] Repository 레이어 구현
- [ ] Service 레이어 구현
- [ ] 기본 CRUD 테스트

### 고급 기능
- [ ] 동적 타입 생성 API
- [ ] EAV ↔ Object 매핑
- [ ] 캐싱 구현
- [ ] Batch 처리

### 성능 최적화
- [ ] 인덱스 최적화
- [ ] Materialized View
- [ ] 쿼리 최적화
- [ ] 캐시 전략

### 모니터링
- [ ] 메트릭 수집
- [ ] 로깅 구현
- [ ] 알림 설정
- [ ] 대시보드 구성

## 📚 관련 문서
- [00-overview.md](00-overview.md) - 전체 개요
- [01-target-domain.md](01-target-domain.md) - 타겟 도메인
- [05-migration-guide.md](05-migration-guide.md) - 마이그레이션 가이드
