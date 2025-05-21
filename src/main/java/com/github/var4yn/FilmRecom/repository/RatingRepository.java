package com.github.var4yn.FilmRecom.repository;

import com.github.var4yn.FilmRecom.model.Movie;
import com.github.var4yn.FilmRecom.model.Rating;
import com.github.var4yn.FilmRecom.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserAndMovie(User user, Movie movie);
    List<Rating> findByUser(User user);
    List<Rating> findByMovie(Movie movie);
    Double calculateAverageRatingByMovie(Movie movie);
}