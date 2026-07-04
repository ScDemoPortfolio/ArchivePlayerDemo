package com.archiveplayer.dto;

import java.time.LocalDateTime;

public class RecentListenDTO {
    private Long id;
    private LocalDateTime timestamp;
    private String songTitle;
    private String artistName;
    private String albumTitle;
    private Long listenCount;
    private String listenerUsername;

    public RecentListenDTO() {}

    public RecentListenDTO(Long id, LocalDateTime timestamp, String songTitle, String artistName, String albumTitle, Long listenCount, String listenerUsername) {
        this.id = id;
        this.timestamp = timestamp;
        this.songTitle = songTitle;
        this.artistName = artistName;
        this.albumTitle = albumTitle;
        this.listenCount = listenCount;
        this.listenerUsername = listenerUsername;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getSongTitle() { return songTitle; }
    public void setSongTitle(String songTitle) { this.songTitle = songTitle; }
    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public String getAlbumTitle() { return albumTitle; }
    public void setAlbumTitle(String albumTitle) { this.albumTitle = albumTitle; }
    public Long getListenCount() { return listenCount; }
    public void setListenCount(Long listenCount) { this.listenCount = listenCount; }
    public String getListenerUsername() { return listenerUsername; }
    public void setListenerUsername(String listenerUsername) { this.listenerUsername = listenerUsername; }
}