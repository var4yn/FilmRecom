package com.github.var4yn.FilmRecom.repository;

import com.github.var4yn.FilmRecom.model.ERole;
import com.github.var4yn.FilmRecom.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(ERole name);

    boolean existsByName(ERole name);
}