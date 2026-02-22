-- =============================================================================
-- 기존 NPC/기관투자자 잔고 초기화 SQL
-- 이미 생성된 NPC/기관투자자 중 trading.investor_balances에 레코드가 없는 경우 INSERT
-- 실행 대상: PostgreSQL (stocksim DB)
-- =============================================================================

-- 1. 기존 NPC 잔고 초기화
-- investorId 규칙: "NPC_{npcId}"
INSERT INTO trading.investor_balances (investor_id, investor_type, cash, created_at, updated_at)
SELECT
    'NPC_' || n.npc_id,
    'NPC',
    n.capital,
    NOW(),
    NOW()
FROM stocks.npcs n
WHERE NOT EXISTS (
    SELECT 1
    FROM trading.investor_balances ib
    WHERE ib.investor_id = 'NPC_' || n.npc_id
      AND ib.investor_type = 'NPC'
);

-- 2. 기존 기관투자자 잔고 초기화
-- investorId 규칙: "INST_{institutionId}"
INSERT INTO trading.investor_balances (investor_id, investor_type, cash, created_at, updated_at)
SELECT
    'INST_' || i.institution_id,
    'INSTITUTION',
    i.capital,
    NOW(),
    NOW()
FROM stocks.institutions i
WHERE NOT EXISTS (
    SELECT 1
    FROM trading.investor_balances ib
    WHERE ib.investor_id = 'INST_' || i.institution_id
      AND ib.investor_type = 'INSTITUTION'
);

-- 확인 쿼리
-- SELECT investor_id, investor_type, cash FROM trading.investor_balances WHERE investor_type IN ('NPC', 'INSTITUTION') ORDER BY investor_id;
