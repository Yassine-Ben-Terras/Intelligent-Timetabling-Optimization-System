<template>
  <div class="regroup-page">
    <div class="regroup-header">
      <div>
        <h1 class="regroup-title">Gestion des Regroupements</h1>
        <p class="regroup-subtitle">Regroupez les sections ou groupes pour la génération des emplois du temps</p>
      </div>
      <v-btn color="primary" variant="elevated" rounded="lg" @click="openDialog()">
        <v-icon start>mdi-plus</v-icon>
        Nouveau regroupement
      </v-btn>
    </div>

    <v-alert v-if="regroupementStore.error" type="error" dismissible class="mb-4" rounded="lg">
      {{ regroupementStore.error }}
    </v-alert>

    <v-card variant="outlined" rounded="lg">
      <v-card-title class="d-flex align-center pa-4">
        <v-icon start color="primary">mdi-group</v-icon>
        <span class="text-h6">Regroupements existants</span>
        <v-spacer></v-spacer>
        <v-text-field
          v-model="search"
          label="Rechercher..."
          prepend-inner-icon="mdi-magnify"
          variant="outlined"
          density="compact"
          hide-details
          clearable
          single-line
          style="max-width: 280px;"
        ></v-text-field>
      </v-card-title>
      <v-divider></v-divider>

      <v-data-table
        :headers="headers"
        :items="filteredRegroupements"
        :loading="regroupementStore.loading"
        item-key="id"
        hover
        density="comfortable"
        no-data-text="Aucun regroupement trouvé."
        loading-text="Chargement des regroupements..."
        items-per-page="15"
      >
        <!-- Type chip -->
        <template v-slot:item.typeRegroupement="{ item }">
          <v-chip
            :color="item.typeRegroupement === 'SECTION' ? 'blue' : 'teal'"
            variant="tonal"
            size="small"
            label
          >
            <v-icon start size="14">
              {{ item.typeRegroupement === 'SECTION' ? 'mdi-view-grid-outline' : 'mdi-account-group-outline' }}
            </v-icon>
            {{ item.typeRegroupement }}
          </v-chip>
        </template>

        <!-- Details inline -->
        <template v-slot:item.details="{ item }">
          <div class="detail-chips">
            <v-chip
              v-for="(detail, index) in item.details.slice(0, 3)"
              :key="index"
              size="x-small"
              variant="outlined"
              class="mr-1"
            >
              <span v-if="detail.sectionLibelle">{{ detail.sectionLibelle }}</span>
              <span v-else-if="detail.groupeLibelle">{{ detail.groupeLibelle }}</span>
              <span v-if="detail.moduleLibelle" class="text-medium-emphasis ml-1">· {{ detail.moduleLibelle }}</span>
            </v-chip>
            <v-chip v-if="item.details.length > 3" size="x-small" variant="tonal" color="grey">
              +{{ item.details.length - 3 }}
            </v-chip>
          </div>
        </template>

        <!-- Actions -->
        <template v-slot:item.actions="{ item }">
          <v-btn icon size="small" variant="text" color="error" @click="confirmDelete(item.id!)">
            <v-icon>mdi-delete-outline</v-icon>
            <v-tooltip activator="parent" location="top">Supprimer</v-tooltip>
          </v-btn>
        </template>

        <template v-slot:no-data>
          <div class="text-center pa-6 text-medium-emphasis">
            <v-icon size="48" class="mb-2">mdi-folder-open-outline</v-icon>
            <p>Aucun regroupement disponible.</p>
            <v-btn color="primary" variant="tonal" size="small" @click="openDialog()">
              Créer un regroupement
            </v-btn>
          </div>
        </template>
      </v-data-table>
    </v-card>

    <!-- Dialog pour Ajouter un Regroupement -->
    <v-dialog v-model="dialog" max-width="850px" persistent>
      <v-card rounded="lg">
        <div class="dialog-header-regroup">
          <div class="d-flex align-center ga-3">
            <v-avatar color="primary" size="40">
              <v-icon color="white">mdi-group</v-icon>
            </v-avatar>
            <div>
              <h2 class="dialog-title-text">{{ formTitle }}</h2>
              <span class="dialog-subtitle-text">Définissez les éléments à regrouper ensemble</span>
            </div>
          </div>
          <v-btn icon="mdi-close" variant="text" size="small" @click="closeDialog"></v-btn>
        </div>
        <v-divider></v-divider>

        <v-card-text class="pa-5">
          <v-form ref="form" v-model="valid" lazy-validation>
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="editedRegroupement.libelle"
                  label="Nom du regroupement"
                  :rules="[v => !!v || 'Le libellé est requis']"
                  required
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-label-outline"
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-select
                  v-model="editedRegroupement.typeRegroupement"
                  :items="['SECTION', 'GROUPE']"
                  label="Type de regroupement"
                  :rules="[v => !!v || 'Le type est requis']"
                  required
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-shape-outline"
                ></v-select>
              </v-col>
            </v-row>

            <v-divider class="my-4"></v-divider>

            <div class="d-flex align-center justify-space-between mb-3">
              <h3 class="text-body-1 font-weight-bold">
                <v-icon start size="18" color="primary">mdi-format-list-bulleted</v-icon>
                Détails (min. 2)
              </h3>
              <v-btn color="primary" variant="tonal" size="small" @click="addDetail">
                <v-icon start>mdi-plus</v-icon>
                Ajouter
              </v-btn>
            </div>

            <v-alert v-if="detailError" type="warning" variant="tonal" density="compact" class="mb-3" rounded="lg">
              {{ detailError }}
            </v-alert>

            <div class="details-list">
              <div v-for="(detail, index) in editedRegroupement.details" :key="index" class="detail-row">
                <div class="detail-number">{{ index + 1 }}</div>
                <v-row align="center" class="flex-grow-1" dense>
                  <v-col cols="12" md="5">
                    <v-select
                      v-if="editedRegroupement.typeRegroupement === 'SECTION'"
                      v-model="detail.sectionId"
                      :items="sectionStore.sections"
                      item-title="libelle"
                      item-value="id"
                      label="Section"
                      :rules="[v => !!v || 'Requise']"
                      required
                      clearable
                      variant="outlined"
                      density="compact"
                      @update:model-value="val => handleSectionOrGroupeChange(detail, val, 'section')"
                      :menu-props="{ maxHeight: '200' }"
                    ></v-select>
                    <v-select
                      v-else-if="editedRegroupement.typeRegroupement === 'GROUPE'"
                      v-model="detail.groupeId"
                      :items="groupeStore.groupes"
                      item-title="libelle"
                      item-value="id"
                      label="Groupe"
                      :rules="[v => !!v || 'Requis']"
                      required
                      clearable
                      variant="outlined"
                      density="compact"
                      @update:model-value="val => handleSectionOrGroupeChange(detail, val, 'groupe')"
                      :menu-props="{ maxHeight: '200' }"
                    ></v-select>
                  </v-col>
                  <v-col cols="12" md="5">
                    <v-select
                      v-model="detail.moduleId"
                      :items="getFilteredModules(detail)"
                      item-title="libelle"
                      item-value="id"
                      label="Module"
                      :rules="[v => !!v || 'Requis']"
                      :disabled="!detail.sectionId && !detail.groupeId"
                      required
                      clearable
                      variant="outlined"
                      density="compact"
                      @update:model-value="val => { detail.moduleId = val; detail.moduleLibelle = moduleStore.modules.find(m => m.id === val)?.libelle || ''; }"
                      :menu-props="{ maxHeight: '200' }"
                    ></v-select>
                  </v-col>
                  <v-col cols="12" md="2" class="text-center">
                    <v-btn icon size="small" variant="text" color="error" @click="removeDetail(index)" :disabled="editedRegroupement.details.length <= 2">
                      <v-icon>mdi-close-circle-outline</v-icon>
                      <v-tooltip activator="parent" location="top">Retirer</v-tooltip>
                    </v-btn>
                  </v-col>
                </v-row>
              </div>
            </div>
          </v-form>
        </v-card-text>

        <v-divider></v-divider>
        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="closeDialog">Annuler</v-btn>
          <v-btn color="primary" variant="elevated" @click="saveRegroupement" :disabled="!isSaveEnabled">
            <v-icon start>mdi-content-save</v-icon>
            Enregistrer
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Dialog de confirmation de suppression -->
    <v-dialog v-model="deleteConfirmDialog" max-width="450px">
      <v-card rounded="lg">
        <v-card-title class="d-flex align-center ga-2 pa-4">
          <v-icon color="error">mdi-alert-circle-outline</v-icon>
          Confirmer la suppression
        </v-card-title>
        <v-card-text>Êtes-vous sûr de vouloir supprimer ce regroupement ? Cette action est irréversible.</v-card-text>
        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="deleteConfirmDialog = false">Annuler</v-btn>
          <v-btn color="error" variant="elevated" @click="deleteRegroupementConfirmed">
            <v-icon start>mdi-delete</v-icon>
            Supprimer
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRegroupementStore } from '../../stores/regroupementStore';
import { useSectionStore } from '../../stores/sections';
import { useGroupeStore } from '../../stores/groupes';
import { useModuleStore } from '../../stores/modules';
import { useFilModStore } from '../../stores/filMods';
import { useSecParamStore } from '../../stores/secParams';
import type { RegroupementPayload, RegroupementDetailDto } from '../../types/types';
import type { VForm } from 'vuetify/components';

