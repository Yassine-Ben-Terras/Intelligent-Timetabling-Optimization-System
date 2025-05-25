package com.Session.EmploiTempsV1.emploi.repository;


import com.Session.EmploiTempsV1.emploi.entities.AnneeUniversitaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnneeUniversitaireRepository extends JpaRepository<AnneeUniversitaire, Integer> {

    Optional<AnneeUniversitaire> findByLibelle(String libelle);
}
