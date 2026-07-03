package com.afam.identity.boundary;

import com.afam.identity.entity.Allegato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AllegatoDBMSBoundary extends JpaRepository<Allegato, java.util.UUID> {
}
