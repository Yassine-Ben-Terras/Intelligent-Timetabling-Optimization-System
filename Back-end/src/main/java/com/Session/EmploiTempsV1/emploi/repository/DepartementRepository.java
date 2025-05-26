package com.Session.EmploiTempsV1.emploi.repository;

import com.Session.EmploiTempsV1.emploi.entities.Departement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface DepartementRepository extends JpaRepository<Departement, Integer> {
    Optional<Departement> findByLibelle(String libelle);

}
