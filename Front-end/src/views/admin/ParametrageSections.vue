<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { storeToRefs } from "pinia";
import type { DataTableHeader, SecParamPayload, FiliereResponse } from "../../types/types.ts";

// --- INITIALISATION DES STORES ---
import { useSecParamStore } from "../../stores/secParams";
import { useFiliereStore } from "../../stores/filieres";
import { useSectionStore } from "../../stores/sections";
import { useSemestreStore } from "../../stores/semestres";

const secParamStore = useSecParamStore();
const filiereStore = useFiliereStore();
const sectionStore = useSectionStore();
const semestreStore = useSemestreStore();

// --- STATE ---
const { liaisons, loading: liaisonsLoading, error } = storeToRefs(secParamStore);
const { filieres, loading: filieresLoading } = storeToRefs(filiereStore);
const { sections } = storeToRefs(sectionStore);
const { semestres } = storeToRefs(semestreStore);

// --- STATE UI ---
const showManageDialog = ref(false);
const selectedFiliere = ref<FiliereResponse | null>(null);
const searchFiliere = ref('');
// Objet pour le formulaire d'ajout dans le dialogue
const newParam = ref<Partial<Pick<SecParamPayload, 'sectionId' | 'semestreId'>>>({});
const formError = ref<string | null>(null);
const formLoading = ref(false);

// --- LOGIQUE DE DONNÉES ---
const tableLoading = computed(() => filieresLoading.value || liaisonsLoading.value);

// Crée des Maps pour une recherche rapide des libellés (meilleure performance)
const sectionMap = computed(() => new Map(sections.value.map(i => [i.id, i.libelle])));
const semestreMap = computed(() => new Map(semestres.value.map(i => [i.id, i.libelle])));

// Calcule les paramétrages liés à la FILIÈRE sélectionnée
const parametresLies = computed(() => {
  if (!selectedFiliere.value) return [];

  const filiereId = selectedFiliere.value.id;
  const result = [];

  for (const key of liaisons.value) {
      const ids = key.split('-').map(Number);
      const liaisonFiliereId = ids[0]; // filiereId

      if (liaisonFiliereId === filiereId) {
        const [, sectionId, semestreId] = ids; // on ignore le premier élément (filiereId)
        result.push({
        key,
        filiereId,
        sectionId,
        semestreId,
        sectionLibelle: sectionMap.value.get(sectionId) || 'N/A',
        semestreLibelle: semestreMap.value.get(semestreId) || 'N/A',
      });
    }
  }
  return result.sort((a,b) => a.sectionLibelle.localeCompare(b.sectionLibelle)); // Tri pour la lisibilité
});


// --- CONFIGURATION DU COMPOSANT ---
const filieresHeaders: DataTableHeader[] = [
  { key: "libelle", title: "Nom de la Filière" },
  { key: "actions", title: "Gérer les Sections", sortable: false, align: 'end', width: '250px' },
];

// --- CYCLE DE VIE ---
onMounted(() => {
  Promise.all([
    filiereStore.fetchFilieres(),
    sectionStore.fetchSections(),
    semestreStore.fetchSemestres(),
    secParamStore.fetchSecParams(),
  ]).catch(err => {
    console.error("Erreur lors du chargement des données:", err);
  });
});

// --- GESTIONNAIRES D'ÉVÉNEMENTS ---
function openManageDialog(filiere: FiliereResponse) {
  selectedFiliere.value = filiere;
  newParam.value = {}; // Réinitialise le formulaire
  formError.value = null;
  showManageDialog.value = true;
}

function closeManageDialog() {
  showManageDialog.value = false;
  selectedFiliere.value = null;
}

// --- ACTIONS API ---
async function ajouterLiaison() {
  formError.value = null;
  if (!selectedFiliere.value || !newParam.value.sectionId || !newParam.value.semestreId) {
    formError.value = "Veuillez sélectionner une section et un semestre.";
    return;
  }
  const payload: SecParamPayload = {
    filiereId: selectedFiliere.value.id,
    sectionId: newParam.value.sectionId,
    semestreId: newParam.value.semestreId,
  };
  
  formLoading.value = true;
  try {
      await secParamStore.addSecParam(payload);
      // On ne réinitialise pas le semestre pour faciliter les ajouts multiples
      newParam.value.sectionId = undefined;
  } catch(err) {
      formError.value = (err as Error).message || "Erreur lors de l'ajout.";
  } finally {
      formLoading.value = false;
  }

}

