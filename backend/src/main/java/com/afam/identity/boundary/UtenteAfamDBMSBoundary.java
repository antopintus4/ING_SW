package com.afam.identity.boundary;

import com.afam.identity.entity.UtenteAfam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtenteAfamDBMSBoundary extends JpaRepository<UtenteAfam, String> {
    Optional<UtenteAfam> findByUsername(String username);
    Optional<UtenteAfam> findByEmail(String email);
}
