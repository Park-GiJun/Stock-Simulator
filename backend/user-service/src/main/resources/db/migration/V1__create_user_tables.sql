-- Users 테이블 (users 스키마)
CREATE TABLE IF NOT EXISTS users (
                                     user_id BIGSERIAL PRIMARY KEY,          -- ✅ IDENTITY 전략
                                     email VARCHAR(255) NOT NULL UNIQUE,
                                     password VARCHAR(255) NOT NULL,
                                     username VARCHAR(100) NOT NULL UNIQUE,
                                     role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
                                     created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                     updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS balances (
                                        balance_id BIGSERIAL PRIMARY KEY,        -- ✅ IDENTITY 전략
                                        user_id BIGINT NOT NULL UNIQUE,
                                        cash BIGINT NOT NULL DEFAULT 5000000,
                                        total_asset BIGINT NOT NULL DEFAULT 5000000,
                                        stock_value BIGINT NOT NULL DEFAULT 0,
                                        created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                        updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                        CONSTRAINT fk_balance_user FOREIGN KEY (user_id)
                                            REFERENCES users(user_id) ON DELETE CASCADE
);
-- 인덱스
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_balances_user_id ON balances(user_id);