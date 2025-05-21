package com.github.var4yn.FilmRecom.service;

import com.github.var4yn.FilmRecom.model.Genre;
import com.github.var4yn.FilmRecom.model.Movie;
import com.github.var4yn.FilmRecom.model.Rating;
import com.github.var4yn.FilmRecom.model.User;
import com.github.var4yn.FilmRecom.repository.MovieRepository;
import com.github.var4yn.FilmRecom.repository.RatingRepository;
import com.github.var4yn.FilmRecom.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private final MovieRepository movieRepository;
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    public RecommendationService(MovieRepository movieRepository,
                                 RatingRepository ratingRepository,
                                 UserRepository userRepository) {
        this.movieRepository = movieRepository;
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
    }

    /**
     * Рекомендации на основе жанров
     * */
    public List<Movie> getContentBasedRecommendations(User user, int limit) {
        // Получить оценки пользователя
        List<Rating> userRatings = ratingRepository.findByUser(user);

        if (userRatings.isEmpty()) {
            return getPopularMovies(limit);
        }

        // Найти любимые жанры
        Map<Genre, Long> genreCounts = userRatings.stream()
                .filter(r -> r.getScore() >= 4.0)
                .flatMap(r -> r.getMovie().getGenres().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        if (genreCounts.isEmpty()) {
            return getPopularMovies(limit);
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
                .collect(Collectors.toList());
    }
    /**
     * Рекомендации по схожести пользователей
     * */
    public List<Movie> getCollaborativeFilteringRecommendations(User user, int limit) {

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
                .collect(Collectors.toList());
    }

    /**
     * Вычисление схожести пользователей на основе синусоидальное сходство
     * */
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

    private List<Movie> getPopularMovies(int limit) {
        return movieRepository.findByOrderByPopularityDesc().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}