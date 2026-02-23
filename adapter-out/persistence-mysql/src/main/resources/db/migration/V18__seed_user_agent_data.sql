-- V18__seed_user_agent_data.sql
-- UserAgent Pool 시드 데이터 150건
-- 분포: MOBILE 90건 (60%), DESKTOP 45건 (30%), TABLET 15건 (10%)
-- 모든 UserAgent는 IDLE 상태, health_score 100, token NULL (Lazy Issuance)

-- =====================================================
-- MOBILE - iPhone / iOS / Safari (25건)
-- =====================================================
INSERT INTO user_agent (token, user_agent_string, device_type, device_brand, os_type, os_version, browser_type, browser_version, status, health_score, last_used_at, requests_per_day, cooldown_until, consecutive_rate_limits, created_at, updated_at) VALUES
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 18_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.2 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '18.2', 'SAFARI', '18.2', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 18_1_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.1.1 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '18.1.1', 'SAFARI', '18.1.1', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 18_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.1 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '18.1', 'SAFARI', '18.1', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 18_0_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0.1 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '18.0.1', 'SAFARI', '18.0.1', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '18.0', 'SAFARI', '18.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_7_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.7.2 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.7.2', 'SAFARI', '17.7.2', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_7_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.7.1 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.7.1', 'SAFARI', '17.7.1', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.7 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.7', 'SAFARI', '17.7', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_6_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.6.1 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.6.1', 'SAFARI', '17.6.1', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.6 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.6', 'SAFARI', '17.6', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5.1 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.5.1', 'SAFARI', '17.5.1', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.5', 'SAFARI', '17.5', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4.1 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.4.1', 'SAFARI', '17.4.1', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.4', 'SAFARI', '17.4', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.3.1 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.3.1', 'SAFARI', '17.3.1', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.3 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.3', 'SAFARI', '17.3', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_2_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2.1 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.2.1', 'SAFARI', '17.2.1', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.2', 'SAFARI', '17.2', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_1_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1.2 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.1.2', 'SAFARI', '17.1.2', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.1', 'SAFARI', '17.1', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0.3 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.0.3', 'SAFARI', '17.0.3', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.0', 'SAFARI', '17.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_7_10 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.7.10 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '16.7.10', 'SAFARI', '16.7.10', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_7_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.7.5 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '16.7.5', 'SAFARI', '16.7.5', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '16.6', 'SAFARI', '16.6', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6));

-- =====================================================
-- MOBILE - iPhone / iOS / Chrome (10건)
-- =====================================================
INSERT INTO user_agent (token, user_agent_string, device_type, device_brand, os_type, os_version, browser_type, browser_version, status, health_score, last_used_at, requests_per_day, cooldown_until, consecutive_rate_limits, created_at, updated_at) VALUES
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 18_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/131.0.6778.103 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '18.2', 'CHROME', '131.0.6778.103', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 18_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/131.0.6778.73 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '18.1', 'CHROME', '131.0.6778.73', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/130.0.6723.90 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '18.0', 'CHROME', '130.0.6723.90', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/130.0.6723.37 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.7', 'CHROME', '130.0.6723.37', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/129.0.6668.69 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.6', 'CHROME', '129.0.6668.69', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/128.0.6613.98 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.5', 'CHROME', '128.0.6613.98', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/127.0.6533.77 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.4', 'CHROME', '127.0.6533.77', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/126.0.6478.54 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.3', 'CHROME', '126.0.6478.54', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/125.0.6422.80 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.2', 'CHROME', '125.0.6422.80', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/124.0.6367.88 Mobile/15E148 Safari/604.1', 'MOBILE', 'IPHONE', 'IOS', '17.0', 'CHROME', '124.0.6367.88', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6));

