package com.Session.EmploiTempsV1.emploi.repository;

import com.Session.EmploiTempsV1.emploi.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AffectationEnseignantRepository extends JpaRepository<AffectationEnseignant, Long> {

    // Trouve toutes les affectations pour un enseignement donné dans une section (ex: un COURS)
    List<AffectationEnseignant> findByModuleAndTypeSeanceAndSection(ModuleEntity module, TypeSeance typeSeance, Section section);

    // Trouve toutes les affectations pour un enseignement donné dans un groupe (ex: un TD ou TP)
    List<AffectationEnseignant> findByModuleAndTypeSeanceAndGroupe(ModuleEntity module, TypeSeance typeSeance, Groupe groupe);

    // Vos anciennes méthodes peuvent être conservées si elles sont utilisées ailleurs
    List<AffectationEnseignant> findByProfesseurId(Integer professeurId);
    List<AffectationEnseignant> findByModule_Semestre_Session_Id(Integer id);

    Optional<AffectationEnseignant> findByProfesseurAndModuleAndTypeSeanceAndGroupe(Professeur prof, ModuleEntity module, TypeSeance typeSeance, Groupe groupe);

    Optional<AffectationEnseignant> findByProfesseurAndModuleAndTypeSeanceAndSection(Professeur prof, ModuleEntity module, TypeSeance typeSeance, Section section);

    boolean existsByGroupeAndModuleAndTypeSeance(Groupe groupe, ModuleEntity module, TypeSeance typeSeance);

}