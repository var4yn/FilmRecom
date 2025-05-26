package com.github.var4yn.FilmRecom.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RatingDTO {
    private Long id;
    private Long userId;
    private Long movieId;
    private double score;
    private LocalDateTime ratedAt;
    private String review;
    private MovieDTO movie;
} 