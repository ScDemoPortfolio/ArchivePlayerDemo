package com.archiveplayer.services;

import com.archiveplayer.repositories.UnifiedSearchRepository;
import com.archiveplayer.repositories.SongRepository;
import com.archiveplayer.dto.SearchResultDTO; // Import SearchResultDTO
import com.archiveplayer.dto.SongDTO;
import com.archiveplayer.entities.Searchable; // Import Searchable
import com.archiveplayer.entities.Song;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final UnifiedSearchRepository unifiedSearchRepository;
    private final SongRepository songRepository;

    public SearchService(SongRepository songRepository, UnifiedSearchRepository unifiedSearchRepository) {
        this.songRepository = songRepository;
        this.unifiedSearchRepository = unifiedSearchRepository;
    }

    @Transactional(readOnly = true)
    public SearchResultDTO globalSearch(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return new SearchResultDTO(Collections.emptyList());
        }
        List<Searchable> results = unifiedSearchRepository.globalSearch(query.trim(), limit);
        return new SearchResultDTO(results);
    }

    @Transactional(readOnly = true)
    public List<SongDTO> getSongsByAlbum(Long albumId) {
        return songRepository.findByAlbumId(albumId).stream()
                .map(this::convertSongToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SongDTO> getSongsByArtist(Long artistId) {
        return songRepository.findByArtistId(artistId).stream()
                .map(this::convertSongToDTO)
                .collect(Collectors.toList());
    }

    public SongDTO convertSongToDTO(Song song) {
        return new SongDTO(
            song.getId(),
            song.getTitle(),
            song.getDurationInSeconds(),
            song.getArtist() != null ? song.getArtist().getName() : "Unknown Artist",
            song.getAlbum() != null ? song.getAlbum().getName() : "Unknown Album"
        );
    }
}