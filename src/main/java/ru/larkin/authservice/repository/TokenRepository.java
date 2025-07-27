package ru.larkin.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.larkin.authservice.model.RefreshToken;

@Repository
public interface TokenRepository extends JpaRepository<RefreshToken, Long> {
}
