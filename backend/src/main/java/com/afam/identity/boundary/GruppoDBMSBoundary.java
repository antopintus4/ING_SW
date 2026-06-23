package com.afam.identity.boundary;

import com.afam.identity.entity.Gruppo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GruppoDBMSBoundary extends JpaRepository<Gruppo, Long> {
    List<Gruppo> findByNomeContainingIgnoreCase(String nome);
    List<Gruppo> findByProfiloId(Long profiloId);
}
