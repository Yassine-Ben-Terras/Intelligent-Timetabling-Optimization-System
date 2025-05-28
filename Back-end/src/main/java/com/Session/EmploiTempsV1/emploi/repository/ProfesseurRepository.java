package com.Session.EmploiTempsV1.emploi.repository;


import com.Session.EmploiTempsV1.emploi.entities.Professeur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ProfesseurRepository extends JpaRepository<Professeur, Integer> {

    Optional<Professeur> findByNomAndPrenom(String nom, String prenom);
    // Optional<Professeur> findByNomAndPrenom(String nom, String prenom);
}

