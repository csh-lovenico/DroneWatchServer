package tech.tennoji.dronewatchserver.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import tech.tennoji.dronewatchserver.entity.Subscription;

import java.util.List;

@Repository
@Slf4j
public class MongoRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Subscription> findSubscribersByTopic(String topic) {
        Query query = new Query();
        query.addCriteria(Criteria.where("topic").is(topic));
        return mongoTemplate.find(query, Subscription.class, "subscription");
    }

    public void addSubscription(Subscription subscription) {
        mongoTemplate.insert(subscription, "subscription");
    }

    public long removeSubscription(String token, String topic) {
        Query query = new Query();
        query.addCriteria(Criteria.where("topic").is(topic).andOperator(Criteria.where("token").is(token)));
        var result = mongoTemplate.remove(query, "subscription");
        return result.getDeletedCount();
    }

    public long removeDevice(String token) {
        Query query = new Query();
        query.addCriteria(Criteria.where("token").is(token));
        var result = mongoTemplate.remove(query, "subscription");
        return result.getDeletedCount();
    }
}
