package ru.larkin.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.larkin.authservice.dto.request.RegisterUserRequest;
import ru.larkin.authservice.dto.response.UserResponse;
import ru.larkin.authservice.exception.AlreadyExistsException;
import ru.larkin.authservice.mapper.UserMapper;
import ru.larkin.authservice.model.User;
import ru.larkin.authservice.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(RegisterUserRequest userDto) {
        Optional<User> maybeUser = userRepository.findByUsername(userDto.getUsername());
        if (maybeUser.isPresent()) {
            throw new AlreadyExistsException("User with username: %s already exists".formatted(userDto.getUsername()));
        }

        User user = userMapper.dtoToEntity(userDto, passwordEncoder);
        userRepository.save(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::entityToDto).toList();
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
}
