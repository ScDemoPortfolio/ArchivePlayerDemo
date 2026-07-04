package com.archiveplayer.repositories;

import com.archiveplayer.entities.Album;
import com.archiveplayer.entities.Artist;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByNameContainingIgnoreCase(String cleanQuery, Pageable pageable);

    Optional<Album> findByNameAndArtist(String name, Artist artist);
}