package com.archiveplayer.controllers;

import com.archiveplayer.dto.SearchResultDTO; // Import SearchResultDTO
import com.archiveplayer.dto.SongDTO;
import com.archiveplayer.services.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResultDTO> globalSearch( // Changed return type
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) { // Added limit parameter
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(new SearchResultDTO(Collections.emptyList())); // Return empty SearchResultDTO
        }
        return ResponseEntity.ok(searchService.globalSearch(query, limit)); // Pass limit to service
    }

    @GetMapping("/albums/{albumId}/songs")
    public ResponseEntity<List<SongDTO>> getSongsByAlbum(@PathVariable Long albumId) {
        return ResponseEntity.ok(searchService.getSongsByAlbum(albumId));
    }

    @GetMapping("/artists/{artistId}/songs")
    public ResponseEntity<List<SongDTO>> getSongsByArtist(@PathVariable Long artistId) {
        return ResponseEntity.ok(searchService.getSongsByArtist(artistId));
    }
}