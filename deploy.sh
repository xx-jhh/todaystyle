#!/bin/bash
set -euo pipefail
# EC2에서 실행: GHCR의 최신 이미지를 받아 todaystyle-docker.service를 재시작한다.
# GitHub Actions는 테스트+이미지 빌드/push까지만 자동화하고, 운영 서버 반영은
# 이 스크립트를 사람이 직접 실행하는 수동 단계로 남겨뒀다(실제 서비스 안전을 위해).
echo "[1/2] 최신 이미지 pull..."
sudo docker pull ghcr.io/xx-jhh/todaystyle:latest

echo "[2/2] 서비스 재시작..."
sudo systemctl restart todaystyle-docker.service

# 앱 기동(마이그레이션 포함)까지 보통 15초 안팎 걸려서 여유 있게 기다린 뒤 확인한다.
sleep 20
sudo systemctl is-active todaystyle-docker.service
curl -s -o /dev/null -w 'health check: HTTP %{http_code}\n' http://localhost:8080/
