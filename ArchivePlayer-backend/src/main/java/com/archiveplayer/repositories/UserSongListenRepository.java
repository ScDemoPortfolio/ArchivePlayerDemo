package com.archiveplayer.repositories;

import com.archiveplayer.entities.UserSongListen;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface UserSongListenRepository extends JpaRepository<UserSongListen, Long> {

    @Query("SELECT usl FROM UserSongListen usl " +
           "JOIN FETCH usl.account " +
           "JOIN FETCH usl.song s " +
           "LEFT JOIN FETCH s.artist " +
           "LEFT JOIN FETCH s.album " +
           "WHERE usl.account.id = :accountId AND usl.timestamp >= :date " +
           "ORDER BY usl.timestamp DESC")
    List<UserSongListen> findRecentListens(@Param("accountId") Long accountId, @Param("date") LocalDateTime date, Pageable pageable);

    @Query("SELECT usl FROM UserSongListen usl " +
           "JOIN FETCH usl.account " +
           "JOIN FETCH usl.song s " +
           "LEFT JOIN FETCH s.artist " +
           "LEFT JOIN FETCH s.album " +
           "WHERE usl.account.id IN :accountIds AND usl.timestamp >=:date " +
           "ORDER BY usl.timestamp DESC")
    List<UserSongListen> findRecentListensFromAccounts(@Param("accountIds") Set<Long> accountIds, @Param("date") LocalDateTime date, Pageable pageable);
}