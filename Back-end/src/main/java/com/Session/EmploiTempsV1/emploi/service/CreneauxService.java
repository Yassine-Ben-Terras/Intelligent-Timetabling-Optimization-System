package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.CreneauxRequestDto;
import com.Session.EmploiTempsV1.emploi.dto.CreneauxResponseDto;
import com.Session.EmploiTempsV1.emploi.entities.*;
import com.Session.EmploiTempsV1.emploi.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreneauxService {
    private final CreneauxRepository creneauxRepository;
    private final AnneeUniversitaireRepository anneeRepository;
    private final JourRepository jourRepository;
    private final SessionRepository sessionRepository;

    // Injection des services pour réutiliser la logique de conversion DTO
    private final AnneeUniversitaireService anneeService;
    private final JourService jourService;
    private final SessionService sessionService;

    public CreneauxService(CreneauxRepository creneauxRepository, AnneeUniversitaireRepository anneeRepository, JourRepository jourRepository, SessionRepository sessionRepository, AnneeUniversitaireService anneeService, JourService jourService, SessionService sessionService) {
        this.creneauxRepository = creneauxRepository;
        this.anneeRepository = anneeRepository;
        this.jourRepository = jourRepository;
        this.sessionRepository = sessionRepository;
        this.anneeService = anneeService;
        this.jourService = jourService;
        this.sessionService = sessionService;
    }

    // Méthode de conversion
    public CreneauxResponseDto toResponseDto(Creneaux creneau) {
        return new CreneauxResponseDto(
                creneau.getId(),
                creneau.getHeureDebut(),
                creneau.getHeureFin(),
                creneau.getNbrHeures(),
                anneeService.toDto(creneau.getAnnee()),        // Utilise le service correspondant
                jourService.toDto(creneau.getJour()),          // Utilise le service correspondant
                sessionService.toResponseDto(creneau.getSession()) // Utilise le service correspondant
        );
    }

    public List<CreneauxResponseDto> findAll() {
        return creneauxRepository.findAll().stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    public CreneauxResponseDto findById(Integer id) {
        return creneauxRepository.findById(id).map(this::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Créneau non trouvé avec l'id: " + id));
    }

    public CreneauxResponseDto create(CreneauxRequestDto dto) {
        AnneeUniversitaire annee = anneeRepository.findById(dto.anneeId()).orElseThrow(() -> new EntityNotFoundException("Année non trouvée avec l'id: " + dto.anneeId()));
        Jour jour = jourRepository.findById(dto.jourId()).orElseThrow(() -> new EntityNotFoundException("Jour non trouvé avec l'id: " + dto.jourId()));
        Session session = sessionRepository.findById(dto.sessionId()).orElseThrow(() -> new EntityNotFoundException("Session non trouvée avec l'id: " + dto.sessionId()));

        Creneaux creneau = new Creneaux();
        creneau.setAnnee(annee);
        creneau.setJour(jour);
        creneau.setSession(session);
        creneau.setHeureDebut(dto.heureDebut());
        creneau.setHeureFin(dto.heureFin());
        creneau.setNbrHeures(dto.nbrHeures());

        return toResponseDto(creneauxRepository.save(creneau));
    }

    public CreneauxResponseDto update(Integer id, CreneauxRequestDto dto) {
        Creneaux creneau = creneauxRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Créneau non trouvé avec l'id: " + id));
        AnneeUniversitaire annee = anneeRepository.findById(dto.anneeId()).orElseThrow(() -> new EntityNotFoundException("Année non trouvée avec l'id: " + dto.anneeId()));
        Jour jour = jourRepository.findById(dto.jourId()).orElseThrow(() -> new EntityNotFoundException("Jour non trouvé avec l'id: " + dto.jourId()));
        Session session = sessionRepository.findById(dto.sessionId()).orElseThrow(() -> new EntityNotFoundException("Session non trouvée avec l'id: " + dto.sessionId()));

        creneau.setAnnee(annee);
        creneau.setJour(jour);
        creneau.setSession(session);
        creneau.setHeureDebut(dto.heureDebut());
        creneau.setHeureFin(dto.heureFin());
        creneau.setNbrHeures(dto.nbrHeures());

        return toResponseDto(creneauxRepository.save(creneau));
    }

    public void delete(Integer id) {
        if (!creneauxRepository.existsById(id)) {
            throw new EntityNotFoundException("Créneau non trouvé avec l'id: " + id);
        }
        creneauxRepository.deleteById(id);
    }
}