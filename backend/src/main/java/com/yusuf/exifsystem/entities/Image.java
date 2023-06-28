package com.yusuf.exifsystem.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Embedded
    private Coordinates coordinates;

    private String filename;
    private String thumbnailName;

    public Image(Coordinates coordinates, String filename, String thumbnailPath) {
        this.coordinates = coordinates;
        this.filename = filename;
        this.thumbnailName = thumbnailPath;
    }
}
