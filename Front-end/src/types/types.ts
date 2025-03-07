// src/types/index.ts

// Vue types (none needed currently)

export interface AnneeUniversitaire {
    id: number;
    libelle: string;
}

export interface Departement {
    id: number;
    libelle: string;
}

export interface Jour {
    id: number;
    libelle: string;
}

export interface Section {
    id: number;
    libelle: string;
    nbrEtudiants: number;
}

export interface TypeLocal {
    id: number;
    libelle: string;
}

// --- Session ---
export interface SessionResponse {
    id: number;
    libelle: string;
    annee: AnneeUniversitaire;
}
export interface SessionPayload {
    libelle: string;
}

// --- Semestre ---
export interface SemestreResponse {
    id: number;
    libelle: string;
    session: SessionResponse;
}
export interface SemestrePayload {
    libelle: string;
    sessionId: number;
}

// --- Module ---
// DTO spécial utilisé dans le service Module

export interface ModuleResponse {
    id: number;
    libelle: string;
    heuresCours: number;
    heuresTD: number;
    heuresTP: number;
    semDemTP: string;
    semDemTD: string;
    semestre: Semestre;
    typeLocalRequisCours?: TypeLocal | null; // Le '?' et 'null' les rendent optionnels
    typeLocalRequisTD?: TypeLocal | null;
    typeLocalRequisTP?: TypeLocal | null;
}

export interface ModulePayload {
    libelle: string;
    heuresCours: number;
    heuresTD: number;
    heuresTP: number;
    semDemTP: string;
    semDemTD: string;
    semestreId: number;
    typeLocalRequisCoursId?: number | null;
    typeLocalRequisTDId?: number | null;
    typeLocalRequisTPId?: number | null;
}

// --- Filiere ---
export interface FiliereResponse {
    id: number;
    codeFiliere: string;
    libelle: string;
    departement: Departement;
}
export interface FilierePayload {
    codeFiliere: string;
    libelle: string;
    departementId: number;
}

// --- Professeur ---
export interface ProfesseurResponse {
    id: number;
    nom: string;
    prenom: string;
    statut: string;
    departement: Departement;
}
export interface ProfesseurPayload {
    nom: string;
    prenom: string;
    statut: string;
    departementId: number;
}

// --- Local (Salle) ---
export interface LocalResponse {
    id: number;
    libelle: string;
    capacite: number;
    type: TypeLocal;
}
export interface LocalPayload {
    libelle: string;
    capacite: number;
    typeId?: number | null;
}


export interface GroupeResponse {
    id: number;
    libelle: string;
    nbrEtudiants: number;
    modules: ModuleResponse[]; // C'était 'module: ModuleResponse'
}

// --- CHANGEMENT ICI ---
// Ce que le frontend envoie pour créer/mettre à jour un groupe
export interface GroupePayload {
    libelle: string;
    nbrEtudiants: number;
    moduleIds: number[]; // C'était 'moduleId: number'
}

// --- Creneaux ---
export interface CreneauResponse {
    id: number;
    heureDebut: string;
    heureFin: string;
    nbrHeures: number;
    jour: Jour;
    session: SessionResponse;
}
export interface CreneauPayload {
    heureDebut: string;
    heureFin: string;
    nbrHeures: number;
    jourId: number;
    sessionId: number;
}

export interface DispLocPayload {
    localId: number;
    sessionId: number;
    dispo: boolean;
}

export interface DispoProfPayload {
    profId: number;
    jourId: number;
    periode: 'MATIN' | 'SOIR';
}

export interface FilModPayload {
    filiereId: number;
    moduleId: number;
}

export interface GrpParamPayload {
    sectionId: number;
    groupeId: number;
    semestreId: number;
}



export interface SecParamPayload {
    filiereId: number;
    sectionId: number;
    semestreId: number;
}

// Configuration des en-têtes pour v-data-table
export interface DataTableHeader {
    key: string;
    title: string;
    sortable?: boolean;
    align?: 'start' | 'end' | 'center';
    width?: string | number;
}


// Configuration des en-têtes pour v-data-table
export interface DataTableHeader {
    key: string;
    title: string;
    sortable?: boolean;
    align?: 'start' | 'end' | 'center';
    width?: string | number;
}

// Configuration générique pour les champs de formulaire
export interface FormField {
  key: string;
  label: string;
  type: 'text' | 'select' | 'number' | 'textarea' | 'boolean' | string ;
  required?: boolean;
  hint?: string;
  props?: Record<string, any>;
  items?: readonly any[];
  itemTitle?: string;
  itemValue?: string;
}


// Dans src/types/index.ts

