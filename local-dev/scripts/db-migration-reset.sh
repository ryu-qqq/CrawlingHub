#!/bin/bash

# ===============================================
# Database Migration Reset Script
# ===============================================
# crawler 스키마의 모든 테이블을 드랍하고 Flyway 재적용을 준비합니다.
#
# 흐름:
#   Phase 1: 데이터 백업 (seller, scheduler 테이블)
#   Phase 2: V20 seller 시드 데이터 마이그레이션 자동 생성
#   Phase 3: 모든 테이블 드랍 (flyway_schema_history 포함)
#   Phase 4: 안내 — V20 커밋 → 앱 배포 (Flyway V1-V20 자동 적용)
#   Phase 5: scheduler 데이터 복원 (배포 후 수동 실행)
#
# 사전 조건:
#   1. aws-port-forward.sh로 포트 포워딩 활성화 (localhost:13307)
#   2. mysql 클라이언트 설치
#
# 사용법:
#   ./db-migration-reset.sh
#   ./db-migration-reset.sh --password <pw>
#   ./db-migration-reset.sh --restore-only --backup-dir <path>
# ===============================================

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

# 기본값
DB_HOST=${DB_HOST:-127.0.0.1}
DB_PORT=${DB_PORT:-13307}
DB_USER=${DB_USER:-admin}
DB_NAME=${DB_NAME:-crawler}
RESTORE_ONLY=false
RESTORE_BACKUP_DIR=""

# 인자 파싱
while [[ $# -gt 0 ]]; do
    case $1 in
        --host) DB_HOST="$2"; shift 2 ;;
        --port) DB_PORT="$2"; shift 2 ;;
        --user) DB_USER="$2"; shift 2 ;;
        --db)   DB_NAME="$2"; shift 2 ;;
        --password) DB_PASSWORD="$2"; shift 2 ;;
        --restore-only) RESTORE_ONLY=true; shift ;;
        --backup-dir) RESTORE_BACKUP_DIR="$2"; shift 2 ;;
        *) echo -e "${RED}알 수 없는 옵션: $1${NC}"; exit 1 ;;
    esac
done

