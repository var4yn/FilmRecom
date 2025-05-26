package com.github.var4yn.FilmRecom.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, columnDefinition = "varchar(20) check (name in ('ROLE_USER', 'ROLE_MODERATOR', 'ROLE_ADMIN'))")
    private ERole name;
}