# 서비스 기획 정리 (OOTD 기반 코디 추천 서비스)

## 1. 서비스 개요
사용자가 매일 자신의 OOTD(오늘의 옷차림)를 사진으로 기록하면, 쌓인 데이터를 기반으로 사용자가 가진 옷들 중 실제로 입어보지 않은 조합을 추천해주는 패션 서비스.

## 2. 핵심 사용자 흐름

1. 회원가입
   - 신체 정보 입력: 키, 몸무게, 허리 인치
   - 체형 선택: 여러 체형 이미지 보기를 제시하고, 사용자가 자신과 가장 가까운 체형 이미지를 선택
   - 선호 스타일 선택: 캐주얼, 아메카지, 스트릿 등 카테고리 중 선택
2. 데일리 OOTD 업로드
   - 사용자가 매일 자신의 착장 사진을 찍어서 업로드
   - 업로드가 누적되며 사용자의 의류 데이터베이스가 쌓임
3. 코디 조합 추천 (핵심 기능)
   - 서로 다른 날짜에 촬영된 아이템들 중, 실제로 함께 입은 적 없는 조합을 매칭해서 추천
   - 예: 8월 1일에 입은 상의 + 8월 3일에 입은 하의가 잘 어울린다고 알려주는 방식
4. 스타일 기반 추천
   - 가입 시 선택한 선호 스타일을 바탕으로, 사용자가 가진 옷과 어울릴 만한 다른 의류 추천
5. 데일리 날씨 기반 코디 팁
   - 오늘의 날씨 정보 제공
   - 날씨에 맞는 간단한 착장 가이드 제공 (레이어링, 기온별 팁 등)

## 3. 배포/플랫폼 방향

- 최소 목표: 웹앱(PWA)으로 우선 출시, 지인들에게 링크 공유로 무료 배포
- 추후 확장: 반응이 좋으면 Capacitor로 웹 코드를 감싸서 iOS/Android 네이티브 앱으로 전환 (코드 재작성 최소화하는 경로로 처음부터 설계)
- iOS 정식 배포(TestFlight/App Store)는 Apple Developer Program 연 $99 필요, Android는 Google Play Console 최초 1회 $25. 지금 단계에서는 비용 없이 PWA로 시작하는 것으로 결정.

## 4. 기술 스택 (확정)

- 프론트엔드: TypeScript + React (추후 PWA → Capacitor 전환 고려)
- 백엔드: Java 21 + Spring Boot 4.1.0 (Gradle) — 개발자 본인이 Java에 익숙하고 추후 직접 코드 리뷰가 필요해 Java로 확정 (Kotlin은 검토 후 배제)
- 데이터베이스: MySQL (로컬 개발 환경, DB명 `todaystyle`)
- 백엔드 의존성: Spring Web, Spring Data JPA, Spring Security, Validation, Spring Boot DevTools, MySQL Connector/J, Lombok
- Lombok: 엔티티에 `@Getter @Setter @NoArgsConstructor @AllArgsConstructor` 등으로 보일러플레이트 축소, DTO는 Java record 사용 권장
- IDE: IntelliJ IDEA (Ultimate)
- 프로젝트명 / 그룹·패키지: `todaystyle` / `com.example.todaystyle`

## 5. 현재 진행 상태

- IntelliJ에서 Spring Boot 프로젝트 생성 완료 (프로젝트명 `todaystyle`, Java 기준)
- 로컬 MySQL 설치 확인 및 서비스 실행 확인 (`brew services`)
- `todaystyle` 데이터베이스 생성 완료
- `application.yml`에 DB 연결 정보(url, username, password) 설정 완료, 앱 실행 시 MySQL 정상 연결 확인(HikariPool)
- Gradle 의존성 구성 완료 (Spring Web, Spring Data JPA, Spring Security, Validation, DevTools, MySQL Connector/J, Lombok)
- 회원가입/로그인 기능 구현 예정 (Spring Security 기본 포함 상태 — 현재는 임시 자동 생성 로그인 방식으로 동작 중이며, 추후 실제 회원가입/로그인 로직으로 교체 예정)
- 아직 도메인 엔티티(옷 아이템, OOTD 기록, 스타일 선호도 등) 및 API 엔드포인트는 미구현 — 다음 단계로 진행 예정
- API 동작 확인용으로 Swagger UI(springdoc-openapi) 또는 IntelliJ HTTP Client 사용 권장

## 6. 향후 고려사항 / 결정된 방향

- 옷 이미지 인식(상의/하의/색상 분류): 기성 API(Google Cloud Vision, api4ai Fashion API 등) 활용 또는 자체 모델(YOLO 계열) 학습 가능. 초기에는 API 연동으로 시작하는 것을 권장.
- 조합 추천 로직: 초기엔 색상 이론 기반 규칙으로 시작, 데이터가 쌓이면 임베딩 기반 유사도 매칭으로 고도화.
- 브랜드 의류 이미지(예: COS) 사용 관련: 브랜드 상품 사진은 저작권 보호 대상이므로 직접 크롤링/저장은 지양. 사용자가 직접 업로드한 사진만 저장하고, 브랜드 상품 추천이 필요하면 이미지 저장 없이 공식 링크로 연결하는 방식 권장. 모델 학습용 이미지가 필요하면 DeepFashion 같은 공개 연구용 데이터셋 활용.
