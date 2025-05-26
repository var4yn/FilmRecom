package com.github.var4yn.FilmRecom.service;

import com.github.var4yn.FilmRecom.converter.MovieConverter;
import com.github.var4yn.FilmRecom.dto.GenreDTO;
import com.github.var4yn.FilmRecom.dto.MovieDTO;
import com.github.var4yn.FilmRecom.model.Genre;
import com.github.var4yn.FilmRecom.model.Movie;
import com.github.var4yn.FilmRecom.model.Rating;
import com.github.var4yn.FilmRecom.model.User;
import com.github.var4yn.FilmRecom.repository.MovieRepository;
import com.github.var4yn.FilmRecom.repository.RatingRepository;
import com.github.var4yn.FilmRecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);
    private final MovieRepository movieRepository;
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final MovieService movieService;
    private final TMDBService tmdbService;

    @Transactional(readOnly = true)
    public List<MovieDTO> getContentBasedRecommendations(User user, int limit) {
        // Получить оценки пользователя
        List<Rating> userRatings = ratingRepository.findByUser(user);

        if (userRatings.isEmpty()) {
            return getPopularMovies(limit).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        // Найти любимые жанры
        Map<Genre, Long> genreCounts = userRatings.stream()
                .filter(r -> r.getScore() >= 4.0)
                .flatMap(r -> r.getMovie().getGenres().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        if (genreCounts.isEmpty()) {
            return getPopularMovies(limit).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        // Получить топ-3 жанра
        List<Genre> favoriteGenres = genreCounts.entrySet().stream()
                .sorted(Map.Entry.<Genre, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        // Найти фильмы в этих жанрах, которые пользователь не оценил
        Set<Long> ratedMovieIds = userRatings.stream()
                .map(r -> r.getMovie().getId())
                .collect(Collectors.toSet());

        List<Movie> recommendations = new ArrayList<>();

        for (Genre genre : favoriteGenres) {
            List<Movie> genreMovies = movieRepository.findByGenres_Id(genre.getId()).stream()
                    .filter(m -> !ratedMovieIds.contains(m.getId()))
                    .sorted(Comparator.comparingDouble(Movie::getVoteAverage).reversed())
                    .limit(limit / favoriteGenres.size())
                    .toList();

            recommendations.addAll(genreMovies);
        }

        // Если недостаточно рекомендаций, то вернуть популярные фильмы
        if (recommendations.size() < limit) {
            int remaining = limit - recommendations.size();
            List<Movie> popularMovies = getPopularMovies(remaining).stream()
                    .filter(m -> !ratedMovieIds.contains(m.getId()))
                    .toList();

            recommendations.addAll(popularMovies);
        }

        return recommendations.stream()
                .distinct()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MovieDTO> getCollaborativeFilteringRecommendations(User user, int limit) {
        // Получить список пользователей
        List<User> allUsers = userRepository.findAll();

        // Найти похожих пользователей
        Map<User, Double> userSimilarities = new HashMap<>();

        for (User otherUser : allUsers) {
            if (otherUser.getId().equals(user.getId())) continue;

            double similarity = calculateUserSimilarity(user, otherUser);
            if (similarity > 0.3) { // Граница схожести
                userSimilarities.put(otherUser, similarity);
            }
        }

        if (userSimilarities.isEmpty()) {
            return getContentBasedRecommendations(user, limit);
        }

        // Найти фильмы, которые высоко оценили похожие юзеры.
        Set<Long> ratedMovieIds = ratingRepository.findByUser(user).stream()
                .map(r -> r.getMovie().getId())
                .collect(Collectors.toSet());

        Map<Movie, Double> movieScores = new HashMap<>();

        for (Map.Entry<User, Double> entry : userSimilarities.entrySet()) {
            User similarUser = entry.getKey();
            double similarity = entry.getValue();

            List<Rating> similarUserRatings = ratingRepository.findByUser(similarUser);

            for (Rating rating : similarUserRatings) {
                if (rating.getScore() >= 4.0 && !ratedMovieIds.contains(rating.getMovie().getId())) {
                    double weightedScore = rating.getScore() * similarity;
                    movieScores.merge(rating.getMovie(), weightedScore, Double::sum);
                }
            }
        }

        // Отсортировать по весу
        return movieScores.entrySet().stream()
                .sorted(Map.Entry.<Movie, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    private double calculateUserSimilarity(User user1, User user2) {
        // Получить совпадающие оценки
        List<Rating> user1Ratings = ratingRepository.findByUser(user1);
        List<Rating> user2Ratings = ratingRepository.findByUser(user2);

        Map<Long, Double> user1RatingMap = user1Ratings.stream()
                .collect(Collectors.toMap(r -> r.getMovie().getId(), Rating::getScore));

        Map<Long, Double> user2RatingMap = user2Ratings.stream()
                .collect(Collectors.toMap(r -> r.getMovie().getId(), Rating::getScore));

        // Найти совпадающие фильмы
        Set<Long> commonMovieIds = new HashSet<>(user1RatingMap.keySet());
        commonMovieIds.retainAll(user2RatingMap.keySet());

        if (commonMovieIds.isEmpty()) return 0.0;

        // Вычислить коррелирующий коэффициент
        double sum1 = 0.0, sum2 = 0.0;
        double sum1Sq = 0.0, sum2Sq = 0.0;
        double pSum = 0.0;
        int n = commonMovieIds.size();

        for (Long movieId : commonMovieIds) {
            double rating1 = user1RatingMap.get(movieId);
            double rating2 = user2RatingMap.get(movieId);

            sum1 += rating1;
            sum2 += rating2;
            sum1Sq += Math.pow(rating1, 2);
            sum2Sq += Math.pow(rating2, 2);
            pSum += rating1 * rating2;
        }

        double num = pSum - (sum1 * sum2 / n);
        double den = Math.sqrt((sum1Sq - Math.pow(sum1, 2) / n) * (sum2Sq - Math.pow(sum2, 2) / n));

        if (den == 0) return 0.0;

        return num / den;
    }

    @Transactional(readOnly = true)
    private List<Movie> getPopularMovies(int limit) {
        var els = tmdbService.getPopularMovies(1).getMovies().stream().map(MovieConverter::toEntity).toList();
        var res = new ArrayList<>(movieRepository
                .findByOrderByPopularityDesc()
                .stream()
                .limit(limit)
                .toList());
        res.addAll(els);
        return res.stream()
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovieDTO> getRecommendations(Long userId) {
        // Получаем оценки пользователя
        List<Rating> userRatings = ratingRepository.findByUserId(userId);
        
        // Если у пользователя нет оценок, возвращаем популярные фильмы
        if (userRatings.isEmpty()) {
            return getPopularMovies(15).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        // Собираем жанры из фильмов, которые пользователь оценил высоко (4-5)
        Set<Genre> favoriteGenres = userRatings.stream()
                .filter(rating -> rating.getScore() >= 4)
                .flatMap(rating -> rating.getMovie().getGenres().stream())
                .collect(Collectors.toSet());

        // Получаем все фильмы
        List<Movie> allMovies = getPopularMovies(15);
        var users = getCollaborativeFilteringRecommendations(userRepository.getReferenceById(userId), 25);

        // Фильтруем фильмы по любимым жанрам и исключаем уже оцененные
        Set<Long> ratedMovieIds = userRatings.stream()
                .map(rating -> rating.getMovie().getId())
                .collect(Collectors.toSet());

        var res = allMovies.stream()
                .filter(movie -> !ratedMovieIds.contains(movie.getId()))
                .filter(movie -> movie.getGenres().stream().anyMatch(favoriteGenres::contains))
                .sorted(Comparator.comparingDouble(Movie::getVoteAverage).reversed())
                .limit(10)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        users.addAll(res);

        return users;
    }

    @Transactional(readOnly = true)
    public List<MovieDTO> getSimilarMovies(Long movieId) {
        Movie targetMovie = movieService.getMovieDetails(movieId);
        Set<Genre> targetGenres = targetMovie.getGenres();

        List<Movie> allMovies = movieService.getPopularMovies(1);

        return allMovies.stream()
                .filter(movie -> !movie.getId().equals(movieId))
                .filter(movie -> movie.getGenres().stream().anyMatch(targetGenres::contains))
                .sorted(Comparator.comparingDouble((Movie movie) -> {
                    long commonGenres = movie.getGenres().stream()
                            .filter(targetGenres::contains)
                            .count();
                    return commonGenres * movie.getVoteAverage();
                }).reversed())
                .limit(10)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MovieDTO> getUserRecommendations(Long userId) {
        logger.info("Получение рекомендаций для пользователя: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<MovieDTO> contentBased = getContentBasedRecommendations(user, 5);
        List<MovieDTO> collaborative = getCollaborativeFilteringRecommendations(user, 25);

        Set<MovieDTO> allRecommendations = new LinkedHashSet<>();
        allRecommendations.addAll(collaborative);
        allRecommendations.addAll(contentBased);

        return new ArrayList<>(allRecommendations);
    }

    private MovieDTO convertToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setTmdbId(movie.getTmdbId());
        dto.setTitle(movie.getTitle());
        dto.setOverview(movie.getOverview());
        dto.setPosterPath(movie.getPosterPath());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setReleaseYear(movie.getReleaseYear());
        dto.setVoteAverage(movie.getVoteAverage());
        dto.setVoteCount(movie.getVoteCount());
        dto.setPopularity(movie.getPopularity());
        dto.setOriginalTitle(movie.getOriginalTitle());
        dto.setOriginalLanguage(movie.getOriginalLanguage());
        dto.setAdult(movie.getAdult());
        dto.setBackdropPath(movie.getBackdropPath());
        dto.setGenres(movie.getGenres().stream()
                .map(genre -> {
                    GenreDTO genreDTO = new GenreDTO();
                    genreDTO.setId(genre.getId());
                    genreDTO.setName(genre.getName());
                    return genreDTO;
                })
                .collect(Collectors.toSet()));
        return dto;
    }
}