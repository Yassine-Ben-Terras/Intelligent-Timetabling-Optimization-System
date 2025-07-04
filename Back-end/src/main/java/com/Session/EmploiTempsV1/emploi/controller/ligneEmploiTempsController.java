package com.Session.EmploiTempsV1.emploi.controller;


import com.Session.EmploiTempsV1.emploi.fetshResult.EmploiTempsService;
import com.Session.EmploiTempsV1.emploi.fetshResult.TimetableEntryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//@RestController
//@RequestMapping("/api/emploi")
//
//public class ligneEmploiTempsController {
//    @Autowired
//    LigneEmploiTempsRepository ligneEmploiTemps ;
//    @GetMapping("/all")
//    public List<LigneEmploiTemps> getAllEmploi(){
//        return ligneEmploiTemps.findAll() ;
//    }
//
//}

@RestController
@RequestMapping("/api/emploi")
public class ligneEmploiTempsController {

    private final EmploiTempsService emploiTempsService;

    @Autowired
    public ligneEmploiTempsController(EmploiTempsService emploiTempsService) {
        this.emploiTempsService = emploiTempsService;
    }

    @GetMapping("/all") // New endpoint for the simplified view
    public List<TimetableEntryDTO> getAllSimplifiedEmploi() {
        return emploiTempsService.getAllSimplifiedEmploi();
    }


}
