package com.yusuf.exifsystem.services.implementation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import com.yusuf.exifsystem.config.StorageProperties;
import com.yusuf.exifsystem.dtos.mapper.ImageDTOMapper;
import com.yusuf.exifsystem.entities.Image;
import com.yusuf.exifsystem.entities.Coordinates;
import com.yusuf.exifsystem.services.service.StorageService;
import jakarta.transaction.Transactional;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.yusuf.exifsystem.dtos.dto.ImageDTO;
import com.yusuf.exifsystem.models.response.MessageResponse;
import com.yusuf.exifsystem.repositories.ImageRepository;
import com.yusuf.exifsystem.services.service.ImageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ImageServiceImpl implements ImageService {
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private final StorageService storageService;
    private final StorageProperties storageProperties;
    private final ImageRepository imageRepository;

    @Override
    public List<ImageDTO> getImagesBetweenCoordinates(float minLon, float maxLon, float minLat, float maxLat) {
        ImageDTOMapper mapper = new ImageDTOMapper();

        return imageRepository.findImagesInBoundingBox(minLon, maxLon, minLat, maxLat).stream()
                .map(mapper)
                .toList();
    }

    @Override
    public MessageResponse deleteImageByFilename(String filename) {
        Long deletedImage = imageRepository.deleteByFilename(filename);

        if (deletedImage == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image with provided name not found!");
        }

        storageService.deleteOne(filename);
        storageService.deleteOne("thumbnail_" + filename);
        return new MessageResponse("Image deleted");
    }

    @Override
    public MessageResponse uploadImages(MultipartFile[] images) throws ImageProcessingException, IOException {
        storageService.storeMany(images);
        Map<MultipartFile, Coordinates> imagesWithCoordinates = new HashMap<>();

        for (MultipartFile image : images) {
            Coordinates coordinates = getCoordinates(image);
            if (coordinates != null) {
                imagesWithCoordinates.put(image, coordinates);
            }
        }

        saveFileAndGenerateThumbnails(imagesWithCoordinates);
        return new MessageResponse("Images uploaded");
    }

    @Override
    public ResponseEntity<Resource> getImage(String filename) {
        Resource file = storageService.loadAsResource(storageProperties.getLocation(), filename);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(file);
    }

    private void generateThumbnail(MultipartFile image) throws IOException {
        String inputImagePath = "images/" + image.getOriginalFilename();
        File inputFile = new File(inputImagePath);
        String outputPath = "images/" + "thumbnail_" + image.getOriginalFilename();
        Thumbnails.of(inputFile)
                .size(256, 256)
                .toFile(outputPath);
    }

    private void saveFileAndGenerateThumbnails(Map<MultipartFile, Coordinates> imagesWithCoordinates) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Map.Entry<MultipartFile, Coordinates> set : imagesWithCoordinates.entrySet()) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                storageService.store(set.getKey());
                try {
                    generateThumbnail(set.getKey());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Coordinates coordinates = new Coordinates(
                        set.getValue().getLat(),
                        set.getValue().getLon());

                Image image = new Image(
                        coordinates,
                        set.getKey().getOriginalFilename(),
                        "thumbnail_" + set.getKey().getOriginalFilename());
                imageRepository.save(image);
            }, executorService);

            futures.add(future);

        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .join();

        executorService.shutdown();
    }

    private Coordinates getCoordinates(MultipartFile image) throws IOException, ImageProcessingException {
        InputStream inputStream = image.getInputStream();
        Metadata metadata = ImageMetadataReader.readMetadata(inputStream);

        for (Directory directory : metadata.getDirectories()) {
            if (directory instanceof GpsDirectory gpsDirectory) {
                if (gpsDirectory.getGeoLocation() != null) {
                    double lon = gpsDirectory.getGeoLocation().getLongitude();
                    double lat = gpsDirectory.getGeoLocation().getLatitude();
                    return new Coordinates(lat, lon);
                }
            }
        }
        return null;
    }
}
