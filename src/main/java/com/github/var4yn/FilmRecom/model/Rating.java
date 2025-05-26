package com.github.var4yn.FilmRecom.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "ratings")
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(nullable = false)
    private double score;

    @Column(name = "rated_at")
    private LocalDateTime ratedAt;

    @Column(columnDefinition = "TEXT")
    private String review;

    @PrePersist
    protected void onCreate() {
        ratedAt = LocalDateTime.now();
    }
}