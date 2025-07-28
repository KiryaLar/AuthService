package ru.larkin.authservice.model;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN,
    PREMIUM_USER,
    GUEST
}
