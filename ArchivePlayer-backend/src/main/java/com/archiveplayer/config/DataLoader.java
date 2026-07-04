package com.archiveplayer.config;

import com.archiveplayer.repositories.*;
import com.archiveplayer.entities.*;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final AccountRepository accountRepository;
    private final UserSongListenRepository listenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.music.root-path}")
    private String musicRoot;

    @Value("${app.music.default-duration-seconds:180}")
    private int defaultDuration;

    public DataLoader(ArtistRepository artistRepository,
                      AlbumRepository albumRepository,
                      SongRepository songRepository,
                      AccountRepository accountRepository,
                      UserSongListenRepository listenRepository,
                      PasswordEncoder passwordEncoder) {
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.songRepository = songRepository;
        this.accountRepository = accountRepository;
        this.listenRepository = listenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("ArchivePlayer: Initializing Data Seeding...");
        
        File rootDir = new File(musicRoot);
        if (rootDir.exists() && rootDir.isDirectory()) {
            logger.debug("ArchivePlayer: Music root directory found: {}", musicRoot);
            Files.walk(Paths.get(musicRoot))
                    .filter(path -> !Files.isDirectory(path))
                    .filter(path -> path.toString().toLowerCase().endsWith(".mp3"))
                    .forEach(this::processMp3File);
        } else {
            logger.warn("ArchivePlayer: Music root directory NOT FOUND or is not a directory: {}", musicRoot);
        }

        seedDemoData();
        logger.info("ArchivePlayer: Data seeding completed.");
    }

    @Transactional
    protected void seedDemoData() {
        if (accountRepository.count() > 1 && listenRepository.count() > 0) return;

        Account user1 = createAccountIfMissing("MusicLover99", "demo123");
        Account user2 = createAccountIfMissing("VinylCollector", "demo123");
        Account admin = createAccountIfMissing("admin", "admin123");

        user1 = accountRepository.findByIdWithFollowing(user1.getId()).orElse(user1);
        user2 = accountRepository.findByIdWithFollowing(user2.getId()).orElse(user2);
        admin = accountRepository.findByIdWithFollowing(admin.getId()).orElse(admin);

        admin.getFollowing().add(user1);
        admin.getFollowing().add(user2);
        user1.getFollowing().add(admin);
        user2.getFollowing().add(admin);
        
        accountRepository.saveAll(List.of(admin, user1, user2));
        logger.info("ArchivePlayer: Seeded demo accounts: admin, MusicLover99, VinylCollector.");

        List<Song> songs = songRepository.findAll();
        if (!songs.isEmpty()) {
            logger.debug("ArchivePlayer: Found {} songs in the database. Seeding listens...", songs.size());
            for (int i = 0; i < Math.min(songs.size(), 5); i++) {
                seedListen(user1, songs.get(i));
                seedListen(user2, songs.get(songs.size() - 1 - i));
            }
        } else {
            logger.warn("ArchivePlayer: No songs found in the database. Cannot seed listens.");
        }
    }

    private Account createAccountIfMissing(String username, String password) {
        return accountRepository.findByAccountName(username)
                .orElseGet(() -> {
                    logger.debug("ArchivePlayer: Creating account: {}", username);
                    return accountRepository.save(new Account(username, passwordEncoder.encode(password)));
                });
    }

    private void seedListen(Account account, Song song) {
        if (song == null) return;
        listenRepository.save(new UserSongListen(account, song, LocalDateTime.now().minusMinutes((long) (Math.random() * 120))));
        logger.debug("ArchivePlayer: Seeded listen for user {} on song {}", account.getAccountName(), song.getTitle());
    }

    @Transactional
    private void processMp3File(Path path) {
        logger.debug("ArchivePlayer: Processing MP3 file: {}", path.toString());
        try (FileInputStream inputStream = new FileInputStream(path.toFile())) {
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            Mp3Parser mp3Parser = new Mp3Parser();
            ParseContext context = new ParseContext();

            try {
                mp3Parser.parse(inputStream, handler, metadata, context);
            } catch (Exception e) {
                logger.error("ArchivePlayer: Error parsing MP3 metadata for {}: {}", path.toString(), e.getMessage(), e);
                return;
            }

            String title = metadata.get(TikaCoreProperties.TITLE);
            String artistName = metadata.get("xmpDM:artist");
            String albumName = metadata.get("xmpDM:album");
            
            int duration = defaultDuration;
            String durationStr = metadata.get("xmpDM:duration");
            if (durationStr != null) {
                try {
                    duration = (int) (Double.parseDouble(durationStr) / 1000.0);
                } catch (NumberFormatException e) {
                    logger.warn("ArchivePlayer: Could not parse duration for {}: {}", path.toString(), durationStr);
                }
            }

            Path rootPath = Paths.get(musicRoot).toAbsolutePath();
            Path filePath = path.toAbsolutePath();
            String relativePath = rootPath.relativize(filePath).toString().replace("\\", "/");


            if (artistName == null || artistName.isEmpty() || albumName == null || albumName.isEmpty()) {
                String[] parts = relativePath.split("/");
                if (parts.length >= 3) {
                    artistName = parts[0];
                    albumName = parts[1];
                    logger.debug("ArchivePlayer: Using fallback names from path for {}: Artist={}, Album={}", path.toString(), artistName, albumName);
                } else {
                    logger.warn("ArchivePlayer: Insufficient path parts for fallback names for {}. Path: {}", path.toString(), relativePath);
                }
            }

            if (title == null || title.isEmpty()) title = path.getFileName().toString().replace(".mp3", "");
            if (artistName == null || artistName.isEmpty()) artistName = "Unsigned Artist";
            if (albumName == null || albumName.isEmpty()) albumName = "Singles & EPs";

            logger.debug("ArchivePlayer: Extracted metadata for {}: Title='{}', Artist='{}', Album='{}', Duration={}s", path.toString(), title, artistName, albumName, duration);

            final String finalArtistName = artistName;
            Artist artist = artistRepository.findByName(finalArtistName)
                    .orElseGet(() -> {
                        logger.debug("ArchivePlayer: Saving new artist: {}", finalArtistName);
                        return artistRepository.save(new Artist(finalArtistName));
                    });

            final String finalAlbumName = albumName;
            Album album = albumRepository.findByNameAndArtist(finalAlbumName, artist)
                    .orElseGet(() -> {
                        logger.debug("ArchivePlayer: Saving new album: {} by {}", finalAlbumName, finalArtistName);
                        return albumRepository.save(new Album(finalAlbumName, artist));
                    });

            if (songRepository.findByFilePath(relativePath).isEmpty() &&
                songRepository.findDuplicateSongs(title, artistName, albumName).isEmpty()) {
                songRepository.save(new Song(title, duration, album, artist, relativePath));
            } else {
                logger.debug("ArchivePlayer: Song '{}' by '{}' from album '{}' already exists or is a duplicate. Skipping.", title, artistName, albumName);
            }

        } catch (Exception e) {
            logger.error("ArchivePlayer: Unexpected error processing MP3 file {}: {}", path.toString(), e.getMessage(), e);
        }
    }
}