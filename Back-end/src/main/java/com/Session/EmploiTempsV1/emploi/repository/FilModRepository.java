package com.Session.EmploiTempsV1.emploi.repository;

import com.Session.EmploiTempsV1.emploi.entities.FilMod;
import com.Session.EmploiTempsV1.emploi.entities.FilModId;
import com.Session.EmploiTempsV1.emploi.entities.Filiere;
import com.Session.EmploiTempsV1.emploi.entities.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FilModRepository extends JpaRepository<FilMod, FilModId> {
    List<FilMod> findByModuleId(Integer id);
    List<FilMod> findByFiliere_Id(Integer filiereId);
    List<FilMod> findByModule_Id(Integer moduleId);


    List<FilMod> findByFiliere(Filiere filiere);
    List<FilMod> findByModule(ModuleEntity module);
    boolean existsByFiliereAndModule(Filiere filiere, ModuleEntity module);
}
