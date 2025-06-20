package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.SessionRequestDto;
import com.Session.EmploiTempsV1.emploi.dto.SessionResponseDto;
import com.Session.EmploiTempsV1.emploi.entities.AnneeUniversitaire;
import com.Session.EmploiTempsV1.emploi.entities.Session;
import com.Session.EmploiTempsV1.emploi.repository.AnneeUniversitaireRepository;
import com.Session.EmploiTempsV1.emploi.repository.SessionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;
    private final AnneeUniversitaireRepository anneeRepository;
    private final AnneeUniversitaireService anneeService;

    public SessionService(SessionRepository sessionRepository, AnneeUniversitaireRepository anneeRepository, AnneeUniversitaireService anneeService) {
        this.sessionRepository = sessionRepository;
        this.anneeRepository = anneeRepository;
        this.anneeService = anneeService;
    }
    public SessionResponseDto toResponseDto(Session session) {
        return new SessionResponseDto(session.getId(), session.getLibelle(), anneeService.toDto(session.getAnnee()));
    }
    public List<SessionResponseDto> findAll() { return sessionRepository.findAll().stream().map(this::toResponseDto).collect(Collectors.toList()); }
    public SessionResponseDto findById(Integer id) { return sessionRepository.findById(id).map(this::toResponseDto).orElseThrow(() -> new EntityNotFoundException("Session non trouvée avec l'id: " + id)); }
    public SessionResponseDto create(SessionRequestDto dto) {
        AnneeUniversitaire annee = anneeRepository.findById(dto.anneeId()).orElseThrow(() -> new EntityNotFoundException("Année Universitaire non trouvée avec l'id: " + dto.anneeId()));
        Session session = new Session();
        session.setLibelle(dto.libelle());
        session.setAnnee(annee);
        return toResponseDto(sessionRepository.save(session));
    }
    public SessionResponseDto update(Integer id, SessionRequestDto dto) {
        Session session = sessionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Session non trouvée avec l'id: " + id));
        AnneeUniversitaire annee = anneeRepository.findById(dto.anneeId()).orElseThrow(() -> new EntityNotFoundException("Année Universitaire non trouvée avec l'id: " + dto.anneeId()));
        session.setLibelle(dto.libelle());
        session.setAnnee(annee);
        return toResponseDto(sessionRepository.save(session));
    }
    public void delete(Integer id) {
        if (!sessionRepository.existsById(id)) { throw new EntityNotFoundException("Session non trouvée avec l'id: " + id); }
        sessionRepository.deleteById(id);
    }
}