-- =====================================================
-- MOBILE - Samsung / Android / Chrome (15건)
-- =====================================================
INSERT INTO user_agent (token, user_agent_string, device_type, device_brand, os_type, os_version, browser_type, browser_version, status, health_score, last_used_at, requests_per_day, cooldown_until, consecutive_rate_limits, created_at, updated_at) VALUES
(NULL, 'Mozilla/5.0 (Linux; Android 15; SM-S928B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.104 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '15', 'CHROME', '131.0.6778.104', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 15; SM-S926B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.104 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '15', 'CHROME', '131.0.6778.104', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 15; SM-S921B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.73 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '15', 'CHROME', '131.0.6778.73', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '14', 'CHROME', '130.0.6723.102', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; SM-S916B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.58 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '14', 'CHROME', '130.0.6723.58', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; SM-S911B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.100 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '14', 'CHROME', '129.0.6668.100', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; SM-A556B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.104 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '14', 'CHROME', '131.0.6778.104', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; SM-A546B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.90 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '14', 'CHROME', '130.0.6723.90', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; SM-A356B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.69 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '14', 'CHROME', '129.0.6668.69', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 13; SM-S908B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.127 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '13', 'CHROME', '128.0.6613.127', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 13; SM-S906B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.6533.103 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '13', 'CHROME', '127.0.6533.103', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 13; SM-S901B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6478.71 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '13', 'CHROME', '126.0.6478.71', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; SM-F946B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.104 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '14', 'CHROME', '131.0.6778.104', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; SM-F731B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '14', 'CHROME', '130.0.6723.102', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 13; SM-A346B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.6422.113 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '13', 'CHROME', '125.0.6422.113', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6));

-- =====================================================
-- MOBILE - Samsung / Android / Samsung Browser (10건)
-- =====================================================
INSERT INTO user_agent (token, user_agent_string, device_type, device_brand, os_type, os_version, browser_type, browser_version, status, health_score, last_used_at, requests_per_day, cooldown_until, consecutive_rate_limits, created_at, updated_at) VALUES
(NULL, 'Mozilla/5.0 (Linux; Android 15; SM-S928B) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/27.0 Chrome/125.0.6422.165 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '15', 'SAMSUNG_BROWSER', '27.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 15; SM-S926B) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/27.0 Chrome/125.0.6422.165 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '15', 'SAMSUNG_BROWSER', '27.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/26.0 Chrome/122.0.6261.105 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '14', 'SAMSUNG_BROWSER', '26.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; SM-S916B) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/26.0 Chrome/122.0.6261.105 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '14', 'SAMSUNG_BROWSER', '26.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; SM-A556B) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/25.0 Chrome/121.0.6167.178 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '14', 'SAMSUNG_BROWSER', '25.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; SM-F946B) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/27.0 Chrome/125.0.6422.165 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '14', 'SAMSUNG_BROWSER', '27.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 13; SM-S908B) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/25.0 Chrome/121.0.6167.178 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '13', 'SAMSUNG_BROWSER', '25.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 13; SM-S901B) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/24.0 Chrome/117.0.5938.60 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '13', 'SAMSUNG_BROWSER', '24.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; SM-A546B) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/26.0 Chrome/122.0.6261.105 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '14', 'SAMSUNG_BROWSER', '26.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 13; SM-A346B) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/24.0 Chrome/117.0.5938.60 Mobile Safari/537.36', 'MOBILE', 'SAMSUNG', 'ANDROID', '13', 'SAMSUNG_BROWSER', '24.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6));

-- =====================================================
-- MOBILE - Pixel / Android / Chrome (10건)
-- =====================================================
INSERT INTO user_agent (token, user_agent_string, device_type, device_brand, os_type, os_version, browser_type, browser_version, status, health_score, last_used_at, requests_per_day, cooldown_until, consecutive_rate_limits, created_at, updated_at) VALUES
(NULL, 'Mozilla/5.0 (Linux; Android 15; Pixel 9 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.104 Mobile Safari/537.36', 'MOBILE', 'PIXEL', 'ANDROID', '15', 'CHROME', '131.0.6778.104', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 15; Pixel 9 Pro XL) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.73 Mobile Safari/537.36', 'MOBILE', 'PIXEL', 'ANDROID', '15', 'CHROME', '131.0.6778.73', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36', 'MOBILE', 'PIXEL', 'ANDROID', '15', 'CHROME', '130.0.6723.102', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 15; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.104 Mobile Safari/537.36', 'MOBILE', 'PIXEL', 'ANDROID', '15', 'CHROME', '131.0.6778.104', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 15; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.58 Mobile Safari/537.36', 'MOBILE', 'PIXEL', 'ANDROID', '15', 'CHROME', '130.0.6723.58', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 15; Pixel 8a) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.100 Mobile Safari/537.36', 'MOBILE', 'PIXEL', 'ANDROID', '15', 'CHROME', '129.0.6668.100', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; Pixel 7 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.127 Mobile Safari/537.36', 'MOBILE', 'PIXEL', 'ANDROID', '14', 'CHROME', '128.0.6613.127', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.6533.103 Mobile Safari/537.36', 'MOBILE', 'PIXEL', 'ANDROID', '14', 'CHROME', '127.0.6533.103', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; Pixel 7a) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6478.71 Mobile Safari/537.36', 'MOBILE', 'PIXEL', 'ANDROID', '14', 'CHROME', '126.0.6478.71', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; Pixel 6a) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.6422.113 Mobile Safari/537.36', 'MOBILE', 'PIXEL', 'ANDROID', '14', 'CHROME', '125.0.6422.113', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6));

