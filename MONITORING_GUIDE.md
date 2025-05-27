# Autocoin API 모니터링 설정 가이드

이 가이드는 Autocoin Spring Boot API에 대한 포괄적인 모니터링 시스템을 설정하는 방법을 안내합니다.

## 🏗️ 아키텍처 개요

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Spring Boot   │───▶│   Prometheus    │───▶│     Grafana     │
│      API        │    │   (메트릭 수집)  │    │   (시각화)      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                        │                        │
         │                        ▼                        │
         │              ┌─────────────────┐                │
         │              │  AlertManager   │                │
         │              │   (알림 관리)   │                │
         │              └─────────────────┘                │
         │                        │                        │
         ▼                        ▼                        ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Sentry      │    │     Slack       │    │     Email       │
│  (에러 추적)    │    │   (알림 전송)   │    │   (알림 전송)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🚀 빠른 시작

### 1. 환경 설정

```bash
# .env 파일에 필요한 환경 변수 설정
SENTRY_DSN=your-sentry-dsn-here
SLACK_WEBHOOK_URL=your-slack-webhook-url-here
```

### 2. 모니터링 환경 시작

```bash
# Linux/Mac
./scripts/start-monitoring.sh

# Windows
scripts\start-monitoring.bat
```

### 3. Spring Boot 애플리케이션 실행

```bash
./gradlew bootRun
```

## 📊 모니터링 대시보드 접속

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3001 (admin/autocoin123!)
- **AlertManager**: http://localhost:9093

## 🔍 주요 기능

### 1. Spring Boot Actuator

**활성화된 엔드포인트:**
- `/actuator/health` - 애플리케이션 상태 확인
- `/actuator/metrics` - 메트릭 조회
- `/actuator/prometheus` - Prometheus 형식 메트릭
- `/actuator/env` - 환경 변수 정보
- `/actuator/threaddump` - 스레드 덤프
- `/actuator/heapdump` - 힙 덤프

**주요 설정:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
      base-path: /actuator
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
```

### 2. Prometheus 메트릭 수집

**수집 중인 메트릭:**
- HTTP 요청 수/응답 시간
- JVM 메모리/GC/스레드
- 데이터베이스 연결 풀
- 커스텀 비즈니스 메트릭

**커스텀 메트릭 예시:**
```java
// 로그인 성공/실패
customMetricsService.recordLoginSuccess("google");
customMetricsService.recordLoginFailure("kakao", "invalid_token");

// API 요청 추적
customMetricsService.recordUpbitApiRequest("/v1/accounts");

// 거래 실행 기록
customMetricsService.recordTradeExecution("KRW-BTC", "buy", true);
```

### 3. Sentry 오류 추적

**설정된 기능:**
- 자동 예외 캡처
- 사용자 컨텍스트 추가
- 요청 정보 필터링 (개인정보 보호)
- 성능 트레이싱

**사용 예시:**
```java
// 수동으로 예외 전송
Sentry.captureException(exception);

// 사용자 정보 설정
Sentry.setUser(User.fromMap(Map.of("id", "123", "username", "user")));

// 태그와 컨텍스트 추가
Sentry.setTag("feature", "trading");
Sentry.setExtra("request_id", requestId);
```

### 4. Grafana 대시보드

**포함된 패널:**
- HTTP 요청 속도
- 응답 시간 (95th percentile)
- JVM 메모리 사용량
- 데이터베이스 연결 풀
- 커스텀 비즈니스 메트릭

## 🧪 테스트 기능

API에는 모니터링 기능을 테스트할 수 있는 엔드포인트가 포함되어 있습니다:

### Sentry 알림 테스트
```bash
# 기본 예외 발생
curl -X POST "http://localhost:5000/api/monitoring/test-sentry"

# 특정 예외 타입
curl -X POST "http://localhost:5000/api/monitoring/test-sentry?exceptionType=npe"
```

### 메트릭 테스트
```bash
# 커스텀 메트릭 생성
curl -X POST "http://localhost:5000/api/monitoring/test-metrics"
```

### 성능 테스트
```bash
# CPU/메모리 부하 생성
curl -X POST "http://localhost:5000/api/monitoring/test-performance?iterations=1000"
```

### 메모리 사용량 테스트
```bash
# 메모리 할당 테스트
curl -X POST "http://localhost:5000/api/monitoring/test-memory?sizeMB=100"
```

## ⚠️ 알림 설정

### Prometheus 알림 규칙

**설정된 알림:**
- 애플리케이션 다운
- 높은 응답 시간 (>1초)
- 높은 에러율 (>5%)
- 높은 메모리 사용률 (>80%)
- 데이터베이스 연결 문제
- Upbit API 에러율 증가

### Slack 알림 설정

1. Slack에서 Incoming Webhook 생성
2. `.env` 파일에 `SLACK_WEBHOOK_URL` 설정
3. AlertManager가 자동으로 알림 전송

## 🛠️ 커스터마이징

### 새로운 메트릭 추가

1. `CustomMetricsService`에 메서드 추가:
```java
public void recordNewMetric(String value) {
    Counter.builder("autocoin.new.metric")
            .tag("type", value)
            .register(meterRegistry)
            .increment();
}
```

2. 비즈니스 로직에서 호출:
```java
@Autowired
private CustomMetricsService metricsService;

public void someBusinessMethod() {
    metricsService.recordNewMetric("success");
}
```

### 새로운 알림 규칙 추가

`monitoring/prometheus/rules/autocoin-alerts.yml`에 추가:
```yaml
- alert: NewAlert
  expr: your_metric > threshold
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "New alert triggered"
    description: "Description of the alert"
