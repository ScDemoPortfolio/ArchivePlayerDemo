package com.archiveplayer.services;

import com.archiveplayer.repositories.SongRepository;
import com.archiveplayer.entities.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class MusicService {

    private static final Logger logger = LoggerFactory.getLogger(MusicService.class);
    private final SongRepository songRepository;

    @Value("${app.music.root-path}")
    private String musicRoot;

    public MusicService(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    public Resource getSongResource(Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found."));

        String relPath = song.getFilePath();
        File file = new File(musicRoot, relPath);

        logger.info("Streaming request for ID: {} | Expected at: {}", songId, file.getAbsolutePath());

        if (!file.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found on disk.");
        }

        return new FileSystemResource(file);
    }

    public Path getSongFilePath(Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found."));

        String relPath = song.getFilePath();
        Path filePath = Paths.get(musicRoot, relPath);

        logger.info("Attempting to stream song ID: {} | Path: {}", songId, filePath.toAbsolutePath());

        if (!Files.exists(filePath)) {
            logger.error("MP3 FILE MISSING: {}", filePath.toAbsolutePath());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found on disk.");
        }
        return filePath;
    }
    
    public String getFileName(Long songId) {
        return songRepository.findById(songId)
                .map(s -> new File(s.getFilePath()).getName())
                .orElse("unknown.mp3");
    }
}