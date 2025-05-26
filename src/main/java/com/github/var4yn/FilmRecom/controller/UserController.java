package com.github.var4yn.FilmRecom.controller;

import com.github.var4yn.FilmRecom.exception.ResourceNotFoundException;
import com.github.var4yn.FilmRecom.model.Movie;
import com.github.var4yn.FilmRecom.model.Rating;
import com.github.var4yn.FilmRecom.model.User;
import com.github.var4yn.FilmRecom.model.UserMovieList;
import com.github.var4yn.FilmRecom.payload.RegisterRequest;
import com.github.var4yn.FilmRecom.payload.UserMovieListRequest;
import com.github.var4yn.FilmRecom.repository.MovieRepository;
import com.github.var4yn.FilmRecom.repository.RatingRepository;
import com.github.var4yn.FilmRecom.repository.UserMovieListRepository;
import com.github.var4yn.FilmRecom.repository.UserRepository;
import com.github.var4yn.FilmRecom.security.services.UserDetailsImpl;
import com.github.var4yn.FilmRecom.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final UserMovieListRepository userMovieListRepository;
    private final MovieRepository movieRepository;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userDetails.getId()));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}/ratings")
    public ResponseEntity<List<?>> getUserRatings(@PathVariable Long id) {
        return ResponseEntity.ok(ratingRepository.findByUserId(id));
    }

    @GetMapping("/{id}/lists")
    public ResponseEntity<List<UserMovieList>> getUserLists(@PathVariable Long id) {
        return ResponseEntity.ok(userMovieListRepository.findByUserId(id));
    }

    @PostMapping("/{id}/lists")
    public ResponseEntity<?> createUserList(@PathVariable Long id, @RequestBody UserMovieListRequest request) {
        UserMovieList list = new UserMovieList();
        list.setUser(userService.findById(id));
        list.setMovie(movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new RuntimeException("Фильм не найден")));
        list.setName(request.getName());
        list.setDescription(request.getDescription());
        return ResponseEntity.ok(userMovieListRepository.save(list));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        // Check if username already exists
        if (userService.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Ошибка: Имя пользователя уже занято!");
        }

        // Check if email already exists
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Ошибка: Email уже используется!");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        return ResponseEntity.ok(userService.registerUser(user));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }
}