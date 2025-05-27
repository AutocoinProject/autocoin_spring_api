# AutoCoin Spring API 배포 가이드

이 가이드는 AutoCoin Spring API를 서버에 배포하고 서버 재시작 후에도 자동으로 실행되도록 설정하는 방법을 설명합니다.

## 준비 사항

- 리눅스 서버 환경 (Ubuntu, CentOS, Amazon Linux 등)
- JDK 17 이상 설치
- MySQL 데이터베이스 설정 완료
- sudo 권한 (systemd 서비스 설정용)

## 배포 절차

### 1. 프로젝트 빌드

로컬 개발 환경에서 다음 명령을 실행하여 프로젝트를 빌드합니다:

```bash
./gradlew clean bootJar -x test
```

이 명령은 `build/libs` 디렉토리에 실행 가능한 JAR 파일을 생성합니다.

### 2. 서버에 파일 업로드

다음 파일들을 서버에 업로드합니다:

- 빌드된 JAR 파일 (`build/libs/` 디렉토리에서)
- 배포 스크립트 (`scripts/deploy.sh`)
- 환경 변수 스크립트 (`scripts/setup_env.sh`)
- systemd 설정 스크립트 (`scripts/setup_systemd.sh`)

모든 파일을 서버의 같은 디렉토리에 업로드하세요.

### 3. 환경 변수 설정

`setup_env.sh` 스크립트를 편집하여 실제 환경에 맞는 값으로 환경 변수를 설정합니다:

```bash
nano setup_env.sh
# 환경 변수 값 수정 후 저장
```

### 4. 배포 실행

배포 스크립트를 실행하여 애플리케이션을 배포합니다:

```bash
chmod +x deploy.sh
./deploy.sh
```

이 스크립트는 다음 작업을 수행합니다:

1. 필요한 디렉토리 생성
2. 환경 변수 설정
3. JAR 파일 확인 및 복사
4. systemd 서비스 설정
5. 애플리케이션 시작 및 상태 확인

### 5. 배포 확인

배포가 완료되면 다음 명령으로 애플리케이션 상태를 확인할 수 있습니다:

```bash
sudo systemctl status autocoin
```

로그 확인:
```bash
sudo journalctl -u autocoin -f
```

## 서비스 관리

애플리케이션이 systemd 서비스로 설정되었기 때문에 다음 명령으로 서비스를 관리할 수 있습니다:

- 서비스 시작: `sudo systemctl start autocoin`
- 서비스 중지: `sudo systemctl stop autocoin`
- 서비스 재시작: `sudo systemctl restart autocoin`
- 서비스 상태 확인: `sudo systemctl status autocoin`

## 자동 시작 설정

이 설정으로 서버가 재시작되어도 AutoCoin Spring API가 자동으로 시작됩니다.
배포 스크립트는 이미 `systemctl enable` 명령을 통해 자동 시작을 설정하고 있습니다.

## 문제 해결

문제가 발생한 경우 다음을 확인하세요:

1. systemd 로그 확인: `sudo journalctl -u autocoin -f`
2. 환경 변수 설정 확인: `cat ~/app/.env`
3. JAR 파일 존재 여부: `ls -la ~/app/`
4. Java 버전 확인: `java -version`
5. 데이터베이스 연결 확인

## 참고

이 배포 방식은 단일 서버 환경을 위한 것입니다. 보다 복잡한 배포 요구사항이 있는 경우 Docker, Kubernetes 또는 다른 배포 도구를 고려하세요.
