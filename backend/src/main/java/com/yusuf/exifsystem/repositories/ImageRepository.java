package com.yusuf.exifsystem.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.yusuf.exifsystem.entities.Image;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    @Query("SELECT i FROM Image i WHERE i.coordinates.lon BETWEEN :minLon AND :maxLon AND i.coordinates.lat BETWEEN :minLat AND :maxLat")
    List<Image> findImagesInBoundingBox(float minLon, float maxLon, float minLat, float maxLat);

    Image findByFilename(String filename);

    Long deleteByFilename(String filename);
}
