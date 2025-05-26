package com.github.var4yn.FilmRecom.repository;

import com.github.var4yn.FilmRecom.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByOrderByPopularityDesc();
    List<Movie> findByTitleContainingIgnoreCase(String title);
    Optional<Movie> findByTmdbId(Long tmdbId);
    List<Movie> findByOrderByVoteAverageDesc();
    List<Movie> findByGenres_NameIgnoreCase(String genre);
    List<Movie> findByReleaseYear(Integer year);
    List<Movie> findByReleaseDateBetween(LocalDate start, LocalDate end);
    @Query("SELECT m FROM Movie m ORDER BY m.voteAverage DESC")
    List<Movie> findTopRatedMovies();
    
    @Query("SELECT m FROM Movie m JOIN m.genres g WHERE g.id = ?1")
    List<Movie> findByGenres_Id(Long genreId);
    
    @Query("SELECT m FROM Movie m WHERE m.voteAverage >= ?1")
    List<Movie> findByVoteAverageGreaterThanEqual(double minRating);
}