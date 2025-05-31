package com.Session.EmploiTempsV1.emploi.repository;

import com.Session.EmploiTempsV1.emploi.entities.Groupe;
import com.Session.EmploiTempsV1.emploi.entities.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GroupeRepository extends JpaRepository<Groupe, Integer> {
    // Nouvelle méthode pour trouver les groupes contenant un module spécifique
    List<Groupe> findByModulesContains(ModuleEntity module);

    Optional<Groupe> findByLibelle(String libelle);
}