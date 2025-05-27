# Git Secret을 사용한 환경 변수 자동 업데이트 가이드

이 문서는 Git Secret을 사용하여 프로젝트의 환경 변수(.env 파일)를 안전하게 관리하고, CI/CD 파이프라인을 통해 자동으로 업데이트하는 방법을 설명합니다.

## 1. 초기 설정

### Git Secret 설치

각 개발자는 로컬 환경에 Git Secret을 설치해야 합니다:

**macOS:**
```bash
brew install git-secret
```

**Ubuntu/Debian:**
```bash
echo "deb https://gitsecret.jfrog.io/artifactory/git-secret-deb git-secret main" | sudo tee /etc/apt/sources.list.d/git-secret.list
wget -qO - https://gitsecret.jfrog.io/artifactory/api/gpg/key/public | sudo apt-key add -
sudo apt-get update
sudo apt-get install -y git-secret
```

**Windows:**
Git Bash 또는 WSL을 통해 설치합니다. 자세한 내용은 [git-secret.io/installation](https://git-secret.io/installation)을 참조하세요.

### GPG 키 생성 및 설정

1. 프로젝트 초기 설정을 위해 다음 스크립트를 실행합니다:
   ```bash
   bash scripts/git-secret/setup-git-secret.sh
   ```

2. 이 스크립트는:
   - Git Secret을 초기화합니다.
   - 배포용 GPG 키를 생성합니다.
   - 공개 키와 비밀 키를 `scripts/git-secret/keys/` 디렉토리에 저장합니다.

3. 팀원들에게 공개 키를 공유합니다:
   ```bash
   # 공개 키 가져오기
   gpg --import scripts/git-secret/keys/deploy-public-key.asc
   ```

4. CI/CD 시스템에 비밀 키를 설정합니다:
   - GitHub Repository Secrets에 다음 값을 추가합니다:
     - `GPG_PRIVATE_KEY`: 비밀 키 내용 (`scripts/git-secret/keys/deploy-private-key.asc`)
     - `GPG_PASSPHRASE`: 비밀 키 암호 (기본값: "autocoin-deploy-passphrase")

## 2. 환경 변수 암호화 및 저장

로컬에서 .env 파일을 변경한 후 암호화하여 저장소에 푸시합니다:

```bash
# .env 파일 변경 후
git secret add .env  # 처음 추가하는 경우에만 필요
git secret hide      # .env 파일 암호화
git add .env.secret
git commit -m "Update environment variables"
git push
```

## 3. 자동 배포 프로세스

### 전체 배포

소스 코드 또는 환경 변수가 변경되었을 때 자동으로 전체 배포가 실행됩니다:

1. main/master 브랜치로 푸시하면 GitHub Actions 워크플로우가 시작됩니다.
2. 워크플로우는 .env.secret 파일을 복호화합니다.
3. 애플리케이션을 빌드합니다.
4. 빌드된 JAR 파일과 .env 파일을 서버에 복사합니다.
5. 서비스를 재시작합니다.

### 환경 변수만 업데이트

코드 변경 없이 환경 변수만 업데이트하려면:

1. GitHub Actions 대시보드에서 "AutoCoin API Deploy" 워크플로우를 수동으로 실행합니다.
2. Deployment type으로 "env_only"를 선택합니다.
3. 워크플로우는 .env 파일만 업데이트하고 서비스를 재시작합니다.

## 4. 새 팀원 추가

새 팀원이 프로젝트에 참여할 때:

1. 팀원은 GPG 키를 생성합니다:
   ```bash
   gpg --gen-key
   ```

2. 팀원은 자신의 공개 키를 내보냅니다:
   ```bash
   gpg --export --armor your@email.com > my-public-key.asc
   ```

3. 기존 팀원은 새 팀원의 공개 키를 가져오고 git-secret에 추가합니다:
   ```bash
   gpg --import my-public-key.asc
   git secret tell your@email.com
   git secret reveal    # 현재 .env 파일 복호화
   git secret hide      # 모든 사용자를 위해 다시 암호화
   git add .env.secret
   git commit -m "Add new team member"
   git push
   ```

## 5. 문제 해결

### 복호화 오류

복호화 오류가 발생하면:

1. GPG 키가 올바르게 가져와졌는지 확인합니다:
   ```bash
   gpg --list-keys
   ```

2. git-secret이 키를 알고 있는지 확인합니다:
   ```bash
   git secret whoknows
   ```

3. GPG 키를 새로 고치고 다시 시도합니다:
   ```bash
   git secret reveal -f
   ```

### 환경 변수 충돌

팀원들이 동시에 .env 파일을 수정하면 충돌이 발생할 수 있습니다. 이 경우:

1. 가장 최신 코드를 가져옵니다:
   ```bash
   git pull
   ```

2. .env.secret 파일을 복호화합니다:
   ```bash
   git secret reveal -f
   ```

3. 수동으로 충돌을 해결하고 다시 암호화합니다:
   ```bash
   # .env 파일 수정 후
   git secret hide
   git add .env.secret
   git commit -m "Resolve env conflict"
   git push
   ```

## 6. 보안 고려사항

- 복호화된 .env 파일을 절대 커밋하지 마세요.
- GPG 비밀 키를 안전하게 보관하세요.
- GitHub Secrets의 환경 변수 값은 주기적으로 갱신하세요.
- 팀원이 퇴사하면 해당 사용자의 GPG 키를 제거하고 모든 비밀을 다시 암호화하세요:
  ```bash
  git secret killperson ex-teammate@email.com
  git secret hide
  git add .env.secret
  git commit -m "Remove team member"
  git push
  ```
