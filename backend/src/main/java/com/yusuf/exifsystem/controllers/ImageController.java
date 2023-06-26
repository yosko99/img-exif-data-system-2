package com.yusuf.exifsystem.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yusuf.exifsystem.services.service.ImageService;
import com.yusuf.exifsystem.services.service.StorageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {
    private final ImageService imageService;
    private final StorageService storageService;

    @PostMapping
    public String uploadTest(@RequestParam("images") @Valid MultipartFile[] images) {
        System.out.println(images.length);
        return "a";
    }

}
