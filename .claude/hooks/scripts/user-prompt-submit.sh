#!/bin/bash
# ========================================
# Claude Code Dynamic Hook
# user-prompt-submit: 사용자 요청 제출 시 실행
# ========================================
# Claude가 코드를 생성하기 BEFORE 실행
# 요청 분석 및 모듈 컨텍스트 주입
# ========================================

set -e

HOOK_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$HOOK_DIR/../.." && pwd)"

# Colors
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${BLUE}🤖 [Dynamic Hook] $1${NC}" >&2
}

log_warning() {
    echo -e "${YELLOW}⚠️  [Dynamic Hook] $1${NC}" >&2
}

# ========================================
# 사용자 요청 분석
# ========================================

USER_PROMPT="$1"

log_info "Analyzing user request..."

# ========================================
# 모듈 컨텍스트 감지
# ========================================

MODULE_CONTEXT=""

if echo "$USER_PROMPT" | grep -qi "domain\|도메인\|비즈니스 로직"; then
    MODULE_CONTEXT="domain"
elif echo "$USER_PROMPT" | grep -qi "usecase\|application\|서비스\|유즈케이스"; then
    MODULE_CONTEXT="application"
elif echo "$USER_PROMPT" | grep -qi "controller\|rest\|api\|어댑터"; then
    MODULE_CONTEXT="adapter"
elif echo "$USER_PROMPT" | grep -qi "repository\|jpa\|database\|persistence\|entity"; then
    MODULE_CONTEXT="adapter-out-persistence"
elif echo "$USER_PROMPT" | grep -qi "request\|response\|dto\|컨트롤러"; then
    MODULE_CONTEXT="adapter-in-web"
fi

# ========================================
# 컨텍스트 기반 가이드라인 주입
# ========================================

if [ -n "$MODULE_CONTEXT" ]; then
    log_info "Module context detected: $MODULE_CONTEXT"

    case $MODULE_CONTEXT in
        domain)
            cat << 'EOF'

# 🏛️ DOMAIN MODULE - 핵심 규칙

## ❌ 절대 금지
- Spring Framework (org.springframework.*)
- JPA/Hibernate (jakarta.persistence.*, org.hibernate.*)
- Lombok, Jackson 애노테이션
- 인프라 의존성

## ✅ 허용
- Pure Java (java.*, javax.validation.*)
- Apache Commons Lang3
- 비즈니스 로직만

## 📚 상세 가이드
- **아키텍처**: docs/ENTERPRISE_SPRING_STANDARDS_SUMMARY.md (Domain Layer)
- **DDD 패턴**: docs/DDD_AGGREGATE_MIGRATION_GUIDE.md
- **Value Object**: docs/JAVA_RECORD_GUIDE.md (Record 권장)
- **예외 처리**: docs/EXCEPTION_HANDLING_GUIDE.md

## 🎯 테스트: 90%+ 커버리지

EOF
            ;;

        application)
            cat << 'EOF'

# 🔧 APPLICATION MODULE - 핵심 규칙

## ❌ 절대 금지
- Adapter imports (com.company.template.adapter.*)
- Lombok imports or annotations
- 직접적인 JPA 사용 (adapter-out-persistence 소관)

## ✅ 허용
- Domain imports (com.company.template.domain.*)
- Spring DI (@Service, @Transactional)
- Port interfaces (in/out)

## 📝 필수 패턴
- **@Transactional**: 이 레이어에서만, Adapter에서는 절대 금지
- **UseCase Pattern**: 단일 책임 원칙
- **Port 추상화**: Adapter 직접 참조 금지
- **Domain 객체만**: JPA Entity 직접 사용 금지

## 📚 상세 가이드
- **아키텍처**: docs/ENTERPRISE_SPRING_STANDARDS_SUMMARY.md (Application Layer)
- **DTO 패턴**: docs/DTO_PATTERNS_GUIDE.md
- **예외 처리**: docs/EXCEPTION_HANDLING_GUIDE.md

## 🎯 테스트: 80%+ 커버리지

EOF
            ;;

        adapter)
            cat << 'EOF'

# 📡 ADAPTER MODULE - 핵심 규칙

## ❌ 절대 금지
- Lombok imports or annotations
- 비즈니스 로직 (Domain 소관)

## ✅ 허용
- Domain and Application imports
- Spring Framework (Web, JPA, etc.)
- Infrastructure code (HTTP, DB, AWS SDK)

## 📝 필수 사항
- Controller 접미사: ~Controller
- Repository 접미사: ~Repository
- Public 메서드: Javadoc + @author
- Pure Java (Lombok 금지)

