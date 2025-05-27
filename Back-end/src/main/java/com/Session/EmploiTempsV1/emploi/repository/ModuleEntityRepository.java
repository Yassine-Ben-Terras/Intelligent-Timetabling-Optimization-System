package com.Session.EmploiTempsV1.emploi.repository;


import com.Session.EmploiTempsV1.emploi.entities.ModuleEntity;
import com.Session.EmploiTempsV1.emploi.entities.Semestre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ModuleEntityRepository extends JpaRepository<ModuleEntity, Integer> {
    List<ModuleEntity> findAllBySemestre(Semestre semestre);
    Optional<ModuleEntity> findByLibelleAndSemestre(String libelle, Semestre semestre);
    @Query("SELECT m FROM ModuleEntity m WHERE m.semestre.session.annee.libelle = :annee AND m.semestre.session.libelle = :session")
    List<ModuleEntity> findModulesByAnneeAndSession(String annee, String session);
    List<ModuleEntity> findBySemestre(Semestre semestre);
    List<ModuleEntity> findBySemestreIn(List<Semestre> semestres);
    List<ModuleEntity> findAllBySemestreIn(List<Semestre> semestres);
}
