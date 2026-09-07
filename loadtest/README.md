# 부하테스트 + 모니터링

로컬에서 부하테스트를 돌리면서, Grafana로 서버 내부 상태(응답시간·에러율·DB 커넥션·JVM)를
실시간 그래프로 보는 구성입니다. **운영(EC2) 서버에는 절대 직접 쏘지 마세요.**

## 1. 준비

```bash
brew install k6   # 이미 설치돼 있으면 생략
```

## 2. 전체 스택(앱+DB+Prometheus+Grafana) 띄우기

```bash
set -a; . ./.env; set +a
docker compose up -d --build
```

- 앱: http://localhost:8080
- Grafana: http://localhost:3000 (로그인 없이 바로 보임 — 로컬 전용 익명 접근 설정)
- Prometheus: http://localhost:9091
- 앱 메트릭 원본: http://localhost:9090/actuator/prometheus

Grafana에 접속하면 "todaystyle" 폴더 안에 "todaystyle - app metrics" 대시보드가 이미
만들어져 있습니다(자동 프로비저닝, 수동 설정 불필요).

## 3. 부하테스트 실행

```bash
K6_WEB_DASHBOARD=true K6_WEB_DASHBOARD_EXPORT=loadtest/report.html \
  k6 run -e BASE_URL=http://localhost:8080 loadtest/k6-script.js
```

- 실행 중 http://127.0.0.1:5665 에서 k6 자체 실시간 그래프(요청수/응답시간)를 볼 수 있습니다.
- 동시에 Grafana(http://localhost:3000)를 열어두면 그 부하가 서버 안에서 어떤 영향을
  주는지(HikariCP 커넥션 소진, JVM 힙 증가 등) 같이 볼 수 있습니다.
- 끝나면 `loadtest/report.html`에 정적 리포트가 남습니다(커밋 대상 아님, gitignore됨).

`k6-script.js` 안에서 VU(가상 사용자) 수/증가 속도는 `options.scenarios.read_endpoints.stages`
에서 조절하세요. 기본값은 0→10명(20초)→10명 유지(40초)→30명 스파이크(20초)→0(20초)입니다.

## 4. 왜 이렇게 설계했는지

- **로그인/회원가입은 반복 부하에 안 씁니다.** `RateLimitInterceptor`가 IP+경로당 분당
  5회로 막아두고 있어서, 그대로 반복 요청하면 실제 부하가 아니라 429만 재게 됩니다.
  그래서 `setup()`에서 딱 한 번만 회원가입하고, 실제 부하는 인증만 필요한 조회
  API(`GET /api/ootd`, `GET /api/recommendations/combos`)에만 겁니다.
- **Cloudinary/Gemini API도 반복 호출 안 합니다.** 둘 다 무료 티어라 한도가 있어서,
  `setup()`에서 OOTD 4장 + 옷 아이템 몇 개만 미리 만들어두고(1회성), 반복 구간에서는
  DB 읽기 + 추천 점수 계산(CPU)만 겁니다.
- **메트릭은 별도 포트(9090)**로 분리했습니다(`application.yaml`의
  `management.server.port`). 메인 앱 포트(8080, nginx가 붙는 포트)와 완전히 분리돼 있어서,
  실수로 `/actuator/**`가 공개 인터넷에 노출될 걱정이 없습니다.

## 5. 정리

```bash
docker compose down        # 컨테이너만 정리 (DB 데이터는 남음)
docker compose down -v     # DB 데이터까지 완전히 지우고 싶을 때
```
