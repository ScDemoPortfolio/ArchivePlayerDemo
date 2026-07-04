package com.archiveplayer.dto;

public class AuthResponseDTO {
    private Long id;
    private String username;
    private String sessionToken;

    public AuthResponseDTO() {}

    public AuthResponseDTO(Long id, String username, String sessionToken) {
        this.id = id;
        this.username = username;
        this.sessionToken = sessionToken;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
}