package com.Session.EmploiTempsV1.emploi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchivePDFDTO {
    private Long id;
    private String fileName;
    private String contentType;
    // You might include byte[] pdfData here if you intend to send it directly in JSON/base64,
    // but often for file downloads, you stream the bytes directly from the controller.
    // For simplicity, we'll assume direct byte streaming for download.
    // private byte[] pdfData;
}