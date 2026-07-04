package com.archiveplayer.services;

import com.archiveplayer.repositories.AccountRepository;
import com.archiveplayer.repositories.UserSongListenRepository;
import com.archiveplayer.dto.RecentListenDTO;
import com.archiveplayer.dto.UserDTO;
import com.archiveplayer.entities.Account;
import com.archiveplayer.entities.UserSongListen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SocialService {

    private static final Logger logger = LoggerFactory.getLogger(SocialService.class);
    private final AccountRepository accountRepository;
    private final UserSongListenRepository userSongListenRepository;

    public SocialService(AccountRepository accountRepository, UserSongListenRepository userSongListenRepository) {
        this.accountRepository = accountRepository;
        this.userSongListenRepository = userSongListenRepository;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> searchAccounts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return accountRepository.findByAccountNameContainingIgnoreCase(query.trim()).stream()
                .filter(account -> !account.isPrivateAccount())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO getAccountDetails(Long accountId, Long currentUserId) {
        Account target = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Account currentUser = accountRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Current user not found"));

        if (target.getBlockedUsers().contains(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This user has blocked you.");
        }

        UserDTO dto = convertToDTO(target);
        dto.setBlockedByMe(currentUser.getBlockedUsers().contains(target));
        dto.setFollowingMe(target.getFollowers().contains(currentUser));
        dto.setFollowedByMe(currentUser.getFollowing().contains(target));

        return dto;
    }

    @Transactional
    public void togglePrivateAccount(Long accountId, boolean isPrivate) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        account.setPrivateAccount(isPrivate);
        accountRepository.save(account);
    }

    @Transactional
    public UserDTO changeUsername(Long accountId, String newUsername) {
        if (newUsername == null || newUsername.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot be empty.");
        }

        String trimmedUsername = newUsername.trim();
        if (accountRepository.findByAccountName(trimmedUsername).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken.");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        account.setAccountName(trimmedUsername);
        return convertToDTO(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> getFollowCounts(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Map<String, Integer> counts = new HashMap<>();
        counts.put("followers", account.getFollowers().size());
        counts.put("following", account.getFollowing().size());
        return counts;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getFollowers(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
                .getFollowers().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getFollowing(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
                .getFollowing().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void followAccount(Long followerId, Long targetId) {
        if (followerId.equals(targetId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot follow yourself.");
        }
        Account follower = accountRepository.findById(followerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Account target = accountRepository.findById(targetId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (target.getBlockedUsers().contains(follower) || follower.getBlockedUsers().contains(target)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action not allowed due to block.");
        }

        follower.addFollowing(target);
        target.addFollower(follower);
        accountRepository.save(follower);
        accountRepository.save(target);
    }

    @Transactional
    public void unfollowAccount(Long followerId, Long targetId) {
        Account follower = accountRepository.findById(followerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Account target = accountRepository.findById(targetId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        follower.removeFollowing(target);
        target.removeFollower(follower);
        accountRepository.save(follower);
        accountRepository.save(target);
    }

    @Transactional
    public void blockAccount(Long userId, Long targetId) {
        Account user = accountRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Account target = accountRepository.findById(targetId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        user.removeFollowing(target);
        target.removeFollower(user);
        target.removeFollowing(user);
        user.removeFollower(target);

        user.addBlock(target);
        accountRepository.save(user);
        accountRepository.save(target);
    }

    @Transactional
    public void unblockAccount(Long userId, Long targetId) {
        Account blocker = accountRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Account target = accountRepository.findById(targetId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        blocker.removeBlock(target);
        accountRepository.save(blocker);
    }

    @Transactional(readOnly = true)
    public List<RecentListenDTO> getFollowingRecentListens(Long accountId) {
        Account currentUser = accountRepository.findById(accountId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Set<Account> followedAccounts = currentUser.getFollowing();

        if (followedAccounts.isEmpty()) {
            return List.of();
        }

        Set<Long> followedIds = followedAccounts.stream().map(Account::getId).collect(Collectors.toSet());

        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        List<UserSongListen> allRecentListens = userSongListenRepository.findRecentListensFromAccounts(followedIds, twentyFourHoursAgo, PageRequest.of(0, 10));

        return allRecentListens.stream()
                .map(this::convertToRecentListenDTO)
                .collect(Collectors.toList());
    }

    private UserDTO convertToDTO(Account account) {
        return new UserDTO(account.getId(), account.getAccountName(), account.isPrivateAccount());
    }

    private RecentListenDTO convertToRecentListenDTO(UserSongListen l) {
        String title = "Unknown Title";
        String artist = "Unknown Artist";
        String album = "Unknown Album";
        String listener = "Unknown User";
        
        try {
            if (l.getSong() != null) {
                title = l.getSong().getTitle();
                if (l.getSong().getArtist() != null) artist = l.getSong().getArtist().getName();
                if (l.getSong().getAlbum() != null) album = l.getSong().getAlbum().getName();
            }
            if (l.getAccount() != null) {
                listener = l.getAccount().getAccountName();
            }
        } catch (Exception e) {
            logger.error("Error converting listen record to DTO", e);
        }

        return new RecentListenDTO(
            l.getId(),
            l.getTimestamp(),
            title,
            artist,
            album,
            l.getSong() != null ? l.getSong().getListenCount() : 0L,
            listener
        );
    }
}