const regroupementStore = useRegroupementStore();
const sectionStore = useSectionStore();
const groupeStore = useGroupeStore();
const moduleStore = useModuleStore();
const filModStore = useFilModStore();
const secParamStore = useSecParamStore();

const dialog = ref(false);
const deleteConfirmDialog = ref(false);
const regroupementToDeleteId = ref<number | null>(null);

const defaultRegroupementPayload: RegroupementPayload = {
  libelle: '',
  typeRegroupement: 'SECTION',
  details: [],
};

const editedRegroupement = ref<RegroupementPayload>({ ...defaultRegroupementPayload });

const formTitle = computed(() => 'Nouveau regroupement');
const search = ref('');
const valid = ref(true);
const form = ref<VForm | null>(null);
const detailError = ref<string | null>(null);

const headers = [
  { title: 'Libellé', key: 'libelle', sortable: true },
  { title: 'Type', key: 'typeRegroupement', sortable: true, width: '130px' },
  { title: 'Détails', key: 'details', sortable: false },
  { title: '', key: 'actions', sortable: false, width: '60px', align: 'end' as const },
];

const filteredRegroupements = computed(() => {
  if (!search.value) return regroupementStore.regroupements;
  const searchText = search.value.toLowerCase();
  return regroupementStore.regroupements.filter(reg =>
    reg.libelle.toLowerCase().includes(searchText) ||
    reg.typeRegroupement.toLowerCase().includes(searchText) ||
    reg.details.some(detail =>
      (detail.sectionLibelle && detail.sectionLibelle.toLowerCase().includes(searchText)) ||
      (detail.groupeLibelle && detail.groupeLibelle.toLowerCase().includes(searchText)) ||
      (detail.moduleLibelle && detail.moduleLibelle.toLowerCase().includes(searchText))
    )
  );
});

