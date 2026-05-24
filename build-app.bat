@echo off
REM Build script for Sharma Khata & Billing Android App

cd /d "%~dp0"

echo.
echo ========================================
echo Building Sharma Khata & Billing App
echo ========================================
echo.

REM Check if gradle is available
where gradle >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo Using system Gradle...
    call gradle build --scan
) else (
    echo Error: Gradle is not installed or not in PATH
    echo.
    echo Please ensure:
    echo 1. Android SDK is installed
    echo 2. ANDROID_HOME environment variable is set
    echo 3. Java JDK is installed
    echo 4. Add to PATH or use Android Studio
    exit /b 1
)

if %ERRORLEVEL% equ 0 (
    echo.
    echo ========================================
    echo Build Successful!
    echo ========================================
    echo APK location: app\build\outputs\apk\debug\app-debug.apk
) else (
    echo.
    echo Build Failed!
    exit /b 1
)
