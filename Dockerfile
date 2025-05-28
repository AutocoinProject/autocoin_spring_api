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

# 헬스체크 추가 (Spring Boot 시작 시간을 고려하여 설정)
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=5 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 디버깅을 위한 스크립트 생성
RUN echo '#!/bin/sh' > /app/start.sh && \
    echo 'echo "=== Environment Variables ==="' >> /app/start.sh && \
    echo 'echo "DB_URL: $DB_URL"' >> /app/start.sh && \
    echo 'echo "DB_USERNAME: $DB_USERNAME"' >> /app/start.sh && \
    echo 'echo "SPRING_PROFILES_ACTIVE: $SPRING_PROFILES_ACTIVE"' >> /app/start.sh && \
    echo 'echo "============================"' >> /app/start.sh && \
    echo 'exec java -Xms512m -Xmx1024m -XX:+UseG1GC -jar /app/app.jar' >> /app/start.sh && \
    chmod +x /app/start.sh

# 애플리케이션 실행
ENTRYPOINT ["/app/start.sh"]
