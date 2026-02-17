-- Trading schema 생성
CREATE SCHEMA IF NOT EXISTS trading;

-- Orders 테이블
CREATE TABLE IF NOT EXISTS trading.orders (
    order_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    stock_id VARCHAR(20) NOT NULL,
    order_type VARCHAR(10) NOT NULL,
    order_kind VARCHAR(10) NOT NULL,
    price BIGINT,
    quantity BIGINT NOT NULL,
    filled_quantity BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON trading.orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_stock_id ON trading.orders(stock_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON trading.orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON trading.orders(created_at);
