package com.Session.EmploiTempsV1.emploi.repository;


import com.Session.EmploiTempsV1.emploi.entities.TypeLocal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TypeLocalRepository extends JpaRepository<TypeLocal, Integer> {
    Optional<TypeLocal> findByLibelle(String libelle); // Assurez-vous que cette méthode existe

}
