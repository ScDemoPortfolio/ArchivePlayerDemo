package com.archiveplayer.repositories;

import com.archiveplayer.entities.Artist;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    List<Artist> findByNameContainingIgnoreCase(String cleanQuery, Pageable pageable);

    Optional<Artist> findByName(String name);
}