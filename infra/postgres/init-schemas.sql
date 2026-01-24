-- =============================================================================
-- PostgreSQL Schema Initialization for Stock Simulator
-- =============================================================================
-- 이 스크립트는 각 마이크로서비스가 사용할 스키마를 생성합니다.
-- =============================================================================

-- users 스키마 (user-service)
CREATE SCHEMA IF NOT EXISTS users;
GRANT ALL PRIVILEGES ON SCHEMA users TO stocksim;

-- stocks 스키마 (stock-service)
CREATE SCHEMA IF NOT EXISTS stocks;
GRANT ALL PRIVILEGES ON SCHEMA stocks TO stocksim;

-- trading 스키마 (trading-service)
CREATE SCHEMA IF NOT EXISTS trading;
GRANT ALL PRIVILEGES ON SCHEMA trading TO stocksim;

-- events 스키마 (event-service)
CREATE SCHEMA IF NOT EXISTS events;
GRANT ALL PRIVILEGES ON SCHEMA events TO stocksim;

-- scheduler 스키마 (scheduler-service)
CREATE SCHEMA IF NOT EXISTS scheduler;
GRANT ALL PRIVILEGES ON SCHEMA scheduler TO stocksim;

-- season 스키마 (season-service)
CREATE SCHEMA IF NOT EXISTS season;
GRANT ALL PRIVILEGES ON SCHEMA season TO season;

-- 확인 메시지
DO $$
BEGIN
    RAISE NOTICE 'All schemas created successfully!';
END $$;
