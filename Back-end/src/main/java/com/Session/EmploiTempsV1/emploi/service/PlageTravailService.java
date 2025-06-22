package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.PlageTravailDto;
import com.Session.EmploiTempsV1.emploi.entities.*;
import com.Session.EmploiTempsV1.emploi.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlageTravailService {

    private final PlageTravailRepository plageTravailRepository;
    private final AnneeUniversitaireRepository anneeRepository;
    private final JourRepository jourRepository;
    private final SessionRepository sessionRepository;

    public PlageTravailService(PlageTravailRepository plageTravailRepository, AnneeUniversitaireRepository anneeRepository, JourRepository jourRepository, SessionRepository sessionRepository) {
        this.plageTravailRepository = plageTravailRepository;
        this.anneeRepository = anneeRepository;
        this.jourRepository = jourRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public PlageTravailDto createPlageTravail(PlageTravailDto dto) {
        PlageTravail plage = mapToEntity(dto);
        PlageTravail savedPlage = plageTravailRepository.save(plage);
        return mapToDto(savedPlage);
    }

    @Transactional(readOnly = true)
    public List<PlageTravailDto> getAllPlagesTravail() {
        return plageTravailRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // NOUVELLE MÉTHODE - Récupérer par ID
    @Transactional(readOnly = true)
    public PlageTravailDto getPlageTravailById(Integer id) {
        return plageTravailRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException("PlageTravail non trouvée avec l'ID: " + id));
    }

    // NOUVELLE MÉTHODE - Mettre à jour
    @Transactional
    public PlageTravailDto updatePlageTravail(Integer id, PlageTravailDto dto) {
        PlageTravail plageToUpdate = plageTravailRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PlageTravail non trouvée avec l'ID: " + id));

        // Récupérer les entités associées
        Jour jour = jourRepository.findById(dto.getJourId())
                .orElseThrow(() -> new EntityNotFoundException("Jour non trouvé"));
        Session session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new EntityNotFoundException("Session non trouvée"));
        AnneeUniversitaire annee = session.getAnnee();

        // Mettre à jour les champs de l'entité existante
        plageToUpdate.setAnnee(annee);
        plageToUpdate.setJour(jour);
        plageToUpdate.setSession(session);
        plageToUpdate.setHeureDebut(dto.getHeureDebut());
        plageToUpdate.setHeureFin(dto.getHeureFin());

        PlageTravail updatedPlage = plageTravailRepository.save(plageToUpdate);
        return mapToDto(updatedPlage);
    }


    @Transactional
    public void deletePlageTravail(Integer id) {
        if (!plageTravailRepository.existsById(id)) {
            throw new EntityNotFoundException("PlageTravail non trouvée avec l'ID: " + id);
        }
        plageTravailRepository.deleteById(id);
    }

    // --- Méthodes de mapping (inchangées) ---

    private PlageTravailDto mapToDto(PlageTravail plage) {
        PlageTravailDto dto = new PlageTravailDto();
        dto.setId(plage.getId());
        dto.setHeureDebut(plage.getHeureDebut());
        dto.setHeureFin(plage.getHeureFin());
        if (plage.getAnnee() != null) dto.setAnneeId(plage.getAnnee().getId());
        if (plage.getJour() != null) dto.setJourId(plage.getJour().getId());
        if (plage.getSession() != null) dto.setSessionId(plage.getSession().getId());
        return dto;
    }

    private PlageTravail mapToEntity(PlageTravailDto dto) {
        Jour jour = jourRepository.findById(dto.getJourId())
                .orElseThrow(() -> new EntityNotFoundException("Jour non trouvé avec l'id: " + dto.getJourId()));
        Session session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new EntityNotFoundException("Session non trouvée avec l'id: " + dto.getSessionId()));

        // Dériver l'annee depuis la session (la session est déjà liée à une année)
        AnneeUniversitaire annee = session.getAnnee();
        if (annee == null) {
            throw new EntityNotFoundException("La session " + dto.getSessionId() + " n'est pas liée à une année universitaire");
        }

        PlageTravail plage = new PlageTravail();
        plage.setAnnee(annee);
        plage.setJour(jour);
        plage.setSession(session);
        plage.setHeureDebut(dto.getHeureDebut());
        plage.setHeureFin(dto.getHeureFin());
        return plage;
    }
}