@echo off
REM ============================================================
REM build.bat – Compile and run MediReminder on Windows
REM ============================================================

SET SRC_DIR=src
SET OUT_DIR=out
SET MAIN_CLASS=Main

if not exist %OUT_DIR% mkdir %OUT_DIR%

echo => Compiling...
dir /s /b %SRC_DIR%\*.java > sources.txt

javac -cp "lib/*" -d %OUT_DIR% @sources.txt

IF %ERRORLEVEL% NEQ 0 (
    echo => Compilation FAILED.
    del sources.txt
    exit /b 1
)

del sources.txt
echo => Compilation SUCCESS.

echo => Running MediReminder...
java -cp "out;lib/*" %MAIN_CLASS%
