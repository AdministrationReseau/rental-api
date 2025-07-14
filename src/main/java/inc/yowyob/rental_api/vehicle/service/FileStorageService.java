package inc.yowyob.rental_api.vehicle.service;

import inc.yowyob.rental_api.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(AppProperties appProperties) {
        this.fileStorageLocation = Paths.get(appProperties.getFile().getUploadDir())
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File upload directory created at: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file, UUID organizationId, String resourceType) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = FilenameUtils.getExtension(originalFileName);
        String newFileName = UUID.randomUUID() + "." + extension;

        try {
            if (originalFileName.contains("..")) {
                throw new RuntimeException("Filename contains invalid path sequence " + originalFileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored file {} as {}", originalFileName, newFileName);

            return newFileName;
        } catch (IOException ex) {
            log.error("Could not store file {}. Please try again!", originalFileName, ex);
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                log.warn("File not found or not readable: {}", fileName);
                throw new RuntimeException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }
    
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", fileName);
        } catch (IOException ex) {
            log.error("Could not delete file {}", fileName, ex);
            throw new RuntimeException("Could not delete file " + fileName, ex);
        }
    }
}