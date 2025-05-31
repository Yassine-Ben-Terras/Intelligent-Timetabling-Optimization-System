package com.Session.EmploiTempsV1.emploi.repository;

import com.Session.EmploiTempsV1.emploi.entities.Regroupe;
import com.Session.EmploiTempsV1.emploi.entities.TypeRegroupement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegroupeRepository extends JpaRepository<Regroupe, Long> {

    // Recherche par nom (utile pour éviter les doublons)
    Optional<Regroupe> findByNom(String nom);

    // Récupérer tous les regroupements d’un type donné (SECTION ou GROUPE)
    List<Regroupe> findByType(TypeRegroupement type);

    // Vérifier l’existence par nom
    boolean existsByNom(String nom);
}
