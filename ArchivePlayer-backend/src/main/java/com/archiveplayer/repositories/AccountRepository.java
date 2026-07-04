package com.archiveplayer.repositories;

import com.archiveplayer.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountName(String accountName);

    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.following WHERE a.id = :id")
    Optional<Account> findByIdWithFollowing(Long id);

    List<Account> findByAccountNameContainingIgnoreCase(String accountName);
}