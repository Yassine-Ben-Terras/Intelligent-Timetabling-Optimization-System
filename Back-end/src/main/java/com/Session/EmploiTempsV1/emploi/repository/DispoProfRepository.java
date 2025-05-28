package com.Session.EmploiTempsV1.emploi.repository;


import com.Session.EmploiTempsV1.emploi.entities.Jour;
import com.Session.EmploiTempsV1.emploi.entities.DispoProf;
import com.Session.EmploiTempsV1.emploi.entities.DispoProfId;
import com.Session.EmploiTempsV1.emploi.entities.Professeur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface DispoProfRepository extends JpaRepository<DispoProf, DispoProfId> {
    boolean existsByProf_IdAndJour_IdAndPeriode(Integer profId, Integer jourId, String periode);

    Optional<DispoProf> findByProfAndJourAndPeriode(Professeur prof, Jour jour, String periode);

    Collection<? extends DispoProf> findByJour(Jour j);
}
