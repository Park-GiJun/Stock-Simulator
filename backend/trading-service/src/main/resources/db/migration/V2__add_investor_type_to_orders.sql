-- 주문 테이블에 투자자 유형 컬럼 추가
ALTER TABLE trading.orders ADD COLUMN IF NOT EXISTS investor_type VARCHAR(20) NOT NULL DEFAULT 'USER';

CREATE INDEX IF NOT EXISTS idx_orders_investor_type ON trading.orders(investor_type);