## 📚 상세 가이드
- **아키텍처**: docs/ENTERPRISE_SPRING_STANDARDS_SUMMARY.md (Adapter)
- **예외 처리**: docs/EXCEPTION_HANDLING_GUIDE.md

## 🎯 테스트: 70%+ 커버리지, Testcontainers 사용

EOF
            ;;

        adapter-out-persistence)
            cat << 'EOF'

# 💾 PERSISTENCE ADAPTER - 핵심 규칙

## ❌ 절대 금지
- NO Lombok imports or annotations
- NO @Transactional (Application Layer에서만 관리)
- NO JPA Relationships (@OneToMany, @ManyToOne, @OneToOne, @ManyToMany)
- NO public constructors (protected for JPA, private for logic)
- NO setter methods (불변성 보장)
- NO business logic (Domain 소관)

## ✅ 허용
- Spring Data JPA, QueryDSL
- JPA Entity (Domain Entity와 분리)
- Long FK 필드 (userId, orderId 등)
- Mapper classes (Entity ↔ Domain 변환)

## 📝 필수 패턴
- **Static Factory Methods**: `create()`, `reconstitute()`
- **Getter only**: Setter 금지, 불변 객체
- **Mapper Pattern**: Entity ↔ Domain 변환용 전용 클래스
- **FK as Long**: JPA 관계 대신 Long ID 참조

## 📚 상세 가이드
- **아키텍처**: docs/ENTERPRISE_SPRING_STANDARDS_SUMMARY.md (Persistence)
- **Entity 패턴**: docs/DDD_AGGREGATE_MIGRATION_GUIDE.md

## 🎯 테스트: 70%+ 커버리지, Testcontainers 필수

EOF
            ;;

        adapter-in-web)
            cat << 'EOF'

# 🌐 CONTROLLER ADAPTER - 핵심 규칙

## ❌ 절대 금지
- NO Lombok imports or annotations
- NO Inner Classes (Request/Response는 별도 파일)
- NO business logic (Domain 소관)
- NO domain entities 노출
- NO Repository/Entity 직접 의존

## ✅ 허용
- Spring Web (@RestController, @RequestMapping)
- Request/Response DTOs as Records (별도 파일)
- UseCase 의존만 허용

## 📝 필수 패턴
- **DTOs as Records**: Request/Response는 Java Record, 별도 파일로 분리
- **Record Validation**: Bean Validation + Compact constructor 검증
- **UseCase Only**: Repository, Entity, Adapter 직접 참조 금지
- **Thin Controller**: DTO → Command → UseCase → Result → Response 흐름만
- **Conversion Methods**: `toCommand()` (Request), `from(Result)` (Response)

## 📚 상세 가이드
- **아키텍처**: docs/ENTERPRISE_SPRING_STANDARDS_SUMMARY.md (Web Adapter)
- **DTO 패턴**: docs/DTO_PATTERNS_GUIDE.md (Request/Response 변환)
- **예외 처리**: docs/EXCEPTION_HANDLING_GUIDE.md
- **Record 가이드**: docs/JAVA_RECORD_GUIDE.md

## 🎯 테스트: 70%+ 커버리지

EOF
            ;;
    esac
fi

# ========================================
# 글로벌 리마인더 (모든 모듈)
# ========================================

cat << 'EOF'

# 🚨 GLOBAL ENTERPRISE STANDARDS

## 🚫 LOMBOK IS STRICTLY PROHIBITED
- NO @Data, @Builder, @Getter, @Setter, @AllArgsConstructor, @NoArgsConstructor
- Use plain Java with manual getters/setters/constructors
- This is a ZERO TOLERANCE rule

## 📝 DOCUMENTATION REQUIREMENTS
- All public classes MUST have Javadoc
- MUST include: @author Name (email@company.com)
- MUST include: @since YYYY-MM-DD
- Public methods MUST have parameter/return documentation

## 🎯 SCOPE DISCIPLINE
- ONLY write code that is EXPLICITLY requested
- NO additional helper classes unless asked
- NO speculative features or "nice to have" additions
- If you add Utils/Helper/Manager classes, justify why

## ✅ VALIDATION
- Your code will be validated by:
  - ArchUnit tests (architecture rules)
  - Checkstyle (code style)
  - SpotBugs (bug detection)
  - Git pre-commit hooks
  - Dead code detector

## 💡 BEFORE WRITING CODE
1. Identify which module this belongs to (domain/application/adapter)
2. Follow module-specific rules above
3. Use pure Java (no Lombok)
4. Write tests (TDD preferred)
5. Add Javadoc with @author tag

Good luck! 🚀
EOF

# ========================================
# Exit Successfully (allow request)
# ========================================

exit 0
