package com.afam.identity.boundary;

import com.afam.identity.entity.Contenuto;
import com.afam.identity.entity.Profilo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContenutoDBMSBoundary extends JpaRepository<Contenuto, java.util.UUID> {
    List<Contenuto> findByProfilo(Profilo profilo);
    List<Contenuto> findByProfiloOrderByDataCaricamentoDesc(Profilo profilo);

    List<Contenuto> findByProfiloId(java.util.UUID profiloId);
    List<Contenuto> findByProfiloIdOrderByDataCaricamentoDesc(java.util.UUID profiloId);

    List<Contenuto> findByTitoloContainingIgnoreCaseAndPolicyVisibilita(String titolo, String policyVisibilita);
}
