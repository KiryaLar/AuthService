package ru.larkin.authservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.larkin.authservice.dto.request.LoginUserRequest;
import ru.larkin.authservice.dto.request.RefreshTokenRequest;
import ru.larkin.authservice.dto.request.RegisterUserRequest;
import ru.larkin.authservice.dto.response.AuthenticationResponse;
import ru.larkin.authservice.dto.response.SuccessResponse;
import ru.larkin.authservice.service.AuthService;
import ru.larkin.authservice.service.UserService;

import java.util.Date;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<SuccessResponse> register(@Valid @RequestBody RegisterUserRequest userDto) {
        userService.registerUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.builder()
                        .timestamp(new Date().toString())
                        .message("User " + userDto.getUsername() + " has successfully registered")
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginUserRequest user) {
        return ResponseEntity.ok(authService.authenticate(user));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity
                .ok()
                .body(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        authService.setTokenRevoked(refreshToken);
        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponse.builder()
                        .timestamp(new Date().toString())
                        .message("Success logged out")
                        .build());
    }
}
