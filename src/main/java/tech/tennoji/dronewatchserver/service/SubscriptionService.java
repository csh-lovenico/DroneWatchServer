package tech.tennoji.dronewatchserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import tech.tennoji.dronewatchserver.entity.Subscription;
import tech.tennoji.dronewatchserver.repository.MongoRepository;

@Service
@Slf4j
public class SubscriptionService {

    @Autowired
    private MongoRepository mongoRepository;

    public void subscribeTopic(String token, String topic) {
        try {
            mongoRepository.addSubscription(new Subscription(token, topic));
        } catch (DuplicateKeyException e) {
            log.error("Cannot subscribe: Duplicate subscription.");
        } catch (Exception e) {
            log.error("Cannot subscribe: " + e.getMessage());
        }
    }

    public void unsubscribeTopic(String token, String topic) {
        long count = mongoRepository.removeSubscription(token, topic);
    }
}
