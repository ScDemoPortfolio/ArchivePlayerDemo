package com.archiveplayer.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Song")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Song implements Searchable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private Integer durationInSeconds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Artist artist;

    @Column(name = "listen_count", nullable = false)
    private Long listenCount = 0L;

    private String filePath;

    public Song() {}

    public Song(String title, Integer durationInSeconds, Album album, Artist artist, String filePath) {
        this.title = title;
        this.durationInSeconds = durationInSeconds;
        this.album = album;
        this.artist = artist;
        this.filePath = filePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(id, song.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    @JsonProperty("name")
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getDurationInSeconds() { return durationInSeconds; }
    public void setDurationInSeconds(Integer durationInSeconds) { this.durationInSeconds = durationInSeconds; }
    public Album getAlbum() { return album; }
    public void setAlbum(Album album) { this.album = album; }
    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }
    public Long getListenCount() { return listenCount; }
    public void setListenCount(Long listenCount) { this.listenCount = listenCount; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    @Override
    public String getSearchableName() {
        return this.title;
    }

    @Override
    public String getType() {
        return "Song";
    }
}