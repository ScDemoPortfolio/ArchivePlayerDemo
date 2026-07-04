package com.archiveplayer.services;

import com.archiveplayer.repositories.AccountRepository;
import com.archiveplayer.repositories.PlaylistRepository;
import com.archiveplayer.repositories.SongRepository;
import com.archiveplayer.dto.PlaylistDTO;
import com.archiveplayer.dto.PlaylistRequestDTO;
import com.archiveplayer.dto.SongDTO;
import com.archiveplayer.entities.Account;
import com.archiveplayer.entities.Playlist;
import com.archiveplayer.entities.Song;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final AccountRepository accountRepository;
    private final SongRepository songRepository;

    public PlaylistService(PlaylistRepository playlistRepository,
                           AccountRepository accountRepository,
                           SongRepository songRepository) {
        this.playlistRepository = playlistRepository;
        this.accountRepository = accountRepository;
        this.songRepository = songRepository;
    }

    @Transactional(readOnly = true)
    public List<PlaylistDTO> getPlaylistsByAccount(Long accountId) {
        return playlistRepository.findByAccount_Id(accountId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlaylistDTO getPlaylistById(Long playlistId) {
        return playlistRepository.findById(playlistId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));
    }

    @Transactional(readOnly = true)
    public List<PlaylistDTO> getRandomPlaylists(Long accountId, int limit) {
        return playlistRepository.findRandomPlaylists(accountId, limit).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PlaylistDTO createPlaylist(PlaylistRequestDTO payload, Account owner) {
        if (owner == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User profile context not found.");
        }
        
        String trimmedName = payload.getName().trim();
        Optional<Playlist> existingPlaylist = playlistRepository.findByNameAndAccount_Id(trimmedName, owner.getId());
        if (existingPlaylist.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A playlist with that name already exists.");
        }
        
        Account attachedOwner = accountRepository.findById(owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
                
        Playlist savedPlaylist = playlistRepository.save(new Playlist(trimmedName, attachedOwner));
        return convertToDTO(savedPlaylist);
    }

    @Transactional
    public void addSongToPlaylist(Long playlistId, Long songId, Account user) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));
        
        if (!playlist.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this playlist");
        }

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found"));
        
        playlist.getSongs().add(song);
        playlistRepository.save(playlist);
    }

    @Transactional
    public void removeSongFromPlaylist(Long playlistId, Long songId, Account user) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));
        
        if (!playlist.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this playlist");
        }

        playlist.getSongs().removeIf(song -> song.getId().equals(songId));
        playlistRepository.save(playlist);
    }

    @Transactional
    public void deletePlaylist(Long playlistId, Account user) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));
        
        if (!playlist.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this playlist");
        }

        playlistRepository.delete(playlist);
    }

    private PlaylistDTO convertToDTO(Playlist playlist) {
        List<SongDTO> songDTOs = playlist.getSongs().stream()
                .map(this::convertSongToDTO)
                .collect(Collectors.toList());
        return new PlaylistDTO(playlist.getId(), playlist.getName(), songDTOs);
    }

    private SongDTO convertSongToDTO(Song song) {
        return new SongDTO(
            song.getId(),
            song.getTitle(),
            song.getDurationInSeconds() != null ? song.getDurationInSeconds() : 0,
            song.getArtist() != null ? song.getArtist().getName() : "Unknown Artist",
            song.getAlbum() != null ? song.getAlbum().getName() : "Unknown Album"
        );
    }
}