@echo off
echo Checking processes using build files...
for /f "tokens=4" %%p in ('handle -a "C:\DEV\autocoin\autocoin_spring_api\build"') do (
    echo Process: %%p
)
echo Process check completed.
pause