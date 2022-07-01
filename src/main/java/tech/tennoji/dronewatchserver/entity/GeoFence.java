package tech.tennoji.dronewatchserver.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

@Data
public class GeoFence {
    @Id
    private String id;

    private String name;

    private GeoJsonPolygon area;
}
