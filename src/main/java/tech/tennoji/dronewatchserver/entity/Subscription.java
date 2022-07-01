package tech.tennoji.dronewatchserver.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Subscription {
    @Id
    private String id;

    private String token;
    private String topic;

    public Subscription() {
    }

    public Subscription(String token, String topic) {
        this.token = token;
        this.topic = topic;
    }

}
