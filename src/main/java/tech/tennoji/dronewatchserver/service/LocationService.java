package tech.tennoji.dronewatchserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;
import tech.tennoji.dronewatchserver.entity.DroneMetadata;
import tech.tennoji.dronewatchserver.entity.DroneRecord;
import tech.tennoji.dronewatchserver.entity.GeoFence;
import tech.tennoji.dronewatchserver.repository.MongoRepository;
import tech.tennoji.dronewatchserver.repository.RedisRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LocationService {

    @Autowired
    private MongoRepository mongoRepository;

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private FirebaseMessagingService messagingService;

    private final double maxDistance = 100L;

    private final String droneComingMessageTitle = "Drone approaching!";

    private final String droneComingMessageBody = "A drone is near the location you watch: %s";

    private final String droneLeaveMessageTitle = "Drone left";

    private final String droneLeaveMessageBody = "A drone has left an area you watch: %s";

    private final String droneCollectionPrefix = "drone-";

    private final String areaCollectionPrefix = "area-";

    public int reportLocation(String droneId, double longitude, double latitude) {
        List<GeoFence> fenceList = mongoRepository.findFencesByLocation(longitude, latitude, maxDistance);
        if (fenceList.size() > 0) {
            // There is at least one area near the drone
            fenceList.parallelStream().forEach((area) -> {
                String body = String.format(droneComingMessageBody, area.getName());
                messagingService.sendMessageToTopic(droneComingMessageTitle, body, null, area.getName());
                // Add drone-area and area-drone sets to redis, use different prefixes.
                redisRepository.setAdd(droneCollectionPrefix + droneId, area.getName());
                redisRepository.setAdd(areaCollectionPrefix + area.getName(), droneId);
            });
            return 1;
        } else {
            // There is no area near the drone
            return 0;
        }
    }

    public int reportLocationWithImage(String droneId, double longitude, double latitude, String imagePath) {
        DroneRecord record = new DroneRecord();
        record.setMetadata(new DroneMetadata(droneId));
        record.setImagePath(imagePath);
        record.setTimestamp(LocalDateTime.now());
        record.setLocation(new GeoJsonPoint(longitude, latitude));
        mongoRepository.addDroneRecord(record);
        // check if the drone is still in the area.

        // check if the drone enters new areas.
        Set<String> areas = redisRepository.findByKey(droneCollectionPrefix + droneId);
        // get all areas the drone is in.
        List<String> newAreas = mongoRepository.findFencesByLocation(longitude, latitude, maxDistance).parallelStream().map(GeoFence::getName).collect(Collectors.toList());
        // remove the areas the drone has been in from the list.
        newAreas.removeAll(areas);
        newAreas.parallelStream().forEach(area -> {
            String body = String.format(droneComingMessageBody, area);
            messagingService.sendMessageToTopic(droneComingMessageTitle, body, null, area);
            redisRepository.setAdd(droneCollectionPrefix + droneId, area);
            redisRepository.setAdd(areaCollectionPrefix + area, droneId);
        });

        // Get how many areas the drone is in
        var areaCount = new AtomicInteger(areas.size());
        areas.parallelStream().forEach(area -> {
            long count = mongoRepository.findFencesByNameAndLocation(longitude, latitude, maxDistance, area);
            if (count == 0) { // Drone leaves the area
                redisRepository.setRemove(droneCollectionPrefix + droneId, area);
                redisRepository.setRemove(areaCollectionPrefix + area, droneId);
                String body = String.format(droneLeaveMessageBody, area);
                messagingService.sendMessageToTopic(droneLeaveMessageTitle, body, null, area);
                areaCount.decrementAndGet();
            }

        });
        if (areaCount.get() != 0) {
            // Should continue uploading image
            return 0;
        } else {
            // Do not need to upload image,
            return 1;
        }
    }

    public DroneRecord getLatestRecord(String droneId) {
        return mongoRepository.findLatestDroneRecordByDrone(droneId);
    }

    public List<String> getAreaDroneList(String area) {
        return new ArrayList<>(redisRepository.findByKey(areaCollectionPrefix + area));
    }
}
