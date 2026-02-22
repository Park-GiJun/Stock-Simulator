-- ============================================================
-- 마이그레이션: NPC/기관 투자자 타입 수정
-- 원인: TradingEventConsumer에서 ScheduleTradeEvent 처리 시
--       investorType을 PlaceOrderCommand에 전달하지 않아
--       기본값 USER로 저장됨
-- ============================================================

BEGIN;

-- ============================================================
-- 1. trading.orders: NPC/기관 주문의 investor_type 수정
-- ============================================================
-- NPC 주문 (user_id가 'NPC_'로 시작하는데 investor_type이 USER인 것)
UPDATE trading.orders
SET investor_type = 'NPC',
    updated_at = NOW()
WHERE user_id LIKE 'NPC_%'
  AND investor_type = 'USER';

-- 기관 주문 (user_id가 'INST_'로 시작하는데 investor_type이 USER인 것)
UPDATE trading.orders
SET investor_type = 'INSTITUTION',
    updated_at = NOW()
WHERE user_id LIKE 'INST_%'
  AND investor_type = 'USER';

-- ============================================================
-- 2. trading.portfolios: NPC/기관 포트폴리오의 investor_type 수정
-- ============================================================
-- NPC 포트폴리오
UPDATE trading.portfolios
SET investor_type = 'NPC',
    updated_at = NOW()
WHERE investor_id LIKE 'NPC_%'
  AND investor_type = 'USER';

-- 기관 포트폴리오
UPDATE trading.portfolios
SET investor_type = 'INSTITUTION',
    updated_at = NOW()
WHERE investor_id LIKE 'INST_%'
  AND investor_type = 'USER';

-- ============================================================
-- 3. trading.trades: 매수자/매도자 타입 수정
-- ============================================================
-- 매수자가 NPC인 경우
UPDATE trading.trades
SET buyer_type = 'NPC'
WHERE buyer_id LIKE 'NPC_%'
  AND buyer_type = 'USER';

-- 매수자가 기관인 경우
UPDATE trading.trades
SET buyer_type = 'INSTITUTION'
WHERE buyer_id LIKE 'INST_%'
  AND buyer_type = 'USER';

-- 매도자가 NPC인 경우
UPDATE trading.trades
SET seller_type = 'NPC'
WHERE seller_id LIKE 'NPC_%'
  AND seller_type = 'USER';

-- 매도자가 기관인 경우
UPDATE trading.trades
SET seller_type = 'INSTITUTION'
WHERE seller_id LIKE 'INST_%'
  AND seller_type = 'USER';

COMMIT;

-- ============================================================
-- 검증 쿼리 (실행 후 확인용)
-- ============================================================
-- 잘못된 데이터가 남아있는지 확인
-- SELECT 'orders' AS tbl, investor_type, COUNT(*)
-- FROM trading.orders
-- WHERE (user_id LIKE 'NPC_%' OR user_id LIKE 'INST_%')
-- GROUP BY investor_type;
--
-- SELECT 'portfolios' AS tbl, investor_type, COUNT(*)
-- FROM trading.portfolios
-- WHERE (investor_id LIKE 'NPC_%' OR investor_id LIKE 'INST_%')
-- GROUP BY investor_type;
--
-- SELECT 'trades_buyer' AS tbl, buyer_type, COUNT(*)
-- FROM trading.trades
-- WHERE (buyer_id LIKE 'NPC_%' OR buyer_id LIKE 'INST_%')
-- GROUP BY buyer_type;
--
-- SELECT 'trades_seller' AS tbl, seller_type, COUNT(*)
-- FROM trading.trades
-- WHERE (seller_id LIKE 'NPC_%' OR seller_id LIKE 'INST_%')
-- GROUP BY seller_type;