-- =====================================================
-- MOBILE - Xiaomi / Android / Chrome (8건)
-- =====================================================
INSERT INTO user_agent (token, user_agent_string, device_type, device_brand, os_type, os_version, browser_type, browser_version, status, health_score, last_used_at, requests_per_day, cooldown_until, consecutive_rate_limits, created_at, updated_at) VALUES
(NULL, 'Mozilla/5.0 (Linux; Android 14; 2401116SG) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.104 Mobile Safari/537.36', 'MOBILE', 'XIAOMI', 'ANDROID', '14', 'CHROME', '131.0.6778.104', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; 23113RKC6G) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36', 'MOBILE', 'XIAOMI', 'ANDROID', '14', 'CHROME', '130.0.6723.102', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; 2311DRK48C) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.100 Mobile Safari/537.36', 'MOBILE', 'XIAOMI', 'ANDROID', '14', 'CHROME', '129.0.6668.100', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; 23078RKD5C) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.127 Mobile Safari/537.36', 'MOBILE', 'XIAOMI', 'ANDROID', '14', 'CHROME', '128.0.6613.127', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 13; 22081212UG) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.6533.103 Mobile Safari/537.36', 'MOBILE', 'XIAOMI', 'ANDROID', '13', 'CHROME', '127.0.6533.103', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; M2012K11AG) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.73 Mobile Safari/537.36', 'MOBILE', 'XIAOMI', 'ANDROID', '14', 'CHROME', '131.0.6778.73', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 13; Redmi Note 12 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6478.71 Mobile Safari/537.36', 'MOBILE', 'XIAOMI', 'ANDROID', '13', 'CHROME', '126.0.6478.71', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 13; Redmi Note 13 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.58 Mobile Safari/537.36', 'MOBILE', 'XIAOMI', 'ANDROID', '13', 'CHROME', '130.0.6723.58', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6));

-- =====================================================
-- MOBILE - Huawei / Android / Chrome (5건)
-- =====================================================
INSERT INTO user_agent (token, user_agent_string, device_type, device_brand, os_type, os_version, browser_type, browser_version, status, health_score, last_used_at, requests_per_day, cooldown_until, consecutive_rate_limits, created_at, updated_at) VALUES
(NULL, 'Mozilla/5.0 (Linux; Android 12; NOH-AN01) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.104 Mobile Safari/537.36', 'MOBILE', 'HUAWEI', 'ANDROID', '12', 'CHROME', '131.0.6778.104', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 12; ELS-AN00) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36', 'MOBILE', 'HUAWEI', 'ANDROID', '12', 'CHROME', '130.0.6723.102', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 12; OCE-AN10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.69 Mobile Safari/537.36', 'MOBILE', 'HUAWEI', 'ANDROID', '12', 'CHROME', '129.0.6668.69', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 10; VOG-L29) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.127 Mobile Safari/537.36', 'MOBILE', 'HUAWEI', 'ANDROID', '10', 'CHROME', '128.0.6613.127', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 10; MAR-LX1A) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.6533.103 Mobile Safari/537.36', 'MOBILE', 'HUAWEI', 'ANDROID', '10', 'CHROME', '127.0.6533.103', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6));

