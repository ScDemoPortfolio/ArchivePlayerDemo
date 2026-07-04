package com.archiveplayer.controllers;

import com.archiveplayer.dto.PlaylistDTO;
import com.archiveplayer.dto.PlaylistRequestDTO;
import com.archiveplayer.entities.Account;
import com.archiveplayer.services.PlaylistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<PlaylistDTO>> getPlaylistsByAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(playlistService.getPlaylistsByAccount(accountId));
    }

    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistDTO> getPlaylistById(@PathVariable Long playlistId) {
        return ResponseEntity.ok(playlistService.getPlaylistById(playlistId));
    }

    @GetMapping("/account/{accountId}/random")
    public ResponseEntity<List<PlaylistDTO>> getRandomPlaylists(@PathVariable Long accountId, @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(playlistService.getRandomPlaylists(accountId, limit));
    }

    @PostMapping
    public ResponseEntity<PlaylistDTO> createPlaylist(@Valid @RequestBody PlaylistRequestDTO payload, @AuthenticationPrincipal Account owner) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playlistService.createPlaylist(payload, owner));
    }

    @PostMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<?> addSongToPlaylist(@PathVariable Long playlistId, @PathVariable Long songId, @AuthenticationPrincipal Account user) {
        playlistService.addSongToPlaylist(playlistId, songId, user);
        return ResponseEntity.ok().body("Song added successfully");
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<?> removeSongFromPlaylist(@PathVariable Long playlistId, @PathVariable Long songId, @AuthenticationPrincipal Account user) {
        playlistService.removeSongFromPlaylist(playlistId, songId, user);
        return ResponseEntity.ok().body("Song removed successfully");
    }

    @DeleteMapping("/{playlistId}")
    public ResponseEntity<?> deletePlaylist(@PathVariable Long playlistId, @AuthenticationPrincipal Account user) {
        playlistService.deletePlaylist(playlistId, user);
        return ResponseEntity.ok().body("Playlist deleted successfully");
    }
}