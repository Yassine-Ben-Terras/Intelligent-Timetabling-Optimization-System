package com.Session.EmploiTempsV1.emploi.repository;


import com.Session.EmploiTempsV1.emploi.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecParamRepository extends JpaRepository<SecParam, SecParamId> {
    List<SecParam> findByFiliere_IdAndSemestre_IdAndAnnee_Id(Integer filiereId, Integer semestreId, Integer anneeId);

    List<SecParam> findByFiliereAndAnneeAndSemestre(Filiere filiere, AnneeUniversitaire annee, Semestre semestre);

    boolean existsBySection_IdAndAnnee_IdAndSemestre_Id(Integer id, Integer id1, Integer id2);
    List<SecParam> findByAnneeAndFiliereAndSemestre(AnneeUniversitaire annee, Filiere filiere, Semestre semestre);

    List<SecParam> findBySectionAndAnneeAndSemestre(Section section, AnneeUniversitaire annee, Semestre semestre);

}
