package com.mycompany.util;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Generic Redis Utility for common Redis operations
 * Hỗ trợ các thao tác: set, get, delete, exists, increment, etc.
 */
@Slf4j
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * Lưu dữ liệu vào Redis với TTL (Time To Live)
     * 
     * @param key     Redis key
     * @param value   giá trị
     * @param timeout TTL (thời gian sống)
     * @param unit    đơn vị thời gian (SECONDS, MINUTES, HOURS, DAYS)
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            log.debug("Redis SET: key={}, timeout={} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("Error setting value in Redis for key: {}", key, e);
            throw new RuntimeException("Failed to set value in Redis", e);
        }
    }

    /**
     * Lưu dữ liệu vào Redis mà không có TTL
     * 
     * @param key   Redis key
     * @param value giá trị
     */
    public void set(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Redis SET: key={}", key);
        } catch (Exception e) {
            log.error("Error setting value in Redis for key: {}", key, e);
            throw new RuntimeException("Failed to set value in Redis", e);
        }
    }

    /**
     * Lấy dữ liệu từ Redis
     * 
     * @param key Redis key
     * @return giá trị hoặc null nếu không tồn tại
     */
    public String get(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("Redis GET: key={}", key);
            }
            return value;
        } catch (Exception e) {
            log.error("Error getting value from Redis for key: {}", key, e);
            return null;
        }
    }

    /**
     * Xóa dữ liệu khỏi Redis
     * 
     * @param key Redis key
     * @return true nếu xóa thành công, false nếu key không tồn tại
     */
    public boolean delete(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (deleted != null && deleted) {
                log.debug("Redis DELETE: key={}", key);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error deleting value from Redis for key: {}", key, e);
            return false;
        }
    }

    /**
     * Xóa nhiều key
     * 
     * @param keys danh sách Redis keys
     * @return số lượng keys đã xóa
     */
    public long delete(String... keys) {
        try {
            long count = redisTemplate.delete(Arrays.asList(keys));
            if (count > 0) {
                log.debug("Redis DELETE: {} keys deleted", count);
            }
            return count;
        } catch (Exception e) {
            log.error("Error deleting multiple keys from Redis", e);
            return 0;
        }
    }

    /**
     * Kiểm tra key có tồn tại không
     * 
     * @param key Redis key
     * @return true nếu tồn tại
     */
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Error checking key existence in Redis for key: {}", key, e);
            return false;
        }
    }

    /**
     * Lấy TTL của một key (theo giây)
     * 
     * @param key Redis key
     * @return số giây còn lại, -1 nếu key không có TTL, -2 nếu key không tồn tại
     */
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return expire != null ? expire : -2;
        } catch (Exception e) {
            log.error("Error getting expire time from Redis for key: {}", key, e);
            return -2;
        }
    }

    /**
     * Thiết lập TTL cho một key
     * 
     * @param key     Redis key
     * @param timeout TTL (thời gian sống)
     * @param unit    đơn vị thời gian
     * @return true nếu thiết lập thành công
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            Boolean result = redisTemplate.expire(key, timeout, unit);
            if (result != null && result) {
                log.debug("Redis EXPIRE: key={}, timeout={} {}", key, timeout, unit);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error setting expire time in Redis for key: {}", key, e);
            return false;
        }
    }

    /**
     * Increment giá trị của một key (dùng cho counters)
     * 
     * @param key Redis key
     * @return giá trị sau khi increment
     */
    public long increment(String key) {
        try {
            Long value = redisTemplate.opsForValue().increment(key);
            log.debug("Redis INCREMENT: key={}, value={}", key, value);
            return value != null ? value : 0;
        } catch (Exception e) {
            log.error("Error incrementing value in Redis for key: {}", key, e);
            return 0;
        }
    }

    /**
     * Increment giá trị của một key với lượng tăng cụ thể
     * 
     * @param key   Redis key
     * @param delta lượng tăng
     * @return giá trị sau khi increment
     */
    public long increment(String key, long delta) {
        try {
            Long value = redisTemplate.opsForValue().increment(key, delta);
            log.debug("Redis INCREMENT: key={}, delta={}, value={}", key, delta, value);
            return value != null ? value : 0;
        } catch (Exception e) {
            log.error("Error incrementing value in Redis for key: {}", key, e);
            return 0;
        }
    }

    /**
     * Decrement giá trị của một key
     * 
     * @param key Redis key
     * @return giá trị sau khi decrement
     */
    public long decrement(String key) {
        try {
            Long value = redisTemplate.opsForValue().decrement(key);
            log.debug("Redis DECREMENT: key={}, value={}", key, value);
            return value != null ? value : 0;
        } catch (Exception e) {
            log.error("Error decrementing value in Redis for key: {}", key, e);
            return 0;
        }
    }

    /**
     * Lưu Hash vào Redis
     * 
     * @param key     Redis key
     * @param hashKey hash field name
     * @param value   giá trị
     */
    public void hset(String key, String hashKey, String value) {
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
            log.debug("Redis HSET: key={}, hashKey={}", key, hashKey);
        } catch (Exception e) {
            log.error("Error setting hash value in Redis for key: {}", key, e);
            throw new RuntimeException("Failed to set hash value in Redis", e);
        }
    }

    /**
     * Lấy Hash từ Redis
     * 
     * @param key     Redis key
     * @param hashKey hash field name
     * @return giá trị
     */
    public Object hget(String key, String hashKey) {
        try {
            return redisTemplate.opsForHash().get(key, hashKey);
        } catch (Exception e) {
            log.error("Error getting hash value from Redis for key: {}", key, e);
            return null;
        }
    }

    /**
     * Xóa Hash field từ Redis
     * 
     * @param key     Redis key
     * @param hashKey hash field name
     */
    public void hdel(String key, String hashKey) {
        try {
            redisTemplate.opsForHash().delete(key, hashKey);
            log.debug("Redis HDEL: key={}, hashKey={}", key, hashKey);
        } catch (Exception e) {
            log.error("Error deleting hash value from Redis for key: {}", key, e);
        }
    }

    /**
     * Xóa toàn bộ Redis data (CAUTION!)
     */
    public void flushDb() {
        try {
            redisTemplate.execute((RedisCallback<Object>) connection -> {
                connection.serverCommands().flushDb();
                return null;
            });
            log.warn("Redis database flushed!");
        } catch (Exception e) {
            log.error("Error flushing Redis database", e);
        }
    }
}
