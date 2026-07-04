package com.archiveplayer.repositories;

import com.archiveplayer.entities.Song;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    @Query("SELECT s FROM Song s JOIN s.artist a JOIN s.album al WHERE s.title = :title AND a.name = :artistName AND al.name = :albumName")
    List<Song> findDuplicateSongs(@Param("title") String title, @Param("artistName") String artistName, @Param("albumName") String albumName);
    List<Song> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    List<Song> findByAlbumId(Long albumId);
    List<Song> findByArtistId(Long artistId);

    Optional<Song> findByFilePath(String filePath);
}