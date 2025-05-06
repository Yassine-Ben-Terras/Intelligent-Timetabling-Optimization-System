package com.Session.EmploiTempsV1.emploi.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String name; // e.g., ROLE_ADMIN, ROLE_CONSULTATION

    public Role(String name) {
        this.name = name;
    }
}