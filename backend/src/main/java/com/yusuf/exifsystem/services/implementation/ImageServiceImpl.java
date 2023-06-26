package com.yusuf.exifsystem.services.implementation;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.yusuf.exifsystem.dtos.dto.ImageDTO;
import com.yusuf.exifsystem.models.response.MessageResponse;
import com.yusuf.exifsystem.repositories.ImageRepository;
import com.yusuf.exifsystem.services.service.ImageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {
    private final ImageRepository imageRepository;

    @Override
    public List<ImageDTO> getImages(float minLon, float maxLon, float minLat, float maxLat) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getImages'");
    }

    @Override
    public MessageResponse deleteImage(long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteImage'");
    }

    @Override
    public MessageResponse uploadImages(MultipartFile[] files) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'uploadImages'");
    }

    @Override
    public ImageDTO getImage(String filename) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getImage'");
    }
}
