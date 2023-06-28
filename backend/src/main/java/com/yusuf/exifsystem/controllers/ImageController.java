package com.yusuf.exifsystem.controllers;

import com.yusuf.exifsystem.dtos.dto.ImageDTO;
import com.yusuf.exifsystem.models.response.MessageResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import com.drew.imaging.ImageProcessingException;
import com.yusuf.exifsystem.services.service.ImageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images/")
@CrossOrigin
public class ImageController {
    private final ImageService imageService;

    @PostMapping
    public MessageResponse uploadImages(@RequestParam("images") @Valid MultipartFile[] images) throws ImageProcessingException, IOException {
        return imageService.uploadImages(images);
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        return imageService.getImage(filename);
    }

    @GetMapping
    public List<ImageDTO> getImagesBetweenCoordinates(
            @RequestParam(value = "minLon", defaultValue = "0") int minLon,
            @RequestParam(value = "maxLon", defaultValue = "0") int maxLon,
            @RequestParam(value = "minLat", defaultValue = "0") int minLat,
            @RequestParam(value = "maxLat", defaultValue = "0") int maxLat
    ) {
        return imageService.getImagesBetweenCoordinates(minLon, maxLon, minLat, maxLat);
    }

    @DeleteMapping("/{filename:.+}")
    public MessageResponse deleteImageByFilename(@PathVariable String filename) {
        return imageService.deleteImageByFilename(filename);
    }

}
