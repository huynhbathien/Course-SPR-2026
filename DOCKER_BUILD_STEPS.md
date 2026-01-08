# 🐳 Hướng dẫn Build Docker với IntelliJ IDEA

## Yêu cầu
- ✅ Docker Desktop cài đặt và chạy
- ✅ Docker CLI (command line)
- ✅ Git Bash hoặc PowerShell

---

## 🚀 Các Bước Build Docker Từ Terminal IntelliJ

### **Bước 1: Mở Terminal Trong IntelliJ**

1. Trong IntelliJ IDEA, nhấn **`Alt + F12`** hoặc:
   - Menu: **View → Tool Windows → Terminal**
   - Hoặc nhấn biểu tượng **Terminal** ở góc dưới

2. Terminal sẽ mở tại thư mục root của project

### **Bước 2: Kiểm Tra Docker Đã Cài Đặt**

```bash
docker --version
docker-compose --version
```

**Output mong muốn:**
```
Docker version 24.0.x, build xxxxx
Docker Compose version 2.x.x
```

---

## 📋 **Phương Pháp 1: Build Với Docker Compose (Recommended)**

### **Bước 1: Build All Services**
```bash
# Navigate to project root (nếu không đã là root)
cd D:\Course-SPR-2026\untitled

# Xây dựng tất cả services
docker-compose build
```

**Output mong muốn:**
```
[+] Building 45.2s
 => mysql
 => redis
 => zookeeper
 => kafka
 => app
```

### **Bước 2: Start All Services**
```bash
# Khởi động tất cả services (background)
docker-compose up -d
```

### **Bước 3: Kiểm Tra Services**
```bash
# Xem trạng thái tất cả containers
docker-compose ps
```

**Output mong muốn:**
```
NAME                    STATUS          PORTS
course-app              Up 10 seconds    0.0.0.0:8080->8080/tcp
course-mysql            Up 12 seconds    0.0.0.0:3306->3306/tcp
course-redis            Up 11 seconds    0.0.0.0:6379->6379/tcp
course-kafka            Up 8 seconds     0.0.0.0:9092->9092/tcp
course-zookeeper        Up 9 seconds     0.0.0.0:2181->2181/tcp
```

### **Bước 4: Xem Logs**
```bash
# Xem logs của tất cả services
docker-compose logs -f

# Xem logs của service cụ thể
docker-compose logs -f app
docker-compose logs -f mysql
docker-compose logs -f redis
```

### **Bước 5: Dừng Services**
```bash
# Dừng tất cả services
docker-compose down

# Dừng và xóa volumes (data)
docker-compose down -v

# Dừng service cụ thể
docker-compose stop app
```

---

## 🏗️ **Phương Pháp 2: Build Docker Image Thủ Công**

### **Bước 1: Clean & Build JAR**
```bash
# Xóa build cũ và compile
mvn clean package -DskipTests
```

**Output mong muốn:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 45.235s
```

### **Bước 2: Build Docker Image**
```bash
# Build image từ Dockerfile
docker build -f src/main/java/com/mycompany/Dockerfile -t course-app:latest .
```

**Output mong muốn:**
```
[+] Building 25.4s
...
 => exporting to image
 => => writing image sha256:abc123...
 => => naming to docker.io/library/course-app:latest
```

### **Bước 3: Verify Image**
```bash
# Liệt kê tất cả images
docker images | grep course-app

# Chi tiết về image
docker inspect course-app:latest
```

### **Bước 4: Run Container**
```bash
# Chạy container từ image
docker run -d \
  --name course-app-container \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/course_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  -e SPRING_REDIS_HOST=host.docker.internal \
  -e SPRING_REDIS_PORT=6379 \
  course-app:latest
```

### **Bước 5: Kiểm Tra Container**
```bash
# Xem logs
docker logs -f course-app-container

# Kiểm tra health
curl http://localhost:8080/api/actuator/health
```

---

## 🔧 **Phương Pháp 3: Build Từng Service Riêng Lẻ**

### **Build MySQL**
```bash
docker-compose build mysql
docker-compose up -d mysql
```

### **Build Redis**
```bash
docker-compose build redis
docker-compose up -d redis
```

### **Build Kafka + Zookeeper**
```bash
docker-compose build zookeeper kafka
docker-compose up -d zookeeper kafka
```

### **Build Spring Boot App**
```bash
docker-compose build app
docker-compose up -d app
```

---

## 📊 **Các Lệnh Hữu Ích**

### **Kiểm Tra & Debugging**

```bash
# Xem tất cả containers (đang chạy + đã dừng)
docker ps -a

