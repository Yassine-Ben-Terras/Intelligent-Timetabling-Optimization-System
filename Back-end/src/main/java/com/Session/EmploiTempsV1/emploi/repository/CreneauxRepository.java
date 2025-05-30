package com.Session.EmploiTempsV1.emploi.repository;

import com.Session.EmploiTempsV1.emploi.entities.AnneeUniversitaire;
import com.Session.EmploiTempsV1.emploi.entities.Creneaux;
import com.Session.EmploiTempsV1.emploi.entities.Jour;
import com.Session.EmploiTempsV1.emploi.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreneauxRepository extends JpaRepository<Creneaux, Integer> {
    List<Creneaux> findAllByAnneeAndSession(AnneeUniversitaire annee, Session session);


    List<Creneaux> findByAnnee(AnneeUniversitaire annee);

    List<Creneaux> findByAnneeAndSession(AnneeUniversitaire anneeU, Session session);

    List<Creneaux> findAllByAnnee_IdAndSession_Id(Integer id, Integer id1);

    List<Creneaux> findByAnneeAndSessionAndJourIn(AnneeUniversitaire annee, Session session, List<Jour> jours);

}