```

### 커스텀 Health Indicator 추가

```java
@Component("customHealth")
public class CustomHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // 커스텀 헬스 체크 로직
        return Health.up()
                .withDetail("custom", "OK")
                .build();
    }
}
```

## 🔧 운영 환경 설정

### 보안 고려사항

1. **Actuator 엔드포인트 제한:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
```

2. **인증 추가:**
```yaml
management:
  endpoint:
    health:
      show-details: when-authorized
```

3. **네트워크 접근 제한:**
```yaml
management:
  server:
    port: 8081  # 다른 포트 사용
```

### 성능 최적화

1. **메트릭 샘플링 조정:**
```yaml
management:
  metrics:
    export:
      prometheus:
        step: 30s  # 기본 10s에서 증가
```

2. **불필요한 메트릭 필터링:**
```java
@Bean
MeterRegistryCustomizer<MeterRegistry> configurer() {
    return registry -> registry.config()
            .meterFilter(MeterFilter.deny(id -> {
                String name = id.getName();
                return name.startsWith("unnecessary.metric");
            }));
}
```

3. **Sentry 샘플링 설정:**
```yaml
sentry:
  traces-sample-rate: 0.1  # 운영환경에서는 낮게 설정
  debug: false
```

## 📈 모니터링 베스트 프랙티스

### 1. 메트릭 네이밍 규칙

- **접두사 사용:** `autocoin.` 으로 시작
- **카테고리 구분:** `autocoin.users.`, `autocoin.trades.`
- **단위 포함:** `_total`, `_seconds`, `_bytes`

예시:
```
autocoin.users.login.success_total
autocoin.trades.execution.duration_seconds
autocoin.api.upbit.response.size_bytes
```

### 2. 알림 설정 가이드라인

- **Critical:** 즉시 대응 필요 (서비스 다운, 데이터 손실)
- **Warning:** 모니터링 필요 (성능 저하, 에러율 증가)
- **Info:** 참고용 (배포 완료, 설정 변경)

### 3. 대시보드 구성

- **개요 대시보드:** 전체 시스템 상태
- **상세 대시보드:** 특정 서비스/기능별
- **SLA 대시보드:** 서비스 수준 지표

## 🐛 트러블슈팅

### 일반적인 문제들

#### 1. Prometheus가 메트릭을 수집하지 않는 경우

**확인사항:**
- Spring Boot 애플리케이션이 실행 중인지
- `/actuator/prometheus` 엔드포인트 접근 가능한지
- Prometheus 설정에서 `host.docker.internal:5000` 사용

**해결방법:**
```bash
# 메트릭 엔드포인트 확인
curl http://localhost:5000/actuator/prometheus

# Prometheus 타겟 상태 확인
# http://localhost:9090/targets
```

#### 2. Grafana 대시보드가 데이터를 표시하지 않는 경우

**확인사항:**
- Prometheus 데이터 소스 연결 상태
- 메트릭 이름이 정확한지
- 시간 범위 설정

**해결방법:**
```bash
# Prometheus에서 메트릭 쿼리 테스트
# http://localhost:9090/graph

# Grafana 데이터 소스 테스트
# http://localhost:3001/datasources
```

#### 3. Sentry 이벤트가 전송되지 않는 경우

**확인사항:**
- `SENTRY_DSN` 환경 변수 설정
- 네트워크 연결 상태
- Sentry 프로젝트 설정

**해결방법:**
```bash
# 테스트 예외 발생
curl -X POST "http://localhost:5000/api/monitoring/test-sentry"

# 로그에서 Sentry 관련 메시지 확인
```

### 로그 확인 방법

```bash
# Spring Boot 애플리케이션 로그
tail -f logs/autocoin-api.log

# Docker 컨테이너 로그
docker-compose -f docker-compose.monitoring.yml logs -f prometheus
docker-compose -f docker-compose.monitoring.yml logs -f grafana
```

## 🔄 업데이트 및 유지보수

### 정기 작업

1. **메트릭 데이터 정리:** Prometheus는 기본 15일 후 데이터 삭제
2. **알림 규칙 검토:** 월 1회 알림 효과성 검토
3. **대시보드 업데이트:** 새로운 기능 추가 시 대시보드 갱신

### 백업

```bash
# Grafana 설정 백업
docker exec autocoin-grafana grafana-cli admin export-dashboard > backup.json

# Prometheus 설정 백업
cp monitoring/prometheus/prometheus.yml prometheus-backup.yml
```

## 📚 추가 리소스

### 공식 문서

- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer](https://micrometer.io/docs)
- [Prometheus](https://prometheus.io/docs/)
- [Grafana](https://grafana.com/docs/)
- [Sentry](https://docs.sentry.io/)

### 커뮤니티 리소스

- [Awesome Prometheus](https://github.com/roaldnefs/awesome-prometheus)
- [Grafana Dashboards](https://grafana.com/grafana/dashboards/)
- [Spring Boot Monitoring Examples](https://github.com/making/spring-boot-actuator-sample)

## 🤝 기여하기

모니터링 시스템 개선을 위한 기여를 환영합니다:

1. 새로운 메트릭 제안
2. 대시보드 개선
3. 알림 규칙 최적화
4. 문서 업데이트

## 📞 지원

문제가 발생하거나 질문이 있으시면:

1. 이슈 생성: GitHub Issues
2. 문서 확인: 이 README
3. 로그 분석: 위의 트러블슈팅 가이드 참조

---

**⚡ 중요:** 운영 환경에서는 보안과 성능을 고려하여 설정을 조정해주세요. 특히 Actuator 엔드포인트 노출과 메트릭 수집 주기를 적절히 설정하시기 바랍니다.
