import { defineStore } from 'pinia';
import apiClient from '../services/api'; // Ensure this path is correct
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

interface BackendTimetableEntry {
    id: number;
    localLibelle?: string;
    moduleLibelle?: string;
    semestreLibelle?: string;
    filiereLibelle?: string;
    jourLibelle?: string;
    profNom?: string;
    profPrenom?: string;
    heureDebut?: string;
    heureFin?: string;
}

export interface EmploiTempsEntry {
    id: string;
    local: string;
    module: string;
    semestre: string;
    filiere: string;
    jour: string;
    professeur: string;
    heureDebut: string; // Heure de début originale normalisée
    heureFin: string;   // Heure de fin originale normalisée
    // Nouveau champ pour stocker le créneau canonique auquel cette entrée est assignée
    assignedCanonicalSlot: string | null;
    type: string;
}

const GRID_DAYS_ORDER = ['LUNDI', 'MARDI', 'MERCREDI', 'JEUDI', 'VENDREDI', 'SAMEDI'];

// Vos créneaux horaires cibles pour l'affichage et le PDF
const CANONICAL_TIME_SLOTS = [
    '08:00-09:30',
    '09:30-11:00',
    '11:00-12:30',
    '14:00-15:30',
    '15:30-17:00',
    '17:00-18:30'
]; // Déjà trié chronologiquement

const normalizeTime = (timeStr?: string): string => {
    if (!timeStr || typeof timeStr !== 'string' || !timeStr.includes(':')) return '00:00';
    const parts = timeStr.split(':');
    if (parts.length < 2) return '00:00';
    return `${parts[0].padStart(2, '0')}:${parts[1].padStart(2, '0')}`;
};

// Fonction pour mapper une heure de début/fin à un créneau canonique
// Cette logique est cruciale et peut nécessiter des ajustements fins
const mapToCanonicalSlot = (heureDebut: string, heureFin: string): string | null => {
    // Stratégie simple : si l'heure de début de l'entrée est DANS ou PROCHE du début d'un créneau canonique,
    // et que l'heure de fin est AUSSI dans ou proche de la fin de CE MÊME créneau canonique, on l'assigne.
    // On pourrait aussi se baser uniquement sur l'heure de début et prendre le premier créneau canonique qui commence.

    const entryStartMinutes = parseInt(heureDebut.split(':')[0]) * 60 + parseInt(heureDebut.split(':')[1]);
    // const entryEndMinutes = parseInt(heureFin.split(':')[0]) * 60 + parseInt(heureFin.split(':')[1]); // Moins utilisé dans cette logique simple

    for (const slot of CANONICAL_TIME_SLOTS) {
        const [slotStart, slotEnd] = slot.split('-');
        const slotStartMinutes = parseInt(slotStart.split(':')[0]) * 60 + parseInt(slotStart.split(':')[1]);
        const slotEndMinutes = parseInt(slotEnd.split(':')[0]) * 60 + parseInt(slotEnd.split(':')[1]);

        // Tolérance : combien de minutes avant/après le début du slot canonique une entrée peut commencer pour y être mappée.
        const toleranceMinutes = 15; // Exemple : un cours à 09:35 sera mappé à 09:30

        if (entryStartMinutes >= slotStartMinutes - toleranceMinutes && entryStartMinutes < slotEndMinutes) {
            // Si l'heure de début de l'entrée est dans la plage du créneau canonique (avec tolérance)
            // On pourrait ajouter une vérification sur heureFin si nécessaire, mais pour simplifier, on mappe sur le premier créneau correspondant au début.
            return slot;
        }
    }

    // Si aucun créneau canonique n'est trouvé pour l'heure de début
    console.warn(`Aucun créneau canonique trouvé pour ${heureDebut}-${heureFin}. L'entrée pourrait ne pas s'afficher.`);
    return null;
};


const formatTimeSlotForPdfHeader = (timeSlotKey: string): string => {
    if (!timeSlotKey || !timeSlotKey.includes('-')) return timeSlotKey;
    const [start, end] = timeSlotKey.split('-');
    const formatPart = (part: string) => (part || "00:00").replace(':', 'H');
    return `${formatPart(start)}\n${formatPart(end)}`;
};

