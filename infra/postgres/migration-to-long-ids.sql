-- ===================================================================
-- Stock Simulator - ID Type Migration (UUID String → Long)
-- Spring Boot 4.0 Upgrade Migration Script
-- ===================================================================
-- 목적: UserJpaEntity와 BalanceJpaEntity의 ID 타입을 String UUID에서 Long으로 변경
-- 주의: 기존 데이터가 손실되므로 개발 환경에서만 사용하세요!
-- ===================================================================

-- 1. 기존 데이터 백업 (선택사항)
-- CREATE TABLE users.users_backup AS SELECT * FROM users.users;
-- CREATE TABLE users.balances_backup AS SELECT * FROM users.balances;

-- 2. 기존 테이블 삭제 (외래 키 제약 조건이 있다면 먼저 삭제)
DROP TABLE IF EXISTS users.balances CASCADE;
DROP TABLE IF EXISTS users.users CASCADE;

-- 3. Users 테이블 재생성 (Long ID with IDENTITY)
CREATE TABLE users.users (
    user_id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Balances 테이블 재생성 (Long ID with IDENTITY, Foreign Key to users)
CREATE TABLE users.balances (
    balance_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users.users(user_id) ON DELETE CASCADE,
    cash BIGINT NOT NULL DEFAULT 5000000,
    total_asset BIGINT NOT NULL DEFAULT 5000000,
    stock_value BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. 인덱스 생성
CREATE INDEX idx_users_email ON users.users(email);
CREATE INDEX idx_balances_user_id ON users.balances(user_id);

-- 6. 코멘트 추가
COMMENT ON TABLE users.users IS '사용자 테이블 (Long ID 사용)';
COMMENT ON TABLE users.balances IS '잔고 테이블 (Long ID 사용)';

-- 7. 완료 메시지
SELECT 'Migration completed: users and balances tables recreated with BIGINT IDs' AS status;
