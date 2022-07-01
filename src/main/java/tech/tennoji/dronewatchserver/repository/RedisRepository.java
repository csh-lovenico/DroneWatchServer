package tech.tennoji.dronewatchserver.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class RedisRepository {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void testAdd() {
        redisTemplate.opsForSet().add("test", "eee", "fff");
    }

    public Set<String> testQuery() {
        return redisTemplate.opsForSet().members("test");
    }

}
