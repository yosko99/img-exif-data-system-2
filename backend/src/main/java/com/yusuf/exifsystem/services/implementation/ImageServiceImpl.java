package com.yusuf.exifsystem.services.implementation;

import java.awt.*;
import java.awt.image.BufferedImage;
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
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.yusuf.exifsystem.dtos.dto.ImageDTO;
import com.yusuf.exifsystem.models.response.MessageResponse;
import com.yusuf.exifsystem.models.response.UploadImageResponse;
import com.yusuf.exifsystem.repositories.ImageRepository;
import com.yusuf.exifsystem.services.service.ImageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;

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
        log.info("Fetching images in bounding box lon ({})-({}) and lat ({})-({})", minLon, maxLon, minLat, maxLat);

        return imageRepository.findImagesInBoundingBox(minLon, maxLon, minLat, maxLat).stream()
                .map(new ImageDTOMapper())
                .toList();
    }

    @Override
    public MessageResponse deleteImageByFilename(String filename) {
        Long deletedImage = imageRepository.deleteByFilename(filename);
        log.info("Deleting image with name ({})", filename);

        if (deletedImage == 0) {
            log.warn("Could not find image with provided name ({})", filename);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image with provided name not found!");
        }

        storageService.deleteOne(filename);
        storageService.deleteOne("thumbnail_" + filename);
        log.info("Image with name ({}) deleted", filename);
        return new MessageResponse("Image deleted");
    }

    @Override
    public UploadImageResponse uploadImages(MultipartFile[] images) throws ImageProcessingException, IOException {
        log.info("Uploading images");
        storageService.storeMany(images);
        Map<MultipartFile, Coordinates> imagesWithCoordinates = new HashMap<>();

        List<String> namesOfImagesWithCoordinates = new ArrayList<>();
        List<String> namesOfImagesWithoutCoordinates = new ArrayList<>();

        for (MultipartFile image : images) {
            Coordinates coordinates = getCoordinates(image);
            if (coordinates != null) {
                namesOfImagesWithCoordinates.add(image.getOriginalFilename());
                imagesWithCoordinates.put(image, coordinates);
            } else {
                namesOfImagesWithoutCoordinates.add(image.getOriginalFilename());
                storageService.deleteOne(image.getOriginalFilename());
            }
        }

        saveFileAndGenerateThumbnails(imagesWithCoordinates);

        String responseString = String.format(
                "%d Images are uploaded and %d are not, because they do not have coordinates",
                namesOfImagesWithCoordinates.size(),
                namesOfImagesWithoutCoordinates.size());
        log.info(responseString);

        return new UploadImageResponse(
                namesOfImagesWithCoordinates,
                namesOfImagesWithoutCoordinates,
                responseString);
    }

    @Override
    public ResponseEntity<Resource> getImage(String filename) {
        Resource file = storageService.loadAsResource(storageProperties.getLocation(), filename);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(file);
    }

    private void generateThumbnail(MultipartFile image) throws IOException {
        log.info("Generating thumbnail of image ({})", image.getOriginalFilename());

        String imagePath = "images/" + image.getOriginalFilename();
        String outputImagePath = "images/thumbnail_" + image.getOriginalFilename();

        File inputFile = new File(imagePath);
        BufferedImage inputImage = ImageIO.read(inputFile);

        BufferedImage thumbnail = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = thumbnail.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics2D.drawImage(inputImage, 0, 0, 256, 256, null);
        graphics2D.dispose();

        File outputFile = new File(outputImagePath);
        ImageIO.write(thumbnail, "jpg", outputFile);

        log.info("Thumbnail created");
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
