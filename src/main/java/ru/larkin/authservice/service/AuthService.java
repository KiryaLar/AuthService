package ru.larkin.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.larkin.authservice.dto.request.LoginUserRequest;
import ru.larkin.authservice.dto.response.AuthenticationResponse;
import ru.larkin.authservice.model.RefreshToken;
import ru.larkin.authservice.model.TokenType;
import ru.larkin.authservice.model.User;
import ru.larkin.authservice.repository.TokenRepository;
import ru.larkin.authservice.repository.UserRepository;
import ru.larkin.authservice.exception.InvalidJwtTokenException;
import ru.larkin.authservice.security.service.JwtProvider;
import ru.larkin.authservice.security.userdetails.UserDetailsImpl;
import ru.larkin.authservice.security.userdetails.UserDetailsServiceImpl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;


    public AuthenticationResponse authenticate(LoginUserRequest loginUserRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginUserRequest.getUsername(),
                        loginUserRequest.getPassword()
                )
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return generateAndSaveTokens(userDetails);
    }

    public AuthenticationResponse refreshToken(String refreshToken) {
        jwtProvider.validateRefreshToken(refreshToken);
        setTokenRevoked(refreshToken);

        String login = jwtProvider.extractUsername(refreshToken, TokenType.REFRESH);
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(login);
        return generateAndSaveTokens(userDetails);
    }

    public void setTokenRevoked(String refreshToken) {
        Optional<RefreshToken> maybeToken = tokenRepository.findRefreshTokenByToken(refreshToken);
        if (maybeToken.isEmpty()) {
            throw new InvalidJwtTokenException("Invalid refresh token");
        }
        RefreshToken token = maybeToken.get();
        token.setRevoked(true);
        tokenRepository.save(token);
        SecurityContextHolder.clearContext();
    }

    private AuthenticationResponse generateAndSaveTokens(UserDetailsImpl userDetails) {
        String accessToken = jwtProvider.generateAccessToken(userDetails);
        String refreshToken = jwtProvider.generateRefreshToken(userDetails);

        User user = userDetails.getUser();
        Instant expiration = jwtProvider.extractExpiration(refreshToken);
        List<String> roles = jwtProvider.extractRoles(accessToken);

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expirationDate(expiration)
                .build();

        tokenRepository.save(token);
        return new AuthenticationResponse(roles, accessToken, refreshToken);
    }
}
