@echo off
REM Script để build và run Docker từ Windows
REM Chạy file này từ terminal IntelliJ (Alt + F12)

echo ========================================
echo   DOCKER BUILD & RUN SCRIPT
echo ========================================
echo.

REM Kiểm tra Docker đã cài
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ ERROR: Docker không được cài đặt hoặc không trong PATH
    echo Vui lòng cài đặt Docker Desktop trước
    pause
    exit /b 1
)

echo ✅ Docker found
docker --version
echo.

REM Menu chọn action
echo Chọn hành động:
echo 1. Build all services (docker-compose build)
echo 2. Start all services (docker-compose up -d)
echo 3. Stop all services (docker-compose down)
echo 4. View logs (docker-compose logs -f)
echo 5. View status (docker-compose ps)
echo 6. Build + Start (Full setup)
echo 7. Clean + Rebuild (Remove volumes)
echo 8. Exit
echo.

set /p choice="Nhập lựa chọn (1-8): "

if "%choice%"=="1" goto build
if "%choice%"=="2" goto start
if "%choice%"=="3" goto stop
if "%choice%"=="4" goto logs
if "%choice%"=="5" goto status
if "%choice%"=="6" goto full
if "%choice%"=="7" goto clean
if "%choice%"=="8" goto exit
goto invalid

:build
echo.
echo 🔨 Building all services...
docker-compose build
if %errorlevel% neq 0 (
    echo ❌ Build failed!
    pause
    exit /b 1
)
echo ✅ Build completed!
pause
goto end

:start
echo.
echo 🚀 Starting all services...
docker-compose up -d
if %errorlevel% neq 0 (
    echo ❌ Start failed!
    pause
    exit /b 1
)
echo ✅ Services started!
docker-compose ps
pause
goto end

:stop
echo.
echo 🛑 Stopping all services...
docker-compose down
if %errorlevel% neq 0 (
    echo ❌ Stop failed!
    pause
    exit /b 1
)
echo ✅ Services stopped!
pause
goto end

:logs
echo.
echo 📋 Viewing logs (Ctrl+C to exit)...
docker-compose logs -f app
goto end

:status
echo.
echo 📊 Current status:
docker-compose ps
pause
goto end

:full
echo.
echo 🔨 Building all services...
docker-compose build
if %errorlevel% neq 0 (
    echo ❌ Build failed!
    pause
    exit /b 1
)
echo ✅ Build completed!
echo.
echo 🚀 Starting all services...
docker-compose up -d
if %errorlevel% neq 0 (
    echo ❌ Start failed!
    pause
    exit /b 1
)
echo ✅ All services started!
echo.
timeout /t 5
docker-compose ps
pause
goto end

:clean
echo.
echo 🗑️  Cleaning up...
docker-compose down -v
if %errorlevel% neq 0 (
    echo ❌ Cleanup failed!
    pause
    exit /b 1
)
echo ✅ Cleaned!
echo.
echo 🔨 Building all services...
docker-compose build --no-cache
if %errorlevel% neq 0 (
    echo ❌ Build failed!
    pause
    exit /b 1
)
echo ✅ Build completed!
echo.
echo 🚀 Starting all services...
docker-compose up -d
if %errorlevel% neq 0 (
    echo ❌ Start failed!
    pause
    exit /b 1
)
echo ✅ All services started!
echo.
timeout /t 5
docker-compose ps
pause
goto end

:invalid
echo ❌ Invalid choice. Please try again.
pause
goto end

:exit
exit /b 0

:end

