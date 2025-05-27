package com.Session.EmploiTempsV1.emploi.repository;


import com.Session.EmploiTempsV1.emploi.entities.ModSec;
import com.Session.EmploiTempsV1.emploi.entities.ModSecId;
import com.Session.EmploiTempsV1.emploi.entities.ModuleEntity;
import com.Session.EmploiTempsV1.emploi.entities.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModSecRepository extends JpaRepository<ModSec, ModSecId> {
    List<ModSec> findBySection_Id(Integer id);

    Optional<Object> findByModuleAndSection(ModuleEntity module, Section section);

    List<ModSec> findAllByModule_Id(Integer id);
    List<ModSec> findByModule(ModuleEntity module);
    List<ModSec> findBySection(Section section);
    boolean existsByModuleAndSection(ModuleEntity module, Section section);


}