export const useEmploiStore = defineStore('emploiStore', {
    state: () => ({
        allEntries: [] as EmploiTempsEntry[],
        loading: false,
        error: null as string | null,
        selectedFiliere: null as string | null,
        selectedSemestre: null as string | null,
        globalAcademicYear: "2024/2025",
        globalSessionName: "Printemps",
    }),

    getters: {
        uniqueFilieres(state): string[] {
            if (!state.allEntries.length) return [];
            return Array.from(new Set(state.allEntries.map(e => e.filiere).filter(Boolean) as string[])).sort();
        },

        uniqueSemestres(state): string[] {
            if (!state.allEntries.length || !state.selectedFiliere) return [];
            const filteredByFiliere = state.allEntries.filter(e => e.filiere === state.selectedFiliere);
            return Array.from(new Set(filteredByFiliere.map(e => e.semestre).filter(Boolean) as string[])).sort();
        },

        filteredEntries(state): EmploiTempsEntry[] {
            if (!state.allEntries.length) return [];
            return state.allEntries.filter(entry => {
                const filiereMatch = !state.selectedFiliere || entry.filiere === state.selectedFiliere;
                const semestreMatch = !state.selectedSemestre || entry.semestre === state.selectedSemestre;
                // On s'assure aussi que l'entrée a pu être mappée à un créneau canonique
                return filiereMatch && semestreMatch && entry.assignedCanonicalSlot !== null;
            });
        },

        dataForGridDisplayAndPdf(state): Record<string, {
            filiereDisplay: string;
            departmentDisplay: string;
            days: string[];
            timeSlots: string[]; // Sera CANONICAL_TIME_SLOTS
            grid: Record<string, Record<string, EmploiTempsEntry[]>>;
        }> {
            const currentFilteredEntries: EmploiTempsEntry[] = this.filteredEntries;
            const result: Record<string, any> = {};

            const entriesBySemestre: Record<string, EmploiTempsEntry[]> = {};
            if (currentFilteredEntries && currentFilteredEntries.length > 0) {
                currentFilteredEntries.forEach(entry => {
                    const semestreKey = entry.semestre || 'Semestre Non Défini';
                    if (!entriesBySemestre[semestreKey]) entriesBySemestre[semestreKey] = [];
                    entriesBySemestre[semestreKey].push(entry);
                });
            }

            if (Object.keys(entriesBySemestre).length === 0 && state.selectedFiliere && state.selectedSemestre) {
                const semestreKey = state.selectedSemestre;
                const filiereForEmptyGrid = state.selectedFiliere || "N/A";
                const departmentForEmptyGrid = filiereForEmptyGrid.split(' ')[0] || 'N/A';

                result[semestreKey] = {
                    filiereDisplay: filiereForEmptyGrid,
                    departmentDisplay: departmentForEmptyGrid,
                    days: GRID_DAYS_ORDER,
                    timeSlots: CANONICAL_TIME_SLOTS, // Utilise les créneaux canoniques
                    grid: GRID_DAYS_ORDER.reduce((acc, day) => {
                        acc[day] = CANONICAL_TIME_SLOTS.reduce((sAcc, slot) => {
                            sAcc[slot] = [];
                            return sAcc;
                        }, {} as Record<string, EmploiTempsEntry[]>);
                        return acc;
                    }, {} as Record<string, Record<string, EmploiTempsEntry[]>>),
                };
                return result;
            }

            for (const semestreName in entriesBySemestre) {
                const entriesForThisSemestre = entriesBySemestre[semestreName];
                if (!entriesForThisSemestre || entriesForThisSemestre.length === 0) continue;

                const gridForThisSemestre: Record<string, Record<string, EmploiTempsEntry[]>> = {};
                GRID_DAYS_ORDER.forEach(day => {
                    gridForThisSemestre[day] = {};
                    CANONICAL_TIME_SLOTS.forEach(canonicalSlotKey => { // Itère sur les créneaux canoniques pour les colonnes
                        gridForThisSemestre[day][canonicalSlotKey] = entriesForThisSemestre.filter(
                            entry => entry.jour === day && entry.assignedCanonicalSlot === canonicalSlotKey
                        );
                    });
                });

                const filiereForThisSemestre = entriesForThisSemestre[0]?.filiere || state.selectedFiliere || 'N/A';
                const departmentForThisSemestre = filiereForThisSemestre.split(' ')[0] || 'N/A';

                result[semestreName] = {
                    filiereDisplay: filiereForThisSemestre,
                    departmentDisplay: departmentForThisSemestre,
                    days: GRID_DAYS_ORDER,
                    timeSlots: CANONICAL_TIME_SLOTS, // Les colonnes sont TOUJOURS CANONICAL_TIME_SLOTS
                    grid: gridForThisSemestre,
                };
            }
            return result;
        },
    },

    actions: {
        async fetchEmplois() {
            this.loading = true;
            this.error = null;
            try {
                const response = await apiClient.get<BackendTimetableEntry[]>('emploi/all');
                if (!response.data || !Array.isArray(response.data)) {
                    this.error = "Réponse invalide de l'API.";
                    this.allEntries = [];
                    return;
                }

                this.allEntries = response.data.map((ligne): EmploiTempsEntry => {
                    let inferredType = 'N/A';
                    if (ligne.localLibelle) {
                        const nomLocLower = ligne.localLibelle.toLowerCase();
                        if (nomLocLower.includes('lab') || nomLocLower.includes('tp')) inferredType = 'TP';
                        else if (nomLocLower.includes('amphi')) inferredType = 'COURS';
                        else if (nomLocLower.includes('td') || nomLocLower.includes('salle')) inferredType = 'TD';
                    }
                    const profPrenom = ligne.profPrenom || '';
                    const profNom = ligne.profNom || '';

                    const normalizedHeureDebut = normalizeTime(ligne.heureDebut);
                    const normalizedHeureFin = normalizeTime(ligne.heureFin);

                    // Mappage au créneau canonique
                    const assignedSlot = mapToCanonicalSlot(normalizedHeureDebut, normalizedHeureFin);

                    // DEBUGGING:
                    // console.log(`ID: ${ligne.id}, Mod: ${ligne.moduleLibelle}, Raw: ${ligne.heureDebut}-${ligne.heureFin}, Norm: ${normalizedHeureDebut}-${normalizedHeureFin}, MappedTo: ${assignedSlot}`);


                    return {
                        id: String(ligne.id),
                        local: ligne.localLibelle || 'N/A',
                        module: ligne.moduleLibelle || 'N/A',
                        semestre: ligne.semestreLibelle || 'N/A',
                        filiere: ligne.filiereLibelle || 'N/A',
                        jour: (ligne.jourLibelle || 'N/A').toUpperCase(),
                        professeur: `${profPrenom} ${profNom}`.trim() || 'N/A',
                        heureDebut: normalizedHeureDebut, // Garde l'heure originale pour info si besoin
                        heureFin: normalizedHeureFin,     // Garde l'heure originale pour info si besoin
                        assignedCanonicalSlot: assignedSlot, // Le créneau canonique utilisé pour le placement
                        type: inferredType,
                    };
                }).filter(entry => entry.assignedCanonicalSlot !== null); // Optionnel: ne garder que les entrées qui ont pu être mappées
            } catch (err: any) {
                console.error("fetchEmplois Error:", err);
                this.error = `Erreur API: ${err.message || 'Erreur inconnue'}`;
                this.allEntries = [];
            } finally {
                this.loading = false;
            }
        },

        setFiliere(filiere: string | null) {
            this.selectedFiliere = filiere;
            this.selectedSemestre = null;
        },
        setSemestre(semestre: string | null) {
            this.selectedSemestre = semestre;
        },
        resetFilters() {
            this.selectedFiliere = null;
            this.selectedSemestre = null;
        },

        generatePdfGrid() {
            const dataForGridPdf = this.dataForGridDisplayAndPdf;

            if (!dataForGridPdf || Object.keys(dataForGridPdf).length === 0) {
                alert("Aucune donnée à afficher ou à exporter en PDF pour les filtres actuels.");
                return;
            }

            const doc = new jsPDF({ orientation: 'landscape' });
            let isFirstPage = true;
            let filiereNameToUseForFilename = "Toutes_Filieres";

            for (const semestreName in dataForGridPdf) {
                if (!dataForGridPdf.hasOwnProperty(semestreName)) continue;

                const semData = dataForGridPdf[semestreName];
                if (!semData || !semData.days || semData.days.length === 0 || !semData.timeSlots || semData.timeSlots.length === 0) {
                    continue;
                }
                if (isFirstPage && semData.filiereDisplay && semData.filiereDisplay !== 'N/A') {
                    filiereNameToUseForFilename = semData.filiereDisplay;
                }

                if (!isFirstPage) doc.addPage();
                isFirstPage = false;

                doc.setFontSize(16);
                doc.text(`Emploi du temps - Session ${this.globalSessionName}`, doc.internal.pageSize.getWidth() / 2, 15, { align: 'center' });
                doc.setFontSize(10);
                doc.text(`Année universitaire: ${this.globalAcademicYear}`, doc.internal.pageSize.getWidth() - 14, 15, { align: 'right' });
                doc.setFontSize(12);
                doc.text(`Filière: ${semData.filiereDisplay}`, 14, 25);
                doc.text(`Semestre: ${semestreName}`, 14, 32);
                doc.text(`Département: ${semData.departmentDisplay}`, 14, 39);

                const headRow = ['Jour'];
                // Utilise semData.timeSlots qui sont les CANONICAL_TIME_SLOTS
                semData.timeSlots.forEach(slotKey => {
                    headRow.push(formatTimeSlotForPdfHeader(slotKey));
                });

                const bodyRows = semData.days.map(day => {
                    const row = [day];
                    semData.timeSlots.forEach(slotKey => { // Itère sur les CANONICAL_TIME_SLOTS pour les colonnes
                        const entriesInCell = semData.grid[day]?.[slotKey];
                        if (entriesInCell && entriesInCell.length > 0) {
                            const entry = entriesInCell[0]; // Prend la première si plusieurs sont mappées au même slot
                            // On pourrait afficher entry.heureDebut - entry.heureFin si on veut voir les heures exactes
                            row.push(`${entry.module}${entry.type !== 'N/A' ? ' ('+entry.type+')' : ''}\n${entry.professeur}\nSalle: ${entry.local}`);
                        } else {
                            row.push('');
                        }
                    });
                    return row;
                });

                autoTable(doc, {
                    startY: 45,
                    head: [headRow],
                    body: bodyRows,
                    theme: 'grid',
                    headStyles: { fillColor: [41, 128, 185], textColor: 255, fontStyle: 'bold', halign: 'center', valign: 'middle', fontSize: 7 },
                    styles: { fontSize: 6.5, cellPadding: 1, overflow: 'linebreak', valign: 'middle', halign: 'center' },
                    columnStyles: { 0: { cellWidth: 25, fontStyle: 'bold' } },
                    didDrawPage: (dataArg) => {
                        doc.setFontSize(8);
                        doc.text('Page ' + doc.internal.getNumberOfPages(), dataArg.settings.margin.left, doc.internal.pageSize.height - 7);
                    }
                });
            }

            if (isFirstPage) {
                if (Object.keys(dataForGridPdf).length > 0) {
                    alert("Aucune donnée valide trouvée dans les semestres pour générer le PDF.");
                }
                return;
            }

            const filiereNameForFile = this.selectedFiliere || filiereNameToUseForFilename || "Emploi";
            const safeFiliereName = filiereNameForFile.replace(/[\s/\\?%*:|"<>]+/g, '_');
            const safeSemestreName = this.selectedSemestre ? `_${this.selectedSemestre.replace(/[\s/]+/g, '_')}` : '_tous_semestres';
            const fileName = `Emploi_Temps_Grille_${safeFiliereName}${safeSemestreName}.pdf`;

            try {
                doc.save(fileName);
            } catch (e) {
                console.error("Error during PDF doc.save():", e);
                alert("Une erreur est survenue lors de la sauvegarde du PDF.");
            }
        }
    },
});