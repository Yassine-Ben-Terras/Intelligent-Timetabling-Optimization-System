package com.Session.EmploiTempsV1.emploi.repository;


import com.Session.EmploiTempsV1.emploi.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GrpParamRepository extends JpaRepository<GrpParam, GrpParamId> {

    List<GrpParam> findBySection_IdAndSemestre_IdAndAnnee_Id(Integer sectionId, Integer semestreId, Integer anneeId);
    Optional<GrpParam> findByGroupeAndAnneeAndSemestre(Groupe groupe, AnneeUniversitaire annee, Semestre semestre);
}

