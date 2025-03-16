<script setup lang="ts">
import { computed } from 'vue' // onMounted retiré
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useDisplay } from 'vuetify'

const router = useRouter()
const authStore = useAuthStore()
const display = useDisplay()

// --- Données de Démonstration (Supprimées car les sections les utilisant sont retirées) ---
// const recentlyGenerated = ref([...]) // Supprimé
// const upcomingConsultations = ref([...]) // Supprimé

// onMounted(() => { ... }) // Supprimé car recentlyGenerated est supprimé

// --- Navigation Rapide ---
const adminQuickNav = [
  { title: 'Professeurs', icon: 'mdi-account-tie-outline', route: '/professeurs', color: 'blue-darken-2' },
  { title: 'Salles', icon: 'mdi-door-open', route: '/salles', color: 'teal-darken-1' },
  { title: 'Modules', icon: 'mdi-book-open-page-variant-outline', route: '/modules', color: 'green-darken-1' },
  { title: 'Sections', icon: 'mdi-account-group-outline', route: '/sections', color: 'purple-darken-1' },
  { title: 'Générer EDT', icon: 'mdi-auto-fix', route: '/generation', color: 'orange-darken-2', highlight: true },
  { title: 'Consulter EDT', icon: 'mdi-calendar-search-outline', route: '/ConsultationEmploi', color: 'indigo-darken-1' }
]

const userQuickNav = [
  { title: 'Consulter Mon EDT', icon: 'mdi-calendar-account-outline', route: '/ConsultationEmploi', color: 'indigo-darken-1', highlight: true },
  { title: 'Mon Profil', icon: 'mdi-account-circle-outline', route: '/profile', color: 'blue-grey-darken-1' }
]

const quickNav = computed(() => authStore.isAdmin ? adminQuickNav : userQuickNav)

function navigateTo(route: string) {
  router.push(route)
}

const getGreeting = () => {
  const hour = new Date().getHours()
  if (hour < 5) return 'Bonne nuit'
  if (hour < 12) return 'Bonjour'
  if (hour < 18) return 'Bon après-midi'
  return 'Bonsoir'
}

// const formatDate = (dateString: string) => { ... } // Supprimé car plus utilisé
</script>

<template>
  <v-container fluid class="pa-sm-6 pa-4" style="background-color: #f9fafb;">
    <!-- En-tête de Bienvenue -->
    <v-row>
      <v-col cols="12">
        <v-sheet rounded="xl" class="pa-6 mb-6" color="surface" elevation="0" border>
          <h1 class="text-h4 font-weight-bold text-grey-darken-3">
            {{ getGreeting() }}, {{ authStore.userName }}!
          </h1>
          <p class="text-body-1 text-grey-darken-1 mt-2">
            Ravi de vous revoir. Voici un accès rapide à vos fonctionnalités clés.
          </p>
        </v-sheet>
      </v-col>
    </v-row>

    <!-- Accès Rapide -->
    <h2 class="text-h5 font-weight-medium text-grey-darken-4 mb-4 mt-2">Vos Actions Rapides</h2>
    <v-row>
      <v-col
          v-for="(item, index) in quickNav"
          :key="index"
          cols="12"
          sm="6"
          :md="authStore.isAdmin ? 4 : 6"
          :lg="authStore.isAdmin ? (quickNav.length > 4 ? 3 : 4) : (display.smAndDown ? 6 : 4)"
      >
        <v-hover v-slot="{ isHovering, props }">
          <v-card
              v-bind="props"
              :elevation="isHovering ? 8 : (item.highlight ? 5 : 3)"
              class="dashboard-quick-nav-card fill-height d-flex flex-column"
              rounded="xl"
              @click="navigateTo(item.route)"
              :color="item.color"
              variant="flat"
              style="transition: all 0.2s ease-in-out;"
              :class="{ 'highlighted-card-hover': isHovering && item.highlight }"
          >
            <v-card-text class="flex-grow-1 d-flex flex-column align-center justify-center text-center pa-5">
              <v-icon
                  color="white"
                  size="44"
                  class="mb-3"
              >{{ item.icon }}</v-icon>
              <div
                  class="text-subtitle-1 font-weight-medium text-white"
              >
                {{ item.title }}
              </div>
            </v-card-text>
            <v-tooltip v-if="item.highlight" activator="parent" location="top" open-delay="300">
              Action prioritaire
            </v-tooltip>
          </v-card>
        </v-hover>
      </v-col>
    </v-row>

    <!-- Les sections "Dernières Générations d'EDT / Vos Dernières Consultations" et "Notifications" ont été SUPPRIMÉES -->
    <!--
    <v-row class="mt-8">
      <v-col cols="12" :md="authStore.isAdmin ? 8 : 12">
        // Contenu de "Dernières Générations d'EDT" / "Vos Dernières Consultations" était ici
      </v-col>

      <v-col cols="12" md="4" v-if="authStore.isAdmin">
        // Contenu de "Notifications" était ici
      </v-col>
    </v-row>
    -->

  </v-container>
</template>

<style scoped>
/* Améliorations pour les cartes d'accès rapide */
.dashboard-quick-nav-card {
  cursor: pointer;
}

.dashboard-quick-nav-card:hover {
  transform: translateY(-4px) scale(1.02); /* Soulèvement et léger agrandissement */
}

/* Effet spécial pour les cartes "highlight" au survol */
/* La classe .highlighted-card-hover est utilisée uniquement pour cibler les styles ci-dessous */
.dashboard-quick-nav-card.orange-darken-2.highlighted-card-hover {
  box-shadow: 0 0 16px 2px rgba(var(--v-theme-orange-darken-2), 0.7) !important;
}
.dashboard-quick-nav-card.indigo-darken-1.highlighted-card-hover {
  box-shadow: 0 0 16px 2px rgba(var(--v-theme-indigo-darken-1), 0.7) !important;
}

/* Style des titres de section */
h2.text-h5 {
  color: rgb(var(--v-theme-grey-darken-4)); /* Assurez-vous que cette variable CSS Vuetify est disponible ou remplacez par une couleur fixe */
}

/* Assurer que le conteneur principal utilise une couleur de fond si ce n'est pas déjà le cas globalement */
.v-main { /* Si votre composant est dans un <v-main> */
  background-color: #f9fafb;
}

/* .list-item-hover { ... } // Supprimé car plus utilisé */
</style>