const isSaveEnabled = computed(() => {
  if (!valid.value) return false;
  if (editedRegroupement.value.details.length < 2) {
    detailError.value = "Un regroupement doit avoir au minimum deux détails.";
    return false;
  }
  for (const detail of editedRegroupement.value.details) {
    if (editedRegroupement.value.typeRegroupement === 'SECTION' && !detail.sectionId) {
      detailError.value = "Chaque détail doit avoir une section sélectionnée.";
      return false;
    }
    if (editedRegroupement.value.typeRegroupement === 'GROUPE' && !detail.groupeId) {
      detailError.value = "Chaque détail doit avoir un groupe sélectionné.";
      return false;
    }
    if (!detail.moduleId) {
      detailError.value = "Chaque détail doit avoir un module sélectionné.";
      return false;
    }
  }
  detailError.value = null;
  return true;
});

onMounted(async () => {
  await regroupementStore.fetchRegroupements();
  await sectionStore.fetchSections();
  await groupeStore.fetchGroupes();
  await moduleStore.fetchModules();
  await filModStore.fetchFilMods();
  await secParamStore.fetchSecParams();
});

watch(dialog, (val) => {
  if (!val) {
    closeDialog();
  } else {
    if (editedRegroupement.value.details.length < 2) {
      addDetail();
      addDetail();
    }
  }
});

watch(() => editedRegroupement.value.typeRegroupement, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    editedRegroupement.value.details = [];
    addDetail();
    addDetail();
  }
});

