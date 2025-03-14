<template>
  <v-container fluid class="fill-height pa-0 bg-gradient-to-br from-sky-400 to-indigo-600">
    <v-row align="center" justify="center" class="fill-height ma-0 py-8">
      <v-col cols="12" sm="10" md="8" lg="6" xl="4">
        <v-card class="pa-6 pa-sm-8 mx-auto elevation-10 rounded-xl" max-width="600">
          <div class="text-center mb-5">
            <v-avatar color="primary" size="60" class="elevation-3">
              <v-icon size="32" color="white">mdi-account-plus-outline</v-icon>
            </v-avatar>
          </div>

          <v-card-title class="text-h5 text-md-h4 font-weight-bold text-center mb-2 text-grey-darken-3">
            Cr├®er un compte
          </v-card-title>
          <v-card-subtitle class="text-center mb-7 text-grey-darken-1">
            Rejoignez notre plateforme en quelques ├®tapes.
          </v-card-subtitle>

          <v-alert
              v-if="errorMessage"
              type="error"
              variant="tonal"
              closable
              class="mb-5 text-body-2"
              @click:close="errorMessage = ''"
              density="compact"
              prominent
              border="start"
              rounded="lg"
              icon="mdi-alert-circle-outline"
          >
            {{ errorMessage }}
          </v-alert>
          <v-alert
              v-if="successMessage"
              type="success"
              variant="tonal"
              closable
              class="mb-5 text-body-2"
              @click:close="successMessage = ''"
              density="compact"
              prominent
              border="start"
              rounded="lg"
              icon="mdi-check-circle-outline"
          >
            {{ successMessage }}
          </v-alert>

          <v-form ref="registerFormRef" @submit.prevent="handleRegister" class="mt-1">
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                    v-model="formState.username"
                    label="Nom d'utilisateur (login)"
                    prepend-inner-icon="mdi-account-key-outline"
                    :rules="[rules.required, rules.minLength(formState.username, 3), rules.maxLength(formState.username, 20)]"
                    required
                    variant="filled"
                    bg-color="grey-lighten-4"
                    flat
                    class="mb-3"
                    :disabled="loading"
                    density="comfortable"
                    color="primary"
                    rounded="lg"
                    clearable
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                    v-model="formState.email"
                    type="email"
                    label="Adresse e-mail"
                    prepend-inner-icon="mdi-email-outline"
                    :rules="[rules.required, rules.email]"
                    required
                    variant="filled"
                    bg-color="grey-lighten-4"
                    flat
                    class="mb-3"
                    :disabled="loading"
                    density="comfortable"
                    color="primary"
                    rounded="lg"
                    clearable
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                    v-model="formState.name"
                    label="Pr├®nom"
                    prepend-inner-icon="mdi-account-outline"
                    :rules="[rules.required, rules.maxLength(formState.name, 50)]"
                    required
                    variant="filled"
                    bg-color="grey-lighten-4"
                    flat
                    class="mb-3"
                    :disabled="loading"
                    density="comfortable"
                    color="primary"
                    rounded="lg"
                    clearable
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                    v-model="formState.prenom"
                    label="Nom de famille"
                    prepend-inner-icon="mdi-account-outline"
                    :rules="[rules.required, rules.maxLength(formState.prenom, 50)]"
                    required
                    variant="filled"
                    bg-color="grey-lighten-4"
                    flat
                    class="mb-3"
                    :disabled="loading"
                    density="comfortable"
                    color="primary"
                    rounded="lg"
                    clearable
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                    v-model="formState.password"
                    label="Mot de passe"
                    prepend-inner-icon="mdi-lock-outline"
                    :rules="[rules.required, rules.minLength(formState.password, 6), rules.maxLength(formState.password, 40)]"
                    :type="showPassword ? 'text' : 'password'"
                    :append-inner-icon="showPassword ? 'mdi-eye-off-outline' : 'mdi-eye-outline'"
                    @click:append-inner="showPassword = !showPassword"
                    required
                    variant="filled"
                    bg-color="grey-lighten-4"
                    flat
                    class="mb-3"
                    :disabled="loading"
                    density="comfortable"
                    color="primary"
                    rounded="lg"
                    clearable
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                    v-model="formState.confirmPassword"
                    label="Confirmer le mot de passe"
                    prepend-inner-icon="mdi-lock-check-outline"
                    :rules="[rules.required, rules.confirmPassword(formState.confirmPassword, formState.password)]"
                    :type="showConfirmPassword ? 'text' : 'password'"
                    :append-inner-icon="showConfirmPassword ? 'mdi-eye-off-outline' : 'mdi-eye-outline'"
                    @click:append-inner="showConfirmPassword = !showConfirmPassword"
                    required
                    variant="filled"
                    bg-color="grey-lighten-4"
                    flat
                    class="mb-3"
                    :disabled="loading"
                    density="comfortable"
                    color="primary"
                    rounded="lg"
                    clearable
                ></v-text-field>
              </v-col>
            </v-row>

            <v-btn
                type="submit"
                class="mt-4 mb-3 elevation-3 text-transform-none"
                color="primary"
                block
                size="large"
                height="50"
                :loading="loading"
                rounded="lg"
            >
              <template v-if="loading">
                <v-progress-circular indeterminate size="24" width="3" class="mr-3"></v-progress-circular>
                Cr├®ation du compte...
              </template>
              <span v-else class="font-weight-bold">Cr├®er mon compte</span>
            </v-btn>

            <v-btn
                variant="text"
                block
                color="grey-darken-1"
                @click="goToLogin"
                :disabled="loading"
                size="large"
                class="font-weight-medium"
            >
              D├®j├á un compte ? Se connecter
            </v-btn>
          </v-form>
          <div class="text-center mt-8">
            <p class="text-caption text-grey-darken-1">
              En cr├®ant un compte, vous acceptez nos <a href="#" @click.prevent>Conditions d'utilisation</a>.
            </p>
          </div>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../stores/auth'; // Adjust path
