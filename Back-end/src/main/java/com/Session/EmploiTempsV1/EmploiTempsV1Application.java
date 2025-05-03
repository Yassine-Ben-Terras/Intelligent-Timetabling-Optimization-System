package com.Session.EmploiTempsV1;
import com.google.ortools.Loader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.io.File;


@SpringBootApplication
@EnableCaching

public class EmploiTempsV1Application {

	public static void main(String[] args) {
        // Créer le dossier de la base de données AVANT que Spring démarre
        String dbDir = System.getProperty("user.home") + "/AppData/Local/Planify";
        new File(dbDir).mkdirs();

        Loader.loadNativeLibraries();
		SpringApplication.run(EmploiTempsV1Application.class, args);

	}

}