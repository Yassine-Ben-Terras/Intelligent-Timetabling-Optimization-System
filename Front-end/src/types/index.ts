// User types
// src/types.ts
// src/types/index.ts

export interface User {
  id: number; // Backend User ID is Long, maps to number in JS/TS
  username: string;
  email: string;
  roles: string[];   // e.g., ["ROLE_ADMIN", "ROLE_USER"]
  firstName?: string; // From users.first_name
  lastName?: string;  // From users.last_name
  name?: string;      // Optional: can be a computed fallback (e.g., firstName + lastName or username)
  // enabled?: boolean; // If you expose this from backend and need it in frontend state
}

// Entity types
export interface Professeur {
  id: number;
  nom: string;
  prenom: string;
  departementId: string;
}

export interface Salle {
  id: number
  libelle : string
  capacite: number
}

export interface Departement {
  id: number
  libelle : string

}

export interface Filiere {
  id: number
  libelle: string
  code: string
  departementId: string
}

export interface Module {
  idModule: number;
  idSemestre?: number;
  nbrHeuresCours?: number;
  nbrHeuresTd?: number;
  nbrHeuresTp?: number;
  libelleModule: string;
  semDemTd?: string;
  semDemTp?: string;
}

export interface ModuleResponse {
    id: number;
    libelle: string;
    // ... autres propriétés si votre API en renvoie
}

export interface Groupe {
  id: number
  nom: string
  filiereId: number
  anneeId: number
}

export interface Section {
  id: number
  nom: string
  groupeId: number
  effectif: number
}

export interface Semestre {
  id: number
  nom: string
  numero: number
  dateDebut: string
  dateFin: string
}
// src/types/index.ts or src/types/departement.ts
export interface Departement {
  id: number ;
  libelle: string ; // To match DB `libelle_departement|varchar(255)|Yes|NULL`
}

export interface Creneau {
  id: number
  heureDebut: string
  heureFin: string
  nbrHeure?: number
}

export interface Jour {
  id: number
  nom: string
  ordre: number
}

export interface Session {
  id: number
  nom: string
  dateDebut: string
  dateFin: string
  type: 'NORMALE' | 'RATTRAPAGE'
}

// src/types/index.ts or src/types/annee.ts
export interface Annee {
  idAnnee: number;
  libelleAnnee: string | null; // Allow null as per DB, or string if you always expect a value
}

export interface DataTableHeader {
  key: string;
  title: string;
  sortable?: boolean;
  // Add other Vuetify DataTable header properties if needed
}

export interface Disponibilite {
  id: number
  professeurId: number
  jourId: number
  creneauId: number
  estDisponible: boolean
}


// Generic entity type for data table
export type EntityType = 
  | Professeur
  | Salle
  | Departement
  | Filiere
  | Module
  | Groupe
  | Section
  | Semestre
  | Creneau
  | Jour
  | Session
  | Annee
  | Disponibilite

// Table headers configuration
export interface DataTableHeader {
  key: string
  title: string
  sortable?: boolean
  align?: 'start' | 'end' | 'center'
  width?: string | number
}

// Filter configuration
export interface FilterOption {
  key: string
  label: string
  type: 'text' | 'select' | 'date' | 'number' | 'boolean'
  options?: Array<{ value: any, title: string }>
}