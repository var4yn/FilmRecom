package com.github.var4yn.FilmRecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class MovieSearchResponse {
    private int page;
    
    @JsonProperty("results")
    private List<MovieDTO> movies;
    
    @JsonProperty("total_pages")
    private int totalPages;
    
    @JsonProperty("total_results")
    private int totalResults;
} 