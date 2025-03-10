import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '../stores/auth'

// Import de toutes vos vues
import Login from '../views/auth/Login.vue'
import Register from '../views/auth/Register.vue'
import Dashboard from '../views/Dashboard.vue'
import NotFound from '../views/NotFound.vue'
import Professeurs from '../views/admin/Professeurs.vue'
import Salles from '../views/admin/Salles.vue'
import Departements from '../views/admin/Departements.vue'
import Filieres from '../views/admin/Filieres.vue'
import Modules from '../views/admin/Modules.vue'
import Groupes from '../views/admin/Groupes.vue'
import Sections from '../views/admin/Sections.vue'
import RegroupementsView from '../views/admin/RegroupementsView.vue'
import ArchiveList from '../views/admin/ArchiveList.vue'


import Semestres from '../views/admin/Semestres.vue'
import Creneaux from '../views/admin/Creneaux.vue'
import Jours from '../views/admin/Jours.vue'
import Sessions from '../views/admin/Sessions.vue'
import Annees from '../views/admin/Annees.vue'
import ImportExcelPage from '../views/admin/ImportExcelPage.vue'
import GenerationEmploi from '../views/admin/GenerationEmploi.vue'
import ConsultationEmploi from '../views/user/ConsultationEmploi.vue'
import Profile from '../views/user/Profile.vue'
import TypeLocaux from "../views/admin/TypeLocaux.vue";
import DispoProfsPage from "../views/admin/DispoProfsPage.vue";
import filMods from "../views/admin/filMods.vue";
import ParametrageSections from "../views/admin/ParametrageSections.vue";
import ProfModules from "../views/admin/ProfModules.vue";
import ParametrageGroupes from "../views/admin/ParametrageGroupes.vue";
import AffectationPage from "../views/admin/AffectationPage.vue";
import PlagesTravail from "../views/admin/PlagesTravail.vue";


