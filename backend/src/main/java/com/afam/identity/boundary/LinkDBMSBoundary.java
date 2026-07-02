package com.afam.identity.boundary;

import com.afam.identity.entity.Link;
import com.afam.identity.entity.Profilo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LinkDBMSBoundary extends JpaRepository<Link, String> {
    List<Link> findByProfilo(Profilo profilo);
    List<Link> findByProfiloId(java.util.UUID profiloId);
}
