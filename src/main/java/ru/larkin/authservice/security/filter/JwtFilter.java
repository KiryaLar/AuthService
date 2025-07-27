package ru.larkin.authservice.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.larkin.authservice.model.Role;
import ru.larkin.authservice.model.TokenType;
import ru.larkin.authservice.security.exception.InvalidJwtTokenException;
import ru.larkin.authservice.security.service.JwtProvider;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("Starting JwtFilter");
        if (request.getRequestURI().startsWith("/auth")) {
            log.debug("Request URI starts with /auth");
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("Request does not contain Authentication header or Bearer prefix");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        try {
            jwtProvider.validateAccessToken(token);

            String username = jwtProvider.extractUsername(token, TokenType.ACCESS);
            String role = jwtProvider.extractRole(token, TokenType.ACCESS);

            User tempUser = org.springframework.security.core.userdetails.User.builder().username(username).role(Role.valueOf(role)).build();


        } catch (InvalidJwtTokenException e) {

        }
    }
}
