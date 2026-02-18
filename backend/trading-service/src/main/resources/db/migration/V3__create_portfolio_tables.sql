-- 포트폴리오 테이블 (투자자별 종목 보유현황)
CREATE TABLE IF NOT EXISTS trading.portfolios (
    id BIGSERIAL PRIMARY KEY,
    investor_id VARCHAR(36) NOT NULL,
    investor_type VARCHAR(20) NOT NULL,
    stock_id VARCHAR(20) NOT NULL,
    quantity BIGINT NOT NULL DEFAULT 0,
    average_price BIGINT NOT NULL DEFAULT 0,
    total_invested BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_portfolio_investor_stock UNIQUE (investor_id, investor_type, stock_id)
);

CREATE INDEX IF NOT EXISTS idx_portfolios_investor ON trading.portfolios(investor_id, investor_type);
CREATE INDEX IF NOT EXISTS idx_portfolios_stock ON trading.portfolios(stock_id);

-- 투자자 잔고 테이블 (통합: user/npc/기관)
CREATE TABLE IF NOT EXISTS trading.investor_balances (
    id BIGSERIAL PRIMARY KEY,
    investor_id VARCHAR(36) NOT NULL,
    investor_type VARCHAR(20) NOT NULL,
    cash BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_investor_balance UNIQUE (investor_id, investor_type)
);

CREATE INDEX IF NOT EXISTS idx_investor_balances_investor ON trading.investor_balances(investor_id, investor_type);
