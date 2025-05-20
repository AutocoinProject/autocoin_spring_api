@echo off
echo Stopping Gradle Daemon...
cd C:\DEV\autocoin\autocoin_spring_api
gradlew --stop
echo Gradle Daemon stopped.
pause