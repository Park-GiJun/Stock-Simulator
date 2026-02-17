CREATE TABLE IF NOT EXISTS stocks.npcs (
    npc_id BIGSERIAL PRIMARY KEY,
    npc_name VARCHAR(100) NOT NULL UNIQUE,
    investment_style VARCHAR(20) NOT NULL,
    capital BIGINT NOT NULL,
    weekly_income BIGINT NOT NULL,
    risk_tolerance DOUBLE PRECISION NOT NULL,
    preferred_sectors TEXT NOT NULL,
    trading_frequency VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_npcs_name ON stocks.npcs(npc_name);
CREATE INDEX idx_npcs_investment_style ON stocks.npcs(investment_style);
