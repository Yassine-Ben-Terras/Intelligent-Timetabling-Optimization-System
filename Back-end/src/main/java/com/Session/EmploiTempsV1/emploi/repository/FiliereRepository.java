package com.Session.EmploiTempsV1.emploi.repository;

import com.Session.EmploiTempsV1.emploi.entities.Departement;
import com.Session.EmploiTempsV1.emploi.entities.Filiere;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface FiliereRepository extends JpaRepository<Filiere,Integer > {
  //  Optional<Filiere> findById(String filiereId);
  Optional<Filiere> findByLibelleAndDepartement(String libelle, Departement departement);

}
