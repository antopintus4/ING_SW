package com.afam.identity.boundary;

import com.afam.identity.entity.Token;
import com.afam.identity.entity.UtenteAfam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface TokenDBMSBoundary extends JpaRepository<Token, java.util.UUID> {
    Optional<Token> findByValore(String valore);
    Optional<Token> findByValoreAndTipo(String valore, String tipo);
    List<Token> findByUtenteAfamAndTipoAndValore(UtenteAfam utenteAfam, String tipo, String valore);
    List<Token> findByUtenteAfamAndTipo(UtenteAfam utenteAfam, String tipo);
}
