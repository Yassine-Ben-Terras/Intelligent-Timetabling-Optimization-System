package com.Session.EmploiTempsV1.emploi.repository;


import com.Session.EmploiTempsV1.emploi.entities.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Integer> {
    Optional<Section> findByLibelle(String libelle);

    // Optional<Section> findByLibelle(String libelleSection);
}

