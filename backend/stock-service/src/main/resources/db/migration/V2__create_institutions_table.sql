-- Institutions 테이블
CREATE TABLE IF NOT EXISTS stocks.institutions (
    institution_id BIGSERIAL PRIMARY KEY,
    institution_name VARCHAR(100) NOT NULL UNIQUE,
    institution_type VARCHAR(30) NOT NULL,
    investment_style VARCHAR(20) NOT NULL,
    capital BIGINT NOT NULL,
    daily_income BIGINT NOT NULL,
    risk_tolerance DOUBLE PRECISION NOT NULL,
    preferred_sectors TEXT NOT NULL,
    trading_frequency VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 인덱스
CREATE INDEX idx_institutions_type ON stocks.institutions(institution_type);
CREATE INDEX idx_institutions_name ON stocks.institutions(institution_name);
