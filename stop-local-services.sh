#!/bin/bash

# =============================================================================
# 로컬 서비스 종료 스크립트
# =============================================================================

echo "Stopping all local services..."

if [ -f logs/eureka-server.pid ]; then
    kill $(cat logs/eureka-server.pid) 2>/dev/null
    rm logs/eureka-server.pid
    echo "✓ Stopped Eureka Server"
fi

if [ -f logs/api-gateway.pid ]; then
    kill $(cat logs/api-gateway.pid) 2>/dev/null
    rm logs/api-gateway.pid
    echo "✓ Stopped API Gateway"
fi

if [ -f logs/user-service.pid ]; then
    kill $(cat logs/user-service.pid) 2>/dev/null
    rm logs/user-service.pid
    echo "✓ Stopped User Service"
fi

if [ -f logs/stock-service.pid ]; then
    kill $(cat logs/stock-service.pid) 2>/dev/null
    rm logs/stock-service.pid
    echo "✓ Stopped Stock Service"
fi

if [ -f logs/trading-service.pid ]; then
    kill $(cat logs/trading-service.pid) 2>/dev/null
    rm logs/trading-service.pid
    echo "✓ Stopped Trading Service"
fi

if [ -f logs/event-service.pid ]; then
    kill $(cat logs/event-service.pid) 2>/dev/null
    rm logs/event-service.pid
    echo "✓ Stopped Event Service"
fi

if [ -f logs/scheduler-service.pid ]; then
    kill $(cat logs/scheduler-service.pid) 2>/dev/null
    rm logs/scheduler-service.pid
    echo "✓ Stopped Scheduler Service"
fi

if [ -f logs/news-service.pid ]; then
    kill $(cat logs/news-service.pid) 2>/dev/null
    rm logs/news-service.pid
    echo "✓ Stopped News Service"
fi

echo ""
echo "All services stopped."
