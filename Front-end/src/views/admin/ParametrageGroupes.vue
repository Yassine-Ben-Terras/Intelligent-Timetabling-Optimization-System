// src/views/GrpParametrage.vue

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { storeToRefs } from "pinia";
import type { DataTableHeader, GrpParamPayload, GrpParamDto, Section } from "../../types/types.ts";

// --- INITIALISATION DES STORES ---
import { useGrpParamStore } from "../../stores/grpParams";
import { useSectionStore } from "../../stores/sections";
import { useGroupeStore } from "../../stores/groupes";
import { useSemestreStore } from "../../stores/semestres";

const grpParamStore = useGrpParamStore();
const sectionStore = useSectionStore();
const groupeStore = useGroupeStore();
const semestreStore = useSemestreStore();

// --- STATE ---
const { liaisons, loading: liaisonsLoading, error } = storeToRefs(grpParamStore);
const { sections, loading: sectionsLoading } = storeToRefs(sectionStore);
const { groupes } = storeToRefs(groupeStore);
const { semestres } = storeToRefs(semestreStore);

// --- STATE UI ---
const showManageDialog = ref(false);
const selectedSection = ref<Section | null>(null);
const searchSection = ref('');
const formError = ref<string | null>(null);
const formLoading = ref(false);

// Objet pour le formulaire d'ajout dans la modale
const newParam = ref<Partial<Pick<GrpParamPayload, 'groupeId' | 'semestreId'>>>({});

// --- LOGIQUE DE DONNÉES ---
const tableLoading = computed(() => sectionsLoading.value || liaisonsLoading.value);

// Calcule les paramétrages (groupes) liés à la SECTION sélectionnée
const groupesLies = computed(() => {
  if (!selectedSection.value) return [];
  return liaisons.value
    .filter(l => l.sectionId === selectedSection.value!.id)
    .sort((a, b) => a.groupeLibelle.localeCompare(b.groupeLibelle)); // Tri pour la lisibilité
});

// --- CONFIGURATION DU COMPOSANT ---
const sectionsHeaders: DataTableHeader[] = [
  { key: "libelle", title: "Nom de la Section" },
  { key: "actions", title: "Gérer les Groupes", sortable: false, align: 'end', width: '250px' },
];

// --- CYCLE DE VIE ---
onMounted(() => {
  Promise.all([
    grpParamStore.fetchGrpParams(),
    sectionStore.fetchSections(),
    groupeStore.fetchGroupes(),
    semestreStore.fetchSemestres(),
  ]).catch(err => {
    console.error("Erreur lors du chargement des données:", err);
    // Vous pouvez assigner une erreur globale ici si nécessaire
  });
});

// --- GESTIONNAIRES D'ÉVÉNEMENTS ---
function openManageDialog(section: Section) {
  selectedSection.value = section;
  newParam.value = {}; // Réinitialise le formulaire
  formError.value = null; // Réinitialise les erreurs du formulaire
  showManageDialog.value = true;
}

function closeManageDialog() {
  showManageDialog.value = false;
  selectedSection.value = null;
}

// --- ACTIONS API ---
async function ajouterLiaison() {
  formError.value = null;
  if (!selectedSection.value || !newParam.value.groupeId || !newParam.value.semestreId) {
    formError.value = "Veuillez remplir tous les champs.";
    return;
  }
  
  const payload: GrpParamPayload = {
    sectionId: selectedSection.value.id,
    groupeId: newParam.value.groupeId,
    semestreId: newParam.value.semestreId,
  };

  formLoading.value = true;
  try {
    await grpParamStore.addGrpParam(payload);
    // On ne réinitialise que le groupe pour faciliter les ajouts multiples
    newParam.value.groupeId = undefined; 
  } catch (err) {
      formError.value = (err as Error).message;
  } finally {
      formLoading.value = false;
  }
}

async function supprimerLiaison(item: GrpParamDto) {
  const { sectionId, groupeId, semestreId } = item;
  await grpParamStore.deleteGrpParam({ sectionId, groupeId, semestreId });
}
</script>

<template>
  <div>
    <h1 class="text-h4 font-weight-bold mb-6">Paramétrage des Groupes par Section</h1>
    <v-alert v-if="error" type="error" closable class="mb-4">{{ error }}</v-alert>

    <v-card variant="outlined">
      <v-card-title class="d-flex align-center pa-4">
        <span class="text-h6">Sélectionnez une section pour gérer ses groupes</span>
        <v-spacer></v-spacer>
        <v-text-field
            v-model="searchSection"
            label="Rechercher une section..."
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
          :headers="sectionsHeaders"
          :items="sections"
          :loading="tableLoading"
          :search="searchSection"
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
            Gérer les groupes
          </v-btn>
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-model="showManageDialog" max-width="1000px" persistent scrollable>
      <v-card v-if="selectedSection">
        <v-card-title class="d-flex align-center pa-4">
          <v-icon start color="primary">mdi-google-classroom</v-icon>
          <span class="text-h6">
            Groupes de la section : {{ selectedSection.libelle }}
          </span>
          <v-spacer></v-spacer>
          <v-btn icon="mdi-close" variant="text" @click="closeManageDialog"></v-btn>
        </v-card-title>
        <v-divider></v-divider>

        <v-card-text>
          <v-row>
            <!-- Colonne pour ajouter une nouvelle liaison -->
            <v-col cols="12" md="5">
              <h3 class="text-subtitle-1 font-weight-medium mb-4">Lier un nouveau groupe</h3>
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
                    ></v-select>
                  </v-col>
                  <v-col cols="12">
                    <v-select
                        v-model="newParam.groupeId"
                        :items="groupes"
                        item-title="libelle"
                        item-value="id"
                        label="Groupe à lier"
                        variant="outlined"
                        density="compact"
                    ></v-select>
                  </v-col>
                </v-row>
                <v-btn
                  block
                  color="success"
                  class="mt-4"
                  @click="ajouterLiaison"
                  :loading="formLoading"
                  :disabled="!newParam.groupeId || !newParam.semestreId"
                >
                  <v-icon start>mdi-link-variant-plus</v-icon>
                  Lier le groupe
                </v-btn>
              </v-sheet>
            </v-col>

            <!-- Colonne des groupes déjà liés -->
            <v-col cols="12" md="7">
              <h3 class="text-subtitle-1 font-weight-medium mb-4">Groupes déjà liés</h3>
              <v-list density="compact" class="border rounded-lg" style="max-height: 400px; overflow-y: auto;">
                <v-list-item
                    v-for="item in groupesLies"
                    :key="item.key"
                    :title="item.groupeLibelle"
                    :subtitle="`Semestre : ${item.semestreLibelle}`"
                >
                  <template v-slot:append>
                    <v-btn
                        icon="mdi-link-variant-off"
                        variant="text"
                        color="error"
                        size="small"
                        @click="supprimerLiaison(item)"
                        title="Délier ce groupe"
                    ></v-btn>
                  </template>
                </v-list-item>
                <v-list-item v-if="groupesLies.length === 0">
                  <v-list-item-title class="text-center text-medium-emphasis py-4">
                    Aucun groupe lié à cette section.
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