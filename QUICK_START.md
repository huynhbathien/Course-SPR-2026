# ⚡ QUICK START - Build Docker Trong IntelliJ

## 🎯 5 Bước Nhanh Nhất

### Bước 1️⃣: Mở Terminal
```
Alt + F12  (hoặc View → Tool Windows → Terminal)
```

### Bước 2️⃣: Đảm Bảo Đang Ở Thư Mục Root
```bash
cd D:\Course-SPR-2026\untitled
```

### Bước 3️⃣: Build Docker Images
```bash
docker-compose build
```
⏱️ **Thời gian:** ~45-60 giây (lần đầu), 10-15 giây (lần sau)

### Bước 4️⃣: Start Services
```bash
docker-compose up -d
```
✅ Tất cả services (MySQL, Redis, Kafka, App) sẽ khởi động

### Bước 5️⃣: Kiểm Tra Trạng Thái
```bash
docker-compose ps
```

**Output mong muốn:**
```
NAME            STATUS
course-app      Up 30 seconds
course-mysql    Up 32 seconds
course-redis    Up 31 seconds
course-kafka    Up 28 seconds
course-zookeeper Up 29 seconds
```

---

## 🔍 Test Application

```bash
# Browser
http://localhost:8080/api/actuator/health

# Terminal
curl http://localhost:8080/api/actuator/health
```

---

## 🧹 Cleanup (Nếu Cần)

```bash
# Dừng tất cả services
docker-compose down

# Xóa tất cả volumes (database data)
docker-compose down -v

# Rebuild từ đầu (không cache)
docker-compose build --no-cache
```

---

## 📝 Các Lệnh Thường Dùng

| Lệnh | Mục Đích |
|------|---------|
| `docker-compose build` | Build images |
| `docker-compose up -d` | Start services (background) |
| `docker-compose down` | Stop services |
| `docker-compose ps` | Xem trạng thái |
| `docker-compose logs -f app` | Xem logs của app |
| `docker-compose logs -f` | Xem logs tất cả |
| `docker-compose stop` | Pause services |
| `docker-compose restart` | Restart services |

---

## 🚨 Troubleshooting

### ❌ Port đã bị sử dụng
```bash
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### ❌ Build fail
```bash
# Clean Maven cache
mvn clean

# Rebuild
docker-compose build --no-cache
```

### ❌ Services không start
```bash
# Xem logs chi tiết
docker-compose logs app

# Restart
docker-compose restart app
```

### ❌ Out of memory
Tăng memory Docker Desktop:
- Settings → Resources → Memory: 4GB+

---

## 💡 Tips

1. **Luôn check Docker Desktop đang chạy** (Windows tray icon)
2. **Dùng `-d` flag** để chạy background (không block terminal)
3. **Xem logs bằng `logs -f`** để debug
4. **Dùng `--no-cache`** nếu muốn rebuild from scratch

---

## 🎓 File Hữu Ích

- `DOCKER_BUILD_STEPS.md` - Chi tiết đầy đủ
- `docker-compose.yml` - Config tất cả services
- `docker-build.bat` - Menu script (Windows)
- `docker-build.ps1` - Menu script (PowerShell)

---

## 📱 Endpoints Sau Khi Build

```
Health:  http://localhost:8080/api/actuator/health
Info:    http://localhost:8080/api/actuator/info
Metrics: http://localhost:8080/api/actuator/metrics
```

---

**Good luck! 🚀**

