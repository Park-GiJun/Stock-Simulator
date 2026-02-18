-- 체결 이력 테이블
CREATE TABLE IF NOT EXISTS trading.trades (
    trade_id VARCHAR(36) PRIMARY KEY,
    buy_order_id VARCHAR(36) NOT NULL,
    sell_order_id VARCHAR(36) NOT NULL,
    buyer_id VARCHAR(36) NOT NULL,
    buyer_type VARCHAR(20) NOT NULL,
    seller_id VARCHAR(36) NOT NULL,
    seller_type VARCHAR(20) NOT NULL,
    stock_id VARCHAR(20) NOT NULL,
    price BIGINT NOT NULL,
    quantity BIGINT NOT NULL,
    trade_amount BIGINT NOT NULL,
    traded_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_trades_buyer ON trading.trades(buyer_id, buyer_type);
CREATE INDEX IF NOT EXISTS idx_trades_seller ON trading.trades(seller_id, seller_type);
CREATE INDEX IF NOT EXISTS idx_trades_stock ON trading.trades(stock_id);
CREATE INDEX IF NOT EXISTS idx_trades_traded_at ON trading.trades(traded_at);
