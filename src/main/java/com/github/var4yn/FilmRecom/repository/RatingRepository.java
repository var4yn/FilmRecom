package com.github.var4yn.FilmRecom.repository;

import com.github.var4yn.FilmRecom.model.Movie;
import com.github.var4yn.FilmRecom.model.Rating;
import com.github.var4yn.FilmRecom.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByUserId(Long userId);
    List<Rating> findByMovieId(Long movieId);
    
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.movie.id = ?1")
    Double getAverageRatingForMovie(Long movieId);
    
    @Query("SELECT r FROM Rating r WHERE r.user.id = ?1 AND r.movie.id = ?2")
    Rating findByUserIdAndMovieId(Long userId, Long movieId);

    Optional<Rating> findByUserAndMovie(User user, Movie movie);

    List<Rating> findByUser(User user);

    List<Rating> findByMovie(Movie movie);

    void deleteByUserAndMovie(User user, Movie movie);

    boolean existsByUserAndMovie(User user, Movie movie);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.movie = ?1")
    Double calculateAverageRatingByMovie(Movie movie);
}