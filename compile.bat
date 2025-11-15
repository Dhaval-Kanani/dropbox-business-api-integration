@echo off
REM Compile script for Dropbox API Integration Project (Windows)
REM This script compiles all Java files and prepares them for execution

echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║  Dropbox Business API Integration - Compilation Script    ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

REM Check if lib directory exists
if not exist "lib" (
    echo ⚠️  WARNING: lib directory not found!
    echo Please download the JSON library and place it in the lib/ folder:
    echo https://repo1.maven.org/maven2/org/json/json/20231013/json-20231013.jar
    echo.
)

REM Check if bin directory exists, create if not
if not exist "bin" (
    echo Creating bin directory...
    mkdir bin
)

REM Compile Java files
echo Compiling Java files...
echo.

javac -cp "lib/*" src/*.java -d bin

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Compilation successful!
    echo.
    echo Compiled files are in: bin/
    echo.
    echo To run the application:
    echo   java -cp "bin;lib/*" Main
    echo.
) else (
    echo.
    echo Compilation failed!
    echo Please check the error messages above.
    echo.
    pause
)
