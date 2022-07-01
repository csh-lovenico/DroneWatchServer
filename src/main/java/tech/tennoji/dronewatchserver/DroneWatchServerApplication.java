package tech.tennoji.dronewatchserver;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@Slf4j
public class DroneWatchServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DroneWatchServerApplication.class, args);
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() throws Exception {
        FirebaseOptions firebaseOptions = FirebaseOptions.builder().setCredentials(GoogleCredentials.getApplicationDefault()).build();
        FirebaseApp firebaseApp = FirebaseApp.initializeApp(firebaseOptions);
        log.info("Firebase app initialized.");
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    @Bean
    public ThreadPoolTaskExecutor executor() {
        // Thread pool executor for sending cloud messages asynchronously
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        return executor;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        // Redis for recording areas a drone is in. Use SETS data type.
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        return template;
    }
}
