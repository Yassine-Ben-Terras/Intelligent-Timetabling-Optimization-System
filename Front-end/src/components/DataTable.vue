<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { DataTableHeader, FilterOption } from '../types'

const props = defineProps<{
  title: string
  headers: DataTableHeader[]
  items: any[]
  loading?: boolean
  filters?: FilterOption[]
  itemsPerPageOptions?: number[]
}>()

const emit = defineEmits(['edit', 'delete', 'add', 'filter'])

// Table state
const search = ref('')
const itemsPerPage = ref(10)
const page = ref(1)
const sortBy = ref([
  { key: props.headers[0]?.key || 'id', order: 'asc' as const }
])

const filterValues = ref<Record<string, any>>({})
const showFilters = ref(false)

// Computed
const filteredItems = computed(() => {
  let result = [...props.items]

  if (search.value) {
    const searchLower = search.value.toLowerCase()
    result = result.filter(item => {
      return Object.keys(item).some(key => {
        const value = item[key as keyof typeof item]
        return value && String(value).toLowerCase().includes(searchLower)
      })
    })
  }

  Object.entries(filterValues.value).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      result = result.filter(item => {
        const itemValue = item[key as keyof typeof item]
        if (typeof value === 'string') {
          return String(itemValue).toLowerCase().includes(value.toLowerCase())
        }
        return itemValue === value
      })
    }
  })

  return result
})

