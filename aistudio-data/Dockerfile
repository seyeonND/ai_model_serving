# 기본 이미지를 java로 설정
FROM bitnami/java:17

# 어플리케이션 파일을 컨테이너로 복사
COPY ./ /app/

# Gradle Wrapper 실행 권한 설정
RUN chmod +x /app//gradlew


# Gradle Wrapper를 사용하여 Spring Boot 어플리케이션 빌드
RUN ./gradlew clean build --no-daemon --refresh-dependencies --stacktrace --info > output.log 2>&1

# entrypoint.sh를 컨테이너에 복사
COPY ./entrypoint.sh /app/entrypoint.sh

# entrypoint.sh 파일에 실행 권한 부여
RUN chmod +x /app/entrypoint.sh