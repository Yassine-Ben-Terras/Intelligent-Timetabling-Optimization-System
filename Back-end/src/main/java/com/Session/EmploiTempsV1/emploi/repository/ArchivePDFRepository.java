package com.Session.EmploiTempsV1.emploi.repository;

import com.Session.EmploiTempsV1.emploi.entities.ArchivePDF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArchivePDFRepository extends JpaRepository<ArchivePDF, Long> {
    Optional<ArchivePDF> findByFileName(String fileName);
}