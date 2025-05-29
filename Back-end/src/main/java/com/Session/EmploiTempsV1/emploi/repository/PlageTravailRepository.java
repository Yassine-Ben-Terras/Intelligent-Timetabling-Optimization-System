// FICHIER : com/Session/EmploiTempsV1/emploi/repository/PlageTravailRepository.java
package com.Session.EmploiTempsV1.emploi.repository;

import com.Session.EmploiTempsV1.emploi.entities.AnneeUniversitaire;
import com.Session.EmploiTempsV1.emploi.entities.PlageTravail;
import com.Session.EmploiTempsV1.emploi.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlageTravailRepository extends JpaRepository<PlageTravail, Integer> {

    /**
     * Trouve les plages de travail définies par l'utilisateur pour une session donnée.
     * C'est la méthode clé utilisée par le générateur.
     */
    List<PlageTravail> findByAnneeAndSession(AnneeUniversitaire annee, Session session);
}