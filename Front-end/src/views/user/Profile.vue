<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useAuthStore } from '../../stores/auth'; // Adjust path if needed
import type { PasswordUpdateRequest, AdminUserCreateRequest } from '../../payloads'; // Adjust path if needed

const authStore = useAuthStore();

const userInfo = reactive({
  firstName: '',
  lastName: '',
  username: '',
  email: '',
});

const userRolesDisplay = computed(() => {
  if (!authStore.userRoles || authStore.userRoles.length === 0) {
    return 'Non renseign├®';
  }
  return authStore.userRoles
      .map(role => {
        if (role === 'ROLE_ADMIN') return 'Administrateur';
        if (role === 'ROLE_CONSULTATION') return 'Consultant';
        // Improved fallback for other roles
        return role.startsWith('ROLE_') ?
            role.substring(5).charAt(0).toUpperCase() + role.substring(6).toLowerCase().replace(/_/g, ' ')
            : role;
      })
      .join(', ');
});

const passwordForm = reactive<{currentPassword: string; newPassword: string; confirmNewPassword: string;}>({
  currentPassword: '',
  newPassword: '',
  confirmNewPassword: ''
});

const passwordLoading = ref(false);
const passwordError = ref<string | null>(null);
const passwordSuccess = ref<string | null>(null);

const newAdminForm = reactive<AdminUserCreateRequest>({
  username: '',
  email: '',
  password: '',
  firstName: '',
  lastName: '',
  roles: ['ROLE_ADMIN']
});
const adminCreateLoading = ref(false);
const adminCreateError = ref<string | null>(null);
const adminCreateSuccess = ref<string | null>(null);

onMounted(() => {
  if (authStore.user) {
    userInfo.firstName = authStore.user.firstName || '';
    userInfo.lastName = authStore.user.lastName || '';
    userInfo.username = authStore.user.username || '';
    userInfo.email = authStore.user.email || '';
  }
});

const clearMessages = (type: 'password' | 'admin' | 'all' = 'all') => {
  if (type === 'password' || type === 'all') {
    passwordError.value = null;
    passwordSuccess.value = null;
  }
  if (type === 'admin' || type === 'all') {
    adminCreateError.value = null;
    adminCreateSuccess.value = null;
  }
};

const handleChangePassword = async () => {
  clearMessages('password');
  if (passwordForm.newPassword !== passwordForm.confirmNewPassword) {
    passwordError.value = 'Les nouveaux mots de passe ne correspondent pas.';
    return;
  }
  if (!passwordForm.newPassword || passwordForm.newPassword.length < 6) {
    passwordError.value = 'Le nouveau mot de passe doit comporter au moins 6 caract├¿res.';
    return;
  }

  passwordLoading.value = true;
  try {
    const payload: PasswordUpdateRequest = {
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword,
    };
    await authStore.updatePassword(payload);
    passwordSuccess.value = 'Mot de passe mis ├á jour avec succ├¿s !';
    passwordForm.currentPassword = '';
    passwordForm.newPassword = '';
    passwordForm.confirmNewPassword = '';
    setTimeout(() => clearMessages('password'), 5000);
  } catch (error: any) {
    console.error('Erreur lors de la mise ├á jour du mot de passe:', error);
    passwordError.value = error.response?.data?.message || error.message || 'Une erreur est survenue lors de la mise ├á jour.';
  } finally {
    passwordLoading.value = false;
  }
};

const handleCreateAdmin = async () => {
  clearMessages('admin');
  if (!newAdminForm.username || !newAdminForm.email || !newAdminForm.password) {
    adminCreateError.value = "Nom d'utilisateur, email et mot de passe sont requis.";
    return;
  }
  if (newAdminForm.password.length < 6) {
    adminCreateError.value = 'Le mot de passe doit comporter au moins 6 caract├¿res.';
    return;
  }

  adminCreateLoading.value = true;
  try {
    await authStore.adminCreateUser(newAdminForm);
    adminCreateSuccess.value = `Compte administrateur '${newAdminForm.username}' cr├®├® avec succ├¿s !`;
    newAdminForm.username = '';
    newAdminForm.email = '';
    newAdminForm.password = '';
    newAdminForm.firstName = '';
    newAdminForm.lastName = '';
    setTimeout(() => clearMessages('admin'), 5000);
  } catch (error: any) {
    console.error('Erreur lors de la cr├®ation du compte admin:', error);
    adminCreateError.value = error.response?.data?.message || error.message || "Une erreur est survenue lors de la cr├®ation.";
  } finally {
    adminCreateLoading.value = false;
  }
};
</script>

