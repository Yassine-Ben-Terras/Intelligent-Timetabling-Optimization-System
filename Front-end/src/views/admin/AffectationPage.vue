<script setup lang="ts">
import { ref, onMounted, computed, watch } from "vue";
import { storeToRefs } from "pinia";
import type { DataTableHeader, ProfesseurResponse, AffectationPayload, TypeSeanceResponse, ModuleResponse } from "../../types/types.ts";

// --- IMPORT DE TOUS LES STORES NÉCESSAIRES ---
import { useAffectationStore } from "../../stores/affectationStore";
import { useProfesseurStore } from "../../stores/professeurs";
import { useModuleStore } from "../../stores/modules";
import { useTypeSeanceStore } from "../../stores/typeSeance";
import { useSectionStore } from "../../stores/sections";
import { useGroupeStore } from "../../stores/groupes";
import { useFilModStore } from "../../stores/filMods";
import { useSecParamStore } from "../../stores/secParams";

// --- INITIALISATION DES STORES ---
const affectationStore = useAffectationStore();
const professeurStore = useProfesseurStore();
const moduleStore = useModuleStore();
const typeSeanceStore = useTypeSeanceStore();
const sectionStore = useSectionStore();
const groupeStore = useGroupeStore();
const filModStore = useFilModStore();
const secParamStore = useSecParamStore();

// --- STATE REACTIF GLOBAL ---
const { affectations, loading: affectationsLoading, error } = storeToRefs(affectationStore);
const { professeurs, loading: professeursLoading } = storeToRefs(professeurStore);
const { modules } = storeToRefs(moduleStore);
const { typesSeance } = storeToRefs(typeSeanceStore);
const { sections } = storeToRefs(sectionStore);
const { groupes } = storeToRefs(groupeStore);
const { liaisons: filModLiaisons } = storeToRefs(filModStore);
const { liaisons: secParamLiaisons } = storeToRefs(secParamStore);

// --- STATE LOCAL POUR L'INTERFACE ---
const showManageDialog = ref(false);
const selectedProfesseur = ref<ProfesseurResponse | null>(null);
const searchProfesseur = ref('');
const newAffectation = ref<Partial<AffectationPayload>>({});

// --- PROPRIÉTÉS CALCULÉES ---
const tableLoading = computed(() => affectationsLoading.value || professeursLoading.value);

const affectationsDuProfesseur = computed(() => {
    if (!selectedProfesseur.value) return [];
    return affectations.value.filter(a => a.professeurId === selectedProfesseur.value!.id);
});

const filteredTypesSeance = computed(() => {
    const validTypes = ["COURS", "CM", "TD", "TP"];
    return typesSeance.value.filter(ts => validTypes.includes(ts.libelle.toUpperCase()));
});

const selectedTypeSeance = computed(() => {
    return typesSeance.value.find(ts => ts.id === newAffectation.value.typeSeanceId);
});

// Logique d'affichage conditionnel des champs
const showSectionField = computed(() => {
    if (!selectedTypeSeance.value) return false;
    const libelle = selectedTypeSeance.value.libelle.toUpperCase();
    return libelle === 'COURS' || libelle === 'CM';
});

const showGroupeField = computed(() => {
    if (!selectedTypeSeance.value) return false;
    const libelle = selectedTypeSeance.value.libelle.toUpperCase();
    return libelle === 'TD' || libelle === 'TP';
});

// ✅ MODIFICATION : Le champ Module s'affiche si une section OU un groupe est sélectionné.
const showModuleField = computed(() => {
    if (showSectionField.value && newAffectation.value.sectionId) {
        return true;
    }
    if (showGroupeField.value && newAffectation.value.groupeId) {
        return true;
    }
    return false;
});

// ✅ MODIFICATION MAJEURE : La propriété `filteredModules` gère maintenant les DEUX cas de figure.
const filteredModules = computed<ModuleResponse[]>(() => {
    // CAS 1: Pour les COURS/CM, on filtre par la section.
    if (showSectionField.value && newAffectation.value.sectionId) {
        const sectionId = newAffectation.value.sectionId;
        const filiereIds = new Set<number>();
        for (const liaison of secParamLiaisons.value) {
            const parts = liaison.split('-').map(Number);
            // parts[0] = filiereId, parts[1] = sectionId, parts[2] = semestreId
            if (parts[1] === sectionId) filiereIds.add(parts[0]);
        }
        const moduleIds = new Set<number>();
        for (const liaison of filModLiaisons.value) {
            const parts = liaison.split('-').map(Number);
            // parts[0] = filiereId, parts[1] = moduleId
            if (filiereIds.has(parts[0])) moduleIds.add(parts[1]);
        }
        return modules.value.filter(module => moduleIds.has(module.id));
    }

    // CAS 2: Pour les TD/TP, on filtre par le groupe sélectionné.
    if (showGroupeField.value && newAffectation.value.groupeId) {
        const selectedGroupe = groupes.value.find(g => g.id === newAffectation.value.groupeId);
        // On retourne directement la liste des modules attachés à ce groupe.
        return selectedGroupe ? selectedGroupe.modules : [];
    }
    
    // Si aucune condition n'est remplie, on retourne une liste vide.
    return [];
});

