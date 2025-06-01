package com.Session.EmploiTempsV1.emploi.repository;


import com.Session.EmploiTempsV1.emploi.entities.ModuleEntity;
import com.Session.EmploiTempsV1.emploi.entities.ProfParam;
import com.Session.EmploiTempsV1.emploi.entities.ProfParamId;
import com.Session.EmploiTempsV1.emploi.entities.Professeur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfParamRepository extends JpaRepository<ProfParam, ProfParamId> {
    boolean existsByProfesseur_IdAndModule_Id(Integer id, Integer id1);
    List<ProfParam> findByModule(ModuleEntity module);
    List<ProfParam> findByProfesseur(Professeur professeur);
}

