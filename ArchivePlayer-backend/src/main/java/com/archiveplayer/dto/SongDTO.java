package com.archiveplayer.dto;

public class SongDTO {
    private Long id;
    private String title;
    private Integer durationInSeconds;
    private String artistName;
    private String albumTitle;

    public SongDTO() {}

    public SongDTO(Long id, String title, Integer durationInSeconds, String artistName, String albumTitle) {
        this.id = id;
        this.title = title;
        this.durationInSeconds = durationInSeconds;
        this.artistName = artistName;
        this.albumTitle = albumTitle;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getDurationInSeconds() { return durationInSeconds; }
    public void setDurationInSeconds(Integer durationInSeconds) { this.durationInSeconds = durationInSeconds; }
    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public String getAlbumTitle() { return albumTitle; }
    public void setAlbumTitle(String albumTitle) { this.albumTitle = albumTitle; }
}