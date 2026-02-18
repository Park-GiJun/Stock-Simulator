-- orders 테이블에 investor_type 컬럼 추가
ALTER TABLE trading.orders ADD COLUMN investor_type VARCHAR(20) NOT NULL DEFAULT 'USER';

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_orders_investor_type ON trading.orders(investor_type);
