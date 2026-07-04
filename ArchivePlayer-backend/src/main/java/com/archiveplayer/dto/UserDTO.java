package com.archiveplayer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserDTO {
    private Long id;
    private String username;
    
    @JsonProperty("isPrivate")
    private boolean isPrivate;
    
    @JsonProperty("isBlockedByMe")
    private boolean isBlockedByMe;
    
    @JsonProperty("isFollowingMe")
    private boolean isFollowingMe;
    
    @JsonProperty("isFollowedByMe")
    private boolean isFollowedByMe;

    public UserDTO() {}

    public UserDTO(Long id, String username, boolean isPrivate) {
        this.id = id;
        this.username = username;
        this.isPrivate = isPrivate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }
    
    public boolean isBlockedByMe() { return isBlockedByMe; }
    public void setBlockedByMe(boolean blockedByMe) { isBlockedByMe = blockedByMe; }
    
    public boolean isFollowingMe() { return isFollowingMe; }
    public void setFollowingMe(boolean followingMe) { isFollowingMe = followingMe; }

    public boolean isFollowedByMe() { return isFollowedByMe; }
    public void setFollowedByMe(boolean followedByMe) { isFollowedByMe = followedByMe; }
}