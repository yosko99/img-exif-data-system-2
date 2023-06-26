package com.yusuf.exifsystem.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private float lon;
    private float lat;
    private String filename;
    private String thumbnailName;

    public Image(float lon, float lat, String filename, String thumbnailPath) {
        this.lon = lon;
        this.lat = lat;
        this.filename = filename;
        this.thumbnailName = thumbnailPath;
    }
}
