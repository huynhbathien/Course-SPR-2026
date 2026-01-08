# PowerShell Script để build và run Docker
# Chạy từ terminal IntelliJ hoặc PowerShell

function Write-Title {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "   DOCKER BUILD & RUN SCRIPT" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
}

function Check-Docker {
    try {
        $version = docker --version
        Write-Host "✅ Docker found: $version" -ForegroundColor Green
        return $true
    }
    catch {
        Write-Host "❌ ERROR: Docker not found or not in PATH" -ForegroundColor Red
        Write-Host "Please install Docker Desktop first" -ForegroundColor Red
        return $false
    }
}

function Show-Menu {
    Write-Host "Chọn hành động:" -ForegroundColor Yellow
    Write-Host "1. Build all services (docker-compose build)" -ForegroundColor White
    Write-Host "2. Start all services (docker-compose up -d)" -ForegroundColor White
    Write-Host "3. Stop all services (docker-compose down)" -ForegroundColor White
    Write-Host "4. View logs (docker-compose logs -f)" -ForegroundColor White
    Write-Host "5. View status (docker-compose ps)" -ForegroundColor White
    Write-Host "6. Build + Start (Full setup)" -ForegroundColor White
    Write-Host "7. Clean + Rebuild (Remove volumes)" -ForegroundColor White
    Write-Host "8. Exit" -ForegroundColor White
    Write-Host ""
}

function Invoke-Build {
    Write-Host "🔨 Building all services..." -ForegroundColor Yellow
    docker-compose build
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Build completed!" -ForegroundColor Green
    } else {
        Write-Host "❌ Build failed!" -ForegroundColor Red
    }
}

function Invoke-Start {
    Write-Host "🚀 Starting all services..." -ForegroundColor Yellow
    docker-compose up -d
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Services started!" -ForegroundColor Green
        Write-Host ""
        docker-compose ps
    } else {
        Write-Host "❌ Start failed!" -ForegroundColor Red
    }
}

function Invoke-Stop {
    Write-Host "🛑 Stopping all services..." -ForegroundColor Yellow
    docker-compose down
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Services stopped!" -ForegroundColor Green
    } else {
        Write-Host "❌ Stop failed!" -ForegroundColor Red
    }
}

function Invoke-Logs {
    Write-Host "📋 Viewing logs (Ctrl+C to exit)..." -ForegroundColor Yellow
    docker-compose logs -f app
}

function Invoke-Status {
    Write-Host "📊 Current status:" -ForegroundColor Yellow
    docker-compose ps
}

function Invoke-Full {
    Write-Host "🔨 Building all services..." -ForegroundColor Yellow
    docker-compose build
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Build failed!" -ForegroundColor Red
        return
    }
    Write-Host "✅ Build completed!" -ForegroundColor Green
    Write-Host ""

    Write-Host "🚀 Starting all services..." -ForegroundColor Yellow
    docker-compose up -d
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Start failed!" -ForegroundColor Red
        return
    }
    Write-Host "✅ All services started!" -ForegroundColor Green
    Write-Host ""

    Start-Sleep -Seconds 5
    docker-compose ps
}

function Invoke-Clean {
    Write-Host "🗑️  Cleaning up..." -ForegroundColor Yellow
    docker-compose down -v
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Cleanup failed!" -ForegroundColor Red
        return
    }
    Write-Host "✅ Cleaned!" -ForegroundColor Green
    Write-Host ""

    Write-Host "🔨 Building all services..." -ForegroundColor Yellow
    docker-compose build --no-cache
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Build failed!" -ForegroundColor Red
        return
    }
    Write-Host "✅ Build completed!" -ForegroundColor Green
    Write-Host ""

    Write-Host "🚀 Starting all services..." -ForegroundColor Yellow
    docker-compose up -d
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Start failed!" -ForegroundColor Red
        return
    }
    Write-Host "✅ All services started!" -ForegroundColor Green
    Write-Host ""

    Start-Sleep -Seconds 5
    docker-compose ps
}

# Main script
Write-Title

if (-not (Check-Docker)) {
    exit 1
}

Write-Host ""
Show-Menu

$choice = Read-Host "Nhập lựa chọn (1-8)"

switch ($choice) {
    "1" { Invoke-Build }
    "2" { Invoke-Start }
    "3" { Invoke-Stop }
    "4" { Invoke-Logs }
    "5" { Invoke-Status }
    "6" { Invoke-Full }
    "7" { Invoke-Clean }
    "8" { exit 0 }
    default { Write-Host "❌ Invalid choice. Please try again." -ForegroundColor Red }
}

Write-Host ""

