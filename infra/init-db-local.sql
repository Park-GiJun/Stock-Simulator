-- =============================================================================
-- PostgreSQL Schema Initialization for Stock Simulator (Local Development)
-- =============================================================================
-- Creates all schemas used by microservices.
-- =============================================================================

-- users schema (user-service)
CREATE SCHEMA IF NOT EXISTS users;
GRANT ALL PRIVILEGES ON SCHEMA users TO stocksim;

-- stocks schema (stock-service)
CREATE SCHEMA IF NOT EXISTS stocks;
GRANT ALL PRIVILEGES ON SCHEMA stocks TO stocksim;

-- trading schema (trading-service)
CREATE SCHEMA IF NOT EXISTS trading;
GRANT ALL PRIVILEGES ON SCHEMA trading TO stocksim;

-- events schema (event-service)
CREATE SCHEMA IF NOT EXISTS events;
GRANT ALL PRIVILEGES ON SCHEMA events TO stocksim;

-- scheduler schema (scheduler-service)
CREATE SCHEMA IF NOT EXISTS scheduler;
GRANT ALL PRIVILEGES ON SCHEMA scheduler TO stocksim;

-- news schema (news-service)
CREATE SCHEMA IF NOT EXISTS news;
GRANT ALL PRIVILEGES ON SCHEMA news TO stocksim;

DO $$
BEGIN
    RAISE NOTICE 'All schemas created successfully for local development!';
END $$;
