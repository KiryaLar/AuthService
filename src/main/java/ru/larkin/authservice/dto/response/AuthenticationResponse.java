package ru.larkin.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class AuthenticationResponse {

    private List<String> roles;
    private String accessToken;
    private String refreshToken;
}
