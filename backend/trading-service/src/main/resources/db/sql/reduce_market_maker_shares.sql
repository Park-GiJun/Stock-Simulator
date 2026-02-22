-- ============================================
-- 기존 MARKET_MAKER 매도 주문/포트폴리오 수량 대폭 축소
-- 목표: 대형주 ~100만, 중형주 ~10만, 소형주 ~1만 수준
-- 실행 전 반드시 trading-service를 중지하고,
-- 실행 후 Redis의 orderbook 캐시를 초기화한 뒤 재시작
-- ============================================

BEGIN;

-- 1. PENDING 상태인 MARKET_MAKER 매도 주문 수량을 1/100으로 축소 (최소 1주)
UPDATE trading.orders
SET quantity = GREATEST(quantity / 100, 1),
    filled_quantity = GREATEST(filled_quantity / 100, 0)
WHERE user_id = 'SYSTEM_MARKET_MAKER'
  AND status = 'PENDING'
  AND order_type = 'SELL';

-- 2. MARKET_MAKER 포트폴리오 보유수량도 1/100으로 축소
UPDATE trading.portfolios
SET quantity = GREATEST(quantity / 100, 1),
    total_invested = GREATEST(total_invested / 100, 1)
WHERE investor_id = 'SYSTEM_MARKET_MAKER'
  AND investor_type = 'MARKET_MAKER';

-- 3. 확인용 조회
SELECT 'PENDING 매도 주문' AS label, COUNT(*) AS cnt,
       SUM(quantity) AS total_qty, AVG(quantity)::bigint AS avg_qty
FROM trading.orders
WHERE user_id = 'SYSTEM_MARKET_MAKER'
  AND status = 'PENDING'
  AND order_type = 'SELL';

SELECT 'MARKET_MAKER 포트폴리오' AS label, COUNT(*) AS cnt,
       SUM(quantity) AS total_qty, AVG(quantity)::bigint AS avg_qty
FROM trading.portfolios
WHERE investor_id = 'SYSTEM_MARKET_MAKER';

COMMIT;

-- ============================================
-- 실행 후 필수 작업:
-- 1. Redis에서 orderbook 캐시 삭제:
--    redis-cli -a stocksim123 -p 6380 KEYS "orderbook:*" | xargs redis-cli -a stocksim123 -p 6380 DEL
-- 2. trading-service 재시작 (서비스 기동 시 Redis에서 orderbook 재로드)
-- ============================================
