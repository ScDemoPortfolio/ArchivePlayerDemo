package com.archiveplayer.services;

import com.archiveplayer.repositories.AccountRepository;
import com.archiveplayer.repositories.SongRepository;
import com.archiveplayer.repositories.UserSongListenRepository;
import com.archiveplayer.dto.RecentListenDTO;
import com.archiveplayer.entities.Account;
import com.archiveplayer.entities.Song;
import com.archiveplayer.entities.UserSongListen;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListenService {

    private final UserSongListenRepository userSongListenRepository;
    private final AccountRepository accountRepository;
    private final SongRepository songRepository;

    public ListenService(UserSongListenRepository userSongListenRepository,
                        AccountRepository accountRepository,
                        SongRepository songRepository) {
        this.userSongListenRepository = userSongListenRepository;
        this.accountRepository = accountRepository;
        this.songRepository = songRepository;
    }

    @Transactional
    public void recordListen(Account account, Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found"));
        //TODO: update to prevent DB locking on concurrent listens
        song.setListenCount(song.getListenCount() + 1);
        songRepository.save(song);

        UserSongListen listen = new UserSongListen(account, song, LocalDateTime.now());
        userSongListenRepository.save(listen);
    }

    @Transactional(readOnly = true)
    public List<RecentListenDTO> getRecentListens(Long accountId) {
        LocalDateTime recentCutoff = LocalDateTime.now().minusHours(24);
        
        List<UserSongListen> listens = userSongListenRepository.findRecentListens(
            accountId, 
            recentCutoff,
            PageRequest.of(0, 100)
        );

        return listens.stream()
                .map(this::convertToRecentListenDTO)
                .collect(Collectors.toList());
    }

    private RecentListenDTO convertToRecentListenDTO(UserSongListen l) {
        return new RecentListenDTO(
            l.getId(),
            l.getTimestamp(),
            l.getSong().getTitle(),
            l.getSong().getArtist() != null ? l.getSong().getArtist().getName() : null,
            l.getSong().getAlbum() != null ? l.getSong().getAlbum().getName() : null,
            l.getSong().getListenCount(),
            l.getAccount().getAccountName()
        );
    }
}