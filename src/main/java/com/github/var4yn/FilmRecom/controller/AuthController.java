package com.github.var4yn.FilmRecom.controller;

import com.github.var4yn.FilmRecom.model.ERole;
import com.github.var4yn.FilmRecom.model.Role;
import com.github.var4yn.FilmRecom.model.User;
import com.github.var4yn.FilmRecom.payload.request.LoginRequest;
import com.github.var4yn.FilmRecom.payload.request.SignupRequest;
import com.github.var4yn.FilmRecom.payload.response.JwtResponse;
import com.github.var4yn.FilmRecom.payload.response.MessageResponse;
import com.github.var4yn.FilmRecom.repository.RoleRepository;
import com.github.var4yn.FilmRecom.repository.UserRepository;
import com.github.var4yn.FilmRecom.security.jwt.JwtUtils;
import com.github.var4yn.FilmRecom.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Попытка входа пользователя: {}", loginRequest.getUsername());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // Проверяем, что у пользователя есть роль ROLE_USER
            boolean hasUserRole = roles.stream()
                .anyMatch(role -> role.equals("ROLE_USER"));
                
            if (!hasUserRole) {
                logger.warn("У пользователя {} нет роли ROLE_USER", loginRequest.getUsername());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("У пользователя нет роли ROLE_USER"));
            }

            logger.info("Успешный вход пользователя: {}, роли: {}", loginRequest.getUsername(), roles);
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles));
        } catch (Exception e) {
            logger.error("Ошибка аутентификации для пользователя {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Неверное имя пользователя или пароль"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            logger.info("Попытка регистрации пользователя: {}", signUpRequest.getUsername());

            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                logger.warn("Имя пользователя {} уже занято", signUpRequest.getUsername());
                return ResponseEntity.badRequest().body(new MessageResponse("Имя пользователя уже занято"));
            }

            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                logger.warn("Email {} уже используется", signUpRequest.getEmail());
                return ResponseEntity.badRequest().body(new MessageResponse("Email уже используется"));
            }

            // Создание нового пользователя
            User user = new User(signUpRequest.getUsername(),
                    signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword()));

            Set<String> strRoles = signUpRequest.getRole();
            Set<Role> roles = new HashSet<>();

            if (strRoles == null) {
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Роль ROLE_USER не найдена"));
                roles.add(userRole);
                logger.info("Установлена роль ROLE_USER для пользователя {}", signUpRequest.getUsername());
            } else {
                strRoles.forEach(role -> {
                    switch (role) {
                        case "admin":
                            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                    .orElseThrow(() -> new RuntimeException("Роль ROLE_ADMIN не найдена"));
                            roles.add(adminRole);
                            logger.info("Установлена роль ROLE_ADMIN для пользователя {}", signUpRequest.getUsername());
                            break;
                        default:
                            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                    .orElseThrow(() -> new RuntimeException("Роль ROLE_USER не найдена"));
                            roles.add(userRole);
                            logger.info("Установлена роль ROLE_USER для пользователя {}", signUpRequest.getUsername());
                    }
                });
            }

            // Проверяем, что у пользователя есть роль ROLE_USER
            boolean hasUserRole = roles.stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_USER);
                
            if (!hasUserRole) {
                logger.warn("У пользователя {} нет роли ROLE_USER", signUpRequest.getUsername());
                return ResponseEntity.badRequest().body(new MessageResponse("У пользователя должна быть роль ROLE_USER"));
            }

            user.setRoles(roles);
            userRepository.save(user);

            logger.info("Пользователь {} успешно зарегистрирован с ролями: {}", 
                signUpRequest.getUsername(), 
                roles.stream().map(r -> r.getName().name()).collect(Collectors.joining(", ")));
            return ResponseEntity.ok(new MessageResponse("Пользователь успешно зарегистрирован"));
        } catch (Exception e) {
            logger.error("Ошибка при регистрации пользователя {}: {}", signUpRequest.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка при регистрации: " + e.getMessage()));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }
}