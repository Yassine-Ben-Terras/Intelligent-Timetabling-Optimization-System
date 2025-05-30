package com.Session.EmploiTempsV1.emploi.repository;


import com.Session.EmploiTempsV1.emploi.entities.AnneeUniversitaire;
import com.Session.EmploiTempsV1.emploi.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface SessionRepository extends JpaRepository<Session, Integer> {

    Optional<Session> findByLibelleAndAnnee(String libelle, AnneeUniversitaire annee);
    List<Session> findByAnnee(AnneeUniversitaire annee);

    Optional<Session> findByLibelleAndAnnee_Id(String sessionLibelle, Integer id);


}

