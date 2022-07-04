package tech.tennoji.dronewatchserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {
    private final Path fileStorageLocation;

    public FileStorageService() {
        this.fileStorageLocation = Paths.get("./upload").toAbsolutePath().normalize();
        try {
            Files.createDirectory(this.fileStorageLocation);
        } catch (FileAlreadyExistsException e) {
            log.warn("Directory already exists.");
        } catch (Exception e) {
            log.error("Cannot create directory.");
        }
    }

    public String storeFile(MultipartFile file) throws Exception {
        // generate random filename
        var uuid = UUID.randomUUID();
//        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = uuid.toString();
        switch (file.getContentType()) {
            // add file extension by MIME type
            case "image/jpeg":
                fileName += ".jpg";
                break;
            case "image/png":
                fileName += ".png";
                break;
            case "image/webp":
                fileName += ".webp";
                break;
        }
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    public Resource getFile(String filename) throws Exception {
        Path filePath = this.fileStorageLocation.resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists()) {
            return resource;
        } else {
            throw new Exception("File not found");
        }
    }

}
