package com.Session.EmploiTempsV1.emploi.repository;


import com.Session.EmploiTempsV1.emploi.entities.Jour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JourRepository extends JpaRepository<Jour, Integer> {

    List<Jour> findByLibelleIn(List<String> libelles);

}

