package com.Session.EmploiTempsV1.emploi.config;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;

/**
 * Ensures the directory for the SQLite database file exists before 
 * Hibernate tries to create the database. Without this, SQLite will 
 * fail with "unable to open database file" if the parent directory 
 * doesn't exist.
 */
@Configuration
public class DatabaseDirectoryConfig {

    @PostConstruct
    public void createDatabaseDirectory() {
        String userHome = System.getProperty("user.home");
        File dbDir = new File(userHome + "/AppData/Local/Planify");
        if (!dbDir.exists()) {
            boolean created = dbDir.mkdirs();
            if (created) {
                System.out.println("[Planify] Dossier de base de données créé : " + dbDir.getAbsolutePath());
            }
        }
    }
}
