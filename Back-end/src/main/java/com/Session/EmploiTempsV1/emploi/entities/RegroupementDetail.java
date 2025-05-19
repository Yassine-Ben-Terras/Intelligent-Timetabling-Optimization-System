package com.Session.EmploiTempsV1.emploi.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "REGROUPEMENT_DETAILS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegroupementDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_REGROUPEMENT_DETAIL")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_REGROUPEMENT", nullable = false)
    private Regroupement regroupement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SECTION", nullable = true)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_GROUPE", nullable = true)
    private Groupe groupe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_MODULE", nullable = false)
    private ModuleEntity module;

    // La validation Java est une bonne mesure de sécurité, mais une contrainte DB est préférable.
    // Cette méthode intercepte les problèmes avant qu'ils ne soient persistés si possible,
    // ou après le chargement/mise à jour.
    @PostLoad
    @PostPersist
    @PostUpdate
    private void validateEntities() {
        if (section != null && groupe != null) {
            throw new IllegalStateException("A RegroupementDetail cannot be associated with both a Section and a Groupe.");
        }
        if (section == null && groupe == null) {
            throw new IllegalStateException("A RegroupementDetail must be associated with either a Section or a Groupe.");
        }
    }
}