// --- Session ---
export interface SessionResponse {
    id: number;
    libelle: string;
    annee: AnneeUniversitaire; // Assurez-vous que AnneeUniversitaire est aussi défini
}
export interface SessionPayload {
    libelle: string;
}
export interface FilModPayload {
    filiereId: number;
    moduleId: number;
}

export interface FiliereResponse {
    id: number;
    codeFiliere: string;
    libelle: string;
    departement: Departement; // L'objet Departement complet est imbriqué
}

export interface FilierePayload {
    codeFiliere: string;
    libelle: string;
    departementId: number; // On envoie seulement l'ID du département
}

export interface SecParamPayload {
    filiereId: number;
    sectionId: number;
    semestreId: number;
}

// Dans src/types/index.ts

// ... (autres types)

// --- Professeur ---
export interface ProfesseurResponse {
    id: number;
    nom: string;
    prenom: string;
    statut: string;
    departement: Departement;
}
export interface ProfesseurPayload {
    nom: string;
    prenom: string;
    statut: string;
    departementId: number;
}

// ... (autres types)

// --- Payload pour la table de liaison ProfParam ---


export interface ProfParamPayload {
    professeurId: number;
    moduleId: number;
    typeSeanceId: number; // Ajout de cette propriété
}

export interface GrpParamPayload {
    sectionId: number;
    groupeId: number;
    semestreId: number;
}

export interface TypeSeanceResponse {
    id: number;
    libelle: string;
}

export interface TypeSeanceResponse {
    id: number;
    libelle: string;
}

// NOUVEAU : Le type pour les objets Section venant de l'API
export interface SectionResponse {
    id: number;
    libelle: string;
    // ... autres champs si nécessaire
}


// src/types/types.ts

// ... autres types

// Le payload pour créer ou supprimer un paramétrage (uniquement les IDs)
export interface GrpParamPayload {
    sectionId: number;
    groupeId: number;
    semestreId: number;
}

export interface GrpParamDto extends GrpParamPayload {
    key: string; // "sectionId-groupeId-semestreId"
    sectionLibelle: string;
    groupeLibelle: string;
    semestreLibelle: string;
}

export interface TypeLocal {
    id: number;
    libelle: string;
}

// NOUVEAU : Interface pour les données de Semestre
export interface Semestre {
    id: number;
    libelle: string;
    sessionId: number; // ou session: Session si vous avez l'objet complet
}

export interface AffectationDto {
    id: number;
    professeurId: number;
    professeurNom: string;
    moduleId: number;
    moduleLibelle: string;
    typeSeanceId: number;
    typeSeanceLibelle: string;
    sectionId?: number;       // Optionnel
    sectionLibelle?: string;  // Optionnel
    groupeId?: number;        // Optionnel
    groupeLibelle?: string;   // Optionnel
}

// Payload envoyé à l'API (POST /api/affectations) pour la création

export interface AffectationPayload {
    professeurId: number;
    moduleId: number;
    typeSeanceId: number;
    sectionId?: number | null; // Peut être null
    groupeId?: number | null;  // Peut être null
}

// DTO pour AFFICHER une affectation (ce que l'API renvoie)
export interface AffectationResponseDto {
    id: number;
    professeurId: number;
    professeurNom: string;
    moduleId: number;
    moduleLibelle: string;
    typeSeanceId: number;
    typeSeanceLibelle: string;
    sectionId?: number;
    sectionLibelle?: string;
    groupeId?: number;
    groupeLibelle?: string;
}


// DTO pour MODIFIER les affectations (ce qu'on envoie à l'API de synchronisation)
export interface AffectationSyncRequestDto {
    moduleId: number;
    typeSeanceId: number;
    sectionId: number | null;
    groupeId: number | null;
    professeurIds: number[]; // La liste complète des IDs de professeurs
}

// src/types/index.ts
export interface TimetableEntryDTO {
    id: number;
    jourLibelle: string;
    heureDebut: string; // ex: "09:00"
    heureFin: string;   // ex: "11:00"
    profNom: string;
    profPrenom: string;
    moduleLibelle: string;
    localLibelle: string;
    filiereLibelle: string;
    semestreLibelle: string;
    typeSeance: 'Cours' | 'TD' | 'TP';
    sectionLibelle: string;
    groupeLibelle: string | null;
}

/**
 * Définit la structure de l'objet de filtres utilisé dans le store.
 */
export interface TimetableFilters {
    filiere: string | null;
    semestre: string | null;
    groupe: string | null;
    professeur: string | null;
}

export type TypeRegroupement = 'SECTION' | 'GROUPE';

// --- DTO pour AFFICHER un regroupement (ce que l'API renvoie avec GET) ---


// --- Payload pour CRÉER ou MODIFIER un regroupement (ce que le frontend envoie avec POST/PUT) ---
export interface RegroupementRequestDto {
    nom: string;
    type: TypeRegroupement;
    sectionIds: number[]; // Tableau des IDs de section si type='SECTION'
    groupeIds: number[];  // Tableau des IDs de groupe si type='GROUPE'
}

