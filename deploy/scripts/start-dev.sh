#!/bin/bash
set -e

echo "Starting Zong development environment..."
docker-compose -f docker-compose.yml up --build -d

echo ""
echo "Services running:"
echo "  Frontend:  http://localhost:3000"
echo "  Gateway:   http://localhost:8080"
echo "  Community: http://localhost:8081"
echo "  Knowledge: http://localhost:8082"
echo "  Agent:     http://localhost:8083"
