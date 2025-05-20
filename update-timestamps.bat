@echo off
echo Updating file timestamps...
for /R %cd%\src /D %%d in (*) do (
    pushd %%d
    for %%f in (*.java) do (
        copy /b "%%f"+,, "%%f" >nul
        echo Updated: %%f
    )
    popd
)
echo Timestamp update completed.
pause