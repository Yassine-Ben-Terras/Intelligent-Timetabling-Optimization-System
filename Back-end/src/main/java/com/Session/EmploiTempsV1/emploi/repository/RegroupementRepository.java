package com.Session.EmploiTempsV1.emploi.repository;

import com.Session.EmploiTempsV1.emploi.entities.Regroupement;
import com.Session.EmploiTempsV1.emploi.entities.TypeRegroupement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegroupementRepository extends JpaRepository<Regroupement, Integer> {
    Optional<Regroupement> findByLibelle(String libelle);
    List<Regroupement> findByTypeRegroupement(TypeRegroupement type);
}