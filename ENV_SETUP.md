# 환경 설정 가이드

이 프로젝트는 다양한 환경에서 실행할 수 있도록 설계되었습니다. 각 환경별 설정 방법을 안내합니다.

## 환경별 프로파일

1. **local**: 로컬 개발 환경 (로컬 MySQL + AWS S3)
2. **dev**: 개발 서버 환경 (AWS RDS + AWS S3)
3. **prod**: 운영 서버 환경 (운영 AWS RDS + AWS S3)

## 환경 변수 설정 방법

### 로컬 개발 환경 설정

1. `.env.local.template` 파일을 `.env`로 복사
2. 필요한 값들을 실제 정보로 수정
3. 로컬 MySQL 서버가 실행 중인지 확인
4. 애플리케이션 실행 시 `-Dspring.profiles.active=local` 옵션 추가

```bash
# 예시
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 개발 서버 환경 설정

1. `.env.dev.template` 파일을 `.env`로 복사
2. AWS RDS와 S3 정보를 실제 값으로 수정
3. 애플리케이션 실행 시 `-Dspring.profiles.active=dev` 옵션 추가

```bash
# 예시
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 운영 서버 환경 설정

1. `.env.prod.template` 파일을 `.env`로 복사하거나, 환경 변수로 직접 설정
2. 운영 AWS RDS와 S3 정보를 실제 값으로 수정
3. 애플리케이션 실행 시 `-Dspring.profiles.active=prod` 옵션 추가

```bash
# 예시
./gradlew bootRun --args='--spring.profiles.active=prod'
```

또는 Docker 환경에서 환경 변수 설정:

```yaml
# docker-compose.yml 예시
version: '3'
services:
  app:
    image: autocoin-spring-api:latest
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - PROD_RDS_HOSTNAME=your-prod-db-hostname
      - PROD_RDS_PORT=3306
      - PROD_RDS_DB_NAME=autocoin_db
      - PROD_RDS_USERNAME=your-db-username
      - PROD_RDS_PASSWORD=your-db-password
      - PROD_S3_BUCKET=your-prod-s3-bucket
      - PROD_AWS_ACCESS_KEY=your-prod-aws-access-key
      - PROD_AWS_SECRET_KEY=your-prod-aws-secret-key
```

## 데이터베이스 마이그레이션

- 로컬 및 개발 환경: `ddl-auto: update` 설정으로 자동 스키마 업데이트
- 운영 환경: `ddl-auto: validate` 설정으로 스키마 변경 없음 (실제 변경은 별도 DB 마이그레이션 도구 사용 권장)

## 주의사항

1. `.env` 파일은 버전 관리에 포함하지 마세요. (`.gitignore`에 추가)
2. 실제 비밀번호, API 키 등은 안전하게 관리하세요.
3. 운영 환경에서는 환경 변수를 사용하는 것이 권장됩니다.
