#!/bin/bash

# Test Redis Connection Script
# This script tests connection to Upstash Redis

echo "=========================================="
echo "Testing Redis Connection"
echo "=========================================="
echo ""

# Load environment variables
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
    echo "✓ Loaded .env file"
else
    echo "✗ .env file not found"
    exit 1
fi

echo ""
echo "Redis Configuration:"
echo "  Host: $REDIS_HOST"
echo "  Port: $REDIS_PORT"
echo "  SSL: ${REDIS_SSL_ENABLED:-true}"
echo ""

# Test connection using redis-cli (if available)
if command -v redis-cli &> /dev/null; then
    echo "Testing with redis-cli..."
    redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD --tls PING
    if [ $? -eq 0 ]; then
        echo "✓ Redis connection successful!"
    else
        echo "✗ Redis connection failed!"
        exit 1
    fi
else
    echo "⚠ redis-cli not found, skipping CLI test"
fi

echo ""
echo "Testing with curl (REST API)..."
if [ ! -z "$UPSTASH_REDIS_REST_URL" ]; then
    response=$(curl -s -w "\n%{http_code}" \
        -H "Authorization: Bearer $UPSTASH_REDIS_REST_TOKEN" \
        "$UPSTASH_REDIS_REST_URL/ping")
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n-1)
    
    if [ "$http_code" = "200" ]; then
        echo "✓ REST API connection successful!"
        echo "  Response: $body"
    else
        echo "✗ REST API connection failed!"
        echo "  HTTP Code: $http_code"
        echo "  Response: $body"
        exit 1
    fi
else
    echo "⚠ UPSTASH_REDIS_REST_URL not configured"
fi

echo ""
echo "=========================================="
echo "Redis Connection Test Complete"
echo "=========================================="
