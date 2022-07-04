package tech.tennoji.dronewatchserver.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class RedisRepository {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public Long setAdd(String key, String... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    public Long setRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    public Set<String> findByKey(String key) {
        return redisTemplate.opsForSet().members(key);
    }

}
