package com.github.var4yn.FilmRecom.security.services;

import com.github.var4yn.FilmRecom.model.User;
import com.github.var4yn.FilmRecom.model.ERole;
import com.github.var4yn.FilmRecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Attempting to load user by username: {}", username);
        
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> {
                    logger.error("User not found with username: {}", username);
                    return new UsernameNotFoundException("Пользователь не найден: " + username);
                });
        
        logger.info("User found: {}, roles: {}", username, 
            user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.joining(", ")));
                
        return UserDetailsImpl.build(user);
    }
} 