# Xem tất cả images
docker images

# Xem network
docker network ls

# Xem volumes
docker volume ls
```

### **Xóa & Cleanup**

```bash
# Xóa container
docker rm course-app-container

# Xóa image
docker rmi course-app:latest

# Xóa tất cả unused resources
docker system prune

# Xóa tất cả unused resources + volumes
docker system prune -a --volumes
```

### **Access Services**

```bash
# Truy cập MySQL
docker exec -it course-mysql mysql -u root -proot course_db

# Truy cập Redis
docker exec -it course-redis redis-cli

# Truy cập container bash
docker exec -it course-app-container bash
```

### **Network Issues**

```bash
# Kiểm tra network connectivity
docker network inspect course-network

# Ping giữa containers
docker exec course-app ping mysql
docker exec course-app ping redis
```

---

## ⚠️ **Troubleshooting**

### **Port Đã Được Sử Dụng**
```bash
# Tìm process sử dụng port 8080
netstat -ano | findstr :8080

# Kill process (Windows)
taskkill /PID <PID> /F

# Hoặc thay đổi port trong docker-compose.yml
```

### **Dockerfile Not Found**
```bash
# Kiểm tra path Dockerfile
dir src\main\java\com\mycompany\

# Đảm bảo bạn đang ở thư mục root project
cd D:\Course-SPR-2026\untitled
```

### **Build Fail - Dependencies**
```bash
# Clean và rebuild
mvn clean install -DskipTests

# Force rebuild Docker image
docker-compose build --no-cache
```

### **Container Không Start**
```bash
# Xem logs chi tiết
docker-compose logs app

# Kiểm tra health
docker-compose ps

# Rebuild và restart
docker-compose down
docker-compose up -d --build
```

### **MySQL manifest not found**
```bash
# Lỗi: manifest for mysql:8.0-alpine not found

# Fix: Sử dụng MySQL version ổn định hơn
# docker-compose.yml đã được cập nhật sang mysql:8.3

# Nếu vẫn lỗi, try pull image trước:
docker pull mysql:8.3

# Hoặc xóa tất cả và rebuild
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```

### **Zookeeper Error - Context canceled**
```bash
# Lỗi: context canceled khi khởi động Zookeeper

# Nguyên nhân: Docker không đủ resources hoặc network issue

# Fix 1: Xóa volumes và rebuild
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d

# Fix 2: Tăng timeout (chỉnh sửa docker-compose.yml)
# Nếu vẫn fail, thử xóa tất cả Docker data
docker system prune -a --volumes

# Fix 3: Kiểm tra Docker resources
# Settings → Resources → CPU: 4, Memory: 4GB
```

### **Pull Failed / Network Issue**
```bash
# Lỗi khi pull image từ Docker Hub

# Fix 1: Kiểm tra internet connection
ping google.com

# Fix 2: Try pull images manually trước
docker pull mysql:8.3
docker pull redis:7-alpine
docker pull confluentinc/cp-kafka:7.6.0
docker pull confluentinc/cp-zookeeper:7.6.0

# Fix 3: Sử dụng image local (nếu đã pull trước)
docker images
```

### **All Services Stuck**
```bash
# Nếu tất cả services stuck hoặc timeout

# Fix: Hard restart
docker-compose down -v
docker system prune -a --volumes
docker-compose build --no-cache
docker-compose up -d

# Sau đó check logs
docker-compose logs -f
```

---

## 🎯 **Tóm Tắt - Quick Reference**

```bash
# 1. Mở Terminal: Alt + F12

# 2. Build everything
docker-compose build

# 3. Start everything
docker-compose up -d

# 4. Check status
docker-compose ps

# 5. View logs
docker-compose logs -f app

# 6. Stop everything
docker-compose down
```

---

## 📱 **Test Application**

Sau khi container chạy, test các endpoints:

```bash
# Health check
curl http://localhost:8080/api/actuator/health

# Get metrics
curl http://localhost:8080/api/actuator/metrics

# In browser
http://localhost:8080/api/actuator/health
```

---

## 💡 **Tips & Best Practices**

1. **Luôn check Docker Desktop đang chạy** trước build
2. **Dùng `docker-compose up -d`** để chạy background (không block terminal)
3. **Xem logs bằng `docker-compose logs -f`** để debug
4. **Dùng `--no-cache`** nếu muốn rebuild từ đầu
5. **Tắt services cũ** bằng `docker-compose down` trước khi rebuild

---

## 🔗 **Tài Liệu Tham Khảo**

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker)
- [Docker Cheat Sheet](https://github.com/wsargent/docker-cheat-sheet)

