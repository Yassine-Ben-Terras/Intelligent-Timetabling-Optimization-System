<script setup lang="ts">
const props = defineProps<{
  modelValue: boolean
  title: string
  text: string
  confirmText?: string
  cancelText?: string
  confirmColor?: string
}>()

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const confirmButton = props.confirmText || 'Confirmer'
const cancelButton = props.cancelText || 'Annuler'
const color = props.confirmColor || 'error'

function confirm() {
  emit('confirm')
  emit('update:modelValue', false)
}

function cancel() {
  emit('cancel')
  emit('update:modelValue', false)
}
</script>

<template>
  <v-dialog
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    max-width="500px"
  >
    <v-card>
      <v-card-title class="text-h5">
        {{ title }}
      </v-card-title>

      <v-card-text>
        {{ text }}
      </v-card-text>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          color="grey"
          variant="text"
          @click="cancel"
        >
          {{ cancelButton }}
        </v-btn>
        <v-btn
          :color="color"
          variant="elevated"
          @click="confirm"
        >
          {{ confirmButton }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>