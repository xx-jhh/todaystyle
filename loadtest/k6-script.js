// todaystyle 부하테스트 스크립트.
//
// 로그인/회원가입은 RateLimiter가 IP+경로당 분당 5회로 막아뒀기 때문에(브루트포스 방지용,
// security/RateLimiter.java 참고) 이 스크립트는 setup()에서 딱 한 번만 회원가입하고,
// 실제 부하(다수 VU 반복)는 rate limit이 없는 조회 API(GET /api/ootd, GET
// /api/recommendations/combos)에만 건다. Cloudinary/Gemini 무료 API 호출도 setup()에서
// 몇 장만 올려 데이터를 미리 만들어두고, 반복 구간에서는 절대 호출하지 않는다.
//
// 실행 (반드시 로컬/테스트 환경에다 돌릴 것 — 운영 서버에 직접 쏘지 말 것):
//   ./gradlew bootRun 로 백엔드를 띄운 뒤,
//   K6_WEB_DASHBOARD=true K6_WEB_DASHBOARD_EXPORT=loadtest/report.html \
//     k6 run loadtest/k6-script.js
// 실행 중 http://127.0.0.1:5665 에서 실시간 그래프를 볼 수 있고, 끝나면
// loadtest/report.html 로 정적 리포트가 남는다.
//
// 대상 서버를 바꾸려면: k6 run -e BASE_URL=http://localhost:8080 loadtest/k6-script.js

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const SAMPLE_IMAGE = open('./fixtures/sample.png', 'b');

export const options = {
  scenarios: {
    read_endpoints: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '20s', target: 10 }, // 서서히 10명까지 늘림
        { duration: '40s', target: 10 }, // 10명으로 버팀
        { duration: '20s', target: 30 }, // 30명까지 스파이크
        { duration: '20s', target: 0 },  // 정리
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],     // 실패율 1% 미만
    http_req_duration: ['p(95)<500'],   // 95%가 500ms 안에 응답
  },
};

/** VU가 시작되기 전 딱 한 번만 실행 — 회원가입 + 추천에 쓸 OOTD/옷 데이터 시딩. */
export function setup() {
  const email = `loadtest_${Date.now()}@todaystyle.test`;
  const signupRes = http.post(
    `${BASE_URL}/api/auth/signup`,
    JSON.stringify({ email, password: 'loadtest1234', nickname: 'loadtest' }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  check(signupRes, { 'signup 201': (r) => r.status === 201 });
  const token = signupRes.json('accessToken');
  const authHeaders = { headers: { Authorization: `Bearer ${token}` } };

  // 서로 다른 날짜로 OOTD 4장을 올리고, 각각 상의/하의를 번갈아 수동 등록해서
  // 추천 API(원피스×아우터, 상의×하의)가 실제로 조합을 계산할 데이터를 만든다.
  const categories = ['TOP', 'BOTTOM', 'TOP', 'BOTTOM'];
  const colors = ['#FF0000', '#0000FF', '#00FF00', '#FFFF00'];
  for (let i = 0; i < 4; i++) {
    const recordDate = daysAgo(i);
    const uploadRes = http.post(
      `${BASE_URL}/api/ootd`,
      {
        recordDate,
        image: http.file(SAMPLE_IMAGE, 'sample.png', 'image/png'),
      },
      authHeaders,
    );
    check(uploadRes, { 'ootd upload 201': (r) => r.status === 201 });
    const ootdId = uploadRes.json('id');

    http.post(
      `${BASE_URL}/api/ootd/${ootdId}/items`,
      JSON.stringify({ category: categories[i], color: colors[i], fit: 'REGULAR' }),
      { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` } },
    );
  }

  return { token };
}

/** 각 VU가 반복 실행하는 본 부하 구간 — DB 읽기 + 추천 점수 계산(CPU) 위주. */
export default function (data) {
  const authHeaders = { headers: { Authorization: `Bearer ${data.token}` } };

  const listRes = http.get(`${BASE_URL}/api/ootd?page=0&size=20`, authHeaders);
  check(listRes, { 'ootd list 200': (r) => r.status === 200 });

  const comboRes = http.get(`${BASE_URL}/api/recommendations/combos?limit=10`, authHeaders);
  check(comboRes, { 'combos 200': (r) => r.status === 200 });

  sleep(1);
}

function daysAgo(n) {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return d.toISOString().slice(0, 10);
}
