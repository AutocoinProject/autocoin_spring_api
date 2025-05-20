@echo off
echo Forcibly cleaning build directories...

echo Checking for Java processes...
tasklist /FI "IMAGENAME eq java.exe" /FO TABLE

echo Press any key to continue with cleanup...
pause > nul

cd C:\DEV\autocoin\autocoin_spring_api

echo Removing .gradle directory...
rmdir /S /Q ".gradle"
echo Done.

echo Removing build directory...
rmdir /S /Q "build"
echo Done.

echo Removing out directory...
rmdir /S /Q "out"
echo Done.

echo Creating empty directories...
mkdir "build"
mkdir "build\classes"
mkdir "build\classes\java"
mkdir "build\classes\java\main"

echo All directories cleaned.
echo.
echo Please restart your IDE before attempting to build again.
pause