package ru.larkin.authservice.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.larkin.authservice.dto.request.RegisterUserRequest;
import ru.larkin.authservice.dto.response.UserResponse;
import ru.larkin.authservice.model.User;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Mapping(source = "password", target = "password", qualifiedByName = "encodePassword")
    public abstract User dtoToEntity(RegisterUserRequest userDto, @Context PasswordEncoder passwordEncoder);

    public abstract UserResponse entityToDto(User user);

    @Named("encodePassword")
    public String encodePassword(String password, @Context PasswordEncoder passwordEncoder) {
        if (password == null) {
            return null;
        }
        return passwordEncoder.encode(password);
    }
}
