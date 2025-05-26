package com.github.var4yn.FilmRecom.controller;

import com.github.var4yn.FilmRecom.converter.MovieConverter;
import com.github.var4yn.FilmRecom.dto.MovieDTO;
import com.github.var4yn.FilmRecom.exception.ResourceNotFoundException;
import com.github.var4yn.FilmRecom.model.Movie;
import com.github.var4yn.FilmRecom.model.Rating;
import com.github.var4yn.FilmRecom.model.User;
import com.github.var4yn.FilmRecom.repository.MovieRepository;
import com.github.var4yn.FilmRecom.repository.RatingRepository;
import com.github.var4yn.FilmRecom.security.services.UserDetailsImpl;
import com.github.var4yn.FilmRecom.service.RecommendationService;
import com.github.var4yn.FilmRecom.service.TMDBService;
import com.github.var4yn.FilmRecom.service.MovieService;
import com.github.var4yn.FilmRecom.dto.MovieSearchResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {
    private final MovieRepository movieRepository;
    private final RatingRepository ratingRepository;
    private final RecommendationService recommendationService;
    private final TMDBService tmdbService;
    private final MovieService movieService;

    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);

    @GetMapping
    public ResponseEntity<List<MovieDTO>> getAllMovies() {
        List<Movie> movies = movieRepository.findAll();
        return ResponseEntity.ok(movies.stream()
                .map(MovieConverter::toDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDTO> getMovieById(@PathVariable Long id) {
        Movie movie = tmdbService.getMovieDetails(id);
        if (movie == null) {
            throw new ResourceNotFoundException("Movie not found with id: " + id);
        }
        logger.info("getMovieById: {}", movie);
        return ResponseEntity.ok(MovieConverter.toDto(movie));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieDTO>> searchMovies(@RequestParam String query) {
        List<Movie> movies = movieRepository.findByTitleContainingIgnoreCase(query);
        if (movies.isEmpty()) {
            Movie newMovie = tmdbService.searchMovieByTitle(query);
            if (newMovie != null) {
                movies = Collections.singletonList(newMovie);
            }
        }
        return ResponseEntity.ok(movies.stream()
                .map(MovieConverter::toDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<MovieDTO>> getRecommendations(@RequestParam(defaultValue = "10") int limit) {
        User user = new User();
        logger.info("Создаю пользователя для рекомендаций");
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        user.setId(userDetails.getId());

        logger.info("Получаем рекомендации");
        List<MovieDTO> cfRecommendations = recommendationService.getCollaborativeFilteringRecommendations(user, limit);
        if (cfRecommendations.size() < limit) {
            List<MovieDTO> recommendations = recommendationService.getContentBasedRecommendations(user, limit - cfRecommendations.size());
            cfRecommendations.addAll(recommendations);
        }

        logger.info("Рекомендации: {}", cfRecommendations);
        return ResponseEntity.ok(cfRecommendations);
    }

    @PostMapping("/{movieId}/rate")
    public ResponseEntity<Rating> rateMovie(@PathVariable Long movieId, @RequestParam double score) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

        User user = new User();
        user.setId(userDetails.getId());

        Rating rating = ratingRepository.findByUserAndMovie(user, movie)
                .orElse(new Rating());

        rating.setUser(user);
        rating.setMovie(movie);
        rating.setScore(score);
        rating.setRatedAt(LocalDateTime.now());

        Rating savedRating = ratingRepository.save(rating);

        Double averageRating = ratingRepository.calculateAverageRatingByMovie(movie);
        movie.setVoteAverage(averageRating);
        movieRepository.save(movie);

        return ResponseEntity.ok(savedRating);
    }

    @GetMapping("/popular")
    public ResponseEntity<MovieSearchResponse> getPopularMovies(@RequestParam(defaultValue = "1") int page) {
        logger.info("Получен запрос на популярные фильмы из TMDB API");
        return ResponseEntity.ok(tmdbService.getPopularMovies(page));
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<MovieDTO>> getTopRatedMovies() {
        return ResponseEntity.ok(movieService.getTopRatedMovies().stream()
                .map(MovieConverter::toDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<MovieDTO>> getMoviesByGenre(@PathVariable String genre) {
        return ResponseEntity.ok(movieService.getMoviesByGenre(genre).stream()
                .map(MovieConverter::toDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<List<MovieDTO>> getMoviesByYear(@PathVariable Integer year) {
        return ResponseEntity.ok(movieService.getMoviesByYear(year).stream()
                .map(MovieConverter::toDto)
                .collect(Collectors.toList()));
    }
}