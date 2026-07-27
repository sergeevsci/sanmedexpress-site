package ru.sanmedexpress.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sanmedexpress.domain.Client;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByPhone(String phone);
}
