package tech.tennoji.dronewatchserver;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
}
