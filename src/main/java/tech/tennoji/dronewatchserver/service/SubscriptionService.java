package tech.tennoji.dronewatchserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tech.tennoji.dronewatchserver.entity.FenceStatus;
import tech.tennoji.dronewatchserver.entity.GeoFence;
import tech.tennoji.dronewatchserver.entity.Subscription;
import tech.tennoji.dronewatchserver.repository.MongoRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubscriptionService {

    @Autowired
    private MongoRepository mongoRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final String areaCollectionPrefix = "area-";

    public int subscribeToTopic(String token, String topic) {
        try {
            mongoRepository.addSubscription(new Subscription(token, topic));
            return 0;
        } catch (DuplicateKeyException e) {
            log.error("Cannot subscribe: Duplicate subscription.");
            return 1;
        }
    }

    public void unsubscribeToTopic(String token, String topic) {
        long count = mongoRepository.removeSubscription(token, topic);
    }

    public List<String> getSubscribedTopics(String token) {
        var subscriptions = mongoRepository.findSubscriptionByToken(token);
        return subscriptions.stream().map(Subscription::getTopic).collect(Collectors.toList());
    }

    public List<FenceStatus> getSubscribedAreaStatus(String token) {
        List<FenceStatus> statusList = mongoRepository.findSubscriptionByToken(token).stream().map(
                subscription -> {
                    FenceStatus status = new FenceStatus();
                    status.setName(subscription.getTopic());
                    return status;
                }
        ).collect(Collectors.toList());
        statusList.forEach(status -> {
            Long num = redisTemplate.opsForSet().size(areaCollectionPrefix + status.getName());
            status.setNumber(Objects.requireNonNullElse(num, 0).longValue());
        });
        return statusList;
    }

    public List<String> getNotSubscribedTopics(String token) {
        var allTopics = mongoRepository.findAllAreas().stream().map(GeoFence::getName).collect(Collectors.toList());
        var subscribedTopics = mongoRepository.findSubscribersByTopic(token).stream().map(Subscription::getTopic).collect(Collectors.toList());
        allTopics.removeAll(subscribedTopics);
        return allTopics;
    }

    public long removeAllSubscriptionsByDevice(String token) {
        return mongoRepository.removeDevice(token);
    }
}
