// FICHIER : com/Session/EmploiTempsV1/emploi/entities/PlageTravail.java
package com.Session.EmploiTempsV1.emploi.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Représente une plage horaire large définie par l'utilisateur (ex: Lundi 08:00-18:30).
 * C'est la source de vérité pour les temps de travail disponibles.
 * Cette table est gérée par l'utilisateur et est en lecture seule pour le générateur.
 */
@Entity
@Table(name = "PLAGES_TRAVAIL")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlageTravail {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ANNEE", nullable = false)
    private AnneeUniversitaire annee;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_JOURS", nullable = false)
    private Jour jour;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SESSION", nullable = false)
    private Session session;

    @Column(name = "HEURE_DEBUT", nullable = false)
    private String heureDebut;

    @Column(name = "HEURE_FIN", nullable = false)
    private String heureFin;
}