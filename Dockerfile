# 1단계: 빌드 스테이지
FROM public.ecr.aws/amazoncorretto/amazoncorretto:17 AS build
WORKDIR /app
# 프로젝트 정보를 환경 변수로 설정
ENV PROJECT_NAME=otboo
ENV PROJECT_VERSION=0.0.1-SNAPSHOT
# Gradle Wrapper, 설정, 의존성 캐시
COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY gradle/ gradle/
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon || true
# 소스 복사 및 빌드
COPY src/ src/
RUN ./gradlew clean bootJar --no-daemon

# 2단계: 런타임 스테이지 (경량)
FROM public.ecr.aws/amazoncorretto/amazoncorretto:17-alpine3.22-jdk
WORKDIR /app
# curl 설치
RUN apk add --no-cache curl
# 프로젝트 정보를 환경 변수로 설정
ENV PROJECT_NAME=otboo
ENV PROJECT_VERSION=0.0.1-SNAPSHOT

COPY --from=build /app/build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
