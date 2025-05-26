package com.github.var4yn.FilmRecom.repository;

import com.github.var4yn.FilmRecom.model.Watchlist;
import com.github.var4yn.FilmRecom.model.WatchlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    List<Watchlist> findByUserId(Long userId);
    List<Watchlist> findByUserIdAndStatus(Long userId, WatchlistStatus status);
    Watchlist findByUserIdAndMovieId(Long userId, Long movieId);
} 