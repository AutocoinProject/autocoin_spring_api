# JaCoCo 테스트 커버리지 가이드

이 프로젝트는 JaCoCo(Java Code Coverage)를 사용하여 테스트 커버리지를 측정합니다. 테스트 커버리지는 코드 품질을 유지하고 버그를 줄이는 데 중요한 지표입니다.

## JaCoCo 설정 내용

`build.gradle` 파일에 다음과 같은 JaCoCo 설정이 추가되었습니다:

1. **JaCoCo 플러그인 적용**: `id 'jacoco'`
2. **JaCoCo 버전 설정**: `jacoco { toolVersion = "0.8.11" }`
3. **리포트 생성 설정**: `jacocoTestReport` 태스크
4. **커버리지 검증 설정**: `jacocoTestCoverageVerification` 태스크
5. **제외 대상 설정**: configuration, DTO, entity 등 테스트가 필요하지 않은 클래스 제외

## 테스트 커버리지 확인 방법

### 로컬 개발 환경에서 확인

```bash
# 테스트 실행 및 JaCoCo 리포트 생성
./gradlew test jacocoTestReport

# 테스트 커버리지 검증 (최소 기준 충족 여부 확인)
./gradlew jacocoTestCoverageVerification
```

생성된 HTML 리포트는 `build/reports/jacoco/test/html/index.html` 경로에서 브라우저로 확인할 수 있습니다.

### CI/CD 파이프라인에서 확인

GitHub Actions 워크플로우에 JaCoCo 테스트 커버리지 단계가 추가되었습니다. 워크플로우가 실행되면 다음 단계가 실행됩니다:

1. `Run tests with JaCoCo coverage`: 테스트 실행 및 커버리지 분석
2. `Upload JaCoCo coverage report`: 커버리지 리포트를 아티팩트로 업로드
3. `Check JaCoCo coverage`: 커버리지 검증 (미통과 시에도 워크플로우 계속 진행)

GitHub Actions 실행 후 Artifacts 탭에서 `jacoco-report`를 다운로드하여 내용을 확인할 수 있습니다.

## 커버리지 제외 대상 커스터마이징

특정 클래스나 패키지를 테스트 커버리지 측정에서 제외하려면 `build.gradle`의 다음 부분을 수정하세요:

```groovy
jacocoTestReport {
    // ...
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                "**/config/**",
                "**/dto/**",
                "**/entity/**",
                "**/AutocoinSpringApiApplication.class",
                "**/exception/**",
                "**/repository/**",
                // 추가 제외 대상
            ])
        }))
    }
}

jacocoTestCoverageVerification {
    // ...
    violationRules {
        rule {
            // ...
            excludes = [
                '*.config.*',
                '*.dto.*',
                '*.entity.*',
                '*.exception.*',
                '*.AutocoinSpringApiApplication',
                // 추가 제외 대상
            ]
        }
    }
}
```

## 커버리지 기준 변경

현재 기준은 다음과 같습니다:
- 라인 커버리지: 50% 이상
- 분기 커버리지: 50% 이상

이 기준을 변경하려면 `build.gradle`의 다음 부분을 수정하세요:

```groovy
jacocoTestCoverageVerification {
    // ...
    violationRules {
        rule {
            // ...
            limit {
                counter = 'LINE'
                value = 'COVEREDRATIO'
                minimum = 0.5  // 50% 에서 원하는 값으로 변경 (0.0 ~ 1.0)
            }
            limit {
                counter = 'BRANCH'
                value = 'COVEREDRATIO'
                minimum = 0.5  // 50% 에서 원하는 값으로 변경 (0.0 ~ 1.0)
            }
        }
    }
}
```

## SonarCloud 통합 (선택 사항)

JaCoCo 리포트를 SonarCloud와 통합하려면:

1. `sonar.gradle` 파일의 주석을 해제하고 조직 이름을 수정합니다.
2. `build.gradle`에서 `apply from: 'sonar.gradle'` 주석을 해제합니다.
3. GitHub Actions 워크플로우에 SonarCloud 스캔 단계를 추가합니다.

SonarCloud 통합 후에는 SonarCloud 대시보드에서 코드 품질 및 커버리지를 확인할 수 있습니다.

## 참고 자료

- [JaCoCo 공식 문서](https://www.jacoco.org/jacoco/trunk/doc/)
- [Gradle JaCoCo 플러그인](https://docs.gradle.org/current/userguide/jacoco_plugin.html)
- [SonarQube Java Test Coverage](https://docs.sonarqube.org/latest/analysis/coverage/)
