package com.Session.EmploiTempsV1.emploi.repository;


import com.Session.EmploiTempsV1.emploi.entities.Semestre;
import com.Session.EmploiTempsV1.emploi.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface SemestreRepository extends JpaRepository<Semestre, Integer> {
    List<Semestre> findAllBySession(Session session);

    List<Semestre> findBySession(Session sessionSpecifique);

    List<Semestre> findAllBySession_Id(Integer id);

    Optional<Semestre> findByLibelleAndSession(String libelle, Session session);

}