const professeursAvecNomComplet = computed(() =>
    professeurs.value.map(p => ({ ...p, fullName: `${p.nom.toUpperCase()} ${p.prenom}` }))
);

// --- OBSERVATEURS ---
// Ces watchers sont cruciaux pour réinitialiser les champs dépendants et éviter les incohérences.
watch(() => newAffectation.value.typeSeanceId, () => {
    newAffectation.value.sectionId = undefined;
    newAffectation.value.groupeId = undefined;
    newAffectation.value.moduleId = undefined;
});

watch(() => newAffectation.value.sectionId, () => {
    newAffectation.value.moduleId = undefined; // Si la section change, le module doit être re-sélectionné.
});

// ✅ NOUVEAU WATCHER : Si le groupe change, le module doit être re-sélectionné.
watch(() => newAffectation.value.groupeId, () => {
    newAffectation.value.moduleId = undefined;
});


// --- CONFIGURATION DE LA TABLE ---
const professeursHeaders: DataTableHeader[] = [
  { key: "fullName", title: "Nom du Professeur" },
  { key: "actions", title: "Gérer les Affectations", sortable: false, align: 'end', width: '250px' },
];

// --- CYCLE DE VIE DU COMPOSANT ---
onMounted(() => {
    Promise.all([
        affectationStore.fetchAffectations(),
        professeurStore.fetchProfesseurs(),
        moduleStore.fetchModules(),
        typeSeanceStore.fetchTypesSeance(),
        sectionStore.fetchSections(),
        groupeStore.fetchGroupes(),
        filModStore.fetchFilMods(),
        secParamStore.fetchSecParams(),
    ]);
});

// --- MÉTHODES ---
function openManageDialog(prof: ProfesseurResponse) {
    selectedProfesseur.value = prof;
    newAffectation.value = {};
    error.value = null;
    showManageDialog.value = true;
}

function closeManageDialog() {
    showManageDialog.value = false;
    selectedProfesseur.value = null;
}

async function creerAffectation() {
    if (!selectedProfesseur.value || !newAffectation.value.moduleId || !newAffectation.value.typeSeanceId) return;

    const payload: AffectationPayload = {
        professeurId: selectedProfesseur.value.id,
        moduleId: newAffectation.value.moduleId,
        typeSeanceId: newAffectation.value.typeSeanceId,
        sectionId: newAffectation.value.sectionId || null,
        groupeId: newAffectation.value.groupeId || null,
    };

    const success = await affectationStore.createAffectation(payload);
    if (success) {
        newAffectation.value = {};
    }
}

async function supprimerAffectation(id: number) {
    if (confirm("Êtes-vous sûr de vouloir supprimer cette affectation ?")) {
        await affectationStore.deleteAffectation(id);
    }
}

function formatTypeSeanceTitle(item: TypeSeanceResponse): string {
    if (item.libelle.toUpperCase() === 'CM') return 'COURS';
    return item.libelle;
}
</script>

