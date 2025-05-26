package com.github.var4yn.FilmRecom.service;

import com.github.var4yn.FilmRecom.converter.MovieConverter;
import com.github.var4yn.FilmRecom.dto.MovieDTO;
import com.github.var4yn.FilmRecom.dto.MovieSearchResponse;
import com.github.var4yn.FilmRecom.model.Genre;
import com.github.var4yn.FilmRecom.model.Movie;
import com.github.var4yn.FilmRecom.repository.GenreRepository;
import com.github.var4yn.FilmRecom.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TMDBService {
    private final RestTemplate restTemplate;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final String apiKey;
    private final String baseUrl;

    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Logger logger = LoggerFactory.getLogger(TMDBService.class);

    public TMDBService(
            RestTemplate restTemplate,
            MovieRepository movieRepository,
            GenreRepository genreRepository,
            @Value("${tmdb.api.key}") String apiKey,
            @Value("${tmdb.api.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");
        headers.set("Authorization", "Bearer " + apiKey);
        return headers;
    }

    public MovieSearchResponse getPopularMovies(int page) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/popular")
                .queryParam("page", page)
                .queryParam("language", "ru-RU")
                .build()
                .toUriString();

        try {
            HttpEntity<?> entity = new HttpEntity<>(createHeaders());
            logger.info("Запрос на получение популярных фильмов {}", entity);
            logger.info("URL: {}", url);
            var el = restTemplate.exchange(url, HttpMethod.GET, entity, MovieSearchResponse.class);
            logger.info("res = {}", el);
            return el.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении популярных фильмов: " + e.getMessage(), e);
        }
    }

    public MovieSearchResponse searchMovies(String query, int page) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search/movie")
                .queryParam("query", query)
                .queryParam("page", page)
                .queryParam("language", "ru-RU")
                .build()
                .toUriString();

        try {
            HttpEntity<?> entity = new HttpEntity<>(createHeaders());
            return restTemplate.exchange(url, HttpMethod.GET, entity, MovieSearchResponse.class).getBody();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске фильмов: " + e.getMessage(), e);
        }
    }

    public Movie getMovieDetails(Long tmdbId) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/movie/" + tmdbId)
                .queryParam("language", "ru-RU")
                .build()
                .toUriString();

        try {
            HttpEntity<?> entity = new HttpEntity<>(createHeaders());
            MovieDTO movieDTO = restTemplate.exchange(url, HttpMethod.GET, entity, MovieDTO.class).getBody();
            logger.info("Получил детали фильма: {}", movieDTO);
            return MovieConverter.toEntity(movieDTO);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении деталей фильма: " + e.getMessage(), e);
        }
    }

    public Movie searchMovieByTitle(String title) {
        MovieSearchResponse response = searchMovies(title, 1);
        if (response != null && !response.getMovies().isEmpty()) {
            return MovieConverter.toEntity(response.getMovies().get(0));
        }
        return null;
    }
}