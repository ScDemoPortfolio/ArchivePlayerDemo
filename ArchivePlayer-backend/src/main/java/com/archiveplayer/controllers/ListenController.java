package com.archiveplayer.controllers;

import com.archiveplayer.dto.RecentListenDTO;
import com.archiveplayer.entities.Account;
import com.archiveplayer.services.ListenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listens")
public class ListenController {

    private final ListenService listenService;

    public ListenController(ListenService listenService) {
        this.listenService = listenService;
    }

    @PostMapping("/record")
    public ResponseEntity<?> recordListen(@AuthenticationPrincipal Account account, @RequestParam Long songId) {
        listenService.recordListen(account, songId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/account/{accountId}/recent")
    public ResponseEntity<List<RecentListenDTO>> getRecentListens(@PathVariable Long accountId) {
        return ResponseEntity.ok(listenService.getRecentListens(accountId));
    }
}