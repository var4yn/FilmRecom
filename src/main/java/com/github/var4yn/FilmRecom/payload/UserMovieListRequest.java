package com.github.var4yn.FilmRecom.payload;

import lombok.Data;

@Data
public class UserMovieListRequest {
    private String name;
    private String description;
    private Long movieId;
} 