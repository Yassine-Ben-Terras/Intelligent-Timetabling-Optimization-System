package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.SemestreRequestDto;
import com.Session.EmploiTempsV1.emploi.dto.SemestreResponseDto;
import com.Session.EmploiTempsV1.emploi.entities.Semestre;
import com.Session.EmploiTempsV1.emploi.entities.Session;
import com.Session.EmploiTempsV1.emploi.repository.SemestreRepository;
import com.Session.EmploiTempsV1.emploi.repository.SessionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SemestreService {
    private final SemestreRepository semestreRepository;
    private final SessionRepository sessionRepository;
    private final SessionService sessionService; // Pour la conversion

    public SemestreService(SemestreRepository sr, SessionRepository ser, SessionService ss) {
        this.semestreRepository = sr;
        this.sessionRepository = ser;
        this.sessionService = ss;
    }

    public SemestreResponseDto toResponseDto(Semestre semestre) {
        return new SemestreResponseDto(
                semestre.getId(),
                semestre.getLibelle(),
                sessionService.toResponseDto(semestre.getSession())
        );
    }
    public List<SemestreResponseDto> findAll() { return semestreRepository.findAll().stream().map(this::toResponseDto).collect(Collectors.toList()); }
    public SemestreResponseDto findById(Integer id) { return semestreRepository.findById(id).map(this::toResponseDto).orElseThrow(() -> new EntityNotFoundException("Semestre non trouvé: " + id)); }
    public SemestreResponseDto create(SemestreRequestDto dto) {
        Session session = sessionRepository.findById(dto.sessionId()).orElseThrow(() -> new EntityNotFoundException("Session non trouvée: " + dto.sessionId()));
        Semestre semestre = new Semestre();
        semestre.setLibelle(dto.libelle());
        semestre.setSession(session);
        return toResponseDto(semestreRepository.save(semestre));
    }
    public SemestreResponseDto update(Integer id, SemestreRequestDto dto) {
        Semestre semestre = semestreRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Semestre non trouvé: " + id));
        Session session = sessionRepository.findById(dto.sessionId()).orElseThrow(() -> new EntityNotFoundException("Session non trouvée: " + dto.sessionId()));
        semestre.setLibelle(dto.libelle());
        semestre.setSession(session);
        return toResponseDto(semestreRepository.save(semestre));
    }
    public void delete(Integer id) {
        if (!semestreRepository.existsById(id)) { throw new EntityNotFoundException("Semestre non trouvé: " + id); }
        semestreRepository.deleteById(id);
    }
}