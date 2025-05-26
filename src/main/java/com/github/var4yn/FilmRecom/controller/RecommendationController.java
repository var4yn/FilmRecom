package com.github.var4yn.FilmRecom.controller;

import com.github.var4yn.FilmRecom.dto.MovieDTO;
import com.github.var4yn.FilmRecom.security.services.UserDetailsImpl;
import com.github.var4yn.FilmRecom.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class RecommendationController {
    private final RecommendationService recommendationService;
    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MovieDTO>> getUserRecommendations(@PathVariable Long userId, Authentication authentication) {
        logger.info("Запрос рекомендаций для пользователя: {}", userId);
        logger.info("Аутентифицированный пользователь: {}, роли: {}", 
            authentication.getName(),
            authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", ")));
                
        // Проверяем, что у пользователя есть роль ROLE_USER
        boolean hasUserRole = authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER"));
            
        if (!hasUserRole) {
            logger.warn("У пользователя {} нет роли ROLE_USER", authentication.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
                
        List<MovieDTO> recommendations = recommendationService.getUserRecommendations(userId);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/similar/{movieId}")
    public ResponseEntity<List<MovieDTO>> getSimilarMovies(@PathVariable Long movieId) {
        return ResponseEntity.ok(recommendationService.getSimilarMovies(movieId));
    }
} 