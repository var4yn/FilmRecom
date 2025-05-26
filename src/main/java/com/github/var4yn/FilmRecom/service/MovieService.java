package com.github.var4yn.FilmRecom.service;

import com.github.var4yn.FilmRecom.controller.AuthController;
import com.github.var4yn.FilmRecom.dto.MovieDTO;
import com.github.var4yn.FilmRecom.dto.MovieSearchResponse;
import com.github.var4yn.FilmRecom.model.Movie;
import com.github.var4yn.FilmRecom.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;
    private final TMDBService tmdbService;

    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);

    @Transactional(readOnly = true)
    public List<Movie> getPopularMovies(int page) {
        return movieRepository.findByOrderByPopularityDesc();
    }

    private Movie convertToMovie(MovieDTO movieDTO) {
        Movie movie = new Movie();
        movie.setTitle(movieDTO.getTitle());
        movie.setPosterUrl(movieDTO.getPosterUrl());
        movie.setTmdbId(movieDTO.getTmdbId());

        if (movieDTO.getReleaseDate() != null) {
            movie.setReleaseYear(movieDTO.getReleaseDate().getYear());
        }

        movie.setVoteAverage(movieDTO.getVoteAverage());
        movie.setOverview(movieDTO.getOverview());
        return movie;
    }

    @Transactional(readOnly = true)
    public Movie getMovieDetails(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseGet(() -> {
                    Movie movie = tmdbService.getMovieDetails(movieId);
                    if (movie != null) {
                        return movieRepository.save(movie);
                    }
                    return null;
                });
    }

    @Transactional(readOnly = true)
    public List<Movie> searchMovies(String query) {
        return movieRepository.findByTitleContainingIgnoreCase(query);
    }

    @Transactional
    public Movie saveMovie(Movie movie) {
        if (movie.getReleaseDate() != null) {
            movie.setReleaseYear(movie.getReleaseDate().getYear());
        }
        return movieRepository.save(movie);
    }

    @Transactional(readOnly = true)
    public Optional<Movie> findByTmdbId(Long tmdbId) {
        return movieRepository.findByTmdbId(tmdbId);
    }

    @Transactional(readOnly = true)
    public List<Movie> getTopRatedMovies() {
        return movieRepository.findByOrderByVoteAverageDesc();
    }

    @Transactional(readOnly = true)
    public List<Movie> getMoviesByGenre(String genre) {
        return movieRepository.findByGenres_NameIgnoreCase(genre);
    }

    @Transactional(readOnly = true)
    public List<Movie> getMoviesByYear(Integer year) {
        return movieRepository.findByReleaseYear(year);
    }
} 