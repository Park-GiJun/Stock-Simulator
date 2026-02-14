#!/bin/bash

echo "Starting TeamCity setup..."

cd "$(dirname "$0")"

# Check if stocksim-network exists
if ! docker network inspect stock-simulator_stocksim-network >/dev/null 2>&1; then
    echo "Warning: stock-simulator_stocksim-network not found."
    echo "Make sure the main docker-compose is running first."
    echo "Run: docker-compose --profile all up -d"
    exit 1
fi

# Start TeamCity
docker-compose up -d

echo "Waiting for TeamCity server to start (this may take up to 60 seconds)..."
for i in $(seq 1 12); do
    if curl -sf http://localhost:8111 >/dev/null 2>&1; then
        echo "TeamCity server is ready!"
        break
    fi
    echo "  Attempt $i/12 - not ready yet, waiting 5s..."
    sleep 5
done

echo ""
echo "=== TeamCity Setup ==="
echo ""
echo "1. Open http://172.30.1.79:8111 in your browser"
echo "2. On first launch, accept the license agreement"
echo "3. Choose 'Internal (HSQLDB)' for database (or configure PostgreSQL)"
echo "4. Create an admin account"
echo ""
echo "Super User Token (if needed):"
docker exec stockSimulator-teamcity-server cat /data/teamcity_server/datadir/system/token.txt 2>/dev/null || echo "  Token not yet generated. Check after server fully starts."
echo ""
echo "After setup, configure:"
echo "- VCS Root: https://github.com/Park-GiJun/Stock-Simulator.git"
echo "- GHCR credentials in Build Parameters"
echo "- Versioned Settings pointing to .teamcity/settings.kts"
echo ""
echo "TeamCity is running on port 8111"
