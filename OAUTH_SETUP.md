# OAuth2 설정 가이드

## Kakao OAuth 설정

카카오 로그인을 사용하기 위해서는 다음과 같이 설정해야 합니다:

### 1. Kakao 개발자 콘솔 설정

1. [Kakao Developers](https://developers.kakao.com)에 로그인
2. 애플리케이션 선택 또는 새 애플리케이션 생성
3. **앱 설정 > 플랫폼 > Web 플랫폼**에서 사이트 도메인 등록:
   - `http://localhost:5000`
4. **앱 설정 > 카카오 로그인 > Redirect URI** 등록:
   - `http://localhost:5000/login/oauth2/code/kakao`
5. **앱 설정 > 카카오 로그인 > 동의항목** 설정:
   - 필수: 닉네임
   - 선택사항: 이메일 (현재 요청하지 않음)

### 2. application.yml 설정

현재 이 프로젝트에서는 다음과 같이 설정되어 있습니다:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          kakao:
            client-id: 221d0510edf0725b2704e0d370859dea
            client-secret: ${KAKAO_CLIENT_SECRET:your-kakao-client-secret}
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            client-authentication-method: client_secret_post
            authorization-grant-type: authorization_code
            scope:
              - profile_nickname
              # - account_email  # 이메일 정보는 요청하지 않음
            client-name: Kakao
```

### 3. 카카오 로그인 URL

프론트엔드에서 카카오 로그인을 요청할 때는 다음 URL을 사용합니다:
```
http://localhost:5000/oauth2/authorization/kakao
```

### 4. 주의사항

- **Redirect URI 일치**: Kakao 개발자 콘솔에 등록된 Redirect URI와 애플리케이션에서 설정한 URI가 정확히 일치해야 합니다.
- **클라이언트 ID 설정**: application.yml의 client-id는 Kakao 개발자 콘솔의 REST API 키와 일치해야 합니다.
- **환경변수 설정**: 프로덕션 환경에서는 client-secret을 환경변수로 설정해야 합니다.

### 5. 오류 해결

만약 "등록하지 않은 Redirect URI를 사용해 인가 코드를 요청했습니다" 오류가 발생한다면:

1. Kakao 개발자 콘솔에 등록된 Redirect URI 확인
2. 애플리케이션의 redirect-uri 설정 확인
3. Kakao 개발자 콘솔에 정확한 URI 등록: `http://localhost:5000/login/oauth2/code/kakao`
