-- ============================================================
-- 마이그레이션: NPC/기관 투자자 타입 수정
-- 원인: TradingEventConsumer에서 investorType 미전달로 USER로 저장됨
-- 주의: 코드 수정 후 신규 데이터(NPC/INSTITUTION)와 기존 데이터(USER)가
--       동일 투자자+종목으로 중복 존재할 수 있으므로 병합 처리
-- ============================================================

BEGIN;

-- ============================================================
-- 1. trading.orders: investor_type 수정 (unique 제약 없으므로 단순 UPDATE)
-- ============================================================
UPDATE trading.orders
SET investor_type = 'NPC', updated_at = NOW()
WHERE user_id LIKE 'NPC_%' AND investor_type = 'USER';

UPDATE trading.orders
SET investor_type = 'INSTITUTION', updated_at = NOW()
WHERE user_id LIKE 'INST_%' AND investor_type = 'USER';

-- ============================================================
-- 2. trading.portfolios: 중복 병합 후 잘못된 행 삭제
--    unique constraint: (investor_id, investor_type, stock_id)
-- ============================================================

-- 2-1. NPC: 이미 올바른 NPC 행이 있으면 USER 행의 수량을 합산
UPDATE trading.portfolios correct
SET quantity      = correct.quantity + old.quantity,
    total_invested = correct.total_invested + old.total_invested,
    average_price  = CASE
        WHEN (correct.quantity + old.quantity) > 0
        THEN (correct.total_invested + old.total_invested) / (correct.quantity + old.quantity)
        ELSE correct.average_price
    END,
    updated_at = NOW()
FROM trading.portfolios old
WHERE correct.investor_id = old.investor_id
  AND correct.stock_id    = old.stock_id
  AND old.investor_id LIKE 'NPC_%'
  AND old.investor_type   = 'USER'
  AND correct.investor_type = 'NPC';

-- 2-2. INSTITUTION: 동일 병합
UPDATE trading.portfolios correct
SET quantity      = correct.quantity + old.quantity,
    total_invested = correct.total_invested + old.total_invested,
    average_price  = CASE
        WHEN (correct.quantity + old.quantity) > 0
        THEN (correct.total_invested + old.total_invested) / (correct.quantity + old.quantity)
        ELSE correct.average_price
    END,
    updated_at = NOW()
FROM trading.portfolios old
WHERE correct.investor_id = old.investor_id
  AND correct.stock_id    = old.stock_id
  AND old.investor_id LIKE 'INST_%'
  AND old.investor_type   = 'USER'
  AND correct.investor_type = 'INSTITUTION';

-- 2-3. 병합 완료된 USER 행 삭제 (이미 올바른 NPC/INSTITUTION 행이 존재하는 것만)
DELETE FROM trading.portfolios old
USING trading.portfolios correct
WHERE old.investor_id = correct.investor_id
  AND old.stock_id    = correct.stock_id
  AND old.investor_type = 'USER'
  AND old.investor_id LIKE 'NPC_%'
  AND correct.investor_type = 'NPC';

DELETE FROM trading.portfolios old
USING trading.portfolios correct
WHERE old.investor_id = correct.investor_id
  AND old.stock_id    = correct.stock_id
  AND old.investor_type = 'USER'
  AND old.investor_id LIKE 'INST_%'
  AND correct.investor_type = 'INSTITUTION';

-- 2-4. 중복 없이 USER로만 남아있는 행은 단순 타입 변경
UPDATE trading.portfolios
SET investor_type = 'NPC', updated_at = NOW()
WHERE investor_id LIKE 'NPC_%' AND investor_type = 'USER';

UPDATE trading.portfolios
SET investor_type = 'INSTITUTION', updated_at = NOW()
WHERE investor_id LIKE 'INST_%' AND investor_type = 'USER';

-- ============================================================
-- 3. trading.trades: buyer_type / seller_type 수정
-- ============================================================
UPDATE trading.trades SET buyer_type = 'NPC'
WHERE buyer_id LIKE 'NPC_%' AND buyer_type = 'USER';

UPDATE trading.trades SET buyer_type = 'INSTITUTION'
WHERE buyer_id LIKE 'INST_%' AND buyer_type = 'USER';

UPDATE trading.trades SET seller_type = 'NPC'
WHERE seller_id LIKE 'NPC_%' AND seller_type = 'USER';

UPDATE trading.trades SET seller_type = 'INSTITUTION'
WHERE seller_id LIKE 'INST_%' AND seller_type = 'USER';

COMMIT;