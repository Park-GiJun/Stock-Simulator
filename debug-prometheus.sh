#!/bin/bash
# Prometheus 설정 파일 존재 여부 확인 스크립트

echo "=========================================="
echo "Prometheus 파일 존재 여부 체크"
echo "=========================================="

echo ""
echo "1. /deploy/infra 디렉토리 확인:"
ls -la /deploy/infra/ 2>&1 || echo "❌ /deploy/infra/ 디렉토리가 없습니다"

echo ""
echo "2. /deploy/infra/prometheus 디렉토리 확인:"
ls -la /deploy/infra/prometheus/ 2>&1 || echo "❌ /deploy/infra/prometheus/ 디렉토리가 없습니다"

echo ""
echo "3. prometheus.yml 파일 타입 확인:"
if [ -f /deploy/infra/prometheus/prometheus.yml ]; then
    echo "✅ prometheus.yml은 일반 파일입니다"
    ls -lh /deploy/infra/prometheus/prometheus.yml
elif [ -d /deploy/infra/prometheus/prometheus.yml ]; then
    echo "❌ prometheus.yml이 디렉토리입니다! (파일이어야 함)"
    ls -ld /deploy/infra/prometheus/prometheus.yml
else
    echo "❌ prometheus.yml이 존재하지 않습니다"
fi

echo ""
echo "4. prometheus.yml 파일 내용 확인 (처음 5줄):"
head -5 /deploy/infra/prometheus/prometheus.yml 2>&1 || echo "❌ 파일을 읽을 수 없습니다"

echo ""
echo "5. 파일 권한 확인:"
stat /deploy/infra/prometheus/prometheus.yml 2>&1 || echo "❌ stat 실패"

echo ""
echo "=========================================="
