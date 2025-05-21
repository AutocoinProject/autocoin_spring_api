# AutoCoin Spring API 배포 체크리스트

## 1. 프로젝트 빌드
- [ ] Gradle 빌드 실행: `./gradlew clean bootJar -x test`
- [ ] JAR 파일이 `build/libs` 디렉토리에 생성되었는지 확인

## 2. 환경 변수 준비
- [ ] `setup_env.sh` 스크립트에서 실제 환경에 맞는 값으로 변수 업데이트

## 3. 서버 준비
- [ ] 서버에 필요한 디렉토리 구조 생성 
  - `~/app` - 애플리케이션 파일
  - `~/logs/autocoin` - 로그 파일

## 4. 파일 업로드
- [ ] JAR 파일을 서버의 `~/app` 디렉토리로 업로드
- [ ] `deploy.sh` 및 `setup_env.sh` 스크립트를 서버의 적절한 위치에 업로드

## 5. 배포 실행
- [ ] `setup_env.sh` 스크립트 실행하여 환경 변수 설정: `chmod +x setup_env.sh && ./setup_env.sh`
- [ ] `deploy.sh` 스크립트 실행하여 애플리케이션 배포: `chmod +x deploy.sh && ./deploy.sh`

## 6. 확인
- [ ] 애플리케이션이 실행 중인지 확인: `ps aux | grep java`
- [ ] 로그 확인: `tail -f ~/logs/autocoin/application.log`
- [ ] 건강 체크 확인: `curl http://localhost:8080/actuator/health`
- [ ] 기본 API 엔드포인트 확인 (있는 경우)

## 문제 해결
- [ ] 로그 확인
- [ ] 환경 변수가 올바르게 설정되었는지 확인
- [ ] 네트워크 포트 및 방화벽 설정 확인
- [ ] 데이터베이스 연결 확인
