package com.github.var4yn.FilmRecom.repository;

import com.github.var4yn.FilmRecom.model.User;
import com.github.var4yn.FilmRecom.model.UserMovieList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMovieListRepository extends JpaRepository<UserMovieList, Long> {

    List<UserMovieList> findByUserId(Long userId);

    Optional<UserMovieList> findByUserIdAndName(Long userId, String name);

    boolean existsByUserIdAndName(Long userId, String name);

    @Query("SELECT uml FROM UserMovieList uml JOIN uml.movie m WHERE m.id = :movieId")
    List<UserMovieList> findAllByMovieId(@Param("movieId") Long movieId);

    Page<UserMovieList> findByUserId(Long userId, Pageable pageable);

    void deleteByIdAndUserId(Long listId, Long userId);

    long countByUserId(Long userId);

    List<UserMovieList> findByUser(User user);

    UserMovieList findByUserIdAndMovieId(Long userId, Long movieId);
}