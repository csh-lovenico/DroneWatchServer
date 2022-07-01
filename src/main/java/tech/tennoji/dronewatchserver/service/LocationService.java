package tech.tennoji.dronewatchserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import tech.tennoji.dronewatchserver.entity.GeoFence;
import tech.tennoji.dronewatchserver.repository.MongoRepository;

import java.util.List;

public class LocationService {

    @Autowired
    private MongoRepository mongoRepository;

    @Autowired
    private FirebaseMessagingService messagingService;

    private final long maxDistance = 100L;

    private final String droneComingMessageTitle = "Drone approaching!";

    private final String droneComingMessageBody = "A drone is near the location you watch: %s";

    public int reportLocation(String droneId, long longitude, long latitude) {
        List<GeoFence> fenceList = mongoRepository.findFencesNear(longitude, latitude, maxDistance);
        if (fenceList.size() > 0) {
            // There is at least one area near the drone
            fenceList.stream().forEach((it) -> {
                String body = String.format(droneComingMessageBody, it.getName());
                messagingService.sendMessageToTopic(droneComingMessageTitle, body, null, it.getName());
                //todo: record drone in area record
            });
            return 1;
        } else {
            // There is no area near the drone
            return 0;
        }
    }




}
