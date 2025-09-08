FROM openjdk:17-jdk-alpine AS stage1

WORKDIR /app

COPY gradle gradle
COPY src src
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew
RUN ./gradlew bootJar

# 두번째 스테이지
# 이미지 경량화를 위해 스테이지 분리
FROM openjdk:17-jdk-alpine AS stage2

WORKDIR /app

# stage1의 jar파일을 stage2로 copy
COPY --from=stage1 /app/build/libs/*.jar app.jar

# 실행: CMD 또는 ENTRYPOINT를 통해 컨테이너 실행
ENTRYPOINT [ "java", "-jar", "app.jar" ]
