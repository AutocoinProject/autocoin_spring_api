@echo off
echo Cleaning build directories...

:: 실행 중인 Java 프로세스 확인
echo Checking for running Java processes...
tasklist /fi "imagename eq java.exe"

:: 빌드 디렉토리 삭제 시도
echo Attempting to delete build directory...
rd /s /q "C:\DEV\autocoin\autocoin_spring_api\build"

:: .gradle 캐시 디렉토리 삭제 시도
echo Attempting to delete .gradle directory...
rd /s /q "C:\DEV\autocoin\autocoin_spring_api\.gradle"

echo Cleaning completed.
pause