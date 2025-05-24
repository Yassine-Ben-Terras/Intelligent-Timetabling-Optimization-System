package com.Session.EmploiTempsV1.emploi.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ARCHIVE_PDF")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchivePDF {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID_PDF")
    private Long id;

    @Column(name = "FILE_NAME", nullable = false)
    private String fileName;

    @Column(name = "CONTENT_TYPE")
    private String contentType; // e.g., application/pdf

    @Lob
    @Column(
            name = "PDF_DATA",
            nullable = false,
            columnDefinition = "LONGBLOB" // Force MySQL à créer un LONGBLOB
    )
    private byte[] pdfData;
}
