@echo off
echo Starting build without tests...

cd C:\DEV\autocoin\autocoin_spring_api

echo 1. Stopping Gradle Daemon...
call gradlew --stop
echo Gradle Daemon stopped.

echo 2. Cleaning directories...
rmdir /S /Q build 2>nul
rmdir /S /Q .gradle 2>nul
echo Directories cleaned.

echo 3. Building project without tests...
call gradlew clean build -x test --no-daemon --refresh-dependencies

echo Process completed.
pause