import type { RegisterRequest } from '../../payloads'; // Adjust path
import type { VForm } from 'vuetify/components';

const router = useRouter();
const authStore = useAuthStore();
const registerFormRef = ref<InstanceType<typeof VForm> | null>(null);

const formState = reactive({
  username: '',
  name: '',
  prenom: '',
  email: '',
  password: '',
  confirmPassword: ''
});

const showPassword = ref(false);
const showConfirmPassword = ref(false);
const loading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');

const rules = {
  required: (v: string) => !!v || 'Ce champ est requis.',
  minLength: (v: string, min: number) => (v && v.length >= min) || `Au moins ${min} caract├¿res.`,
  maxLength: (v: string, max: number) => (v && v.length <= max) || `Maximum ${max} caract├¿res.`,
  email: (v: string) => /.+@.+\..+/.test(v) || 'L\'adresse e-mail doit ├¬tre valide.',
  confirmPassword: (v: string, pass: string) => v === pass || 'Les mots de passe ne correspondent pas.',
};

async function handleRegister() {
  errorMessage.value = '';
  successMessage.value = '';

  if (!registerFormRef.value) return;
  const { valid } = await registerFormRef.value.validate();

  if (valid) {
    loading.value = true;
    const userData: RegisterRequest = {
      username: formState.username,
      email: formState.email,
      password: formState.password,
      firstName: formState.name,
      lastName: formState.prenom,
    };

    try {
      const registrationSuccessful = await authStore.register(userData); // Assuming register returns a boolean or throws
      if (registrationSuccessful) {
        successMessage.value = 'Compte cr├®├® avec succ├¿s ! Vous pouvez maintenant vous connecter.';
        // R├®initialiser le formulaire et l'├®tat
        if (registerFormRef.value) {
          registerFormRef.value.reset();
        }
        Object.keys(formState).forEach(key => {
          formState[key as keyof typeof formState] = '';
        });
        // Pas besoin de resetValidation() ici car reset() le fait pour les champs

        setTimeout(() => {
          if (successMessage.value) {
            router.push({ name: 'Login' }); // Assurez-vous que la route 'Login' existe
          }
        }, 3000); // D├®lai pour lire le message de succ├¿s
      }
      // No else needed, authStore.register should throw on failure
    } catch (err: any) {
      console.error("Register.vue: Error during handleRegister:", err);
      errorMessage.value = err.response?.data?.message || err.message || "Une erreur est survenue lors de la cr├®ation du compte. Veuillez r├®essayer.";
    } finally {
      loading.value = false;
    }
  } else {
    // errorMessage.value = "Veuillez corriger les erreurs dans le formulaire."; // Les erreurs de champ sont plus utiles
  }
}

function goToLogin() {
  router.push({ name: 'Login' }); // Assurez-vous que la route 'Login' existe
}
</script>

<style scoped>
/* R├®utiliser le m├¬me style de d├®grad├® que la page de connexion */
.bg-gradient-to-br {
  background-image: linear-gradient(to bottom right, var(--tw-gradient-stops));
}
.from-sky-400 {
  --tw-gradient-from: #38bdf8; /* Simplifi├® sans position */
  --tw-gradient-to: rgba(56, 189, 248, 0);
  --tw-gradient-stops: var(--tw-gradient-from), var(--tw-gradient-to);
}
.to-indigo-600 {
  --tw-gradient-to: #4f46e5; /* Sera la couleur 'to' effective */
}

.v-text-field--variant-filled .v-field__overlay {
  background-color: transparent !important;
}

.v-btn.elevation-3:hover {
  box-shadow: 0px 4px 10px -1px var(--v-shadow-key-umbra-opacity, rgba(0, 0, 0, 0.2)),
  0px 3px 8px 0px var(--v-shadow-key-penumbra-opacity, rgba(0, 0, 0, 0.14)),
  0px 2px 16px 1px var(--v-shadow-key-ambient-opacity, rgba(0, 0, 0, 0.12)) !important;
  transform: translateY(-1px);
  transition: all 0.2s ease-out;
}

/* Lien pour les conditions d'utilisation */
.text-caption a {
  color: var(--v-theme-primary);
  text-decoration: none;
}
.text-caption a:hover {
  text-decoration: underline;
}
</style>