# 프로젝트 루트 경로
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
MIGRATION_DIR="$PROJECT_ROOT/adapter-out/persistence-mysql/src/main/resources/db/migration"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Database Migration Reset${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "  Host: ${CYAN}$DB_HOST:$DB_PORT${NC}"
echo -e "  User: ${CYAN}$DB_USER${NC}"
echo -e "  DB:   ${CYAN}$DB_NAME${NC}"
echo ""

# mysql 클라이언트 확인
if ! command -v mysql &> /dev/null; then
    echo -e "${RED}mysql 클라이언트가 설치되어 있지 않습니다.${NC}"
    echo "설치: brew install mysql-client"
    exit 1
fi

# 비밀번호 입력
if [ -z "$DB_PASSWORD" ]; then
    echo -n "DB 비밀번호를 입력하세요: "
    read -s DB_PASSWORD
    echo ""
fi

# MySQL 접속 함수
mysql_cmd() {
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" --default-character-set=utf8mb4 "$@"
}

# 접속 테스트
echo "DB 접속 테스트 중..."
if ! mysql_cmd -e "SELECT 1" > /dev/null 2>&1; then
    echo -e "${RED}DB 접속 실패. 포트 포워딩 및 자격 증명을 확인하세요.${NC}"
    exit 1
fi
echo -e "${GREEN}DB 접속 성공${NC}"
echo ""

# ============================================
# restore-only 모드: Phase 5만 실행
# ============================================
if [ "$RESTORE_ONLY" = true ]; then
    if [ -z "$RESTORE_BACKUP_DIR" ]; then
        echo -e "${RED}--backup-dir 옵션이 필요합니다.${NC}"
        echo "사용법: $0 --restore-only --backup-dir <path>"
        exit 1
    fi

    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Scheduler 데이터 복원 (restore-only)${NC}"
    echo -e "${GREEN}========================================${NC}"

    for TABLE in crawl_scheduler crawl_scheduler_history crawl_scheduler_outbox; do
        BACKUP_FILE="$RESTORE_BACKUP_DIR/${TABLE}_backup.sql"
        if [ -s "$BACKUP_FILE" ]; then
            echo -n "  $TABLE 복원 중... "
            mysql_cmd < "$BACKUP_FILE" 2>/dev/null
            COUNT=$(mysql_cmd -N -e "SELECT COUNT(*) FROM $TABLE" 2>/dev/null)
            echo -e "${GREEN}${COUNT}건${NC}"

            # AUTO_INCREMENT 복원
            MAX_ID=$(mysql_cmd -N -e "SELECT IFNULL(MAX(id), 0) FROM $TABLE" 2>/dev/null)
            if [ "$MAX_ID" -gt 0 ]; then
                mysql_cmd -e "ALTER TABLE $TABLE AUTO_INCREMENT = $((MAX_ID + 1))" 2>/dev/null
            fi
        else
            echo -e "  $TABLE: ${YELLOW}백업 파일 없음 (스킵)${NC}"
        fi
    done

    echo ""
    echo -e "${GREEN}복원 완료!${NC}"
    exit 0
fi

# ============================================
# Phase 1: 데이터 백업
# ============================================
BACKUP_DIR="$SCRIPT_DIR/backups/$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Phase 1: 데이터 백업${NC}"
echo -e "${GREEN}========================================${NC}"

SELLER_COUNT=$(mysql_cmd -N -e "SELECT COUNT(*) FROM seller" 2>/dev/null || echo "0")
SCHEDULER_COUNT=$(mysql_cmd -N -e "SELECT COUNT(*) FROM crawl_scheduler" 2>/dev/null || echo "0")
HISTORY_COUNT=$(mysql_cmd -N -e "SELECT COUNT(*) FROM crawl_scheduler_history" 2>/dev/null || echo "0")
OUTBOX_COUNT=$(mysql_cmd -N -e "SELECT COUNT(*) FROM crawl_scheduler_outbox" 2>/dev/null || echo "0")

echo -e "  seller:                    ${CYAN}${SELLER_COUNT}건${NC}"
echo -e "  crawl_scheduler:           ${CYAN}${SCHEDULER_COUNT}건${NC}"
echo -e "  crawl_scheduler_history:   ${CYAN}${HISTORY_COUNT}건${NC}"
echo -e "  crawl_scheduler_outbox:    ${CYAN}${OUTBOX_COUNT}건${NC}"
echo ""

# mysqldump 우선, 없으면 SELECT 기반 백업
if command -v mysqldump &> /dev/null; then
    echo "mysqldump로 백업 중..."
    for TABLE in seller crawl_scheduler crawl_scheduler_history crawl_scheduler_outbox; do
        mysqldump -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" \
            --no-create-info --complete-insert --skip-triggers --skip-lock-tables \
            --default-character-set=utf8mb4 \
            "$DB_NAME" "$TABLE" > "$BACKUP_DIR/${TABLE}_backup.sql" 2>/dev/null
    done
else
    echo "mysqldump 미설치. SELECT 기반 백업 중..."

    # seller
    mysql_cmd -N -e "
        SELECT CONCAT(
            'INSERT INTO seller (id, must_it_seller_name, seller_name, status, product_count, created_at, updated_at) VALUES (',
            id, ', ', QUOTE(must_it_seller_name), ', ', QUOTE(seller_name), ', ',
            QUOTE(status), ', ', product_count, ', ', QUOTE(created_at), ', ', QUOTE(updated_at), ');'
        ) FROM seller ORDER BY id
    " > "$BACKUP_DIR/seller_backup.sql" 2>/dev/null

    # crawl_scheduler
    mysql_cmd -N -e "
        SELECT CONCAT(
            'INSERT INTO crawl_scheduler (id, seller_id, scheduler_name, cron_expression, status, created_at, updated_at) VALUES (',
            id, ', ', seller_id, ', ', QUOTE(scheduler_name), ', ', QUOTE(cron_expression), ', ',
            QUOTE(status), ', ', QUOTE(created_at), ', ', QUOTE(updated_at), ');'
        ) FROM crawl_scheduler ORDER BY id
    " > "$BACKUP_DIR/crawl_scheduler_backup.sql" 2>/dev/null

    # crawl_scheduler_history
    mysql_cmd -N -e "
        SELECT CONCAT(
            'INSERT INTO crawl_scheduler_history (id, crawl_scheduler_id, seller_id, scheduler_name, cron_expression, status, created_at) VALUES (',
            id, ', ', crawl_scheduler_id, ', ', seller_id, ', ', QUOTE(scheduler_name), ', ',
            QUOTE(cron_expression), ', ', QUOTE(status), ', ', QUOTE(created_at), ');'
        ) FROM crawl_scheduler_history ORDER BY id
    " > "$BACKUP_DIR/crawl_scheduler_history_backup.sql" 2>/dev/null

    # crawl_scheduler_outbox
    mysql_cmd -N -e "
        SELECT CONCAT(
            'INSERT INTO crawl_scheduler_outbox (id, history_id, scheduler_id, seller_id, scheduler_name, cron_expression, scheduler_status, status, error_message, version, created_at, processed_at) VALUES (',
            id, ', ', history_id, ', ', scheduler_id, ', ', seller_id, ', ',
            QUOTE(scheduler_name), ', ', QUOTE(cron_expression), ', ', QUOTE(scheduler_status), ', ',
            QUOTE(status), ', ', IFNULL(QUOTE(error_message), 'NULL'), ', ', version, ', ',
            QUOTE(created_at), ', ', IFNULL(QUOTE(processed_at), 'NULL'), ');'
        ) FROM crawl_scheduler_outbox ORDER BY id
    " > "$BACKUP_DIR/crawl_scheduler_outbox_backup.sql" 2>/dev/null
fi

echo -e "${GREEN}백업 완료: $BACKUP_DIR${NC}"
echo ""

# ============================================
# Phase 2: V20 seller 시드 데이터 마이그레이션 생성
# ============================================
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Phase 2: V20 seller 시드 데이터 생성${NC}"
echo -e "${GREEN}========================================${NC}"

V20_FILE="$MIGRATION_DIR/V20__seed_seller_data.sql"

cat > "$V20_FILE" << 'HEADER'
-- V20__seed_seller_data.sql
-- Seller 시드 데이터
-- 운영 환경에서 추출한 기본 셀러 데이터

HEADER

if [ "$SELLER_COUNT" -gt 0 ]; then
    echo "-- Seller 시드 데이터 (${SELLER_COUNT}건)" >> "$V20_FILE"
    echo "" >> "$V20_FILE"

    mysql_cmd -N -e "
        SELECT CONCAT(
            'INSERT INTO seller (id, must_it_seller_name, seller_name, status, product_count, created_at, updated_at) VALUES (',
            id, ', ', QUOTE(must_it_seller_name), ', ', QUOTE(seller_name), ', ',
            QUOTE(status), ', ', product_count, ', NOW(), NOW());'
        ) FROM seller ORDER BY id
    " >> "$V20_FILE" 2>/dev/null

    echo "" >> "$V20_FILE"

    MAX_SELLER_ID=$(mysql_cmd -N -e "SELECT MAX(id) FROM seller" 2>/dev/null)
    echo "-- AUTO_INCREMENT 복원" >> "$V20_FILE"
    echo "ALTER TABLE seller AUTO_INCREMENT = $((MAX_SELLER_ID + 1));" >> "$V20_FILE"

    echo -e "${GREEN}V20 생성 완료: $V20_FILE${NC}"
else
    echo "-- seller 데이터 없음" >> "$V20_FILE"
    echo -e "${YELLOW}seller 데이터가 없습니다.${NC}"
fi

echo ""

# ============================================
# Phase 3: 테이블 드랍
# ============================================
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Phase 3: 테이블 드랍${NC}"
echo -e "${GREEN}========================================${NC}"

echo "현재 테이블 목록:"
mysql_cmd -N -e "SHOW TABLES" 2>/dev/null | while read table; do
    echo "  - $table"
done
echo ""

echo -e "${YELLOW}경고: crawler 스키마의 모든 테이블이 삭제됩니다!${NC}"
echo -e "${YELLOW}(데이터는 이미 백업되었습니다: $BACKUP_DIR)${NC}"
echo ""
echo -n "계속하시겠습니까? (yes/no): "
read CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    echo -e "${YELLOW}작업을 취소합니다.${NC}"
    echo -e "백업: $BACKUP_DIR"
    echo -e "V20:  $V20_FILE"
    exit 0
fi

echo ""
echo "모든 테이블 드랍 중..."

mysql_cmd -e "
SET FOREIGN_KEY_CHECKS = 0;
SET @tables = NULL;
SELECT GROUP_CONCAT('\`', table_name, '\`') INTO @tables
  FROM information_schema.tables
  WHERE table_schema = '$DB_NAME';
SET @tables = IFNULL(@tables, 'dummy');
SET @drop_stmt = CONCAT('DROP TABLE IF EXISTS ', @tables);
PREPARE stmt FROM @drop_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET FOREIGN_KEY_CHECKS = 1;
" 2>/dev/null

echo -e "${GREEN}모든 테이블 드랍 완료${NC}"
echo ""

# ============================================
# Phase 4: 다음 단계 안내
# ============================================
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Phase 4: 다음 단계${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "DB가 비어있습니다. Flyway가 V1-V20을 자동 적용합니다."
echo -e "(baseline-on-migrate: true 설정으로 빈 DB에서 전체 마이그레이션 실행)"
echo ""
echo -e "${CYAN}[ Step 1 ] V20 커밋 & 푸시${NC}"
echo -e "  git add $V20_FILE"
echo -e "  git commit -m 'feat(migration): V20 seller 시드 데이터 추가'"
echo -e "  git push"
echo ""
echo -e "${CYAN}[ Step 2 ] 앱 배포${NC}"
echo -e "  → Flyway가 V1-V20 자동 적용 (seller 시드 데이터 포함)"
echo ""
echo -e "${CYAN}[ Step 3 ] Scheduler 데이터 복원${NC}"
echo -e "  포트 포워딩 후 다음 명령어 실행:"
echo ""
echo -e "  ${YELLOW}$0 --restore-only --backup-dir $BACKUP_DIR --password <pw>${NC}"
echo ""
echo -e "  또는 개별 실행:"
echo -e "  mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p $DB_NAME < $BACKUP_DIR/crawl_scheduler_backup.sql"
echo -e "  mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p $DB_NAME < $BACKUP_DIR/crawl_scheduler_history_backup.sql"
echo -e "  mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p $DB_NAME < $BACKUP_DIR/crawl_scheduler_outbox_backup.sql"
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Phase 1-3 완료!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "  백업 디렉토리: ${CYAN}$BACKUP_DIR${NC}"
echo -e "  V20 파일:      ${CYAN}$V20_FILE${NC}"
