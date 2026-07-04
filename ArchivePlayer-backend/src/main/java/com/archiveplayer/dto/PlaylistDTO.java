package com.archiveplayer.dto;

import java.util.List;

public class PlaylistDTO {
    private Long id;
    private String name;
    private List<SongDTO> songs;

    public PlaylistDTO() {}

    public PlaylistDTO(Long id, String name, List<SongDTO> songs) {
        this.id = id;
        this.name = name;
        this.songs = songs;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<SongDTO> getSongs() { return songs; }
    public void setSongs(List<SongDTO> songs) { this.songs = songs; }
}