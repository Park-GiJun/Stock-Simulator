-- 체결 이력 테이블
CREATE TABLE IF NOT EXISTS trading.trades (
    trade_id VARCHAR(36) PRIMARY KEY,
    buy_order_id VARCHAR(36) NOT NULL,
    sell_order_id VARCHAR(36) NOT NULL,
    buy_user_id VARCHAR(36) NOT NULL,
    sell_user_id VARCHAR(36) NOT NULL,
    stock_id VARCHAR(20) NOT NULL,
    price BIGINT NOT NULL,
    quantity BIGINT NOT NULL,
    matched_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_trades_stock_id ON trading.trades(stock_id);
CREATE INDEX IF NOT EXISTS idx_trades_buy_user_id ON trading.trades(buy_user_id);
CREATE INDEX IF NOT EXISTS idx_trades_sell_user_id ON trading.trades(sell_user_id);
CREATE INDEX IF NOT EXISTS idx_trades_matched_at ON trading.trades(matched_at);
