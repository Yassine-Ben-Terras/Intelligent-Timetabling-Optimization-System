package com.Session.EmploiTempsV1.emploi.repository;


import com.Session.EmploiTempsV1.emploi.entities.Creneaux;
import com.Session.EmploiTempsV1.emploi.entities.LigneEmploiTemps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LigneEmploiTempsRepository extends JpaRepository<LigneEmploiTemps, Integer> {
    // Méthode pour obtenir le prochain ID disponible si NUM_LIGNE n'est pas auto-généré
    @Query("SELECT COALESCE(MAX(l.id), 0) + 1 FROM LigneEmploiTemps l")
    Integer getNextId();

    Collection<? extends LigneEmploiTemps> findByCreneaux(Creneaux c);

    void deleteByCreneaux_IdIn(List<Integer> creneauIds);

    @Query("SELECT COALESCE(MAX(l.id), 0L) FROM LigneEmploiTemps l")
    Optional<Long> findMaxId();
        // Method to delete existing schedule for a given context if needed
        void deleteByCreneauxIn(List<Creneaux> creneaux); // Or more specific deletion

    @Query("SELECT DISTINCT let FROM LigneEmploiTemps let " +
            "LEFT JOIN FETCH let.local l " +
            "LEFT JOIN FETCH l.type " +
            "LEFT JOIN FETCH let.moduleEntity me " +
            "LEFT JOIN FETCH me.semestre sem " +
            "LEFT JOIN FETCH sem.session sess " +
            "LEFT JOIN FETCH sess.annee anSess " +
            "LEFT JOIN FETCH let.creneaux cr " +
            "LEFT JOIN FETCH cr.annee anCr " +
            "LEFT JOIN FETCH cr.jour j " +
            "LEFT JOIN FETCH cr.session crSess " +
            "LEFT JOIN FETCH crSess.annee anCrSess " +
            "LEFT JOIN FETCH let.prof pr " +
            "LEFT JOIN FETCH pr.departement " +
            "LEFT JOIN FETCH let.section s")
    List<LigneEmploiTemps> findAllWithDetails();


   // List<LigneEmploiTemps> findByCreneauxIn(List<Creneaux> potentiallyOldCreneaux);

    List<LigneEmploiTemps> findByCreneauxIn(Collection<Creneaux> creneaux);

}
