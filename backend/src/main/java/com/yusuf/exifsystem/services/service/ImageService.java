package com.yusuf.exifsystem.services.service;

import java.io.IOException;
import java.util.List;

import com.drew.imaging.ImageProcessingException;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.yusuf.exifsystem.dtos.dto.ImageDTO;
import com.yusuf.exifsystem.models.response.MessageResponse;
import com.yusuf.exifsystem.models.response.UploadImageResponse;

public interface ImageService {
    List<ImageDTO> getImagesBetweenCoordinates(float minLon, float maxLon, float minLat, float maxLat);

    ResponseEntity<Resource> getImage(String filename);

    MessageResponse deleteImageByFilename(String filename);

    UploadImageResponse uploadImages(MultipartFile[] files) throws ImageProcessingException, IOException;
}
