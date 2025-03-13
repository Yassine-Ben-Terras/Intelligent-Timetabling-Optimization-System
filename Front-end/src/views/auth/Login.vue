<template>
<v-container fluid class="pa-0" style="display: flex; align-items: center; justify-content: center; min-height: 100vh;">
    <v-row align="center" justify="center" class="fill-height ma-0">
      <v-col cols="12" sm="8" md="6" lg="4" xl="3">
        <v-card class="pa-6 pa-sm-8 mx-auto elevation-10 rounded-xl" max-width="460">

          <div class="text-center mb-8">
           <!--  <img :src="logoUrl" alt="Planify Logo" style="width: 200px; height: auto;" /> -->
          </div>

          <v-card-title class="text-h5 text-md-h4 font-weight-bold text-center mb-2 text-grey-darken-3">
            Connexion
          </v-card-title>
          <v-card-subtitle class="text-center mb-7 text-grey-darken-1">
            Accédez à votre espace sécurisé.
          </v-card-subtitle>

          <v-alert
              v-if="error"
              type="error"
              variant="tonal"
              closable
              class="mb-5 text-body-2"
              @click:close="error = ''"
              density="compact"
              prominent
              border="start"
              rounded="lg"
              icon="mdi-alert-circle-outline"
          >
            {{ error }}
          </v-alert>

          <v-form @submit.prevent="handleLogin" ref="loginFormRef" class="mt-1">
            <v-text-field
                v-model="formState.username"
                label="Nom d'utilisateur ou E-mail"
                prepend-inner-icon="mdi-account-outline"
                variant="filled"
                bg-color="grey-lighten-4"
                flat
                :disabled="loading"
                required
                class="mb-4"
                :rules="[rules.required, rules.minLength(formState.username, 3)]"
                density="comfortable"
                color="primary"
                clearable
                rounded="lg"
            ></v-text-field>

            <v-text-field
                v-model="formState.password"
                label="Mot de passe"
                prepend-inner-icon="mdi-lock-outline"
                :append-inner-icon="showPassword ? 'mdi-eye-off-outline' : 'mdi-eye-outline'"
                :type="showPassword ? 'text' : 'password'"
                variant="filled"
                bg-color="grey-lighten-4"
                flat
                :disabled="loading"
                required
                class="mb-3"
                :rules="[rules.required]"
                @click:append-inner="showPassword = !showPassword"
                density="comfortable"
                color="primary"
                clearable
                rounded="lg"
            ></v-text-field>

            <div class="d-flex justify-space-between align-center mb-6">
              <v-checkbox
                  v-model="formState.rememberMe"
                  label="Se souvenir de moi"
                  density="compact"
                  hide-details
                  :disabled="loading"
                  color="primary"
              ></v-checkbox>
              <a href="#" class="text-caption text-primary font-weight-medium" @click.prevent="forgotPassword" style="text-decoration: none;">
                Mot de passe oublié ?
              </a>
            </div>

            <v-btn
                type="submit"
                color="primary"
                block
                size="large"
                :loading="loading"
                class="mb-4 elevation-3 text-transform-none"
                height="50"
                rounded="lg"
            >
              <template v-if="loading">
                <v-progress-circular indeterminate size="24" width="3" class="mr-3"></v-progress-circular>
                Connexion en cours...
              </template>
              <span v-else class="font-weight-bold">Se connecter</span>
            </v-btn>

            <v-btn
                variant="outlined"
                block
                color="grey-darken-1"
                @click="goToRegister"
                :disabled="loading"
                size="large"
                height="50"
                class="font-weight-medium"
                rounded="lg"
            >
              Créer un compte
            </v-btn>
          </v-form>

          <div class="text-center mt-8">
            <p class="text-caption text-grey-darken-1">
              © {{ new Date().getFullYear() }} FPT Emplois du Temps. Tous droits réservés.
            </p>
          </div>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>




<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '../../stores/auth'; // Adjust path as needed
import type { LoginRequest } from '../../payloads'; // Adjust path as needed
import type { VForm } from 'vuetify/components'; // For VForm instance type

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const loginFormRef = ref<InstanceType<typeof VForm> | null>(null);

const formState = reactive({
  username: '',
  password: '',
  rememberMe: false,
});

const showPassword = ref(false);
const loading = ref(false);
const error = ref('');

const rules = {
  required: (value: string) => !!value || 'Ce champ est requis.',
  minLength: (value: string, min: number) => (value && value.length >= min) || `Doit contenir au moins ${min} caractères.`,
};

async function handleLogin() {
  error.value = '';
  if (!loginFormRef.value) return;

  const { valid } = await loginFormRef.value.validate();
  if (!valid) return;

  loading.value = true;
  try {
    const credentials: LoginRequest = {
      username: formState.username,
      password: formState.password,
    };
    const loginSuccessful = await authStore.login(credentials);

    if (loginSuccessful) {
      const redirectPath = route.query.redirect as string | undefined;
      if (redirectPath && redirectPath !== '/' && redirectPath !== route.path) {
        router.push(redirectPath);
      } else {
        router.push({ name: 'Dashboard' });
      }
    }
  } catch (err: any) {
    console.error("Login.vue: Error during handleLogin:", err);
    error.value = err.response?.data?.message || err.message || "Nom d'utilisateur ou mot de passe incorrect.";
  } finally {
    loading.value = false;
  }
}

function goToRegister() {
  router.push({ name: 'Register' }); // Assurez-vous d'avoir une route nomm├®e 'Register'
}

function forgotPassword() {
  // Logique pour mot de passe oublié (ex: redirection vers une page dédiée)
  console.log("Redirection vers la page de mot de passe oublié");
  // router.push({ name: 'ForgotPassword' }); // Si vous avez cette route
  alert("Fonctionnalité de mot de passe oublié est en cours d'implémentation.");
}
</script>

<style scoped>
.bg-gradient-to-br {
  background-image: linear-gradient(to bottom right, var(--tw-gradient-stops));
}
.from-sky-400 {
  --tw-gradient-from: #38bdf8 var(--tw-gradient-from-position);
  --tw-gradient-to: rgba(56, 189, 248, 0) var(--tw-gradient-to-position); /* Correction alpha pour Tailwind */
  --tw-gradient-stops: var(--tw-gradient-from), var(--tw-gradient-to);
}
.to-indigo-600 {
  --tw-gradient-to: #4f46e5 var(--tw-gradient-to-position); /* Sera la couleur 'to' effective */
}

/* Style pour les champs remplis pour une meilleure apparence avec bg-color */
.v-text-field--variant-filled .v-field__overlay {
  background-color: transparent !important; /* Emp├¬che le double fond de Vuetify */
}
.v-text-field--variant-filled .v-field__field {
  /* Assure un espacement correct si besoin */
}

/* Bouton principal avec un l├®ger effet au survol */
.v-btn.elevation-3:hover {
  box-shadow: 0px 4px 10px -1px var(--v-shadow-key-umbra-opacity, rgba(0, 0, 0, 0.2)),
  0px 3px 8px 0px var(--v-shadow-key-penumbra-opacity, rgba(0, 0, 0, 0.14)),
  0px 2px 16px 1px var(--v-shadow-key-ambient-opacity, rgba(0, 0, 0, 0.12)) !important;
  transform: translateY(-1px);
  transition: all 0.2s ease-out;
}

/* Assurer la lisibilit├® du label du checkbox */
:deep(.v-label.v-label--clickable) {
  opacity: 1 !important; /* Vuetify peut r├®duire l'opacit├® des labels */
  color: rgba(var(--v-theme-on-surface), var(--v-medium-emphasis-opacity)) !important;
}
</style>
