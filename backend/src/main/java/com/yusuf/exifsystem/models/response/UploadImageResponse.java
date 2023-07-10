package com.yusuf.exifsystem.models.response;

import java.util.List;

public record UploadImageResponse(List<String> imagesWithCoordList, List<String> imagesWithoutCoordList,
        String message) {

}
