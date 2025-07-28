package ru.larkin.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import ru.larkin.authservice.model.Role;

import java.util.Set;

@Data
@Builder
@ToString(exclude = "password")
public class RegisterUserRequest {
    @NotBlank(message = "Login is required")
    private String username;
    @Size(min = 8, message = "Password must contain at least 8 characters.")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])[a-zA-Z0-9]+$",
            message = "Password must contain at least one digit, one uppercase and one lowercase letter."
    )
    @NotBlank(message = "Password is required")
    private String password;
    @Pattern(regexp = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
    message = "Incorrect email input")
    @NotBlank(message = "Email is required")
    private String email;
    @NotNull(message = "Roles are required")
    private Set<Role> roles;
}
