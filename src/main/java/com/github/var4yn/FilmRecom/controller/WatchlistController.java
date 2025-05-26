package com.github.var4yn.FilmRecom.controller;

import com.github.var4yn.FilmRecom.model.Watchlist;
import com.github.var4yn.FilmRecom.model.WatchlistStatus;
import com.github.var4yn.FilmRecom.security.services.UserDetailsImpl;
import com.github.var4yn.FilmRecom.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {
    private final WatchlistService watchlistService;

    @PostMapping("/{movieId}")
    public ResponseEntity<Watchlist> addToWatchlist(
            @PathVariable Long movieId,
            @RequestParam WatchlistStatus status) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(watchlistService.addToWatchlist(userDetails.getId(), movieId, status));
    }

    @GetMapping
    public ResponseEntity<List<Watchlist>> getWatchlist() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(watchlistService.getWatchlist(userDetails.getId()));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Watchlist>> getWatchlistByStatus(@PathVariable WatchlistStatus status) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(watchlistService.getWatchlistByStatus(userDetails.getId(), status));
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> removeFromWatchlist(@PathVariable Long movieId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        watchlistService.removeFromWatchlist(userDetails.getId(), movieId);
        return ResponseEntity.ok().build();
    }
} 