<template>
  <v-container class="py-10 profile-page-container">
    <v-row justify="center">
      <v-col cols="12" md="10" lg="8">
        <!-- Titre principal de la page, plus pro├®minent -->
        <h1 class="text-h3 font-weight-bold text-center mb-10 primary--text">Mon Espace Personnel</h1>

        <!-- User Info Card -->
        <v-card class="mb-10 pa-2" elevation="3" rounded="lg">
          <v-card-title class="d-flex align-center text-h5 font-weight-medium mb-4">
            <v-icon start color="primary" size="large">mdi-account-circle-outline</v-icon>
            Informations Personnelles
          </v-card-title>
          <v-divider class="mb-6"></v-divider>
          <v-card-text>
            <div v-if="authStore.user" >
              <v-row dense>
                <v-col cols="12" sm="6" class="pb-2">
                  <v-text-field
                      label="Pr├®nom"
                      :model-value="userInfo.firstName || 'Non renseign├®'"
                      readonly
                      variant="outlined"
                      density="compact"
                      prepend-inner-icon="mdi-account-outline"
                  />
                </v-col>
                <v-col cols="12" sm="6" class="pb-2">
                  <v-text-field
                      label="Nom"
                      :model-value="userInfo.lastName || 'Non renseign├®'"
                      readonly
                      variant="outlined"
                      density="compact"
                      prepend-inner-icon="mdi-account-outline"
                  />
                </v-col>
                <v-col cols="12" sm="6" class="pb-2">
                  <v-text-field
                      label="Nom d'utilisateur"
                      :model-value="userInfo.username || 'Non renseign├®'"
                      readonly
                      variant="outlined"
                      density="compact"
                      prepend-inner-icon="mdi-at"
                  />
                </v-col>
                <v-col cols="12" sm="6" class="pb-2">
                  <v-text-field
                      label="Email"
                      :model-value="userInfo.email || 'Non renseign├®'"
                      readonly
                      variant="outlined"
                      density="compact"
                      prepend-inner-icon="mdi-email-outline"
                  />
                </v-col>
                <v-col cols="12" class="pb-2">
                  <v-text-field
                      label="R├┤le(s)"
                      :model-value="userRolesDisplay"
                      readonly
                      variant="outlined"
                      density="compact"
                      prepend-inner-icon="mdi-shield-account-outline"
                  />
                </v-col>
              </v-row>
            </div>
            <!-- Am├®lioration de l'indicateur de chargement -->
            <div v-else class="text-center py-10">
              <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
              <p class="mt-4 text-subtitle-1 text-grey-darken-1">Chargement des informations...</p>
            </div>
          </v-card-text>
        </v-card>

        <!-- Password Update Card -->
        <v-card class="mb-10 pa-2" elevation="3" rounded="lg">
          <v-card-title class="d-flex align-center text-h5 font-weight-medium mb-4">
            <v-icon start color="primary" size="large">mdi-lock-reset</v-icon>
            Modifier le mot de passe
          </v-card-title>
          <v-divider class="mb-6"></v-divider>
          <v-card-text>
            <v-form @submit.prevent="handleChangePassword">
              <v-text-field
                  v-model="passwordForm.currentPassword"
                  label="Mot de passe actuel"
                  type="password"
                  required
                  variant="outlined"
                  density="compact"
                  prepend-inner-icon="mdi-lock-outline"
                  clearable
                  class="mb-3"
              />
              <v-text-field
                  v-model="passwordForm.newPassword"
                  label="Nouveau mot de passe"
                  type="password"
                  required
                  variant="outlined"
                  density="compact"
                  prepend-inner-icon="mdi-lock-plus-outline"
                  :rules="[v => !!v || 'Nouveau mot de passe requis', v => (v && v.length >= 6) || 'Minimum 6 caract├¿res']"
                  clearable
                  class="mb-3"
              />
              <v-text-field
                  v-model="passwordForm.confirmNewPassword"
                  label="Confirmer le nouveau mot de passe"
                  type="password"
                  required
                  variant="outlined"
                  density="compact"
                  prepend-inner-icon="mdi-lock-check-outline"
                  :rules="[v => !!v || 'Confirmation requise', v => v === passwordForm.newPassword || 'Les mots de passe ne correspondent pas']"
                  clearable
                  class="mb-3"
              />

              <!-- Alertes plus stylis├®es -->
              <v-alert v-if="passwordError" type="error" density="compact" variant="tonal" class="mt-4 mb-4" closable @click:close="passwordError = null">
                {{ passwordError }}
              </v-alert>
              <v-alert v-if="passwordSuccess" type="success" density="compact" variant="tonal" class="mt-4 mb-4" closable @click:close="passwordSuccess = null">
                {{ passwordSuccess }}
              </v-alert>

              <v-btn
                  type="submit"
                  color="primary"
                  :loading="passwordLoading"
                  class="mt-4"
                  size="large"
                  variant="elevated"
                  prepend-icon="mdi-content-save-edit-outline"
              >
                Mettre ├á jour
              </v-btn>
            </v-form>
          </v-card-text>
        </v-card>

        <!-- Create Admin Card -->
        <v-card v-if="authStore.isAdmin" class="pa-2" elevation="3" rounded="lg">
          <v-card-title class="d-flex align-center text-h5 font-weight-medium mb-4 text-green-darken-2">
            <v-icon start color="success" size="large">mdi-account-plus-outline</v-icon>
            Cr├®er un Compte Administrateur
          </v-card-title>
          <v-divider class="mb-6"></v-divider>
          <v-card-text>
            <v-form @submit.prevent="handleCreateAdmin">
              <v-row dense>
                <v-col cols="12" sm="6" class="pb-2">
                  <v-text-field
                      v-model="newAdminForm.firstName"
                      label="Pr├®nom (Optionnel)"
                      variant="outlined"
                      density="compact"
                      prepend-inner-icon="mdi-account-outline"
                      clearable
                  />
                </v-col>
                <v-col cols="12" sm="6" class="pb-2">
                  <v-text-field
                      v-model="newAdminForm.lastName"
                      label="Nom (Optionnel)"
                      variant="outlined"
                      density="compact"
                      prepend-inner-icon="mdi-account-outline"
                      clearable
                  />
                </v-col>
                <v-col cols="12" class="pb-2">
                  <v-text-field
                      v-model="newAdminForm.username"
                      label="Nom d'utilisateur"
                      required
                      variant="outlined"
                      density="compact"
                      prepend-inner-icon="mdi-at"
                      :rules="[v => !!v || 'Nom d\'utilisateur requis']"
                      clearable
                  />
                </v-col>
                <v-col cols="12" class="pb-2">
                  <v-text-field
                      v-model="newAdminForm.email"
                      label="Email"
                      type="email"
                      required
                      variant="outlined"
                      density="compact"
                      prepend-inner-icon="mdi-email-outline"
                      :rules="[v => !!v || 'Email requis', v => /.+@.+\..+/.test(v) || 'Email invalide']"
                      clearable
                  />
                </v-col>
                <v-col cols="12" class="pb-2">
                  <v-text-field
                      v-model="newAdminForm.password"
                      label="Mot de passe"
                      type="password"
                      required
                      variant="outlined"
                      density="compact"
                      prepend-inner-icon="mdi-lock-plus-outline"
                      :rules="[v => !!v || 'Mot de passe requis', v => (v && v.length >= 6) || 'Minimum 6 caract├¿res']"
                      clearable
                  />
                </v-col>
              </v-row>

              <v-alert v-if="adminCreateError" type="error" density="compact" variant="tonal" class="mt-4 mb-4" closable @click:close="adminCreateError = null">
                {{ adminCreateError }}
              </v-alert>
              <v-alert v-if="adminCreateSuccess" type="success" density="compact" variant="tonal" class="mt-4 mb-4" closable @click:close="adminCreateSuccess = null">
                {{ adminCreateSuccess }}
              </v-alert>

              <v-btn
                  type="submit"
                  color="success"
                  class="mt-4"
                  size="large"
                  variant="elevated"
                  :loading="adminCreateLoading"
                  prepend-icon="mdi-account-check-outline"
              >
                Cr├®er le Compte
              </v-btn>
            </v-form>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped>