// [CORRIGÉ] Voici votre liste de routes complète et originale
const routes: Array<RouteRecordRaw> = [
  { path: '/login', name: 'Login', component: Login, meta: { title: 'Connexion', requiresAuth: false } },
  { path: '/register', name: 'Register', component: Register, meta: { title: 'Créer un compte', requiresAuth: false } },
  { path: '/', name: 'Dashboard', component: Dashboard, meta: { title: 'Tableau de bord', requiresAuth: true } },
  // Admin routes
  { path: '/professeurs', name: 'Professeurs', component: Professeurs, meta: { title: 'Gestion des Professeurs', requiresAuth: true, requiresAdmin: true } },
  { path: '/TypeLocaux', name: 'type salle', component: TypeLocaux, meta: { title: 'Gestion des types de salles', requiresAuth: true, requiresAdmin: true } },
  { path: '/DispoProfsPage', name: 'Dispo Prof', component: DispoProfsPage, meta: { title: 'Gestion des disponibilite des profs', requiresAuth: true, requiresAdmin: true } },
  { path: '/filMods', name: 'Filiere-Module', component: filMods, meta: { title: 'Gestion des modules et des filieres', requiresAuth: true, requiresAdmin: true } },
  { path: '/ParametrageSections', name: 'ParametrageSections', component: ParametrageSections, meta: { title: 'ParametrageSections', requiresAuth: true, requiresAdmin: true } },
  { path: '/ProfModules', name: 'ProfModules', component: ProfModules, meta: { title: 'ProfModules', requiresAuth: true, requiresAdmin: true } },
  { path: '/ParametrageGroupes', name: 'ParametrageGroupes', component: ParametrageGroupes, meta: { title: 'ParametrageGroupes', requiresAuth: true, requiresAdmin: true } },
  { path: '/AffectationPage', name: 'Affectation Prof', component: AffectationPage, meta: { title: 'Affectation Prof', requiresAuth: true, requiresAdmin: true } },
  
  //{ path: '/Fix', name: 'Fix', component: Fix, meta: { title: 'Fix', requiresAuth: true, requiresAdmin: true } },

  { path: '/RegroupementsView', name: 'RegroupementsView', component: RegroupementsView, meta: { title: 'RegroupementsView', requiresAuth: true, requiresAdmin: true } },
  { path: '/ArchiveList', name: 'ArchiveList', component: ArchiveList, meta: { title: 'ArchiveList', requiresAuth: true, requiresAdmin: true } },

  { path: '/salles', name: 'Salles', component: Salles, meta: { title: 'Gestion des Salles', requiresAuth: true, requiresAdmin: true } },
  { path: '/departements', name: 'Departements', component: Departements, meta: { title: 'Gestion des Départements', requiresAuth: true, requiresAdmin: true } },
  { path: '/filieres', name: 'Filieres', component: Filieres, meta: { title: 'Gestion des Filières', requiresAuth: true, requiresAdmin: true } },
  { path: '/modules', name: 'Modules', component: Modules, meta: { title: 'Gestion des Modules', requiresAuth: true, requiresAdmin: true } },
  { path: '/groupes', name: 'Groupes', component: Groupes, meta: { title: 'Gestion des Groupes', requiresAuth: true, requiresAdmin: true } },
  { path: '/sections', name: 'Sections', component: Sections, meta: { title: 'Gestion des Sections', requiresAuth: true, requiresAdmin: true } },
  { path: '/semestres', name: 'Semestres', component: Semestres, meta: { title: 'Gestion des Semestres', requiresAuth: true, requiresAdmin: true } },
  { path: '/creneaux', name: 'Creneaux', component: Creneaux, meta: { title: 'Gestion des Créneaux', requiresAuth: true, requiresAdmin: true } },
  { path: '/jours', name: 'Jours', component: Jours, meta: { title: 'Gestion des Jours', requiresAuth: true, requiresAdmin: true } },
  { path: '/sessions', name: 'Sessions', component: Sessions, meta: { title: 'Gestion des Sessions', requiresAuth: true, requiresAdmin: true } },
  { path: '/annees', name: 'Annees', component: Annees, meta: { title: 'Gestion des Années', requiresAuth: true, requiresAdmin: true } },
  // [CORRIGÉ] La route pour /disponibilites est bien présente ici
  { path: '/disponibilites', name: 'Disponibilites', component: ImportExcelPage, meta: { title: 'Gestion des Disponibilités', requiresAuth: true, requiresAdmin: true } },
  { path: '/generation', name: 'Generation', component: GenerationEmploi, meta: { title: 'Génération des Emplois du Temps', requiresAuth: true, requiresAdmin: true } },
  // User routes
  { path: '/ConsultationEmploi', name: 'Consultation', component: ConsultationEmploi, meta: { title: 'Consultation des Emplois du Temps', requiresAuth: true } },
  { path: '/PlagesTravail', name: 'PlagesTravail', component: PlagesTravail, meta: { title: 'PlagesTravail ', requiresAuth: true } },

  { path: '/profile', name: 'Profile', component: Profile, meta: { title: 'Mon Profil', requiresAuth: true } },


  { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFound, meta: { title: 'Page Non Trouvée' } }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// La garde de navigation reste la m├¬me
router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore();

  if (!authStore.isSessionValidated) {
    await authStore.validateSession();
  }

  // ... le reste de votre logique de garde ...
  if (to.meta.title && typeof to.meta.title === 'string') { document.title = to.meta.title; }
  if (authStore.isAuthenticated && to.meta.requiresAuth === false) { return next({ name: 'Dashboard' }); }
  if (!authStore.isAuthenticated && to.meta.requiresAuth === true) { return next({ name: 'Login', query: { redirect: to.fullPath } }); }
  if (authStore.isAuthenticated && !authStore.isAdmin && to.meta.requiresAdmin === true) { return next({ name: 'Dashboard' }); }

  next();
});

export default router;
