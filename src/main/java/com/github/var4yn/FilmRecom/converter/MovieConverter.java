package com.github.var4yn.FilmRecom.converter;

import com.github.var4yn.FilmRecom.dto.MovieDTO;
import com.github.var4yn.FilmRecom.model.Movie;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class MovieConverter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static Movie toEntity(MovieDTO dto) {
        if (dto == null) {
            return null;
        }

        Movie movie = new Movie();
        // Основные поля
        movie.setTmdbId(dto.getId());
        movie.setTitle(dto.getTitle());
        movie.setOverview(dto.getOverview());
        movie.setVoteAverage(dto.getVoteAverage());
        
        // Пути к изображениям
        movie.setPosterPath(dto.getPosterPath());
        movie.setPosterUrl(dto.getPosterUrl());
        
        // Дата релиза
        movie.setReleaseDate(dto.getReleaseDate());
        if (dto.getReleaseDate() != null) {
            movie.setReleaseYear(dto.getReleaseDate().getYear());
        }

        // Дополнительные поля из TMDB
        movie.setOriginalTitle(dto.getOriginalTitle());
        movie.setOriginalLanguage(dto.getOriginalLanguage());
        movie.setAdult(dto.getAdult());
        movie.setPopularity(dto.getPopularity());
        movie.setVoteCount(dto.getVoteCount());
        movie.setBackdropPath(dto.getBackdropPath());

        return movie;
    }

    public static MovieDTO toDto(Movie entity) {
        if (entity == null) {
            return null;
        }

        MovieDTO dto = new MovieDTO();
        // Основные поля
        dto.setId(entity.getId());
        dto.setTmdbId(entity.getTmdbId());
        dto.setTitle(entity.getTitle());
        dto.setOverview(entity.getOverview());
        dto.setVoteAverage(entity.getVoteAverage());
        
        // Пути к изображениям
        dto.setPosterPath(entity.getPosterPath());
        
        // Дата релиза
        dto.setReleaseDate(entity.getReleaseDate());
        dto.setReleaseYear(entity.getReleaseYear());

        // Дополнительные поля из TMDB
        dto.setOriginalTitle(entity.getOriginalTitle());
        dto.setOriginalLanguage(entity.getOriginalLanguage());
        dto.setAdult(entity.getAdult());
        dto.setPopularity(entity.getPopularity());
        dto.setVoteCount(entity.getVoteCount());
        dto.setBackdropPath(entity.getBackdropPath());

        return dto;
    }
} 