async function supprimerLiaison(item: Omit<SecParamPayload, 'key'>) {
    const { filiereId, sectionId, semestreId } = item;
    await secParamStore.deleteSecParam({ filiereId, sectionId, semestreId });
}
</script>

<template>
  <div>
    <h1 class="text-h4 font-weight-bold mb-6">Paramétrage Sections / Filières</h1>
    <v-alert v-if="error" type="error" closable class="mb-4">{{ error }}</v-alert>

    <v-card class="mb-6" variant="outlined">
      <v-card-title class="d-flex align-center pa-4">
        <span class="text-h6">Sélectionnez une filière pour gérer ses sections</span>
        <v-spacer></v-spacer>
        <v-text-field
            v-model="searchFiliere"
            label="Rechercher une filière..."
            variant="outlined"
            density="compact"
            prepend-inner-icon="mdi-magnify"
            hide-details
            single-line
            style="max-width: 300px;"
        ></v-text-field>
      </v-card-title>
      <v-divider></v-divider>

      <v-data-table
          :headers="filieresHeaders"
          :items="filieres"
          :loading="tableLoading"
          :search="searchFiliere"
          hover
          density="compact"
      >
        <template v-slot:item.actions="{ item }">
          <v-btn
              color="primary"
              variant="tonal"
              size="small"
              @click="openManageDialog(item)"
          >
            <v-icon start>mdi-cogs</v-icon>
            Gérer les sections
          </v-btn>
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-model="showManageDialog" max-width="1000px" persistent scrollable>
      <v-card v-if="selectedFiliere">
        <v-card-title class="d-flex align-center pa-4">
          <v-icon start color="primary">mdi-school-outline</v-icon>
          <span class="text-h6">
            Sections de la filière : {{ selectedFiliere.libelle }}
          </span>
          <v-spacer></v-spacer>
          <v-btn icon="mdi-close" variant="text" @click="closeManageDialog"></v-btn>
        </v-card-title>
        <v-divider></v-divider>

        <v-card-text>
          <v-row>
            <!-- Colonne pour ajouter une nouvelle liaison -->
            <v-col cols="12" md="5">
              <h3 class="text-subtitle-1 font-weight-medium mb-4">Lier une nouvelle section</h3>
              <v-sheet class="border rounded-lg pa-4">
                <v-alert v-if="formError" type="error" density="compact" class="mb-4">{{ formError }}</v-alert>
                <v-row dense>
                   <v-col cols="12">
                    <v-select
                        v-model="newParam.semestreId"
                        :items="semestres"
                        item-title="libelle"
                        item-value="id"
                        label="Semestre"
                        variant="outlined"
                        density="compact"
                        hide-details
                    ></v-select>
                  </v-col>
                  <v-col cols="12">
                    <v-select
                        v-model="newParam.sectionId"
                        :items="sections"
                        item-title="libelle"
                        item-value="id"
                        label="Section à lier"
                        variant="outlined"
                        density="compact"
                        hide-details
                    ></v-select>
                  </v-col>
                </v-row>
                <v-btn
                  block
                  color="success"
                  class="mt-4"
                  @click="ajouterLiaison"
                  :disabled="!newParam.sectionId || !newParam.semestreId"
                  prepend-icon="mdi-plus"
                  :loading="formLoading"
                >Ajouter la section</v-btn>
              </v-sheet>
            </v-col>

            <!-- Colonne des sections déjà liées -->

            <v-col cols="12" md="7">
              <h3 class="text-subtitle-1 font-weight-medium mb-4">Sections déjà liées</h3>
              <v-list density="compact" class="border rounded-lg" style="max-height: 400px; overflow-y: auto;">
                <v-list-item
                    v-for="item in parametresLies"
                    :key="item.key"
                    :title="item.sectionLibelle"
                    :subtitle="`Semestre : ${item.semestreLibelle}`"
                >
                  <template v-slot:append>
                    <v-btn
                        icon="mdi-link-variant-off"
                        variant="text"
                        color="error"
                        size="small"
                        @click="supprimerLiaison(item)"
                        title="Délier cette section"
                    ></v-btn>
                  </template>
                </v-list-item>
                <v-list-item v-if="parametresLies.length === 0">
                  <v-list-item-title class="text-center text-medium-emphasis py-4">
                    Aucune section liée à cette filière.
                  </v-list-item-title>
                </v-list-item>
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