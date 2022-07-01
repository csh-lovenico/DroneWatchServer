package tech.tennoji.dronewatchserver.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.LocalDateTime;

@Data
public class DroneRecord {
    @Id
    private String id;

    private DroneMetadata metadata;

    private GeoJsonPoint location;

    private LocalDateTime timestamp;

    private String imagePath;

}