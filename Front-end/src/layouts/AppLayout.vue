<script setup lang="ts">
import { ref, computed, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth' // Assurez-vous que le chemin est correct
import { useDisplay } from 'vuetify'


// Définitions de types pour la clarté
interface MenuItemConfig {
  title: string;
  icon: string;
  route: string;
}


interface MenuGroupConfig {
  groupTitle: string;
  icon: string; // Icône pour l'en-tête du groupe
  items: MenuItemConfig[];
}

// Type uni pour les éléments du menu
type MenuOrGroup = MenuItemConfig | MenuGroupConfig;

const router = useRouter()
const authStore = useAuthStore()
const display = useDisplay()

const drawer = ref(true)
const isMobile = computed(() => display.xs.value || display.sm.value)

watchEffect(() => {
  drawer.value = !isMobile.value
})

// ========================================================================
// --- DÉBUT DE LA SECTION MODIFIÉE ---
// ========================================================================
const adminMenuDefinition: MenuOrGroup[] = [
  {
    title: 'Tableau de bord',
    icon: 'mdi-view-dashboard',
    route: '/',
  },
  {
    groupTitle: 'Gestion Principale',
    icon: 'mdi-cog-outline',
    items: [
      // Les éléments de paramétrage ont été retirés d'ici
      { title: 'Départements', icon: 'mdi-domain', route: '/departements' },
      { title: 'Professeurs', icon: 'mdi-account-tie', route: '/professeurs' },
      { title: 'Filières', icon: 'mdi-school-outline', route: '/filieres' },
      { title: 'Modules', icon: 'mdi-book-open-page-variant-outline', route: '/modules' },
      { title: 'Sections', icon: 'mdi-account-group-outline', route: '/sections' },
      { title: 'Groupes', icon: 'mdi-account-multiple-outline', route: '/groupes' }, // Cet élément reste à sa place d'origine
      { title: 'Type de salle', icon: 'mdi-home-city-outline', route: '/TypeLocaux' },
      { title: 'Salles', icon: 'mdi-door-closed-lock', route: '/salles' },

    ],
  },
  // NOUVEAU GROUPE AJOUTÉ POUR LE PARAMÉTRAGE
  {
    groupTitle: 'Calendrier & Organisation',
    icon: 'mdi-calendar-month-outline',
    items: [
      { title: 'Années Scolaires', icon: 'mdi-calendar-outline', route: '/annees' },
      { title: 'Sessions', icon: 'mdi-calendar-clock-outline', route: '/sessions' },
      { title: 'Semestres', icon: 'mdi-calendar-range', route: '/semestres' },
      { title: 'Jours', icon: 'mdi-calendar-blank-outline', route: '/jours' },
      { title: 'Créneaux F', icon: 'mdi-clock-outline', route: '/creneaux' },
      { title: 'Créneaux V', icon: 'mdi-clock-outline', route: '/PlagesTravail' },

    ],
  },
  {
    groupTitle: 'Paramétrage',
    icon: 'mdi-cogs', // Icône générale pour les configurations
    items: [
      { title: 'Modules par Filière', icon: 'mdi-link-variant', route: '/filMods' },
      { title: 'Paramétrage Sections', icon: 'mdi-format-list-group', route: '/ParametrageSections' },
      { title: 'Paramétrage Groupes', icon: 'mdi-account-multiple-plus-outline', route: '/ParametrageGroupes' },
      { title: 'Disponibilités Profs', icon: 'mdi-account-clock-outline', route: '/DispoProfsPage' },
      { title: 'Affectation Prof', icon: 'mdi-account-multiple-plus-outline', route: '/AffectationPage' },
      { title: 'RegroupementsView', icon: 'mdi-account-multiple-plus-outline', route: '/RegroupementsView' },

    ]
  },

  {
    groupTitle: 'Emploi du Temps',
    icon: 'mdi-timetable',
    items: [
      { title: 'Insertion automatique ', icon: 'mdi-database-cog-outline', route: '/disponibilites' },
      { title: 'Génération EDT', icon: 'mdi-cogs', route: '/generation' },
      { title: 'Consultation EDT', icon: 'mdi-calendar-search', route: '/ConsultationEmploi' },
    //  { title: 'Consultation ', icon: 'mdi-calendar-search', route: '/Fix' },
      { title: 'Archive', icon: 'mdi-archive-outline', route: '/ArchiveList' } ,

    ],
  },
]
// ========================================================================
// --- FIN DE LA SECTION MODIFIÉE ---
// =================================m=======================================


const userMenuDefinition: MenuItemConfig[] = [
  { title: 'Tableau de bord', icon: 'mdi-view-dashboard', route: '/' },
  { title: 'Consulter EDT', icon: 'mdi-calendar-search', route: '/ConsultationEmploi' },
]

// Typage explicite pour menuItems
const menuItems = computed<MenuOrGroup[]>(() =>
    authStore.isAdmin ? adminMenuDefinition : userMenuDefinition
)

function logout() {
  // Keycloak gère la redirection après déconnexion
  authStore.logout()
}

const userInitials = computed(() => {
  if (authStore.userName) {
    const names = authStore.userName.split(' ')
    if (names.length > 1) {
      return `${names[0][0]}${names[names.length - 1][0]}`.toUpperCase()
    }
    return authStore.userName.substring(0, 2).toUpperCase()
  }
  return 'U'
})

function navigateToProfile() {
  router.push('/profile')
}

const openGroups = ref<string[]>([])
watchEffect(() => {
  if(authStore.isAdmin) {
    // Option: ouvrir le premier groupe par défaut pour les admins si adminMenuDefinition contient des groupes.
    // const firstGroup = adminMenuDefinition.find(item => 'groupTitle' in item) as MenuGroupConfig | undefined;
    // if (firstGroup) {
    //   openGroups.value = [firstGroup.groupTitle];
    // }
  } else {
    openGroups.value = []; // Réinitialiser pour les utilisateurs normaux
  }
});

</script>

<template>
  <v-navigation-drawer
      v-model="drawer"
      app
      :width="280"
      color="surface"
      class="modern-drawer"
  >
    <div class="pa-4 drawer-header">
      <div class="d-flex align-center mb-3">
        <v-avatar color="primary" size="48" class="mr-3 elevation-2">
          <span class="text-h6 font-weight-bold">{{ userInitials }}</span>
        </v-avatar>
        <div>
          <div class="text-subtitle-1 font-weight-bold">{{ authStore.userName || 'Utilisateur Anonyme' }}</div>
          <div class="text-caption text-medium-emphasis">
            {{ authStore.isAdmin ? 'Administrateur' : 'Utilisateur' }}
          </div>
        </div>
        <v-spacer />
        <v-btn
            v-if="!isMobile"
            variant="text"
            icon="mdi-backburger"
            size="small"
            @click.stop="drawer = !drawer"
            class="drawer-toggle-btn"
        />
      </div>
    </div>

    <v-divider />

    <v-list v-model:opened="openGroups" density="compact" nav class="pa-2">
      <!--
        Iteration sur menuItems. Chaque itemOrGroup peut être:
        1. Un MenuGroupConfig (s'il a 'groupTitle')
        2. Un MenuItemConfig (s'il a 'title' et 'route' mais pas 'groupTitle')
      -->
      <template v-for="(itemOrGroup) in menuItems" :key="(itemOrGroup as MenuGroupConfig).groupTitle || (itemOrGroup as MenuItemConfig).title">
        <!-- Cas 1: C'est un groupe (MenuGroupConfig) -->
        <v-list-group
            v-if="'groupTitle' in itemOrGroup && itemOrGroup.items"
            :value="(itemOrGroup as MenuGroupConfig).groupTitle"
        >
          <template v-slot:activator="{ props }">
            <v-list-item
                v-bind="props"
                :prepend-icon="(itemOrGroup as MenuGroupConfig).icon"
                :title="(itemOrGroup as MenuGroupConfig).groupTitle"
                class="text-subtitle-2 font-weight-medium list-group-header"
                rounded="lg"
            />
          </template>

          <!-- Sous-éléments du groupe -->
          <v-list-item
              v-for="subItem in (itemOrGroup as MenuGroupConfig).items"
              :key="subItem.title"
              :prepend-icon="subItem.icon"
              :title="subItem.title"
              :to="subItem.route"
              rounded="lg"
              class="mx-2 my-1 list-sub-item"
              active-class="primary-active-item"
          />
        </v-list-group>

        <!-- Cas 2: C'est un item simple (MenuItemConfig) -->
        <v-list-item
            v-else-if="'title' in itemOrGroup && itemOrGroup.route"
            :prepend-icon="(itemOrGroup as MenuItemConfig).icon"
            :title="(itemOrGroup as MenuItemConfig).title"
            :to="(itemOrGroup as MenuItemConfig).route"
            rounded="lg"
            class="mx-0 my-1 list-main-item"
            active-class="primary-active-item"
        />
      </template>
    </v-list>
  </v-navigation-drawer>

  <v-app-bar app elevation="1" color="surface" class="modern-app-bar">
    <v-app-bar-nav-icon @click="drawer = !drawer" />
    <v-toolbar-title class="font-weight-medium app-title">FPT Emplois du Temps</v-toolbar-title>
    <v-spacer />

    <v-menu offset-y location="bottom end" transition="slide-y-transition" content-class="elevation-3">
      <template v-slot:activator="{ props }">
        <v-btn icon v-bind="props" class="ml-2 mr-2 user-menu-activator">
          <v-badge dot color="success" offset-x="-2" offset-y="-2" :model-value="!!authStore.userName">
            <v-avatar color="primary" size="36">
              <span v-if="authStore.userName" class="text-subtitle-2 font-weight-medium">{{ userInitials }}</span>
              <v-icon v-else>mdi-account-circle-outline</v-icon>
            </v-avatar>
          </v-badge>
        </v-btn>
      </template>

      <v-list density="compact" nav width="250" class="py-2">
        <v-list-item class="px-3 mb-2 user-info-dropdown">
          <template v-slot:prepend>
            <v-avatar color="primary" size="40" class="mr-3">
              <span v-if="authStore.userName" class="text-subtitle-1 font-weight-medium">{{ userInitials }}</span>
              <v-icon v-else>mdi-account-circle-outline</v-icon>
            </v-avatar>
          </template>
          <v-list-item-title class="font-weight-semibold">{{ authStore.userName || 'Utilisateur' }}</v-list-item-title>
          <v-list-item-subtitle class="text-caption">{{ authStore.isAdmin ? 'Administrateur' : 'Utilisateur' }}</v-list-item-subtitle>
        </v-list-item>

        <v-divider class="my-1 mx-3" />

        <v-list-item
            prepend-icon="mdi-account-outline"
            title="Mon Profil"
            @click="navigateToProfile"
            rounded="md"
            class="mx-2 mt-1"
        />
        <v-list-item
            prepend-icon="mdi-logout"
            title="Déconnexion"
            @click="logout"
            rounded="md"
            class="mx-2 mb-1"
        >
          <!-- Style appliqué à l'icône de déconnexion pour la couleur d'erreur -->
          <template v-slot:prepend>
            <v-icon color="error">mdi-logout</v-icon>
          </template>
          <!-- Style appliqué au titre de déconnexion pour la couleur d'erreur -->
          <v-list-item-title class="text-error">Déconnexion</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>
  </v-app-bar>

  <v-main style="background-color: #f7f8fa;">
    <v-container fluid class="pa-5">
      <router-view v-slot="{ Component }">
        <transition name="fade-transform" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </v-container>
  </v-main>

  <v-footer app class="d-flex flex-column pa-0 modern-footer" style="border-top: 1px solid #e0e0e0;">
    <div class="bg-surface d-flex w-100 align-center px-4 py-2">
      <strong class="text-subtitle-2">FPT Emplois du Temps</strong>
      <v-spacer />
      <span class="text-caption">© {{ new Date().getFullYear() }} - Tous droits réservés</span>
    </div>
  </v-footer>
</template>

<style scoped>

.modern-drawer .drawer-toggle-btn {
  color: rgba(var(--v-theme-on-surface), 0.6);
}

.modern-drawer .list-group-header .v-list-item__prepend > .v-icon {
  margin-inline-end: 18px !important;
  opacity: 0.8;
}
.modern-drawer .list-group-header:hover {
  background-color: rgba(var(--v-theme-primary), 0.04); /* Légère teinte au survol */
}

.modern-drawer .list-sub-item .v-list-item__prepend > .v-icon {
  margin-inline-end: 12px !important;
  font-size: 0.8rem;
  opacity: 0.7;
}

.modern-drawer .list-main-item .v-list-item__prepend > .v-icon {
  margin-inline-end: 18px !important;
  opacity: 0.8;
}

/* Style pour l'élément actif */
.primary-active-item {
  background-color: rgba(var(--v-theme-primary), 0.08) !important; /* Couleur de fond active */
  color: rgb(var(--v-theme-primary)) !important; /* Couleur de texte active */
  font-weight: 500 !important; /* Police en gras pour l'élément actif */
}
.primary-active-item .v-list-item__prepend > .v-icon {
  color: rgb(var(--v-theme-primary)) !important; /* Couleur d'icône active */
  opacity: 1;
}

/* App Bar */
.modern-app-bar .app-title {
  letter-spacing: 0.3px;
  font-size: 1.1rem;
}
.modern-app-bar .user-menu-activator {
  transition: transform 0.2s ease-in-out;
}
.modern-app-bar .user-menu-activator:hover {
  transform: scale(1.1);
}

/* User info in dropdown */
.user-info-dropdown .v-list-item-title {
  font-size: 0.95rem;
}
.user-info-dropdown .v-list-item-subtitle {
  font-size: 0.75rem;
}


/* Transitions pour router-view */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease; /* Transition plus rapide */
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(8px); /* Mouvement vertical réduit */
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(-8px); /* Mouvement vertical réduit */
}

:deep(.v-list-group__items .v-list-item) {
  padding-inline-start: 38px !important;
}

/* Assurez-vous que l'overlay ne masque pas notre couleur de fond active custom */
:deep(.v-list-item.primary-active-item .v-list-item__overlay) {

  background-color: transparent !important; /* Ou ajustez l'opacité si besoin */
}

/* Tailles de police pour les titres dans la navigation */
:deep(.v-list-group .v-list-item-title),
:deep(.v-list > .v-list-item .v-list-item-title) { /* Titres de groupe et items de 1er niveau */
  font-size: 0.9rem !important;
  font-weight: 500 !important;
  line-height: 1.2; /* Améliore l'espacement vertical */
}
:deep(.v-list-group__items .v-list-item .v-list-item-title) { /* Items dans un groupe */
  font-size: 0.875rem !important;
  font-weight: 400 !important;
  line-height: 1.2;
}

/* Pour le bouton déconnexion dans le menu utilisateur */
.v-list-item .text-error {
  color: rgb(var(--v-theme-error)) !important;
}
</style>