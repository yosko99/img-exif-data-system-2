package com.yusuf.exifsystem.dtos.mapper;

import java.util.function.Function;

import com.yusuf.exifsystem.dtos.dto.ImageDTO;
import com.yusuf.exifsystem.entities.Image;

public class ImageDTOMapper implements Function<Image, ImageDTO> {

    @Override
    public ImageDTO apply(Image image) {
        return new ImageDTO(image.getFilepath(), image.getThumbnailPath());
    }

}