/*
// Ce que le frontend envoie à l'API pour créer ou mettre à jour une plage de travail.
// Correspond au PlageTravailDto du backend.
export interface PlageTravailPayload {
    heureDebut: string;
    heureFin: string;
    anneeId: number;
    jourId: number;
    sessionId: number;
}

// Ce que l'API renvoie, idéalement avec les objets complets pour un affichage facile.
// C'est l'équivalent de CreneauResponse.
export interface PlageTravailResponse {
    id: number;
    heureDebut: string;
    heureFin: string;
    annee: AnneeUniversitaire;
    jour: Jour;
    session: SessionResponse;
}


*/

// Fichier: src/types/types.ts

// Type pour les données brutes venant de l'API Spring Boot (correspond au DTO)
export interface PlageTravailDto {
    id: number;
    heureDebut: string;
    heureFin: string;
    jourId: number;
    sessionId: number;
}

// Type pour les données enrichies, utilisées par les composants Vue
export interface PlageTravailEnrichie {
    id: number;
    heureDebut: string;
    heureFin: string;
    jour: Jour;               // En supposant que vous avez ce type
    session: SessionResponse; // En supposant que vous avez ce type
}

// Type pour la création/mise à jour d'une plage (ce que le formulaire envoie)
export interface PlageTravailPayload {
    heureDebut: string;
    heureFin: string;
    jourId: number;
    sessionId: number;
}

// types/index.ts (extension)
export interface ArchivePDFDTO {
  id: number;
  fileName: string;
  contentType: string;
}

export interface ArchivePdfRequest {
  fileName: string;
  pdfData: Blob; // The actual PDF binary data
}

export interface ArchivePdfResponse {
  id?: number; // The ID of the archived file, if successful
  fileName: string;
  contentType: string; // e.g., "application/pdf"
  size?: number; // Size in bytes
  uploadDate?: string; // ISO date string
  success: boolean;
  message?: string; // Success or error message
}

// Types communs

export interface SectionModuleDTO {
  sectionId: number | null;
  moduleId: number | null;
}
export interface GroupeModuleDTO {
  groupeId: number | null;
  moduleId: number | null;
}


// DTO pour création/mise à jour
export interface RegroupementCreateDTO {
  nom: string;
  type: TypeRegroupement;
  sections?: SectionModuleDTO[];
  groupes?: GroupeModuleDTO[];
}

// DTO de réponse
export interface RegroupementResponseDTO {
  id: number;
  nom: string;
  type: TypeRegroupement;
  sections?: { id: number; libelle: string; moduleId: number }[];
  groupes?: { id: number; libelle: string; moduleId: number }[];
}

export interface RegroupementResponseDto {
    id: number;
    nom: string;
    type: TypeRegroupement; // SECTION ou GROUPE
    sections: SectionResponse[]; // Liste des sections si type='SECTION'
    groupes: GroupeResponse[];   // Liste des groupes si type='GROUPE'
    // Note: moduleId et typeSeanceId ne sont plus dans RegroupementResponseDto directement
    // Ils seront gérés côté frontend pour la création/édition si nécessaire pour lier un regroupement à une séance.
}
/*
export interface RegroupementResponseDTO {
  id: number;
  nom: string;
  type: TypeRegroupement;
  sections?: SectionModuleDTO_Response[]; // Adjusted for consistency if backend returns full DTO
  groupes?: GroupeModuleDTO_Response[];   // Adjusted for consistency if backend returns full DTO
}*/
export interface SectionModuleDTO_Response {
    sectionId: number;
    moduleId: number | null;
    // Potentially add libelle if you plan to fetch it separately for display
    // sectionLibelle?: string;
}

export interface GroupeModuleDTO_Response {
    groupeId: number;
    moduleId: number | null;
    // Potentially add libelle if you plan to fetch it separately for display
    // groupeLibelle?: string;
}

// DTO pour un détail de regroupement
export interface RegroupementDetailDto {
  id?: number; // Optional for creation
  sectionId?: number;
  sectionLibelle?: string;
  groupeId?: number;
  groupeLibelle?: string;
  moduleId?: number  ;
  moduleLibelle?: string;
}

// DTO pour le regroupement principal
export interface RegroupementDto {
  id?: number; // Optional for creation
  libelle: string;
  typeRegroupement: TypeRegroupement;
  details: RegroupementDetailDto[];
}

// Payload pour la création/mise à jour d'un regroupement (peut être le même que RegroupementDto pour simplicité)
export type RegroupementPayload = Omit<RegroupementDto, 'id'> & { id?: number };

// ... (vos autres types)

