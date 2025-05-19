package com.Session.EmploiTempsV1.emploi.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "REGROUPEMENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Regroupement {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_REGROUPEMENT")
    private Integer id;

    @Column(name = "LIBELLE_REGROUPEMENT", unique = true, nullable = false)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE_REGROUPEMENT", nullable = false)
    private TypeRegroupement typeRegroupement; // SECTION or GROUPE

    @OneToMany(mappedBy = "regroupement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RegroupementDetail> details;
}