function getFilteredModules(detail: RegroupementDetailDto) {
  const type = editedRegroupement.value.typeRegroupement;
  if (type === 'GROUPE' && detail.groupeId) {
    const selectedGroupe = groupeStore.groupes.find(g => g.id === detail.groupeId);
    const groupeData = selectedGroupe as any;
    if (groupeData && Array.isArray(groupeData.modules)) {
      return groupeData.modules;
    }
  }
  if (type === 'SECTION' && detail.sectionId) {
    const targetSectionId = detail.sectionId;
    if (!targetSectionId) return [];
    let filiereId: number | undefined;
    let semestreId: number | undefined;
    for (const liaisonKey of secParamStore.liaisons.values()) {
      const parts = liaisonKey.split('-');
      // parts[0] = filiereId, parts[1] = sectionId, parts[2] = semestreId
      const liaisonSectionId = parseInt(parts[1], 10);
      if (liaisonSectionId === targetSectionId) {
        filiereId = parseInt(parts[0], 10);
        semestreId = parseInt(parts[2], 10);
        break;
      }
    }
    if (!filiereId || !semestreId) return [];
    return moduleStore.modules.filter(module =>
      filModStore.liaisons.has(`${filiereId}-${module.id}`) && module.semestre.id === semestreId
    );
  }
  return [];
}

function handleSectionOrGroupeChange(detail: RegroupementDetailDto, value: number | null, type: 'section' | 'groupe') {
  detail.moduleId = undefined;
  detail.moduleLibelle = '';
  if (type === 'section') {
    detail.sectionId = value || undefined;
    detail.groupeId = undefined;
    detail.sectionLibelle = sectionStore.sections.find(s => s.id === value)?.libelle || '';
  } else {
    detail.groupeId = value || undefined;
    detail.sectionId = undefined;
    detail.groupeLibelle = groupeStore.groupes.find(g => g.id === value)?.libelle || '';
  }
}

function openDialog() {
  detailError.value = null;
  editedRegroupement.value = { ...defaultRegroupementPayload, details: [] };
  addDetail();
  addDetail();
  dialog.value = true;
  setTimeout(() => form.value?.resetValidation());
}

function closeDialog() {
  dialog.value = false;
  setTimeout(() => {
    editedRegroupement.value = { ...defaultRegroupementPayload, details: [] };
    detailError.value = null;
    form.value?.reset();
    form.value?.resetValidation();
  }, 300);
}

function addDetail() {
  editedRegroupement.value.details.push({
    moduleId: undefined,
    sectionId: undefined,
    groupeId: undefined,
    moduleLibelle: '',
    sectionLibelle: '',
    groupeLibelle: '',
  });
}

function removeDetail(index: number) {
  if (editedRegroupement.value.details.length > 2) {
    editedRegroupement.value.details.splice(index, 1);
  } else {
    detailError.value = "Un regroupement doit avoir au minimum deux détails.";
  }
}

async function saveRegroupement() {
  const { valid: formValid } = await (form.value?.validate() || { valid: false });
  if (!formValid || !isSaveEnabled.value) return;
  try {
    if ((editedRegroupement.value as any).id) {
      await regroupementStore.updateRegroupement((editedRegroupement.value as any).id, editedRegroupement.value);
    } else {
      await regroupementStore.addRegroupement(editedRegroupement.value);
    }
    closeDialog();
  } catch (err) {
    console.error("Erreur lors de l'enregistrement du regroupement:", err);
  }
}

function confirmDelete(id: number) {
  regroupementToDeleteId.value = id;
  deleteConfirmDialog.value = true;
}

async function deleteRegroupementConfirmed() {
  if (regroupementToDeleteId.value !== null) {
    await regroupementStore.deleteRegroupement(regroupementToDeleteId.value);
    deleteConfirmDialog.value = false;
    regroupementToDeleteId.value = null;
  }
}
</script>

<style scoped>
.regroup-page {
  padding: 0.5rem;
}

.regroup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.regroup-title {
  font-size: 1.6rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.regroup-subtitle {
  font-size: 0.9rem;
  color: #64748b;
  margin: 0.25rem 0 0 0;
}

/* Detail chips in table */
.detail-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}

/* Dialog header */
.dialog-header-regroup {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  background: linear-gradient(135deg, #eff6ff, #f0f9ff);
}

.dialog-title-text {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.dialog-subtitle-text {
  font-size: 0.8rem;
  color: #64748b;
}

/* Details list in dialog */
.details-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
  background: #fafbfc;
  transition: border-color 0.15s;
}

.detail-row:hover {
  border-color: #93c5fd;
}

.detail-number {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  background: #e0f2fe;
  color: #0369a1;
  border-radius: 50%;
  font-weight: 700;
  font-size: 0.8rem;
  flex-shrink: 0;
}
</style>