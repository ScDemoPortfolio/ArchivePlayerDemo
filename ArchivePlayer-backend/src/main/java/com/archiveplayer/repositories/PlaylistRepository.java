package com.archiveplayer.repositories;

import com.archiveplayer.entities.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByAccount_Id(Long accountId);

    Optional<Playlist> findByNameAndAccount_Id(String name, Long accountId);

    @Query("SELECT playlists FROM Playlist playlists WHERE playlists.account.id = :accountId ORDER BY random() LIMIT :limit")
    List<Playlist> findRandomPlaylists(@Param("accountId") Long accountId, @Param("limit") int limit);
}