package com.yusuf.exifsystem.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import com.yusuf.exifsystem.config.StorageProperties;
import com.yusuf.exifsystem.services.service.ImageService;
import com.yusuf.exifsystem.services.service.StorageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {
    private final ImageService imageService;
    private final StorageService storageService;
    private final StorageProperties storageProperties;

    @PostMapping
    public String uploadTest(@RequestParam("images") @Valid MultipartFile[] images) {
        storageService.storeMany(images);
        return "a";
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) throws IOException {
        Resource file = storageService.loadAsResource(storageProperties.getLocation(), fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(file);
    }

}
