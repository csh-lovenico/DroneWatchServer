package tech.tennoji.dronewatchserver.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import tech.tennoji.dronewatchserver.entity.DroneRecord;
import tech.tennoji.dronewatchserver.entity.GeoFence;
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

    public List<GeoFence> findFencesByLocation(long longitude, long latitude, long distance) {
        GeoJsonPoint point = new GeoJsonPoint(longitude, latitude);
        Query query = new Query();
        query.addCriteria(Criteria.where("location").nearSphere(point).minDistance(0).maxDistance(distance));
        return mongoTemplate.find(query, GeoFence.class, "fence");
    }

    public long findFencesByNameAndLocation(long longitude, long latitude, long distance, String name) {
        GeoJsonPoint point = new GeoJsonPoint(longitude, latitude);
        Query query = new Query();
        query.addCriteria(Criteria.where("name").is(name).andOperator(Criteria.where("location").nearSphere(point).maxDistance(distance)));
        return mongoTemplate.count(query, GeoFence.class, "fence");
    }

    public void addFence(GeoFence fence) {
        mongoTemplate.insert(fence, "fence");
    }

    public void addDroneRecord(DroneRecord record) {
        mongoTemplate.insert(record, "drone_image_record");
    }

    public List<Subscription> findSubscriptionByToken(String token) {
        Query query = new Query();
        query.addCriteria(Criteria.where("token").is(token));
        return mongoTemplate.find(query, Subscription.class, "subscription");
    }

    public List<GeoFence> findAllAreas() {
        return mongoTemplate.findAll(GeoFence.class, "fence");
    }

    public DroneRecord findLatestDroneRecordByDrone(String droneId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("droneId").is(droneId));
        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));
        query.limit(1);
        var result = mongoTemplate.find(query, DroneRecord.class, "drone_image_record");
        if (!result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }

}
