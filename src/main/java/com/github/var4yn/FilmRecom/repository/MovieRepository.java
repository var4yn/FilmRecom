package com.github.var4yn.FilmRecom.repository;

import com.github.var4yn.FilmRecom.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByTmdbId(Long tmdbId);
    List<Movie> findByTitleContainingIgnoreCase(String title);
    List<Movie> findByGenres_Id(Long genreId);
    List<Movie> findByReleaseDateBetween(LocalDate start, LocalDate end);
    List<Movie> findByOrderByVoteAverageDesc();
    List<Movie> findByOrderByPopularityDesc();
}