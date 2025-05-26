package com.github.var4yn.FilmRecom.service;

import com.github.var4yn.FilmRecom.model.Movie;
import com.github.var4yn.FilmRecom.model.User;
import com.github.var4yn.FilmRecom.model.Watchlist;
import com.github.var4yn.FilmRecom.model.WatchlistStatus;
import com.github.var4yn.FilmRecom.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {
    private final WatchlistRepository watchlistRepository;
    private final UserService userService;
    private final MovieService movieService;

    public Watchlist addToWatchlist(Long userId, Long movieId, WatchlistStatus status) {
        User user = userService.findById(userId);
        Movie movie = movieService.getMovieDetails(movieId);

        Watchlist existingWatchlist = watchlistRepository.findByUserIdAndMovieId(userId, movieId);
        if (existingWatchlist != null) {
            existingWatchlist.setStatus(status);
            return watchlistRepository.save(existingWatchlist);
        }

        Watchlist watchlist = new Watchlist();
        watchlist.setUser(user);
        watchlist.setMovie(movie);
        watchlist.setStatus(status);
        watchlist.setAddedAt(LocalDateTime.now());

        return watchlistRepository.save(watchlist);
    }

    public List<Watchlist> getWatchlist(Long userId) {
        return watchlistRepository.findByUserId(userId);
    }

    public List<Watchlist> getWatchlistByStatus(Long userId, WatchlistStatus status) {
        return watchlistRepository.findByUserIdAndStatus(userId, status);
    }

    public void removeFromWatchlist(Long userId, Long movieId) {
        Watchlist watchlist = watchlistRepository.findByUserIdAndMovieId(userId, movieId);
        if (watchlist != null) {
            watchlistRepository.delete(watchlist);
        }
    }
} 