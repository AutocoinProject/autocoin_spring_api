@echo off
echo Starting full rebuild process...

cd C:\DEV\autocoin\autocoin_spring_api

echo 1. Stopping Gradle Daemon...
call gradlew --stop
echo Gradle Daemon stopped.

echo 2. Cleaning directories...
rmdir /S /Q build 2>nul
rmdir /S /Q .gradle 2>nul
rmdir /S /Q out 2>nul
echo Directories cleaned.

echo 3. Building project...
call gradlew clean build --no-daemon --refresh-dependencies

echo Process completed.
pause