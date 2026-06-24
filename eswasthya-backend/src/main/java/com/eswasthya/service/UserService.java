package com.eswasthya.service;

import com.eswasthya.dto.request.LoginRequest;
import com.eswasthya.dto.request.RegisterRequest;
import com.eswasthya.dto.request.UpdateUserRequest;
import com.eswasthya.dto.response.AuthResponse;
import com.eswasthya.dto.response.UserResponse;
import com.eswasthya.entity.User;
import com.eswasthya.exception.BadRequestException;
import com.eswasthya.exception.ResourceNotFoundException;
import com.eswasthya.repository.UserRepository;
import com.eswasthya.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/** Handles registration, authentication, profile updates, and user lookups. */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    // Registration

    /** Registers a user after validating unique username and email values. */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email '" + request.getEmail() + "' is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .age(request.getAge())
                .gender(request.getGender())
                .role(request.getRole())
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {} (role={})", saved.getUsername(), saved.getRole());
        return UserResponse.from(saved);
    }

    // Authentication

    /** Authenticates a user and returns a signed JWT. */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", 0L));

        String token = jwtUtils.generateToken(user.getUsername());
        log.info("User logged in: {}", user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .expiresInMs(jwtUtils.getExpirationMs())
                .build();
    }

    // Profile

    @Transactional(readOnly = true)
    public UserResponse getProfile(String username) {
        User user = findByUsername(username);
        return UserResponse.from(user);
    }

    /** Applies a partial profile update. */
    @Transactional
    public UserResponse updateProfile(String username, UpdateUserRequest request) {
        User user = findByUsername(username);

        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (StringUtils.hasText(request.getGender())) {
            user.setGender(request.getGender());
        }
        if (StringUtils.hasText(request.getEmail())) {
            // Check uniqueness only when the email changes.
            if (!request.getEmail().equalsIgnoreCase(user.getEmail())) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    throw new BadRequestException("Email '" + request.getEmail() + "' is already in use");
                }
                user.setEmail(request.getEmail());
            }
        }

        return UserResponse.from(java.util.Objects.requireNonNull(userRepository.save(user), "Saved user must not be null"));
    }

    // Admin

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {   
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(  Long id) {
        return UserResponse.from(
                userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("User", id)));
    }

    // Internal helper

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
