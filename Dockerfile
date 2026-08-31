# ---- 빌드 스테이지 ----
# JDK와 Node.js가 둘 다 필요하다: build.gradle의 bootJar가 프론트엔드(npm run build)까지
# 함께 실행해서 결과물을 jar 안 static 리소스로 포함시키기 때문이다(로컬 개발과 동일한 방식).
FROM eclipse-temurin:21-jdk-jammy AS build

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates gnupg \
    && curl -fsSL https://deb.nodesource.com/setup_22.x | bash - \
    && apt-get install -y --no-install-recommends nodejs \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew

COPY src ./src
COPY frontend ./frontend

# 이미지 빌드 시점에는 테스트를 돌리지 않는다 — 테스트는 CI(GitHub Actions)에서
# 이미지 빌드 전에 별도로 실행해 이미 통과가 보장된 커밋만 여기까지 온다.
RUN ./gradlew bootJar -x test --no-daemon

# ---- 실행 스테이지 ----
# 빌드 도구·Node·소스 없이 JRE + jar만 남겨서 이미지를 가볍게 유지한다.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
