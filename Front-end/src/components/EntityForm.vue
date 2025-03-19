<script setup lang="ts">

import type { FormField } from '../types/types.ts';

// 1. Définir les props que le composant accepte
const props = defineProps<{
  modelValue: any // Reçoit l'objet via v-model
  fields: FormField[]             // La configuration des champs
  title: string
  isEdit: boolean
}>()

// 2. Définir les événements que le composant peut émettre vers le parent
const emit = defineEmits<{
  (e: 'update:modelValue', value: any): void // Pour faire fonctionner v-model
  (e: 'submit'): void
  (e: 'cancel'): void
}>()

// 3. Fonction pour mettre à jour la valeur.
//    Il n'y a plus de copie locale comme `formData`. On travaille directement avec les props
//    et on notifie le parent de chaque changement.
function updateValue(key: string, value: any) {
  // On crée une copie de l'objet actuel, on met à jour la valeur,
  // et on émet l'événement que le parent écoute (via v-model).
  emit('update:modelValue', { ...props.modelValue, [key]: value })
}
</script>

<template>
  <v-form @submit.prevent="emit('submit')">
    <v-card class="form-card">
      <v-card-title class="d-flex align-center">
        <span>{{ title }}</span>
        <v-spacer></v-spacer>
        <v-btn icon="mdi-close" variant="text" @click="emit('cancel')"></v-btn>
      </v-card-title>

      <v-divider></v-divider>

      <v-card-text>
        <v-row>
          <v-col
              v-for="field in fields"
              :key="field.key"
              cols="12"
              sm="6"
          >
            <!--
              CHANGEMENT MAJEUR ICI :
              On n'utilise plus `v-model` sur une copie locale `formData`,
              mais on lie `:model-value` à la prop et `@update:model-value` à notre fonction `updateValue`.
              Ceci rend le composant entièrement contrôlé par le parent.
            -->
            <v-text-field
                v-if="field.type === 'text'"
                :model-value="modelValue[field.key]"
                @update:model-value="value => updateValue(field.key, value)"
                :label="field.label"
                :required="field.required"
                :hint="field.hint"
                variant="outlined"
                density="compact"
            ></v-text-field>

            <v-text-field
                v-else-if="field.type === 'number'"
                :model-value="modelValue[field.key]"
                @update:model-value="value => updateValue(field.key, Number(value) || null)"
                :label="field.label"
                :required="field.required"
                :hint="field.hint"
                variant="outlined"
                density="compact"
                type="number"
            ></v-text-field>

            <v-select
                v-else-if="field.type === 'select'"
                :model-value="modelValue[field.key]"
                @update:model-value="value => updateValue(field.key, value)"
                :label="field.label"
                :required="field.required"
                :hint="field.hint"
                :items="field.items"
                :item-title="field.itemTitle"
                :item-value="field.itemValue"
                variant="outlined"
                density="compact"
                no-data-text="Aucune donnée disponible"
            ></v-select>

            <!-- Ajoutez ici d'autres types de champs si vous en avez (textarea, boolean, etc.) -->

          </v-col>
        </v-row>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
            variant="outlined"
            color="grey"
            @click="emit('cancel')"
        >
          Annuler
        </v-btn>
        <v-btn
            color="primary"
            @click="emit('submit')"
        >
          {{ isEdit ? 'Modifier' : 'Ajouter' }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-form>
</template>

<style scoped>
.form-card {
  border-radius: 8px;
  overflow: hidden;
}
</style>