-- =====================================================
-- MOBILE - Oppo / Android / Chrome (4건)
-- =====================================================
INSERT INTO user_agent (token, user_agent_string, device_type, device_brand, os_type, os_version, browser_type, browser_version, status, health_score, last_used_at, requests_per_day, cooldown_until, consecutive_rate_limits, created_at, updated_at) VALUES
(NULL, 'Mozilla/5.0 (Linux; Android 14; CPH2581) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.104 Mobile Safari/537.36', 'MOBILE', 'OPPO', 'ANDROID', '14', 'CHROME', '131.0.6778.104', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; CPH2557) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36', 'MOBILE', 'OPPO', 'ANDROID', '14', 'CHROME', '130.0.6723.102', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 13; CPH2449) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.100 Mobile Safari/537.36', 'MOBILE', 'OPPO', 'ANDROID', '13', 'CHROME', '129.0.6668.100', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 13; CPH2365) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.98 Mobile Safari/537.36', 'MOBILE', 'OPPO', 'ANDROID', '13', 'CHROME', '128.0.6613.98', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6));

-- =====================================================
-- MOBILE - OnePlus / Android / Chrome (3건)
-- =====================================================
INSERT INTO user_agent (token, user_agent_string, device_type, device_brand, os_type, os_version, browser_type, browser_version, status, health_score, last_used_at, requests_per_day, cooldown_until, consecutive_rate_limits, created_at, updated_at) VALUES
(NULL, 'Mozilla/5.0 (Linux; Android 15; CPH2607) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.104 Mobile Safari/537.36', 'MOBILE', 'ONEPLUS', 'ANDROID', '15', 'CHROME', '131.0.6778.104', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; CPH2591) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Mobile Safari/537.36', 'MOBILE', 'ONEPLUS', 'ANDROID', '14', 'CHROME', '130.0.6723.102', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; NE2213) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.69 Mobile Safari/537.36', 'MOBILE', 'ONEPLUS', 'ANDROID', '14', 'CHROME', '129.0.6668.69', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6));

-- =====================================================
-- DESKTOP - Windows / Chrome (15건)
-- =====================================================
INSERT INTO user_agent (token, user_agent_string, device_type, device_brand, os_type, os_version, browser_type, browser_version, status, health_score, last_used_at, requests_per_day, cooldown_until, consecutive_rate_limits, created_at, updated_at) VALUES
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'CHROME', '131.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'CHROME', '130.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'CHROME', '129.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'CHROME', '128.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'CHROME', '127.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'CHROME', '126.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'CHROME', '125.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'CHROME', '124.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'CHROME', '123.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'CHROME', '122.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'CHROME', '121.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'CHROME', '120.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'CHROME', '131.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 11.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'WINDOWS', '11', 'CHROME', '131.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 11.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'WINDOWS', '11', 'CHROME', '130.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6));

-- =====================================================
-- DESKTOP - Windows / Edge (8건)
-- =====================================================
INSERT INTO user_agent (token, user_agent_string, device_type, device_brand, os_type, os_version, browser_type, browser_version, status, health_score, last_used_at, requests_per_day, cooldown_until, consecutive_rate_limits, created_at, updated_at) VALUES
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.2903.70', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'EDGE', '131.0.2903.70', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36 Edg/130.0.2849.80', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'EDGE', '130.0.2849.80', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36 Edg/129.0.2792.89', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'EDGE', '129.0.2792.89', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 Edg/128.0.2739.67', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'EDGE', '128.0.2739.67', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36 Edg/127.0.2651.98', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'EDGE', '127.0.2651.98', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36 Edg/126.0.2592.87', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'EDGE', '126.0.2592.87', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 11.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.2903.70', 'DESKTOP', 'GENERIC', 'WINDOWS', '11', 'EDGE', '131.0.2903.70', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 11.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36 Edg/130.0.2849.80', 'DESKTOP', 'GENERIC', 'WINDOWS', '11', 'EDGE', '130.0.2849.80', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6));

