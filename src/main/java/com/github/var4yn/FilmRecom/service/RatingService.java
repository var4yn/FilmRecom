package com.github.var4yn.FilmRecom.service;

import com.github.var4yn.FilmRecom.model.Movie;
import com.github.var4yn.FilmRecom.model.Rating;
import com.github.var4yn.FilmRecom.model.User;
import com.github.var4yn.FilmRecom.repository.MovieRepository;
import com.github.var4yn.FilmRecom.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepository;
    private final MovieRepository movieRepository;
    private final UserService userService;
    private final TMDBService tmdbService;

    @Transactional
    public Rating rateMovie(Long userId, Long movieId, Integer rating, String review) {
        User user = userService.findById(userId);
        
        // Получаем или создаем фильм
        Movie movie = movieRepository.findByTmdbId(movieId)
                .orElseGet(() -> {
                    // Получаем полную информацию о фильме из TMDB
                    Movie newMovie = tmdbService.getMovieDetails(movieId);
                    if (newMovie == null) {
                        // Если не удалось получить информацию, создаем базовый объект
                        newMovie = new Movie();
                        newMovie.setTmdbId(movieId);
                        newMovie.setTitle("Фильм #" + movieId);
                    }
                    return movieRepository.save(newMovie);
                });

        Rating existingRating = ratingRepository.findByUserAndMovie(user, movie)
                .orElse(new Rating());

        existingRating.setUser(user);
        existingRating.setMovie(movie);
        existingRating.setScore(rating.doubleValue());
        existingRating.setReview(review);

        return ratingRepository.save(existingRating);
    }

    public Double getAverageRatingForMovie(Long movieId) {
        return ratingRepository.getAverageRatingForMovie(movieId);
    }

    public List<Rating> getUserRatings(Long userId) {
        return ratingRepository.findByUserId(userId);
    }
} 