const totalPages = computed(() => Math.ceil(filteredItems.value.length / itemsPerPage.value))
const paginatedItems = computed(() => {
  const start = (page.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredItems.value.slice(start, end)
})

const hasActiveFilters = computed(() => {
  return Object.values(filterValues.value).some(v => v !== undefined && v !== null && v !== '')
})

function onFilterChange() {
  page.value = 1
  emit('filter', filterValues.value)
}

function resetFilters() {
  filterValues.value = {}
  search.value = ''
  page.value = 1
}

watch(() => props.items.length, (newLength, oldLength) => {
  if (newLength < oldLength && page.value > totalPages.value) {
    page.value = Math.max(1, totalPages.value)
  }
})
</script>

<template>
  <div class="data-table-component">
    <v-card variant="outlined" rounded="lg" class="mb-6">
      <!-- Header -->
      <div class="dt-header">
        <div class="dt-title-area">
          <span class="dt-title">{{ title }}</span>
          <v-chip
            v-if="filteredItems.length !== items.length"
            size="x-small"
            color="primary"
            variant="tonal"
            class="ml-2"
          >
            {{ filteredItems.length }} / {{ items.length }}
          </v-chip>
        </div>
        <div class="dt-actions-area">
          <v-text-field
            v-model="search"
            prepend-inner-icon="mdi-magnify"
            placeholder="Rechercher..."
            single-line
            hide-details
            density="compact"
            variant="outlined"
            class="dt-search"
          ></v-text-field>

          <v-btn
            v-if="filters && filters.length"
            :variant="showFilters ? 'tonal' : 'outlined'"
            :color="hasActiveFilters ? 'primary' : undefined"
            density="comfortable"
            @click="showFilters = !showFilters"
          >
            <v-icon start>mdi-filter-variant</v-icon>
            Filtres
            <v-badge
              v-if="hasActiveFilters"
              color="primary"
              dot
              inline
            ></v-badge>
          </v-btn>

          <v-btn
            color="primary"
            variant="elevated"
            @click="emit('add')"
          >
            <v-icon start>mdi-plus</v-icon>
            Ajouter
          </v-btn>
        </div>
      </div>

      <!-- Filters area -->
      <v-expand-transition>
        <div v-if="showFilters && filters && filters.length" class="dt-filters">
          <v-row dense>
            <v-col
              v-for="filter in filters"
              :key="filter.key"
              cols="12"
              sm="6"
              md="4"
              lg="3"
            >
              <v-select
                v-if="filter.type === 'select'"
                v-model="filterValues[filter.key]"
                :label="filter.label"
                :items="filter.options"
                clearable
                density="compact"
                variant="outlined"
                @update:model-value="onFilterChange"
              ></v-select>
              <v-text-field
                v-else-if="filter.type === 'text'"
                v-model="filterValues[filter.key]"
                :label="filter.label"
                clearable
                density="compact"
                variant="outlined"
                @update:model-value="onFilterChange"
              ></v-text-field>
              <v-switch
                v-else-if="filter.type === 'boolean'"
                v-model="filterValues[filter.key]"
                :label="filter.label"
                density="compact"
                hide-details
                color="primary"
                @update:model-value="onFilterChange"
              ></v-switch>
            </v-col>
          </v-row>
          <div class="dt-filters-actions">
            <v-btn
              variant="text"
              size="small"
              color="error"
              prepend-icon="mdi-filter-remove"
              @click="resetFilters"
              :disabled="!hasActiveFilters && !search"
            >
              Réinitialiser
            </v-btn>
          </div>
        </div>
      </v-expand-transition>

      <v-divider></v-divider>

      <!-- Table -->
      <v-data-table
        :headers="headers"
        :items="paginatedItems"
        :loading="loading"
        :sort-by="sortBy"
        :items-per-page="itemsPerPage"
        density="comfortable"
        hover
        class="elevation-0"
      >
        <template v-slot:loading>
          <v-skeleton-loader type="table-row@5" class="pa-4"></v-skeleton-loader>
        </template>

        <template v-slot:no-data>
          <div class="dt-empty">
            <v-icon size="48" color="grey-lighten-1">mdi-database-off-outline</v-icon>
            <p class="text-body-1 text-medium-emphasis mt-3 mb-1">Aucune donnée disponible</p>
            <p v-if="search" class="text-body-2 text-disabled">
              Essayez de modifier votre recherche « {{ search }} »
            </p>
          </div>
        </template>

        <!-- Action buttons with tooltips -->
        <template v-slot:item.actions="{ item }">
          <div class="dt-action-buttons">
            <v-btn
              icon
              variant="text"
              color="primary"
              size="small"
              @click="emit('edit', item)"
            >
              <v-icon size="18">mdi-pencil-outline</v-icon>
              <v-tooltip activator="parent" location="top">Modifier</v-tooltip>
            </v-btn>
            <v-btn
              icon
              variant="text"
              color="error"
              size="small"
              @click="emit('delete', item)"
            >
              <v-icon size="18">mdi-delete-outline</v-icon>
              <v-tooltip activator="parent" location="top">Supprimer</v-tooltip>
            </v-btn>
          </div>
        </template>
      </v-data-table>

      <v-divider></v-divider>

      <!-- Footer -->
      <div class="dt-footer">
        <div class="dt-footer-info">
          <span class="text-body-2 text-medium-emphasis">
            {{ filteredItems.length }} élément(s)
          </span>
          <v-select
            v-model="itemsPerPage"
            :items="itemsPerPageOptions || [5, 10, 25, 50]"
            density="compact"
            hide-details
            variant="outlined"
            class="dt-per-page"
          ></v-select>
          <span class="text-body-2 text-medium-emphasis">par page</span>
        </div>
        <v-pagination
          v-if="totalPages > 1"
          v-model="page"
          :length="totalPages"
          rounded="circle"
          :disabled="loading"
          density="compact"
          :total-visible="5"
        ></v-pagination>
      </div>
    </v-card>
  </div>
</template>

<style scoped>
.data-table-component {
  width: 100%;
}

/* Header */
.dt-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.875rem 1rem;
  gap: 1rem;
  flex-wrap: wrap;
}

.dt-title-area {
  display: flex;
  align-items: center;
}

.dt-title {
  font-size: 1.15rem;
  font-weight: 700;
  color: #1e293b;
}

.dt-actions-area {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.dt-search {
  max-width: 240px;
  min-width: 160px;
}

/* Filters */
.dt-filters {
  padding: 0.75rem 1rem;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
}

.dt-filters-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 0.25rem;
}

/* Empty state */
.dt-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2.5rem 1rem;
}

/* Action buttons */
.dt-action-buttons {
  display: flex;
  align-items: center;
  gap: 0.15rem;
  justify-content: flex-end;
}

/* Footer */
.dt-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 1rem;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.dt-footer-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.dt-per-page {
  max-width: 80px;
}

/* Responsive */
@media (max-width: 600px) {
  .dt-header {
    flex-direction: column;
    align-items: stretch;
  }
  .dt-actions-area {
    justify-content: flex-end;
  }
  .dt-search {
    max-width: 100% !important;
    flex: 1;
  }
  .dt-footer {
    flex-direction: column;
    align-items: center;
  }
}
</style>