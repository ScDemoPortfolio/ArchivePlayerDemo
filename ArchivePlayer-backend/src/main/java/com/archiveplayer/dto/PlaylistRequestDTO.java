package com.archiveplayer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PlaylistRequestDTO {
    
    @NotBlank(message = "Playlist name cannot be empty")
    @Size(max = 100, message = "Playlist name cannot exceed 100 characters")
    private String name;

    public PlaylistRequestDTO() {}

    public PlaylistRequestDTO(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}