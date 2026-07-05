package com.afam.identity.boundary;

import com.afam.identity.entity.Profilo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ProfiloDBMSBoundary extends JpaRepository<Profilo, java.util.UUID> {
    Optional<Profilo> findByUtenteAfamUuid(java.util.UUID uuid);
    List<Profilo> findByNomeContainingIgnoreCaseOrCognomeContainingIgnoreCase(String nome, String cognome);
}
