package com.Session.EmploiTempsV1.emploi.service;

import com.Session.EmploiTempsV1.emploi.dto.FilModDto;
import com.Session.EmploiTempsV1.emploi.entities.*;
import com.Session.EmploiTempsV1.emploi.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilModService {
    private final FilModRepository filModRepository;
    private final FiliereRepository filiereRepository;
    private final ModuleEntityRepository moduleRepository;

    public FilModService(FilModRepository r, FiliereRepository f, ModuleEntityRepository m) {
        this.filModRepository = r; this.filiereRepository = f; this.moduleRepository = m;
    }

    private FilModDto toDto(FilMod entity) {
        return new FilModDto(entity.getFiliere().getId(), entity.getModule().getId());
    }

    public List<FilModDto> findAll() { return filModRepository.findAll().stream().map(this::toDto).collect(Collectors.toList()); }

    public FilModDto create(FilModDto dto) {
        Filiere filiere = filiereRepository.findById(dto.filiereId()).orElseThrow(() -> new EntityNotFoundException("Filiere non trouvée"));
        ModuleEntity module = moduleRepository.findById(dto.moduleId()).orElseThrow(() -> new EntityNotFoundException("Module non trouvé"));
        FilMod filMod = new FilMod(filiere, module);
        return toDto(filModRepository.save(filMod));
    }

    public void delete(Integer filiereId, Integer moduleId) {
        FilModId id = new FilModId(filiereId, moduleId);
        if (!filModRepository.existsById(id)) {
            throw new EntityNotFoundException("Lien FilMod non trouvé");
        }
        filModRepository.deleteById(id);
    }
}