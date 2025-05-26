package com.github.var4yn.FilmRecom.controller;

import com.github.var4yn.FilmRecom.converter.MovieConverter;
import com.github.var4yn.FilmRecom.dto.RatingDTO;
import com.github.var4yn.FilmRecom.model.Rating;
import com.github.var4yn.FilmRecom.security.services.UserDetailsImpl;
import com.github.var4yn.FilmRecom.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/{movieId}")
    @Transactional
    public ResponseEntity<RatingDTO> rateMovie(
            @PathVariable Long movieId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String review) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.info("Рейтинг ставлю {}", userDetails);
        Rating savedRating = ratingService.rateMovie(userDetails.getId(), movieId, rating, review);
        return ResponseEntity.ok(convertToDTO(savedRating));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<Double> getAverageRatingForMovie(@PathVariable Long movieId) {
        return ResponseEntity.ok(ratingService.getAverageRatingForMovie(movieId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RatingDTO>> getUserRatings(@PathVariable Long userId) {
        List<Rating> ratings = ratingService.getUserRatings(userId);
        List<RatingDTO> ratingDTOs = ratings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ratingDTOs);
    }

    private RatingDTO convertToDTO(Rating rating) {
        RatingDTO dto = new RatingDTO();
        dto.setId(rating.getId());
        dto.setUserId(rating.getUser().getId());
        dto.setMovieId(rating.getMovie().getId());
        dto.setScore(rating.getScore());
        dto.setRatedAt(rating.getRatedAt());
        dto.setReview(rating.getReview());
        dto.setMovie(MovieConverter.toDto(rating.getMovie()));
        return dto;
    }
} 