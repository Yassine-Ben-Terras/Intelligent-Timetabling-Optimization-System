package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.DispLocDto;
import com.Session.EmploiTempsV1.emploi.entities.*;
import com.Session.EmploiTempsV1.emploi.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DispLocService {
    private final DispLocRepository dispLocRepository;
    private final AnneeUniversitaireRepository anneeRepository;
    private final LocalRepository localRepository;
    private final SessionRepository sessionRepository;

    public DispLocService(DispLocRepository r, AnneeUniversitaireRepository a, LocalRepository l, SessionRepository s) {
        this.dispLocRepository = r; this.anneeRepository = a; this.localRepository = l; this.sessionRepository = s;
    }

    private DispLocDto toDto(DispLoc entity) {
        return new DispLocDto(entity.getAnnee().getId(), entity.getLocal().getId(), entity.getSession().getId(), entity.isDispo());
    }

    public List<DispLocDto> findAll() {
        return dispLocRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public DispLocDto createOrUpdate(DispLocDto dto) {
        AnneeUniversitaire annee = anneeRepository.findById(dto.anneeId()).orElseThrow(() -> new EntityNotFoundException("Année non trouvée"));
        Local local = localRepository.findById(dto.localId()).orElseThrow(() -> new EntityNotFoundException("Local non trouvé"));
        Session session = sessionRepository.findById(dto.sessionId()).orElseThrow(() -> new EntityNotFoundException("Session non trouvée"));

        DispLocId id = new DispLocId(dto.anneeId(), dto.localId(), dto.sessionId());
        DispLoc dispLoc = dispLocRepository.findById(id).orElse(new DispLoc());

        dispLoc.setAnnee(annee);
        dispLoc.setLocal(local);
        dispLoc.setSession(session);
        dispLoc.setDispo(dto.dispo());

        return toDto(dispLocRepository.save(dispLoc));
    }

    public void delete(Integer anneeId, Integer localId, Integer sessionId) {
        DispLocId id = new DispLocId(anneeId, localId, sessionId);
        if (!dispLocRepository.existsById(id)) {
            throw new EntityNotFoundException("Disponibilité de local non trouvée");
        }
        dispLocRepository.deleteById(id);
    }
}