@echo off
echo === AutoCoin Spring API 통합 테스트 실행 ===

echo 1. Gradle 데몬 중지...
call gradlew --stop
echo 완료.

echo 2. 빌드 디렉토리 정리...
rmdir /S /Q build 2>nul
rmdir /S /Q .gradle 2>nul
echo 완료.

echo 3. 테스트만 실행 (프로젝트 빌드 없이)
call gradlew test --no-build-cache --info

echo === 테스트 완료 ===
echo 테스트 보고서: %cd%\build\reports\tests\test\index.html
pause