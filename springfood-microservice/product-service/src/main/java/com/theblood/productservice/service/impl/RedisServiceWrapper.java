package com.theblood.productservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisServiceWrapper {

    private final RedisTemplate<String, Object> redisTemplate;

    public Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void setValue(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setTimeout(String key, long timeout, TimeUnit timeUnit) {
        redisTemplate.expire(key, timeout, timeUnit);
    }

    public void setValueWithTimeout(String key, Object value, long ttl, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value);
            redisTemplate.expire(key, ttl, timeUnit);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean checkExistsKey(String key) {
        boolean check = false;
        try {
            check = redisTemplate.hasKey(key);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return check;
    }

    public void lPushAll(String key, List<String> value) {
        redisTemplate.opsForList().leftPushAll(key, value);
    }

    public Object rPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    public void lPush(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    public void setList(String key, List<?> value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<?> getList(String key) {
        try {
            return (List<?>) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setUuidList(String key, List<UUID> value) {
        try {
            List<String> stringList = value.stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList());
            redisTemplate.opsForValue().set(key, stringList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<UUID> getUuidList(String key) {
        try {
            List<?> rawList = (List<?>) redisTemplate.opsForValue().get(key);
            if (rawList == null) return null;

            return rawList.stream()
                    .filter(obj -> obj instanceof String)
                    .map(obj -> UUID.fromString((String) obj))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Set<String> getKeys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteKey(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

