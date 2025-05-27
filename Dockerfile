# Eclipse Temurin JDK 17을 기반으로 하는 이미지 사용
FROM eclipse-temurin:17-jre-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 시스템 업데이트 및 필요한 패키지 설치
RUN apk update && apk add --no-cache \
    curl

# 애플리케이션 JAR 파일을 컨테이너로 복사
COPY build/libs/*.jar app.jar

# 환경 변수 설정
ENV SPRING_PROFILES_ACTIVE=prod
ENV TZ=Asia/Seoul

# 포트 8080 노출
EXPOSE 8080

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "/app/app.jar"]
