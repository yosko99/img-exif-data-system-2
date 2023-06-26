package com.yusuf.exifsystem.services.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.yusuf.exifsystem.dtos.dto.ImageDTO;
import com.yusuf.exifsystem.models.response.MessageResponse;

public interface ImageService {
    List<ImageDTO> getImages(float minLon, float maxLon, float minLat, float maxLat);

    MessageResponse deleteImage(long id);

    MessageResponse uploadImages(MultipartFile[] files);
}
