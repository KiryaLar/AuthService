package ru.larkin.authservice.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.larkin.authservice.dto.response.ErrorResponse;
import ru.larkin.authservice.model.Role;
import ru.larkin.authservice.model.TokenType;
import ru.larkin.authservice.model.User;
import ru.larkin.authservice.exception.InvalidJwtTokenException;
import ru.larkin.authservice.security.service.JwtProvider;
import ru.larkin.authservice.security.userdetails.UserDetailsImpl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        try {
            jwtProvider.validateAccessToken(token);
            String username = jwtProvider.extractUsername(token, TokenType.ACCESS);

            List<String> rolesList = jwtProvider.extractRoles(token);
            Set<Role> roles = rolesList.stream()
                    .map(Role::valueOf)
                    .collect(Collectors.toSet());

            User tempUser = User.builder().username(username).roles(roles).build();
            UserDetails userDetails = new UserDetailsImpl(tempUser);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (InvalidJwtTokenException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message(e.getMessage())
                    .timestamp(new Date().toString())
                    .build();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
        }
    }
}
