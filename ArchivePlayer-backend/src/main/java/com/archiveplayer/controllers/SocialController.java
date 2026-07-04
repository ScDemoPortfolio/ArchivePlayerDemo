package com.archiveplayer.controllers;

import com.archiveplayer.dto.RecentListenDTO;
import com.archiveplayer.dto.UserDTO;
import com.archiveplayer.entities.Account;
import com.archiveplayer.services.SocialService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/social")
public class SocialController {

    private final SocialService socialService;

    public SocialController(SocialService socialService) {
        this.socialService = socialService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchAccounts(@RequestParam String query) {
        return ResponseEntity.ok(socialService.searchAccounts(query));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<UserDTO> getAccountDetails(@PathVariable Long accountId, @AuthenticationPrincipal Account currentUser) {
        return ResponseEntity.ok(socialService.getAccountDetails(accountId, currentUser.getId()));
    }

    @PutMapping("/account/{accountId}/private")
    public ResponseEntity<?> togglePrivateAccount(@PathVariable Long accountId, @RequestParam boolean isPrivate) {
        socialService.togglePrivateAccount(accountId, isPrivate);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/account/{accountId}/username")
    public ResponseEntity<UserDTO> changeUsername(@PathVariable Long accountId, @RequestParam String newUsername) {
        return ResponseEntity.ok(socialService.changeUsername(accountId, newUsername));
    }

    @GetMapping("/account/{accountId}/counts")
    public ResponseEntity<Map<String, Integer>> getFollowCounts(@PathVariable Long accountId) {
        return ResponseEntity.ok(socialService.getFollowCounts(accountId));
    }

    @GetMapping("/account/{accountId}/followers")
    public ResponseEntity<List<UserDTO>> getFollowers(@PathVariable Long accountId) {
        return ResponseEntity.ok(socialService.getFollowers(accountId));
    }

    @GetMapping("/account/{accountId}/following")
    public ResponseEntity<List<UserDTO>> getFollowing(@PathVariable Long accountId) {
        return ResponseEntity.ok(socialService.getFollowing(accountId));
    }

    @PostMapping("/follow")
    public ResponseEntity<?> followAccount(@AuthenticationPrincipal Account currentUser, @RequestParam Long accountToFollowId) {
        socialService.followAccount(currentUser.getId(), accountToFollowId);
        return ResponseEntity.ok().body("Followed successfully.");
    }

    @PostMapping("/unfollow")
    public ResponseEntity<?> unfollowAccount(@AuthenticationPrincipal Account currentUser, @RequestParam Long accountToUnfollowId) {
        socialService.unfollowAccount(currentUser.getId(), accountToUnfollowId);
        return ResponseEntity.ok().body("Unfollowed successfully.");
    }

    @PostMapping("/block")
    public ResponseEntity<?> blockAccount(@AuthenticationPrincipal Account currentUser, @RequestParam Long targetId) {
        socialService.blockAccount(currentUser.getId(), targetId);
        return ResponseEntity.ok().body("Blocked successfully.");
    }

    @PostMapping("/unblock")
    public ResponseEntity<?> unblockAccount(@AuthenticationPrincipal Account currentUser, @RequestParam Long targetId) {
        socialService.unblockAccount(currentUser.getId(), targetId);
        return ResponseEntity.ok().body("Unblocked successfully.");
    }

    @GetMapping("/following/{accountId}/recent-listens")
    public ResponseEntity<List<RecentListenDTO>> getFollowingRecentListens(@PathVariable Long accountId) {
        return ResponseEntity.ok(socialService.getFollowingRecentListens(accountId));
    }
}