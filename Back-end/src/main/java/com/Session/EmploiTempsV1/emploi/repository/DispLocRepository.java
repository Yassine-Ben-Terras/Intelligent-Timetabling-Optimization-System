package com.Session.EmploiTempsV1.emploi.repository;


import com.Session.EmploiTempsV1.emploi.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DispLocRepository extends JpaRepository<DispLoc, DispLocId> {

   // <T> Optional<T> findByAnneeAndLocalAndSession(AnneeUniversitaire annee, Local local, Session session);

    //DispLoc findByAnneeAndLocalAndSession(AnneeUniversitaire annee, Local local, Session session);

    Optional<DispLoc> findByAnneeAndLocalAndSession(AnneeUniversitaire annee, Local local, Session session);

    Object findByAnneeAndSession(AnneeUniversitaire anneeU, Session session);
}