-- =====================================================
-- DESKTOP - Windows / Firefox (5건)
-- =====================================================
INSERT INTO user_agent (token, user_agent_string, device_type, device_brand, os_type, os_version, browser_type, browser_version, status, health_score, last_used_at, requests_per_day, cooldown_until, consecutive_rate_limits, created_at, updated_at) VALUES
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:133.0) Gecko/20100101 Firefox/133.0', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'FIREFOX', '133.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:132.0) Gecko/20100101 Firefox/132.0', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'FIREFOX', '132.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:131.0) Gecko/20100101 Firefox/131.0', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'FIREFOX', '131.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:130.0) Gecko/20100101 Firefox/130.0', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'FIREFOX', '130.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:129.0) Gecko/20100101 Firefox/129.0', 'DESKTOP', 'GENERIC', 'WINDOWS', '10', 'FIREFOX', '129.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6));

-- =====================================================
-- DESKTOP - macOS / Safari (7건)
-- =====================================================
INSERT INTO user_agent (token, user_agent_string, device_type, device_brand, os_type, os_version, browser_type, browser_version, status, health_score, last_used_at, requests_per_day, cooldown_until, consecutive_rate_limits, created_at, updated_at) VALUES
(NULL, 'Mozilla/5.0 (Macintosh; Intel Mac OS X 15_2) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.2 Safari/605.1.15', 'DESKTOP', 'GENERIC', 'MACOS', '15.2', 'SAFARI', '18.2', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Macintosh; Intel Mac OS X 15_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.1 Safari/605.1.15', 'DESKTOP', 'GENERIC', 'MACOS', '15.1', 'SAFARI', '18.1', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Macintosh; Intel Mac OS X 15_0) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Safari/605.1.15', 'DESKTOP', 'GENERIC', 'MACOS', '15.0', 'SAFARI', '18.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Macintosh; Intel Mac OS X 14_7_2) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.7.2 Safari/605.1.15', 'DESKTOP', 'GENERIC', 'MACOS', '14.7.2', 'SAFARI', '17.7.2', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Macintosh; Intel Mac OS X 14_6_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.6 Safari/605.1.15', 'DESKTOP', 'GENERIC', 'MACOS', '14.6.1', 'SAFARI', '17.6', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Macintosh; Intel Mac OS X 14_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Safari/605.1.15', 'DESKTOP', 'GENERIC', 'MACOS', '14.5', 'SAFARI', '17.5', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Macintosh; Intel Mac OS X 14_4_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4.1 Safari/605.1.15', 'DESKTOP', 'GENERIC', 'MACOS', '14.4.1', 'SAFARI', '17.4.1', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6));

-- =====================================================
-- DESKTOP - macOS / Chrome (5건)
-- =====================================================
INSERT INTO user_agent (token, user_agent_string, device_type, device_brand, os_type, os_version, browser_type, browser_version, status, health_score, last_used_at, requests_per_day, cooldown_until, consecutive_rate_limits, created_at, updated_at) VALUES
(NULL, 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'MACOS', '10.15.7', 'CHROME', '131.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'MACOS', '10.15.7', 'CHROME', '130.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'MACOS', '10.15.7', 'CHROME', '129.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'MACOS', '10.15.7', 'CHROME', '128.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'MACOS', '10.15.7', 'CHROME', '127.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6));

-- =====================================================
-- DESKTOP - Linux / Chrome (3건)
-- =====================================================
INSERT INTO user_agent (token, user_agent_string, device_type, device_brand, os_type, os_version, browser_type, browser_version, status, health_score, last_used_at, requests_per_day, cooldown_until, consecutive_rate_limits, created_at, updated_at) VALUES
(NULL, 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'LINUX', NULL, 'CHROME', '131.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'LINUX', NULL, 'CHROME', '130.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36', 'DESKTOP', 'GENERIC', 'LINUX', NULL, 'CHROME', '129.0.0.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6));

-- =====================================================
-- DESKTOP - Linux / Firefox (2건)
-- =====================================================
INSERT INTO user_agent (token, user_agent_string, device_type, device_brand, os_type, os_version, browser_type, browser_version, status, health_score, last_used_at, requests_per_day, cooldown_until, consecutive_rate_limits, created_at, updated_at) VALUES
(NULL, 'Mozilla/5.0 (X11; Linux x86_64; rv:133.0) Gecko/20100101 Firefox/133.0', 'DESKTOP', 'GENERIC', 'LINUX', NULL, 'FIREFOX', '133.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (X11; Linux x86_64; rv:132.0) Gecko/20100101 Firefox/132.0', 'DESKTOP', 'GENERIC', 'LINUX', NULL, 'FIREFOX', '132.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6));

-- =====================================================
-- TABLET - iPad / iOS / Safari (8건)
-- =====================================================
INSERT INTO user_agent (token, user_agent_string, device_type, device_brand, os_type, os_version, browser_type, browser_version, status, health_score, last_used_at, requests_per_day, cooldown_until, consecutive_rate_limits, created_at, updated_at) VALUES
(NULL, 'Mozilla/5.0 (iPad; CPU OS 18_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.2 Mobile/15E148 Safari/604.1', 'TABLET', 'IPAD', 'IOS', '18.2', 'SAFARI', '18.2', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPad; CPU OS 18_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.1 Mobile/15E148 Safari/604.1', 'TABLET', 'IPAD', 'IOS', '18.1', 'SAFARI', '18.1', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPad; CPU OS 18_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Mobile/15E148 Safari/604.1', 'TABLET', 'IPAD', 'IOS', '18.0', 'SAFARI', '18.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPad; CPU OS 17_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.7 Mobile/15E148 Safari/604.1', 'TABLET', 'IPAD', 'IOS', '17.7', 'SAFARI', '17.7', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPad; CPU OS 17_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.6 Mobile/15E148 Safari/604.1', 'TABLET', 'IPAD', 'IOS', '17.6', 'SAFARI', '17.6', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPad; CPU OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1', 'TABLET', 'IPAD', 'IOS', '17.5', 'SAFARI', '17.5', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPad; CPU OS 17_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Mobile/15E148 Safari/604.1', 'TABLET', 'IPAD', 'IOS', '17.4', 'SAFARI', '17.4', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (iPad; CPU OS 17_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.3 Mobile/15E148 Safari/604.1', 'TABLET', 'IPAD', 'IOS', '17.3', 'SAFARI', '17.3', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6));

-- =====================================================
-- TABLET - Galaxy Tab / Android / Chrome (5건)
-- =====================================================
INSERT INTO user_agent (token, user_agent_string, device_type, device_brand, os_type, os_version, browser_type, browser_version, status, health_score, last_used_at, requests_per_day, cooldown_until, consecutive_rate_limits, created_at, updated_at) VALUES
(NULL, 'Mozilla/5.0 (Linux; Android 14; SM-X910) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.104 Safari/537.36', 'TABLET', 'GALAXY_TAB', 'ANDROID', '14', 'CHROME', '131.0.6778.104', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; SM-X810) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.102 Safari/537.36', 'TABLET', 'GALAXY_TAB', 'ANDROID', '14', 'CHROME', '130.0.6723.102', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 14; SM-X710) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.100 Safari/537.36', 'TABLET', 'GALAXY_TAB', 'ANDROID', '14', 'CHROME', '129.0.6668.100', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 13; SM-X610) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.127 Safari/537.36', 'TABLET', 'GALAXY_TAB', 'ANDROID', '13', 'CHROME', '128.0.6613.127', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 13; SM-X510) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.6533.103 Safari/537.36', 'TABLET', 'GALAXY_TAB', 'ANDROID', '13', 'CHROME', '127.0.6533.103', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6));

-- =====================================================
-- TABLET - Galaxy Tab / Android / Samsung Browser (2건)
-- =====================================================
INSERT INTO user_agent (token, user_agent_string, device_type, device_brand, os_type, os_version, browser_type, browser_version, status, health_score, last_used_at, requests_per_day, cooldown_until, consecutive_rate_limits, created_at, updated_at) VALUES
(NULL, 'Mozilla/5.0 (Linux; Android 14; SM-X910) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/27.0 Chrome/125.0.6422.165 Safari/537.36', 'TABLET', 'GALAXY_TAB', 'ANDROID', '14', 'SAMSUNG_BROWSER', '27.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6)),
(NULL, 'Mozilla/5.0 (Linux; Android 13; SM-X610) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/26.0 Chrome/122.0.6261.105 Safari/537.36', 'TABLET', 'GALAXY_TAB', 'ANDROID', '13', 'SAMSUNG_BROWSER', '26.0', 'IDLE', 100, NULL, 0, NULL, 0, NOW(6), NOW(6));
