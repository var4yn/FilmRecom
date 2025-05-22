package com.github.var4yn.FilmRecom.service;

import com.github.var4yn.FilmRecom.model.Genre;
import com.github.var4yn.FilmRecom.model.Movie;
import com.github.var4yn.FilmRecom.repository.GenreRepository;
import com.github.var4yn.FilmRecom.repository.MovieRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TMDBService {
    private final String TMDB_API_BASE_URL;
    private final String apiKey;
    private final RestTemplate restTemplate;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;

    public TMDBService(@Value("${tmdb.api.key}") String apiKey,
                       @Value("${tmdb.api.base-url}") String tmdbApiBaseUrl,
                       RestTemplate restTemplate,
                       MovieRepository movieRepository,
                       GenreRepository genreRepository) {
        this.apiKey = apiKey;
        this.TMDB_API_BASE_URL = tmdbApiBaseUrl;
        this.restTemplate = restTemplate;
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
    }

    public void fetchPopularMovies() {
        String url = TMDB_API_BASE_URL + "/movie/popular?api_key=" + apiKey;
        TMDBResponse response = restTemplate.getForObject(url, TMDBResponse.class);

        if (response != null && response.getResults() != null) {
            for (TMDBMovie tmdbMovie : response.getResults()) {
                saveMovieFromTMDB(tmdbMovie);
            }
        }
    }

    public Movie searchMovieByTitle(String title) {
        String url = TMDB_API_BASE_URL + "/search/movie?api_key=" + apiKey + "&query=" + title;
        TMDBResponse response = restTemplate.getForObject(url, TMDBResponse.class);

        if (response != null && !response.getResults().isEmpty()) {
            return saveMovieFromTMDB(response.getResults().get(0));
        }
        return null;
    }

    private Movie saveMovieFromTMDB(TMDBMovie tmdbMovie) {
        Optional<Movie> existingMovie = movieRepository.findByTmdbId(tmdbMovie.getId());
        if (existingMovie.isPresent()) {
            return existingMovie.get();
        }

        Movie movie = new Movie();
        movie.setTmdbId(tmdbMovie.getId());
        movie.setTitle(tmdbMovie.getTitle());
        movie.setOriginalTitle(tmdbMovie.getOriginalTitle());
        movie.setOverview(tmdbMovie.getOverview());
        movie.setPosterPath(tmdbMovie.getPosterPath());
        movie.setBackdropPath(tmdbMovie.getBackdropPath());
        movie.setReleaseDate(tmdbMovie.getReleaseDate());
        movie.setPopularity(tmdbMovie.getPopularity());
        movie.setVoteAverage(tmdbMovie.getVoteAverage());
        movie.setVoteCount(tmdbMovie.getVoteCount());

        // Получить детали по фильму
        String detailsUrl = TMDB_API_BASE_URL + "/movie/" + tmdbMovie.getId() + "?api_key=" + apiKey;
        TMDBMovieDetails details = restTemplate.getForObject(detailsUrl, TMDBMovieDetails.class);

        if (details != null) {
            movie.setRuntime(details.getRuntime());
            movie.setStatus(details.getStatus());

            // Заполняем жанры
            Set<Genre> genres = new HashSet<>();
            for (TMDBGenre tmdbGenre : details.getGenres()) {
                Genre genre = genreRepository.findByTmdbId(tmdbGenre.getId())
                        .orElseGet(() -> {
                            Genre newGenre = new Genre();
                            newGenre.setTmdbId(tmdbGenre.getId());
                            newGenre.setName(tmdbGenre.getName());
                            return genreRepository.save(newGenre);
                        });
                genres.add(genre);
            }
            movie.setGenres(genres);
        }

        return movieRepository.save(movie);
    }

    @Data
    private static class TMDBResponse {
        private List<TMDBMovie> results;
    }

    @Data
    private static class TMDBMovie {
        private Long id;
        private String title;
        private String originalTitle;
        private String overview;
        private String posterPath;
        private String backdropPath;
        private LocalDate releaseDate;
        private Double popularity;
        private Double voteAverage;
        private Integer voteCount;
    }

    @Data
    private static class TMDBMovieDetails extends TMDBMovie {
        private Integer runtime;
        private String status;
        private List<TMDBGenre> genres;
    }

    @Data
    private static class TMDBGenre {
        private Long id;
        private String name;
    }
}