package ru.larkin.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.larkin.authservice.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

}