/* Optionnel: Vous pouvez ajouter un l├®ger fond ├á la page si vous le souhaitez */
/* Optionnel: Vous pouvez ajouter un l├®ger fond ├á la page si vous le souhaitez */
/* .profile-page-container {
  background-color: #f9f9f9;
} */

/* Assurer un espacement coh├®rent pour les champs de texte quand dense est utilis├® */
.v-text-field--density-compact {
  margin-bottom: 8px; /* Ajustez si n├®cessaire */
}

/* Am├®lioration de la lisibilit├® des labels quand le champ est readonly et outlined */
:deep(.v-text-field--variant-outlined.v-input--is-readonly .v-field__outline__notch .v-label.v-field-label) {
  color: rgba(var(--v-theme-on-surface), var(--v-high-emphasis-opacity));
  opacity: 1;
}
:deep(.v-text-field--variant-outlined.v-input--is-readonly .v-field__outline__start),
:deep(.v-text-field--variant-outlined.v-input--is-readonly .v-field__outline__end),
:deep(.v-text-field--variant-outlined.v-input--is-readonly .v-field__outline__notch::before),
:deep(.v-text-field--variant-outlined.v-input--is-readonly .v-field__outline__notch::after) {
  border-color: rgba(var(--v-border-color), var(--v-border-opacity)) !important;
}

/* Styles pour les titres de carte */
.v-card-title .v-icon {
  margin-right: 12px; /* Espace entre l'ic├┤ne et le texte du titre */
}

/* Un peu plus d'espace en bas des cartes */
.v-card.mb-10 {
  margin-bottom: 40px !important; /* Vuetify utilise !important pour mb-x, donc on doit le surcharger si on veut plus */
}
</style>
