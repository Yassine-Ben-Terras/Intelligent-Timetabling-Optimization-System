package com.Session.EmploiTempsV1.emploi.repository;

import com.Session.EmploiTempsV1.emploi.entities.TypeSeance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TypeSeanceRepository extends JpaRepository<TypeSeance, Integer> {
    Optional<TypeSeance> findByLibelle(String libelle);
}