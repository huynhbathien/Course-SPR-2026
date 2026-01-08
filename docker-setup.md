# Docker Setup Guide

## Prerequisites
- Docker Desktop installed and running
- Docker Compose installed

## Quick Start

### 1. Build and Run with Docker Compose
```bash
# Navigate to project root
cd D:\Course-SPR-2026\untitled

# Start all services (MySQL, Redis, Kafka, and Spring Boot App)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### 2. Build Docker Image Only
```bash
# Build the Spring Boot application image
docker build -f src/main/java/com/mycompany/Dockerfile -t course-app:latest .

# Run the container
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/course_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  -e SPRING_REDIS_HOST=redis \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  --name course-app-container \
  course-app:latest
```

## Services Overview

### 🚀 Spring Boot Application
- **URL**: http://localhost:8080
- **API Context**: http://localhost:8080/api
- **Port**: 8080

### 📊 MySQL Database
- **Host**: mysql
- **Port**: 3306
- **Root Password**: root
- **Database**: course_db
- **User**: course_user
- **Password**: course_password

### 💾 Redis Cache
- **Host**: redis
- **Port**: 6379
- **No authentication** (default)

### 📨 Apache Kafka
- **Bootstrap Server**: kafka:9092
- **Zookeeper**: zookeeper:2181
- **Broker ID**: 1

## Environment Variables

Set these in `docker-compose.yml` or at runtime:

```yaml
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/course_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
JAVA_OPTS=-Xmx512m -Xms256m
```

## Health Checks

### Check Application Health
```bash
curl http://localhost:8080/api/actuator/health
```

### Check MySQL
```bash
docker exec course-mysql mysqladmin ping -h localhost -u root -proot
```

### Check Redis
```bash
docker exec course-redis redis-cli ping
```

### Check Kafka
```bash
docker exec course-kafka kafka-topics --bootstrap-server localhost:9092 --list
```

## Common Commands

### View Running Containers
```bash
docker-compose ps
```

### View Container Logs
```bash
# All services
docker-compose logs

# Specific service
docker-compose logs -f app
docker-compose logs -f mysql
docker-compose logs -f redis
docker-compose logs -f kafka
```

### Access MySQL
```bash
docker exec -it course-mysql mysql -u root -proot course_db
```

### Access Redis CLI
```bash
docker exec -it course-redis redis-cli
```

### Stop Specific Service
```bash
docker-compose stop app
```

### Rebuild Services
```bash
docker-compose build --no-cache
```

### Remove Volumes and Networks
```bash
docker-compose down -v
```

## Troubleshooting

### Application won't start
1. Check if all dependent services are running: `docker-compose ps`
2. Check logs: `docker-compose logs app`
3. Verify database is ready: `docker-compose logs mysql`

### Port already in use
```bash
# Find and kill process using port 8080
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Or change port in docker-compose.yml
```

### Kafka connection issues
- Ensure Zookeeper is running first
- Wait 10-15 seconds for Kafka to fully start
- Check Kafka logs: `docker-compose logs kafka`

### Redis connection refused
- Check if Redis container is running: `docker-compose ps`
- Check Redis logs: `docker-compose logs redis`

### MySQL connection issues
- Ensure MySQL is fully initialized (takes ~10-15 seconds)
- Check password and database name match
- Use health check status: `docker-compose ps`

## Useful Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker)
- [Kafka Docker Hub](https://hub.docker.com/r/confluentinc/cp-kafka)
- [Redis Docker Hub](https://hub.docker.com/_/redis)
- [MySQL Docker Hub](https://hub.docker.com/_/mysql)

