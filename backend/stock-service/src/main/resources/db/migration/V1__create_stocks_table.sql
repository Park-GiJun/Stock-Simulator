-- Stocks schema 생성
CREATE SCHEMA IF NOT EXISTS stocks;

-- Stocks 테이블
CREATE TABLE IF NOT EXISTS stocks.stocks (
    stock_id VARCHAR(20) PRIMARY KEY,
    stock_name VARCHAR(100) NOT NULL,
    sector VARCHAR(50) NOT NULL,
    base_price BIGINT NOT NULL,
    current_price BIGINT NOT NULL,
    previous_close BIGINT NOT NULL,
    total_shares BIGINT NOT NULL,
    market_cap_grade VARCHAR(20) NOT NULL,
    volatility BIGINT NOT NULL DEFAULT 0,
    per DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    dividend_rate DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    growth_rate DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    event_sensitivity DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    volume BIGINT NOT NULL DEFAULT 0,
    high BIGINT NOT NULL DEFAULT 0,
    low BIGINT NOT NULL DEFAULT 0,
    open_price BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'LISTED',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_stocks_sector ON stocks.stocks(sector);
CREATE INDEX IF NOT EXISTS idx_stocks_status ON stocks.stocks(status);
CREATE INDEX IF NOT EXISTS idx_stocks_market_cap_grade ON stocks.stocks(market_cap_grade);
CREATE INDEX IF NOT EXISTS idx_stocks_stock_name ON stocks.stocks(stock_name);
