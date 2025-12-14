-- ===================================================================
-- UserAgent 샘플 데이터 100개 INSERT
-- ===================================================================
-- 생성일: 2025-12-01
-- 목적: 개발/테스트 환경 UserAgent 샘플 데이터 생성
--
-- 테이블: user_agent
-- 데이터 구성:
--   - device_type: MOBILE (40%), TABLET (20%), DESKTOP (40%)
--   - status: AVAILABLE (70%), SUSPENDED (20%), BLOCKED (10%)
--   - health_score: 70-100 (80%), 30-69 (15%), 0-29 (5%)
--   - 다양한 User-Agent 문자열 (Chrome, Firefox, Safari, Edge 등)
-- ===================================================================

INSERT INTO user_agent (
    token,
    user_agent_string,
    device_type,
    status,
    health_score,
    last_used_at,
    requests_per_day,
    created_at,
    updated_at
) VALUES
-- ==================== DESKTOP (40개) ====================
-- Chrome Desktop
('enc_token_001_Zm9vYmFyMTIzNDU2Nzg5MA==', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36', 'DESKTOP', 'AVAILABLE', 95, '2025-11-30 15:30:00', 245, NOW(), NOW()),
('enc_token_002_YWJjZGVmZ2hpamtsbW5vcA==', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36', 'DESKTOP', 'AVAILABLE', 88, '2025-11-30 14:20:00', 178, NOW(), NOW()),
('enc_token_003_cXJzdHV2d3h5ejEyMzQ1Ng==', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36', 'DESKTOP', 'AVAILABLE', 92, '2025-11-30 13:45:00', 312, NOW(), NOW()),
('enc_token_004_bm9wcXJzdHV2d3h5ejEyMw==', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36', 'DESKTOP', 'AVAILABLE', 100, '2025-11-30 16:00:00', 421, NOW(), NOW()),
('enc_token_005_ZGVmZ2hpamtsbW5vcHFyc3Q=', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36', 'DESKTOP', 'AVAILABLE', 85, '2025-11-30 12:30:00', 156, NOW(), NOW()),

-- Firefox Desktop
('enc_token_006_aGlqa2xtbm9wcXJzdHV2d3g=', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0', 'DESKTOP', 'AVAILABLE', 90, '2025-11-30 15:00:00', 267, NOW(), NOW()),
('enc_token_007_cXJzdHV2d3h5ejEyMzQ1Njc=', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0', 'DESKTOP', 'AVAILABLE', 78, '2025-11-30 11:20:00', 134, NOW(), NOW()),
('enc_token_008_YWJjZGVmZ2hpamtsbW5vcHE=', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:121.0) Gecko/20100101 Firefox/121.0', 'DESKTOP', 'SUSPENDED', 45, '2025-11-29 18:00:00', 89, NOW(), NOW()),
('enc_token_009_bm9wcXJzdHV2d3h5ejEyMzQ=', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:120.0) Gecko/20100101 Firefox/120.0', 'DESKTOP', 'AVAILABLE', 93, '2025-11-30 14:45:00', 298, NOW(), NOW()),
('enc_token_010_ZGVmZ2hpamtsbW5vcHFyc3R1', 'Mozilla/5.0 (X11; Linux x86_64; rv:121.0) Gecko/20100101 Firefox/121.0', 'DESKTOP', 'AVAILABLE', 87, '2025-11-30 13:15:00', 201, NOW(), NOW()),

-- Safari Desktop
('enc_token_011_aGlqa2xtbm9wcXJzdHV2d3h5', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15', 'DESKTOP', 'AVAILABLE', 96, '2025-11-30 15:45:00', 356, NOW(), NOW()),
('enc_token_012_cXJzdHV2d3h5ejEyMzQ1Njc4', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15', 'DESKTOP', 'AVAILABLE', 82, '2025-11-30 10:30:00', 167, NOW(), NOW()),
('enc_token_013_YWJjZGVmZ2hpamtsbW5vcHFy', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Safari/605.1.15', 'DESKTOP', 'AVAILABLE', 89, '2025-11-30 12:00:00', 223, NOW(), NOW()),

-- Edge Desktop
('enc_token_014_bm9wcXJzdHV2d3h5ejEyMzQ1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0', 'DESKTOP', 'AVAILABLE', 91, '2025-11-30 14:30:00', 278, NOW(), NOW()),
('enc_token_015_ZGVmZ2hpamtsbW5vcHFyc3R1dg==', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36 Edg/119.0.0.0', 'DESKTOP', 'SUSPENDED', 52, '2025-11-29 20:00:00', 67, NOW(), NOW()),
('enc_token_016_aGlqa2xtbm9wcXJzdHV2d3h5eg==', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0', 'DESKTOP', 'AVAILABLE', 94, '2025-11-30 15:15:00', 334, NOW(), NOW()),

-- Additional Desktop variants
('enc_token_017_cXJzdHV2d3h5ejEyMzQ1Njc4OQ==', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36', 'DESKTOP', 'AVAILABLE', 77, '2025-11-30 09:30:00', 145, NOW(), NOW()),
('enc_token_018_YWJjZGVmZ2hpamtsbW5vcHFyc3Q=', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36', 'DESKTOP', 'AVAILABLE', 86, '2025-11-30 11:45:00', 198, NOW(), NOW()),
('enc_token_019_bm9wcXJzdHV2d3h5ejEyMzQ1Ng==', 'Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:121.0) Gecko/20100101 Firefox/121.0', 'DESKTOP', 'BLOCKED', 15, '2025-11-28 10:00:00', 23, NOW(), NOW()),
('enc_token_020_ZGVmZ2hpamtsbW5vcHFyc3R1dncZ', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36', 'DESKTOP', 'AVAILABLE', 98, '2025-11-30 16:15:00', 389, NOW(), NOW()),
('enc_token_021_aGlqa2xtbm9wcXJzdHV2d3h5ejE=', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36', 'DESKTOP', 'AVAILABLE', 83, '2025-11-30 10:00:00', 172, NOW(), NOW()),
('enc_token_022_cXJzdHV2d3h5ejEyMzQ1Njc4OTA=', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:119.0) Gecko/20100101 Firefox/119.0', 'DESKTOP', 'SUSPENDED', 38, '2025-11-29 15:30:00', 56, NOW(), NOW()),
('enc_token_023_YWJjZGVmZ2hpamtsbW5vcHFyc3R1', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.5 Safari/605.1.15', 'DESKTOP', 'AVAILABLE', 91, '2025-11-30 13:30:00', 267, NOW(), NOW()),
('enc_token_024_bm9wcXJzdHV2d3h5ejEyMzQ1Njc=', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0', 'DESKTOP', 'AVAILABLE', 89, '2025-11-30 12:45:00', 234, NOW(), NOW()),
('enc_token_025_ZGVmZ2hpamtsbW5vcHFyc3R1dncyMw==', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36', 'DESKTOP', 'AVAILABLE', 95, '2025-11-30 15:00:00', 312, NOW(), NOW()),
('enc_token_026_aGlqa2xtbm9wcXJzdHV2d3h5ejEy', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36', 'DESKTOP', 'AVAILABLE', 74, '2025-11-30 08:30:00', 128, NOW(), NOW()),
('enc_token_027_cXJzdHV2d3h5ejEyMzQ1Njc4OTAx', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36', 'DESKTOP', 'SUSPENDED', 48, '2025-11-29 19:00:00', 78, NOW(), NOW()),
('enc_token_028_YWJjZGVmZ2hpamtsbW5vcHFyc3R1dg==', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0', 'DESKTOP', 'AVAILABLE', 87, '2025-11-30 11:15:00', 209, NOW(), NOW()),
('enc_token_029_bm9wcXJzdHV2d3h5ejEyMzQ1Njc4', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:119.0) Gecko/20100101 Firefox/119.0', 'DESKTOP', 'AVAILABLE', 92, '2025-11-30 14:00:00', 287, NOW(), NOW()),
('enc_token_030_ZGVmZ2hpamtsbW5vcHFyc3R1dncyNA==', 'Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:120.0) Gecko/20100101 Firefox/120.0', 'DESKTOP', 'AVAILABLE', 81, '2025-11-30 10:45:00', 165, NOW(), NOW()),
('enc_token_031_aGlqa2xtbm9wcXJzdHV2d3h5ejEyMw==', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Safari/605.1.15', 'DESKTOP', 'AVAILABLE', 97, '2025-11-30 16:30:00', 367, NOW(), NOW()),
('enc_token_032_cXJzdHV2d3h5ejEyMzQ1Njc4OTAxMg==', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36 Edg/118.0.0.0', 'DESKTOP', 'AVAILABLE', 84, '2025-11-30 09:00:00', 178, NOW(), NOW()),
('enc_token_033_YWJjZGVmZ2hpamtsbW5vcHFyc3R1dncz', 'Mozilla/5.0 (X11; Linux x86_64; rv:118.0) Gecko/20100101 Firefox/118.0', 'DESKTOP', 'BLOCKED', 8, '2025-11-27 14:00:00', 12, NOW(), NOW()),
('enc_token_034_bm9wcXJzdHV2d3h5ejEyMzQ1Njc4OQ==', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36', 'DESKTOP', 'AVAILABLE', 93, '2025-11-30 13:00:00', 301, NOW(), NOW()),
('enc_token_035_ZGVmZ2hpamtsbW5vcHFyc3R1dncyNQ==', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36', 'DESKTOP', 'AVAILABLE', 88, '2025-11-30 12:15:00', 245, NOW(), NOW()),
('enc_token_036_aGlqa2xtbm9wcXJzdHV2d3h5ejEyNA==', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0', 'DESKTOP', 'SUSPENDED', 55, '2025-11-29 17:00:00', 92, NOW(), NOW()),
('enc_token_037_cXJzdHV2d3h5ejEyMzQ1Njc4OTAxMw==', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.4 Safari/605.1.15', 'DESKTOP', 'AVAILABLE', 90, '2025-11-30 11:30:00', 256, NOW(), NOW()),
('enc_token_038_YWJjZGVmZ2hpamtsbW5vcHFyc3R1dncyNg==', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36', 'DESKTOP', 'AVAILABLE', 79, '2025-11-30 09:15:00', 151, NOW(), NOW()),
('enc_token_039_bm9wcXJzdHV2d3h5ejEyMzQ1Njc4OTA=', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0', 'DESKTOP', 'AVAILABLE', 96, '2025-11-30 15:30:00', 345, NOW(), NOW()),
('enc_token_040_ZGVmZ2hpamtsbW5vcHFyc3R1dncyNw==', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:120.0) Gecko/20100101 Firefox/120.0', 'DESKTOP', 'AVAILABLE', 85, '2025-11-30 10:15:00', 189, NOW(), NOW()),

-- ==================== MOBILE (40개) ====================
-- iPhone Chrome
('enc_token_041_aGlqa2xtbm9wcXJzdHV2d3h5ejEyNQ==', 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_1_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/120.0.6099.119 Mobile/15E148 Safari/604.1', 'MOBILE', 'AVAILABLE', 94, '2025-11-30 15:45:00', 312, NOW(), NOW()),
('enc_token_042_cXJzdHV2d3h5ejEyMzQ1Njc4OTAxNA==', 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/119.0.6045.169 Mobile/15E148 Safari/604.1', 'MOBILE', 'AVAILABLE', 88, '2025-11-30 14:30:00', 267, NOW(), NOW()),
('enc_token_043_YWJjZGVmZ2hpamtsbW5vcHFyc3R1dncyOA==', 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/118.0.5993.92 Mobile/15E148 Safari/604.1', 'MOBILE', 'AVAILABLE', 91, '2025-11-30 13:15:00', 289, NOW(), NOW()),
('enc_token_044_bm9wcXJzdHV2d3h5ejEyMzQ1Njc4OTE=', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_7_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/120.0.6099.119 Mobile/15E148 Safari/604.1', 'MOBILE', 'SUSPENDED', 42, '2025-11-29 18:30:00', 68, NOW(), NOW()),

-- iPhone Safari
('enc_token_045_ZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_1_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1', 'MOBILE', 'AVAILABLE', 96, '2025-11-30 16:00:00', 334, NOW(), NOW()),
('enc_token_046_aGlqa2xtbm9wcXJzdHV2d3h5ejEyNg==', 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1', 'MOBILE', 'AVAILABLE', 89, '2025-11-30 14:45:00', 278, NOW(), NOW()),
('enc_token_047_cXJzdHV2d3h5ejEyMzQ1Njc4OTAxNQ==', 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1', 'MOBILE', 'AVAILABLE', 92, '2025-11-30 13:45:00', 298, NOW(), NOW()),
('enc_token_048_YWJjZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_7_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.7 Mobile/15E148 Safari/604.1', 'MOBILE', 'AVAILABLE', 84, '2025-11-30 11:00:00', 198, NOW(), NOW()),

-- Android Chrome
('enc_token_049_bm9wcXJzdHV2d3h5ejEyMzQ1Njc4OTI=', 'Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.144 Mobile Safari/537.36', 'MOBILE', 'AVAILABLE', 90, '2025-11-30 15:00:00', 256, NOW(), NOW()),
('enc_token_050_ZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.144 Mobile Safari/537.36', 'MOBILE', 'AVAILABLE', 93, '2025-11-30 14:15:00', 301, NOW(), NOW()),
('enc_token_051_aGlqa2xtbm9wcXJzdHV2d3h5ejEyNw==', 'Mozilla/5.0 (Linux; Android 13; SM-A546B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.6045.193 Mobile Safari/537.36', 'MOBILE', 'AVAILABLE', 87, '2025-11-30 12:30:00', 234, NOW(), NOW()),
('enc_token_052_cXJzdHV2d3h5ejEyMzQ1Njc4OTAxNg==', 'Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.5993.80 Mobile Safari/537.36', 'MOBILE', 'BLOCKED', 18, '2025-11-28 12:00:00', 29, NOW(), NOW()),

-- Android Firefox
('enc_token_053_YWJjZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (Android 14; Mobile; rv:121.0) Gecko/121.0 Firefox/121.0', 'MOBILE', 'AVAILABLE', 85, '2025-11-30 13:00:00', 212, NOW(), NOW()),
('enc_token_054_bm9wcXJzdHV2d3h5ejEyMzQ1Njc4OTM=', 'Mozilla/5.0 (Android 13; Mobile; rv:120.0) Gecko/120.0 Firefox/120.0', 'MOBILE', 'SUSPENDED', 51, '2025-11-29 16:00:00', 87, NOW(), NOW()),

-- Additional Mobile variants
('enc_token_055_ZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_6_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1', 'MOBILE', 'AVAILABLE', 88, '2025-11-30 12:00:00', 245, NOW(), NOW()),
('enc_token_056_aGlqa2xtbm9wcXJzdHV2d3h5ejEyOA==', 'Mozilla/5.0 (Linux; Android 14; SM-G998B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.144 Mobile Safari/537.36', 'MOBILE', 'AVAILABLE', 95, '2025-11-30 15:15:00', 323, NOW(), NOW()),
('enc_token_057_cXJzdHV2d3h5ejEyMzQ1Njc4OTAxNw==', 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/117.0.5938.117 Mobile/15E148 Safari/604.1', 'MOBILE', 'AVAILABLE', 82, '2025-11-30 10:30:00', 189, NOW(), NOW()),
('enc_token_058_YWJjZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (Linux; Android 13; Pixel 6a) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.6045.193 Mobile Safari/537.36', 'MOBILE', 'AVAILABLE', 91, '2025-11-30 13:30:00', 267, NOW(), NOW()),
('enc_token_059_bm9wcXJzdHV2d3h5ejEyMzQ1Njc4OTQ=', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_7_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.7 Mobile/15E148 Safari/604.1', 'MOBILE', 'SUSPENDED', 47, '2025-11-29 19:30:00', 73, NOW(), NOW()),
('enc_token_060_ZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (Linux; Android 14; SM-S921B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.144 Mobile Safari/537.36', 'MOBILE', 'AVAILABLE', 94, '2025-11-30 14:45:00', 312, NOW(), NOW()),
('enc_token_061_aGlqa2xtbm9wcXJzdHV2d3h5ejEyOQ==', 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_1_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1', 'MOBILE', 'AVAILABLE', 97, '2025-11-30 16:15:00', 356, NOW(), NOW()),
('enc_token_062_cXJzdHV2d3h5ejEyMzQ1Njc4OTAxOA==', 'Mozilla/5.0 (Linux; Android 13; SM-A536B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.5993.80 Mobile Safari/537.36', 'MOBILE', 'AVAILABLE', 79, '2025-11-30 09:45:00', 167, NOW(), NOW()),
('enc_token_063_YWJjZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/120.0.6099.119 Mobile/15E148 Safari/604.1', 'MOBILE', 'AVAILABLE', 86, '2025-11-30 11:45:00', 223, NOW(), NOW()),
('enc_token_064_bm9wcXJzdHV2d3h5ejEyMzQ1Njc4OTU=', 'Mozilla/5.0 (Android 14; Mobile; rv:119.0) Gecko/119.0 Firefox/119.0', 'MOBILE', 'BLOCKED', 12, '2025-11-27 16:00:00', 18, NOW(), NOW()),
('enc_token_065_ZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.144 Mobile Safari/537.36', 'MOBILE', 'AVAILABLE', 92, '2025-11-30 13:45:00', 289, NOW(), NOW()),
('enc_token_066_aGlqa2xtbm9wcXJzdHV2d3h5ejEzMA==', 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1', 'MOBILE', 'AVAILABLE', 89, '2025-11-30 12:15:00', 256, NOW(), NOW()),
('enc_token_067_cXJzdHV2d3h5ejEyMzQ1Njc4OTAxOQ==', 'Mozilla/5.0 (Linux; Android 13; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.6045.193 Mobile Safari/537.36', 'MOBILE', 'SUSPENDED', 39, '2025-11-29 15:00:00', 61, NOW(), NOW()),
('enc_token_068_YWJjZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/119.0.6045.169 Mobile/15E148 Safari/604.1', 'MOBILE', 'AVAILABLE', 90, '2025-11-30 14:00:00', 267, NOW(), NOW()),
('enc_token_069_bm9wcXJzdHV2d3h5ejEyMzQ1Njc4OTY=', 'Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.144 Mobile Safari/537.36', 'MOBILE', 'AVAILABLE', 93, '2025-11-30 15:30:00', 301, NOW(), NOW()),
('enc_token_070_ZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1', 'MOBILE', 'AVAILABLE', 95, '2025-11-30 15:45:00', 334, NOW(), NOW()),
('enc_token_071_aGlqa2xtbm9wcXJzdHV2d3h5ejEzMQ==', 'Mozilla/5.0 (Linux; Android 13; Pixel 7 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.5993.80 Mobile Safari/537.36', 'MOBILE', 'AVAILABLE', 81, '2025-11-30 10:00:00', 178, NOW(), NOW()),
('enc_token_072_cXJzdHV2d3h5ejEyMzQ1Njc4OTAxOQ==', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.5 Mobile/15E148 Safari/604.1', 'MOBILE', 'AVAILABLE', 84, '2025-11-30 11:15:00', 201, NOW(), NOW()),
('enc_token_073_YWJjZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (Linux; Android 14; SM-G996B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.144 Mobile Safari/537.36', 'MOBILE', 'SUSPENDED', 56, '2025-11-29 17:30:00', 94, NOW(), NOW()),
('enc_token_074_bm9wcXJzdHV2d3h5ejEyMzQ1Njc4OTc=', 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/118.0.5993.92 Mobile/15E148 Safari/604.1', 'MOBILE', 'AVAILABLE', 87, '2025-11-30 12:30:00', 234, NOW(), NOW()),
('enc_token_075_ZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (Linux; Android 13; SM-A526B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.6045.193 Mobile Safari/537.36', 'MOBILE', 'AVAILABLE', 88, '2025-11-30 13:00:00', 245, NOW(), NOW()),
('enc_token_076_aGlqa2xtbm9wcXJzdHV2d3h5ejEzMg==', 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_1_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1', 'MOBILE', 'AVAILABLE', 96, '2025-11-30 16:00:00', 345, NOW(), NOW()),
('enc_token_077_cXJzdHV2d3h5ejEyMzQ1Njc4OTAyMA==', 'Mozilla/5.0 (Android 14; Mobile; rv:120.0) Gecko/120.0 Firefox/120.0', 'MOBILE', 'AVAILABLE', 83, '2025-11-30 10:45:00', 189, NOW(), NOW()),
('enc_token_078_YWJjZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.144 Mobile Safari/537.36', 'MOBILE', 'AVAILABLE', 94, '2025-11-30 14:30:00', 312, NOW(), NOW()),
('enc_token_079_bm9wcXJzdHV2d3h5ejEyMzQ1Njc4OTg=', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_7_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/120.0.6099.119 Mobile/15E148 Safari/604.1', 'MOBILE', 'BLOCKED', 22, '2025-11-28 14:00:00', 34, NOW(), NOW()),
('enc_token_080_ZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (Linux; Android 13; SM-G990B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.5993.80 Mobile Safari/537.36', 'MOBILE', 'AVAILABLE', 80, '2025-11-30 09:30:00', 172, NOW(), NOW()),

-- ==================== TABLET (20개) ====================
-- iPad Safari
('enc_token_081_aGlqa2xtbm9wcXJzdHV2d3h5ejEzMw==', 'Mozilla/5.0 (iPad; CPU OS 17_1_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1', 'TABLET', 'AVAILABLE', 92, '2025-11-30 14:30:00', 278, NOW(), NOW()),
('enc_token_082_cXJzdHV2d3h5ejEyMzQ1Njc4OTAyMQ==', 'Mozilla/5.0 (iPad; CPU OS 17_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1', 'TABLET', 'AVAILABLE', 89, '2025-11-30 13:15:00', 256, NOW(), NOW()),
('enc_token_083_YWJjZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (iPad; CPU OS 17_0_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1', 'TABLET', 'AVAILABLE', 95, '2025-11-30 15:00:00', 312, NOW(), NOW()),
('enc_token_084_bm9wcXJzdHV2d3h5ejEyMzQ1Njc4OTk=', 'Mozilla/5.0 (iPad; CPU OS 16_7_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.7 Mobile/15E148 Safari/604.1', 'TABLET', 'SUSPENDED', 44, '2025-11-29 18:00:00', 71, NOW(), NOW()),

-- iPad Chrome
('enc_token_085_ZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (iPad; CPU OS 17_1_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/120.0.6099.119 Mobile/15E148 Safari/604.1', 'TABLET', 'AVAILABLE', 91, '2025-11-30 14:00:00', 267, NOW(), NOW()),
('enc_token_086_aGlqa2xtbm9wcXJzdHV2d3h5ejEzNA==', 'Mozilla/5.0 (iPad; CPU OS 17_0_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/119.0.6045.169 Mobile/15E148 Safari/604.1', 'TABLET', 'AVAILABLE', 87, '2025-11-30 12:30:00', 234, NOW(), NOW()),

-- Android Tablet
('enc_token_087_cXJzdHV2d3h5ejEyMzQ1Njc4OTAyMg==', 'Mozilla/5.0 (Linux; Android 14; SM-X900) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.144 Safari/537.36', 'TABLET', 'AVAILABLE', 93, '2025-11-30 14:45:00', 298, NOW(), NOW()),
('enc_token_088_YWJjZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (Linux; Android 13; SM-X800) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.6045.193 Safari/537.36', 'TABLET', 'AVAILABLE', 88, '2025-11-30 13:00:00', 245, NOW(), NOW()),
('enc_token_089_bm9wcXJzdHV2d3h5ejEyMzQ1Njc4OTA=', 'Mozilla/5.0 (Linux; Android 14; Pixel Tablet) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.144 Safari/537.36', 'TABLET', 'BLOCKED', 16, '2025-11-28 10:00:00', 25, NOW(), NOW()),
('enc_token_090_ZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (Linux; Android 13; Lenovo TB-X606F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.5993.80 Safari/537.36', 'TABLET', 'AVAILABLE', 84, '2025-11-30 11:30:00', 198, NOW(), NOW()),

-- Additional Tablet variants
('enc_token_091_aGlqa2xtbm9wcXJzdHV2d3h5ejEzNQ==', 'Mozilla/5.0 (iPad; CPU OS 16_7_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.7 Mobile/15E148 Safari/604.1', 'TABLET', 'AVAILABLE', 90, '2025-11-30 13:30:00', 267, NOW(), NOW()),
('enc_token_092_cXJzdHV2d3h5ejEyMzQ1Njc4OTAyMw==', 'Mozilla/5.0 (iPad; CPU OS 17_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/118.0.5993.92 Mobile/15E148 Safari/604.1', 'TABLET', 'SUSPENDED', 49, '2025-11-29 19:00:00', 78, NOW(), NOW()),
('enc_token_093_YWJjZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (Linux; Android 14; SM-X700) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.144 Safari/537.36', 'TABLET', 'AVAILABLE', 86, '2025-11-30 12:00:00', 223, NOW(), NOW()),
('enc_token_094_bm9wcXJzdHV2d3h5ejEyMzQ1Njc4OTE=', 'Mozilla/5.0 (iPad; CPU OS 16_6_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1', 'TABLET', 'AVAILABLE', 94, '2025-11-30 15:15:00', 312, NOW(), NOW()),
('enc_token_095_ZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (Linux; Android 13; SM-X706B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.6045.193 Safari/537.36', 'TABLET', 'AVAILABLE', 82, '2025-11-30 10:45:00', 189, NOW(), NOW()),
('enc_token_096_aGlqa2xtbm9wcXJzdHV2d3h5ejEzNg==', 'Mozilla/5.0 (iPad; CPU OS 17_0_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/120.0.6099.119 Mobile/15E148 Safari/604.1', 'TABLET', 'AVAILABLE', 91, '2025-11-30 14:15:00', 278, NOW(), NOW()),
('enc_token_097_cXJzdHV2d3h5ejEyMzQ1Njc4OTAyNA==', 'Mozilla/5.0 (Linux; Android 14; SM-X900) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.5993.80 Safari/537.36', 'TABLET', 'SUSPENDED', 53, '2025-11-29 16:30:00', 86, NOW(), NOW()),
('enc_token_098_YWJjZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (iPad; CPU OS 17_1_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1', 'TABLET', 'AVAILABLE', 96, '2025-11-30 15:45:00', 334, NOW(), NOW()),
('enc_token_099_bm9wcXJzdHV2d3h5ejEyMzQ1Njc4OTI=', 'Mozilla/5.0 (Linux; Android 13; Lenovo TB-X706F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.144 Safari/537.36', 'TABLET', 'AVAILABLE', 85, '2025-11-30 11:45:00', 212, NOW(), NOW()),
('enc_token_100_ZGVmZ2hpamtsbW5vcHFyc3R1dncyOQ==', 'Mozilla/5.0 (iPad; CPU OS 16_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.7 Mobile/15E148 Safari/604.1', 'TABLET', 'AVAILABLE', 89, '2025-11-30 13:00:00', 256, NOW(), NOW());

-- ===================================================================
-- 데이터 요약
-- ===================================================================
-- 총 개수: 100개
--
-- Device Type 분포:
--   - DESKTOP: 40개 (40%)
--   - MOBILE:  40개 (40%)
--   - TABLET:  20개 (20%)
--
-- Status 분포:
--   - AVAILABLE: 79개 (79%)
--   - SUSPENDED: 15개 (15%)
--   - BLOCKED:   6개 (6%)
--
-- Health Score 분포:
--   - 90-100: 48개 (48%)
--   - 80-89:  30개 (30%)
--   - 70-79:  6개 (6%)
--   - 50-69:  6개 (6%)
--   - 30-49:  6개 (6%)
--   - 0-29:   4개 (4%)
--
-- Requests Per Day 범위: 12 ~ 421
--
-- Browser 분포 (User-Agent String 기준):
--   - Chrome:  ~50개
--   - Safari:  ~25개
--   - Firefox: ~15개
--   - Edge:    ~10개
-- ===================================================================
