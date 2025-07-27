package ru.larkin.authservice.model;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {

    private Integer id;
    private Integer userId;
    private String token;
    private Instant expirationDate;
    private boolean revoked;
}
