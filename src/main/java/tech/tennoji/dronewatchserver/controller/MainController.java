package tech.tennoji.dronewatchserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.tennoji.dronewatchserver.entity.DroneRecord;
import tech.tennoji.dronewatchserver.entity.FenceStatus;
import tech.tennoji.dronewatchserver.entity.JsonResponse;
import tech.tennoji.dronewatchserver.service.FileStorageService;
import tech.tennoji.dronewatchserver.service.LocationService;
import tech.tennoji.dronewatchserver.service.SubscriptionService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@Slf4j
public class MainController {

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    LocationService locationService;

    @Autowired
    SubscriptionService subscriptionService;

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> downloadImage(@PathVariable String filename,
                                                  HttpServletRequest request) {
        try {
            Resource resource = fileStorageService.getFile(filename);
            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @PostMapping("/upload")
    public JsonResponse<Integer> uploadImage(@RequestParam("droneId") String droneId,
                                             @RequestParam("x") double longitude,
                                             @RequestParam("y") double latitude,
                                             @RequestParam("file") MultipartFile file) {
        try {
            String filename = fileStorageService.storeFile(file);
            int result = locationService.reportLocationWithImage(droneId, longitude, latitude, filename);
            return new JsonResponse<>(HttpStatus.OK.value(), "ok", result);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return new JsonResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), -1);
        }
    }

    @PostMapping("/reportLocation")
    public JsonResponse<Integer> reportLocation(@RequestParam("droneId") String droneId,
                                                @RequestParam("x") double longitude,
                                                @RequestParam("y") double latitude) {
        try {
            int result = locationService.reportLocation(droneId, longitude, latitude);
            return new JsonResponse<>(HttpStatus.OK.value(), "ok", result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new JsonResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), -1);
        }
    }

    @PostMapping("/subscribe")
    public JsonResponse<Integer> subscribeTopic(@RequestParam("token") String token,
                                                @RequestParam("area") String area) {
        try {
            int result = subscriptionService.subscribeToTopic(token, area);
            if (result == 0) {
                return new JsonResponse<>(HttpStatus.OK.value(), "ok", 0);
            } else {
                return new JsonResponse<>(HttpStatus.BAD_REQUEST.value(), "Duplicate subscription", 1);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return new JsonResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), -1);
        }
    }

    @PostMapping("/unsubscribe")
    public JsonResponse<Integer> unsubscribeTopic(@RequestParam("token") String token,
                                                  @RequestParam("area") String area) {
        try {
            subscriptionService.unsubscribeToTopic(token, area);
            return new JsonResponse<>(HttpStatus.OK.value(), "ok", 0);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new JsonResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), -1);
        }
    }

    @GetMapping("/getSubscribedAreas")
    public JsonResponse<List<String>> getSubscribedAreas(@RequestParam("token") String token) {
        try {
            var result = subscriptionService.getSubscribedTopics(token);
            return new JsonResponse<>(HttpStatus.OK.value(), "ok", result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new JsonResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null);
        }
    }

    @GetMapping("/getNotSubscribedAreas")
    public JsonResponse<List<String>> getNotSubscribedAreas(@RequestParam("token") String token) {
        try {
            var result = subscriptionService.getNotSubscribedTopics(token);
            return new JsonResponse<>(HttpStatus.OK.value(), "ok", result);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new JsonResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null);
        }
    }

    @GetMapping("/getLatestRecord")
    public JsonResponse<DroneRecord> getLatestRecord(@RequestParam("droneId") String droneId) {
        try {
            var result = locationService.getLatestRecord(droneId);
            return new JsonResponse<>(HttpStatus.OK.value(), "ok", result);
        } catch (Exception e) {
            return new JsonResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null);

        }
    }

    @GetMapping("/getSubscribedAreaStatus")
    public JsonResponse<List<FenceStatus>> getSubscribedAreaStatus(@RequestParam("token") String token) {
        try {
            var result = subscriptionService.getSubscribedAreaStatus(token);
            return new JsonResponse<>(HttpStatus.OK.value(), "ok", result);
        } catch (Exception e) {
            return new JsonResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null);
        }
    }

    @GetMapping("/getAreaDroneList")
    public JsonResponse<List<String>> getAreaDroneList(@RequestParam("area") String area) {
        try {
            var result = locationService.getAreaDroneList(area);
            return new JsonResponse<>(HttpStatus.OK.value(), "ok", result);
        } catch (Exception e) {
            return new JsonResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null);
        }
    }

}