<template>
  <div>
    <h1 class="text-h4 font-weight-bold mb-6">Affectations des Professeurs</h1>
    
    <v-card variant="outlined">
        <v-card-title class="d-flex align-center pa-4">
            <span class="text-h6">Sélectionnez un professeur pour gérer ses affectations</span>
            <v-spacer></v-spacer>
            <v-text-field
                v-model="searchProfesseur"
                label="Rechercher un professeur..."
                variant="outlined" density="compact" prepend-inner-icon="mdi-magnify"
                hide-details single-line style="max-width: 300px;"
            ></v-text-field>
        </v-card-title>
        <v-divider></v-divider>

        <v-data-table
            :headers="professeursHeaders"
            :items="professeursAvecNomComplet"
            :loading="tableLoading"
            :search="searchProfesseur"
            hover density="compact" item-value="id"
        >
            <template v-slot:item.actions="{ item }">
                <v-btn color="primary" variant="tonal" size="small" @click="openManageDialog(item)">
                    <v-icon start>mdi-cogs</v-icon>
                    Gérer les affectations
                </v-btn>
            </template>
        </v-data-table>
    </v-card>

    <!-- DIALOGUE DE GESTION DES AFFECTATIONS -->
    <v-dialog v-model="showManageDialog" max-width="1000px" persistent scrollable>
      <v-card v-if="selectedProfesseur">
        <v-card-title class="d-flex align-center pa-4 bg-primary-lighten-1">
          <v-icon start>mdi-account-cog-outline</v-icon>
          <span class="text-h6">
            Affectations de : <strong>{{ selectedProfesseur.nom.toUpperCase() }} {{ selectedProfesseur.prenom }}</strong>
          </span>
          <v-spacer></v-spacer>
          <v-btn icon="mdi-close" variant="text" @click="closeManageDialog"></v-btn>
        </v-card-title>
        <v-divider></v-divider>

        <v-card-text class="pa-4">
          <v-alert v-if="error" type="error" closable class="mb-4" density="compact" @click:close="error = null">{{ error }}</v-alert>
          <v-row>
            <!-- COLONNE DE GAUCHE : FORMULAIRE DE CRÉATION -->
            <v-col cols="12" md="5">
              <v-card variant="tonal" class="pa-4">
                <h3 class="text-subtitle-1 font-weight-medium mb-4">Ajouter une affectation</h3>
                <v-row dense>
                  
                  <v-col cols="12">
                    <v-select 
                        v-model="newAffectation.typeSeanceId" 
                        label="1. Type de Séance" 
                        :items="filteredTypesSeance" 
                        :item-title="formatTypeSeanceTitle" 
                        item-value="id" 
                        variant="outlined" 
                        density="compact" 
                    />
                  </v-col>
                  
                  <!-- S'affiche seulement pour les Cours / CM -->
                  <v-col v-if="showSectionField" cols="12">
                    <v-select v-model="newAffectation.sectionId" label="2. Section" :items="sections" item-title="libelle" item-value="id" variant="outlined" density="compact" />
                  </v-col>
                  
                  <!-- ✅ NOUVEL ORDRE : S'affiche seulement pour les TD / TP -->
                  <v-col v-if="showGroupeField" cols="12">
                    <v-select 
                      v-model="newAffectation.groupeId" 
                      label="2. Groupe" 
                      :items="groupes" 
                      item-title="libelle" 
                      item-value="id" 
                      variant="outlined" 
                      density="compact" 
                    />
                  </v-col>

                  <!-- S'affiche une fois qu'une Section ou un Groupe a été choisi -->
                  <v-col v-if="showModuleField" cols="12">
                     <v-autocomplete 
                        v-model="newAffectation.moduleId" 
                        label="3. Module" 
                        :items="filteredModules" 
                        item-title="libelle" 
                        item-value="id" 
                        variant="outlined" 
                        density="compact"
                        no-data-text="Sélectionnez une section ou un groupe"
                     />
                  </v-col>

                </v-row>
                <v-btn color="success" class="mt-4" block @click="creerAffectation" :loading="affectationsLoading">
                  <v-icon start>mdi-plus-circle-outline</v-icon>
                  Valider l'affectation
                </v-btn>
              </v-card>
            </v-col>
            
            <!-- COLONNE DE DROITE : LISTE DES AFFECTATIONS EXISTANTES -->
            <v-col cols="12" md="7">
              <h3 class="text-subtitle-1 font-weight-medium mb-3">Affectations actuelles</h3>
              <v-list density="compact" class="border rounded-lg" style="max-height: 400px; overflow-y: auto;">
                <template v-if="affectationsDuProfesseur.length > 0">
                  <v-list-item v-for="affectation in affectationsDuProfesseur" :key="affectation.id" class="mb-1">
                    <v-list-item-title class="font-weight-bold">{{ affectation.moduleLibelle }}</v-list-item-title>
                    <v-list-item-subtitle>
                      {{ affectation.typeSeanceLibelle }} -
                      <v-chip size="small" :color="affectation.sectionId ? 'teal' : 'indigo'" label>{{ affectation.sectionLibelle || affectation.groupeLibelle }}</v-chip>
                    </v-list-item-subtitle>
                    <template v-slot:append>
                      <v-btn icon="mdi-delete-outline" variant="text" color="error" size="small" @click="supprimerAffectation(affectation.id)" title="Supprimer"/>
                    </template>
                  </v-list-item>
                </template>
                <v-list-item v-else><v-list-item-title class="text-center text-medium-emphasis py-8">Aucune affectation.</v-list-item-title></v-list-item>
              </v-list>
            </v-col>
          </v-row>
        </v-card-text>
        <v-divider></v-divider>
        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn color="primary" variant="elevated" @click="closeManageDialog">Fermer</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>