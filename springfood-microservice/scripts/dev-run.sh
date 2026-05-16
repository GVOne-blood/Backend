#!/bin/sh
# ============================================================================
# Helper chạy 1 module Spring Boot ở chế độ dev:
#   1. install các module dependency local (-am ... install -DskipTests)
#   2. chạy spring-boot:run CHỈ trên module đích (không -am, không -pl reactor)
#
# Tránh lỗi "Unable to find a suitable main class" khi Maven cố chạy
# spring-boot:run trên parent POM (packaging=pom, không có main class).
# ============================================================================
set -eu

MODULE="${1:?missing module name}"

cd /workspace

echo ">>> [$MODULE] installing local dependencies (skip tests)"
mvn -B -ntp -DskipTests -pl "$MODULE" -am install

echo ">>> [$MODULE] starting Spring Boot"
exec mvn -B -ntp -pl "$MODULE" spring-boot:run -Dspring-boot.run.fork=false
