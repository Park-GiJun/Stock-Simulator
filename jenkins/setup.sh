#!/bin/bash

echo "🚀 Starting Jenkins setup..."

# Jenkins 시작
cd jenkins
docker-compose up -d

echo "⏳ Waiting for Jenkins to start..."
sleep 30

# 초기 비밀번호 출력
echo "🔑 Initial Admin Password:"
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

echo ""
echo "📝 Next Steps:"
echo "1. Open http://172.30.1.79:8090 in your browser"
echo "2. Enter the admin password shown above"
echo "3. Install suggested plugins"
echo "4. Create admin user"
echo ""
echo "After setup, configure:"
echo "- GitHub credentials (github-token)"
echo "- Create Pipeline job pointing to your repository"
echo ""
echo "✅ Jenkins is running on port 8090"
