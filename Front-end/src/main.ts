import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import App from './App.vue'
import router from './router'
import './style.css'





// Vuetify
import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import { aliases, mdi } from 'vuetify/iconsets/mdi'
import '@mdi/font/css/materialdesignicons.css'

//import './assets/tailwind.css';

const vuetify = createVuetify({
  components,
  directives,
  icons: {
    defaultSet: 'mdi',
    aliases,
    sets: {
      mdi,
    },
  },
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        dark: false,
        colors: {
          primary: '#1976D2',
          secondary: '#00796B',
          accent: '#FF8F00',
          error: '#F44336',
          warning: '#FFC107',
          info: '#2196F3',
          success: '#4CAF50',
          background: '#F5F5F5',

          // The missing colors you mentioned:
          'grey-darken-4': '#212121',     // very dark grey (almost black)
          'on-surface': '#121212',        // color used for text/icons on surfaces
          'indigo-darken-1': '#3949AB',   // indigo darken-1 from material palette
          'orange-darken-2': '#f57c00',   // orange darken-2 from material palette
        },
      },
    },
  },
})


const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

const app = createApp(App)
app.use(pinia)
app.use(router)
app.use(vuetify)
app.mount('#app')
